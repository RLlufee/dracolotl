package net.trashelemental.dracolotl.entity.custom;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.trashelemental.dracolotl.Dracolotl;
import net.trashelemental.dracolotl.config.DracolotlConfig;
import net.trashelemental.dracolotl.item.ModItems;
import net.trashelemental.dracolotl.util.ModBucketableInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DracolotlEntity extends TamableAnimal implements GeoEntity, ModBucketableInterface {
    private static final EntityDataAccessor<Boolean> DATA_PLAYING_DEAD = SynchedEntityData.defineId(DracolotlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(DracolotlEntity.class, EntityDataSerializers.BOOLEAN);
    public String BEHAVIOR = "WANDER";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DracolotlEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.applyConfigAttributes();
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYING_DEAD, false);
        builder.define(FROM_BUCKET, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new OwnerHurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && DracolotlEntity.follow(DracolotlEntity.this) && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && DracolotlEntity.follow(DracolotlEntity.this) && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.targetSelector.addGoal(4, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f) {
            @Override
            public boolean canUse() {
                return super.canUse() && DracolotlEntity.follow(DracolotlEntity.this) && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.goalSelector.addGoal(7, new TemptGoal(this, 1.0, Ingredient.of(Items.ENDER_EYE), false) {
            @Override
            public boolean canUse() {
                return super.canUse() && DracolotlEntity.wander(DracolotlEntity.this) && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0) {
            @Override
            public boolean canUse() {
                return super.canUse() && DracolotlEntity.wander(DracolotlEntity.this) && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0f) {
            @Override
            public boolean canUse() {
                return super.canUse() && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && !DracolotlEntity.playingDead(DracolotlEntity.this);
            }
        });
    }

    public static boolean follow(DracolotlEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.isFollowing();
    }

    public static boolean wander(DracolotlEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.isWandering();
    }

    public static boolean playingDead(DracolotlEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.isPlayingDead();
    }

    public static AttributeSupplier.Builder createAttributes() {
        var config = DracolotlConfig.get();
        return Animal.createLivingAttributes()
            .add(Attributes.MAX_HEALTH, config.maxHealth)
            .add(Attributes.MOVEMENT_SPEED, config.groundSpeed)
            .add(Attributes.ATTACK_DAMAGE, config.attackDamage)
            .add(Attributes.ARMOR, config.armor)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
            .add(Attributes.ATTACK_KNOCKBACK, 0.0)
            .add(Attributes.FLYING_SPEED, config.flyingSpeed);
    }

    public void applyConfigAttributes() {
        var config = DracolotlConfig.get();
        var maxHealthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(config.maxHealth);
            if (this.getHealth() > config.maxHealth) {
                this.setHealth((float) config.maxHealth);
            }
        }
        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(config.attackDamage);
        }
        var armorAttr = this.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(config.armor);
        }
        var flyingSpeedAttr = this.getAttribute(Attributes.FLYING_SPEED);
        if (flyingSpeedAttr != null) {
            flyingSpeedAttr.setBaseValue(config.flyingSpeed);
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(false);
        flyingPathNavigation.setCanPassDoors(true);
        return flyingPathNavigation;
    }

    @Override
    public void travel(Vec3 travelVector) {
        var config = DracolotlConfig.get();
        if (this.isFlying() || this.isInLiquid() || this.isInLava()) {
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(config.flyingSpeed);
        } else {
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(config.groundSpeed);
        }
        super.travel(travelVector);
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.BUCKET_OF_DRACOLOTL);
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_AXOLOTL;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.fromBucket() && !this.hasCustomName() && !this.isTame();
    }

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.entityData.set(FROM_BUCKET, fromBucket);
    }

    @Override
    public void saveToBucketTag(ItemStack stack) {
        ModBucketableInterface.saveDefaultDataToBucketTag(this, stack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, data -> {
            if (this.isTame()) {
                data.putBoolean("IsTame", true);
                if (this.getOwnerUUID() != null) {
                    data.putUUID("OwnerUUID", this.getOwnerUUID());
                }
            }
        });
    }

    @Override
    public void loadFromBucketTag(CompoundTag tag) {
        ModBucketableInterface.loadDefaultDataFromBucketTag(this, tag);
        if (tag.contains("IsTame")) {
            this.setTame(tag.getBoolean("IsTame"), false);
        }
        if (tag.contains("OwnerUUID")) {
            this.setOwnerUUID(tag.getUUID("OwnerUUID"));
        }
        if (this.isTame()) {
            this.BEHAVIOR = "FOLLOW";
        }
        this.applyConfigAttributes();
    }

    public void setPlayingDead(boolean playingDead) {
        this.entityData.set(DATA_PLAYING_DEAD, playingDead);
    }

    public boolean isPlayingDead() {
        return this.entityData.get(DATA_PLAYING_DEAD);
    }

    public boolean canBeSeenAsEnemy() {
        return !this.isPlayingDead() && super.canBeSeenAsEnemy();
    }

    public boolean canAttack(LivingEntity livingentity, TargetingConditions condition) {
        return !this.isPlayingDead();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.CAMPFIRE) || source.is(DamageTypes.IN_FIRE)
            || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.FIREBALL)
            || source.is(DamageTypes.UNATTRIBUTED_FIREBALL) || source.is(DamageTypes.LAVA)
            || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.DRAGON_BREATH)) {
            return false;
        }
        float threshold = DracolotlConfig.get().playDeadThreshold;
        if (!this.level().isClientSide && threshold > 0.0f && this.getHealth() - amount <= threshold && !this.isPlayingDead()) {
            this.setPlayingDead(true);
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 3));
            Dracolotl.queueServerWork(200, () -> {
                if (this.isAlive()) {
                    this.setPlayingDead(false);
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
                    if (this.isTame() && this.getOwner() instanceof Player owner) {
                        owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
                    }
                }
            });
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isPlayingDead()) {
            target = null;
        }
        super.setTarget(target);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isPlayingDead()) {
            List<Mob> nearbyEntities = this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(10.0));
            for (Mob entity : nearbyEntities) {
                if (entity.getTarget() == this) {
                    entity.setTarget(null);
                }
            }
        }
        if (this.isPlayingDead() || (!this.isWandering() && !this.isFollowing())) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.5, 0.0));
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        var config = DracolotlConfig.get();

        // 1. 驯服逻辑检测
        if (config.isTameItem(itemstack)) {
            this.usePlayerItem(pPlayer, pHand, itemstack);
            if (!this.isTame()) {
                if (this.random.nextDouble() * 100.0 < config.tameChance) {
                    this.tame(pPlayer);
                    this.BEHAVIOR = "FOLLOW";
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                this.setPersistenceRequired();
                return InteractionResult.SUCCESS;
            }
        } else {
            // 2. 龙息采集
            if (itemstack.getItem() == Items.GLASS_BOTTLE && this.isOwnedBy(pPlayer)) {
                if (!config.enableDragonBreathCollection) {
                    return InteractionResult.PASS;
                }
                if (!pPlayer.isCreative()) {
                    itemstack.shrink(1);
                }
                ItemStack dragonsBreath = new ItemStack(Items.DRAGON_BREATH);
                if (!pPlayer.getInventory().add(dragonsBreath)) {
                    pPlayer.drop(dragonsBreath, false);
                }
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 0.5f, 3.0f);
                return InteractionResult.SUCCESS;
            }

            // 3. 喂食回血与龙蛋掉落
            boolean isHealingFood = config.isHealingFood(itemstack);
            boolean isEggFood = config.isEggDroppingFood(itemstack);

            if (isHealingFood || isEggFood) {
                boolean isInjured = this.getHealth() < this.getMaxHealth();
                if (isInjured || isEggFood) {
                    if (isInjured && isHealingFood) {
                        this.heal(config.foodHealAmount);
                    }
                    if (isEggFood && (this.level().random.nextDouble() * 100.0 < config.eggDropChance)) {
                        ItemStack dragonEgg = new ItemStack(Items.DRAGON_EGG);
                        this.spawnAtLocation(dragonEgg);
                    }

                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.5f, 1.0f);
                    for (int i = 0; i < 5; ++i) {
                        this.level().addParticle(ParticleTypes.SMOKE, this.getX() + (this.level().random.nextDouble() - 0.5), this.getY() + 0.5, this.getZ() + (this.level().random.nextDouble() - 0.5), 0.0, 0.0, 0.0);
                    }
                    if (isInjured && isHealingFood) {
                        for (int i = 0; i < 4; ++i) {
                            this.level().addParticle(ParticleTypes.HEART, this.getX() + (this.level().random.nextDouble() - 0.5), this.getY() + 0.5, this.getZ() + (this.level().random.nextDouble() - 0.5), 0.0, 0.0, 0.0);
                        }
                    }

                    if (!pPlayer.isCreative()) {
                        itemstack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // 4. 装桶机制
            if (itemstack.getItem() == Items.BUCKET) {
                if (!this.isTame() || this.isOwnedBy(pPlayer)) {
                    ModBucketableInterface.bucketMobPickup(pPlayer, pHand, this);
                }
            } else {
                InteractionResult retval = super.mobInteract(pPlayer, pHand);
                if (retval == InteractionResult.SUCCESS || retval == InteractionResult.CONSUME) {
                    this.setPersistenceRequired();
                }
                if (this.isOwnedBy(pPlayer)) {
                    this.cycleBehavior(pPlayer);
                    return InteractionResult.SUCCESS;
                }
                return retval;
            }
        }
        return InteractionResult.PASS;
    }

    public boolean isFollowing() {
        return "FOLLOW".equals(this.BEHAVIOR);
    }

    public boolean isWandering() {
        return "WANDER".equals(this.BEHAVIOR);
    }

    private void cycleBehavior(Player pPlayer) {
        switch (this.BEHAVIOR) {
            case "FOLLOW":
                this.BEHAVIOR = "WANDER";
                pPlayer.displayClientMessage(Component.translatable("message.dracolotl.behavior.wander"), true);
                break;
            case "STAY":
                this.BEHAVIOR = "FOLLOW";
                pPlayer.displayClientMessage(Component.translatable("message.dracolotl.behavior.follow"), true);
                break;
            case "WANDER":
            default:
                this.BEHAVIOR = "STAY";
                pPlayer.displayClientMessage(Component.translatable("message.dracolotl.behavior.stay"), true);
                break;
        }
    }

    public boolean ShouldUseRedDragonSkin() {
        return this.hasCustomName() && "Hellkite".equals(this.getCustomName().getString());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Behavior", this.BEHAVIOR);
        compound.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Behavior")) {
            this.BEHAVIOR = compound.getString("Behavior");
            this.setFromBucket(compound.getBoolean("FromBucket"));
        }
    }

    public boolean isFlying() {
        return !this.onGround() && !this.isPlayingDead();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<DracolotlEntity> dracolotlEntityAnimationState) {
        if (this.isPlayingDead()) {
            dracolotlEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("PLAY_DEAD", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        if (this.isFlying() && !this.isNoAi()) {
            if (dracolotlEntityAnimationState.isMoving()) {
                dracolotlEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("MOVE_AIR", Animation.LoopType.LOOP));
            } else {
                dracolotlEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("IDLE_AIR", Animation.LoopType.LOOP));
            }
            return PlayState.CONTINUE;
        }
        if (dracolotlEntityAnimationState.isMoving()) {
            dracolotlEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("MOVE_GROUND", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        dracolotlEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("IDLE_GROUND", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

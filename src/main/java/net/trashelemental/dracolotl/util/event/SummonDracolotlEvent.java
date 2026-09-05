package net.trashelemental.dracolotl.util.event;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.trashelemental.dracolotl.Dracolotl;
import net.trashelemental.dracolotl.entity.ModEntities;
import net.trashelemental.dracolotl.entity.custom.DracolotlEntity;

public class SummonDracolotlEvent {
    private static final TagKey<Block> DRAGON_EGGS_TAG = TagKey.create(Registries.BLOCK, Dracolotl.id("dragon_eggs"));

    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (blockState.is(DRAGON_EGGS_TAG)
            && isEndCrystalNearby(level, pos.offset(-2, -2, 0))
            && isEndCrystalNearby(level, pos.offset(2, -2, 0))
            && isEndCrystalNearby(level, pos.offset(0, -2, -2))
            && isEndCrystalNearby(level, pos.offset(0, -2, 2))) {

            setCrystalBeams(level, pos);
            level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.removeBlock(pos, false);
            Dracolotl.queueServerWork(40, () -> {
                removeEndCrystals(level, pos.offset(-2, -2, 0));
                level.playSound(null, pos.offset(-2, -2, 0), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
                Dracolotl.queueServerWork(20, () -> {
                    removeEndCrystals(level, pos.offset(2, -2, 0));
                    level.playSound(null, pos.offset(2, -2, 0), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    Dracolotl.queueServerWork(20, () -> {
                        removeEndCrystals(level, pos.offset(0, -2, -2));
                        level.playSound(null, pos.offset(0, -2, -2), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        Dracolotl.queueServerWork(20, () -> {
                            removeEndCrystals(level, pos.offset(0, -2, 2));
                            level.playSound(null, pos, SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.BLOCKS, 1.0f, 2.0f);
                            spawnTamedDracolotl(level, pos, player);
                        });
                    });
                });
            });
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static boolean isEndCrystalNearby(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(EndCrystal.class, new AABB(pos)).isEmpty();
    }

    private static void removeEndCrystals(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, new AABB(pos));
            for (EndCrystal crystal : crystals) {
                crystal.discard();
                sendParticlePacket(level, ParticleTypes.EXPLOSION, crystal.getX(), crystal.getY(), crystal.getZ(), 5);
            }
        }
    }

    private static void spawnTamedDracolotl(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            DracolotlEntity dracolotl = ModEntities.DRACOLOTL.create(level);
            if (dracolotl != null) {
                dracolotl.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0f, 0.0f);
                dracolotl.setTame(true, false);
                dracolotl.setOwnerUUID(player.getUUID());
                dracolotl.BEHAVIOR = "FOLLOW";
                level.addFreshEntity(dracolotl);
                sendParticlePacket(level, ParticleTypes.DRAGON_BREATH, dracolotl.getX(), dracolotl.getY(), dracolotl.getZ(), 20);
            }
        }
    }

    private static void sendParticlePacket(Level level, ParticleOptions particleType, double x, double y, double z, int count) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particleType, x, y, z, count, 0.0, 0.0, 0.0, 0.1);
        }
    }

    private static void setCrystalBeams(Level level, BlockPos eggPos) {
        setCrystalBeamTarget(level, eggPos.offset(-2, -2, 0), eggPos);
        setCrystalBeamTarget(level, eggPos.offset(2, -2, 0), eggPos);
        setCrystalBeamTarget(level, eggPos.offset(0, -2, -2), eggPos);
        setCrystalBeamTarget(level, eggPos.offset(0, -2, 2), eggPos);
    }

    private static void setCrystalBeamTarget(Level level, BlockPos crystalPos, BlockPos eggPos) {
        List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, new AABB(crystalPos).inflate(1.0));
        for (EndCrystal crystal : crystals) {
            crystal.setBeamTarget(eggPos);
        }
    }
}

package net.trashelemental.dracolotl.client.models;

import net.minecraft.resources.ResourceLocation;
import net.trashelemental.dracolotl.Dracolotl;
import net.trashelemental.dracolotl.entity.custom.DracolotlEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class DracolotlModel extends GeoModel<DracolotlEntity> {
    @Override
    public ResourceLocation getModelResource(DracolotlEntity animatable) {
        return Dracolotl.id("geo/models/dracolotl.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DracolotlEntity animatable) {
        if (animatable.ShouldUseRedDragonSkin()) {
            return Dracolotl.id("textures/entity/dracolotl_nether.png");
        }
        return Dracolotl.id("textures/entity/dracolotl.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DracolotlEntity animatable) {
        return Dracolotl.id("animations/dracolotl.animation.json");
    }

    @Override
    public void setCustomAnimations(DracolotlEntity animatable, long instanceId, AnimationState<DracolotlEntity> animationState) {
        GeoBone head = this.getAnimationProcessor().getBone("Head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            if (entityData != null) {
                head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
                head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
            }
        }
    }
}

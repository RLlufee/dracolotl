package net.trashelemental.dracolotl.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.trashelemental.dracolotl.Dracolotl;
import net.trashelemental.dracolotl.entity.custom.DracolotlEntity;

public class ModEntities {
    public static final EntityType<DracolotlEntity> DRACOLOTL = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Dracolotl.id("dracolotl"),
        EntityType.Builder.of(DracolotlEntity::new, MobCategory.CREATURE)
            .sized(0.75f, 1.0f)
            .build("dracolotl")
    );

    public static void register() {
        // Triggers class loading to register entities
    }
}

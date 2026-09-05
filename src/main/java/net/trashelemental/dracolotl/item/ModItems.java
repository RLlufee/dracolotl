package net.trashelemental.dracolotl.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.trashelemental.dracolotl.Dracolotl;
import net.trashelemental.dracolotl.entity.ModEntities;
import net.trashelemental.dracolotl.item.custom.DracolotlBucketItem;

public class ModItems {
    public static final Item BUCKET_OF_DRACOLOTL = Registry.register(
        BuiltInRegistries.ITEM,
        Dracolotl.id("bucket_of_dracolotl"),
        new DracolotlBucketItem(ModEntities.DRACOLOTL, SoundEvents.BUCKET_EMPTY_AXOLOTL, new Item.Properties().stacksTo(1))
    );

    public static final Item DRACOLOTL_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        Dracolotl.id("dracolotl_spawn_egg"),
        new SpawnEggItem(ModEntities.DRACOLOTL, -13948117, -3506983, new Item.Properties())
    );

    public static void register() {
        // Triggers class loading to register items
    }
}

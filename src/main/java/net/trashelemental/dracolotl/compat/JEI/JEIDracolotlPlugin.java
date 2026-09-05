package net.trashelemental.dracolotl.compat.JEI;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.trashelemental.dracolotl.Dracolotl;
import net.trashelemental.dracolotl.item.ModItems;

@JeiPlugin
public class JEIDracolotlPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return Dracolotl.id("jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
            List.of(new ItemStack(ModItems.BUCKET_OF_DRACOLOTL), new ItemStack(ModItems.DRACOLOTL_SPAWN_EGG)),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.dracolotl.dracolotl_info")
        );
    }
}

package net.trashelemental.dracolotl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.trashelemental.dracolotl.client.renderers.DracolotlRenderer;
import net.trashelemental.dracolotl.entity.ModEntities;

public class DracolotlClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DRACOLOTL, DracolotlRenderer::new);
    }
}
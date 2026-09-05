package net.trashelemental.dracolotl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.trashelemental.dracolotl.command.DracolotlCommand;
import net.trashelemental.dracolotl.config.DracolotlConfig;
import net.trashelemental.dracolotl.entity.ModEntities;
import net.trashelemental.dracolotl.entity.custom.DracolotlEntity;
import net.trashelemental.dracolotl.item.ModItems;
import net.trashelemental.dracolotl.util.event.SummonDracolotlEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dracolotl implements ModInitializer {
    public static final String MOD_ID = "dracolotl";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Dracolotl (Fabric)...");

        // 初始化配置
        DracolotlConfig.init();

        // 注册实体与物品
        ModEntities.register();
        ModItems.register();

        // 注册生物属性
        FabricDefaultAttributeRegistry.register(ModEntities.DRACOLOTL, DracolotlEntity.createAttributes());

        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DracolotlCommand.register(dispatcher);
        });

        // 注册创造模式物品栏内容
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.addAfter(Items.AXOLOTL_BUCKET, ModItems.BUCKET_OF_DRACOLOTL);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(ModItems.DRACOLOTL_SPAWN_EGG);
        });

        // 注册龙蛋仪式右键事件
        UseBlockCallback.EVENT.register(SummonDracolotlEvent::onRightClickBlock);

        // 注册服务端延迟任务 Tick 调度
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (workQueue.isEmpty()) return;
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actionsToRun = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() <= 0) {
                    actionsToRun.add(work);
                }
            });
            actionsToRun.forEach(work -> work.getKey().run());
            workQueue.removeAll(actionsToRun);
        });
    }

    public static void queueServerWork(int tickDelay, Runnable action) {
        workQueue.add(new AbstractMap.SimpleEntry<>(action, tickDelay));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}

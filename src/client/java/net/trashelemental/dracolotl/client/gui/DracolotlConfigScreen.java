package net.trashelemental.dracolotl.client.gui;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.trashelemental.dracolotl.config.DracolotlConfig;

/**
 * 基于 Cloth Config 构建的图形配置界面。
 * 支持在模组菜单中可视化修改、持久化保存与游戏内即时热生效。
 */
@Environment(EnvType.CLIENT)
public class DracolotlConfigScreen {

    public static Screen create(Screen parent) {
        DracolotlConfig config = DracolotlConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("title.dracolotl.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ==================== 1. 基础属性 (Attributes) ====================
        ConfigCategory attributesCategory = builder.getOrCreateCategory(Component.translatable("category.dracolotl.attributes"));

        attributesCategory.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.dracolotl.max_health"),
                config.maxHealth)
            .setDefaultValue(40.0)
            .setMin(1.0)
            .setMax(1024.0)
            .setTooltip(Component.translatable("tooltip.dracolotl.max_health"))
            .setSaveConsumer(val -> config.maxHealth = val)
            .build());

        attributesCategory.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.dracolotl.ground_speed"),
                config.groundSpeed)
            .setDefaultValue(0.15)
            .setMin(0.01)
            .setMax(2.0)
            .setTooltip(Component.translatable("tooltip.dracolotl.ground_speed"))
            .setSaveConsumer(val -> config.groundSpeed = val)
            .build());

        attributesCategory.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.dracolotl.flying_speed"),
                config.flyingSpeed)
            .setDefaultValue(0.6)
            .setMin(0.05)
            .setMax(5.0)
            .setTooltip(Component.translatable("tooltip.dracolotl.flying_speed"))
            .setSaveConsumer(val -> config.flyingSpeed = val)
            .build());

        attributesCategory.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.dracolotl.attack_damage"),
                config.attackDamage)
            .setDefaultValue(5.0)
            .setMin(0.0)
            .setMax(1024.0)
            .setTooltip(Component.translatable("tooltip.dracolotl.attack_damage"))
            .setSaveConsumer(val -> config.attackDamage = val)
            .build());

        attributesCategory.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.dracolotl.armor"),
                config.armor)
            .setDefaultValue(4.0)
            .setMin(0.0)
            .setMax(100.0)
            .setTooltip(Component.translatable("tooltip.dracolotl.armor"))
            .setSaveConsumer(val -> config.armor = val)
            .build());

        // ==================== 2. 喂食与掉落 (Feeding & Drops) ====================
        ConfigCategory feedingCategory = builder.getOrCreateCategory(Component.translatable("category.dracolotl.feeding"));

        feedingCategory.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.dracolotl.egg_drop_chance"),
                config.eggDropChance)
            .setDefaultValue(3.0)
            .setMin(0.0)
            .setMax(100.0)
            .setTooltip(Component.translatable("tooltip.dracolotl.egg_drop_chance"))
            .setSaveConsumer(val -> config.eggDropChance = val)
            .build());

        feedingCategory.addEntry(entryBuilder.startFloatField(
                Component.translatable("option.dracolotl.food_heal_amount"),
                config.foodHealAmount)
            .setDefaultValue(4.0f)
            .setMin(0.1f)
            .setMax(1024.0f)
            .setTooltip(Component.translatable("tooltip.dracolotl.food_heal_amount"))
            .setSaveConsumer(val -> config.foodHealAmount = val)
            .build());

        feedingCategory.addEntry(entryBuilder.startStrList(
                Component.translatable("option.dracolotl.healing_foods"),
                new ArrayList<>(config.healingFoods))
            .setDefaultValue(List.of("minecraft:chorus_flower", "minecraft:chorus_fruit"))
            .setTooltip(Component.translatable("tooltip.dracolotl.healing_foods"))
            .setSaveConsumer(val -> config.healingFoods = new ArrayList<>(val))
            .build());

        // ==================== 3. 行为与机制 (Behavior & Mechanics) ====================
        ConfigCategory behaviorCategory = builder.getOrCreateCategory(Component.translatable("category.dracolotl.behavior"));

        behaviorCategory.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.dracolotl.tame_chance"),
                config.tameChance)
            .setDefaultValue(20.0)
            .setMin(0.0)
            .setMax(100.0)
            .setTooltip(Component.translatable("tooltip.dracolotl.tame_chance"))
            .setSaveConsumer(val -> config.tameChance = val)
            .build());

        behaviorCategory.addEntry(entryBuilder.startStrList(
                Component.translatable("option.dracolotl.tame_items"),
                new ArrayList<>(config.tameItems))
            .setDefaultValue(List.of("minecraft:ender_eye"))
            .setTooltip(Component.translatable("tooltip.dracolotl.tame_items"))
            .setSaveConsumer(val -> config.tameItems = new ArrayList<>(val))
            .build());

        behaviorCategory.addEntry(entryBuilder.startFloatField(
                Component.translatable("option.dracolotl.play_dead_threshold"),
                config.playDeadThreshold)
            .setDefaultValue(8.0f)
            .setMin(0.0f)
            .setMax(1024.0f)
            .setTooltip(Component.translatable("tooltip.dracolotl.play_dead_threshold"))
            .setSaveConsumer(val -> config.playDeadThreshold = val)
            .build());

        behaviorCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("option.dracolotl.enable_dragon_breath"),
                config.enableDragonBreathCollection)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("tooltip.dracolotl.enable_dragon_breath"))
            .setSaveConsumer(val -> config.enableDragonBreathCollection = val)
            .build());

        // 保存监听：持久化至 JSON 并即时热应用到当前运行的世界
        builder.setSavingRunnable(() -> {
            DracolotlConfig.save();
            Minecraft client = Minecraft.getInstance();
            if (client.getSingleplayerServer() != null) {
                DracolotlConfig.applyToAllEntities(client.getSingleplayerServer());
            }
        });

        return builder.build();
    }
}

package net.trashelemental.dracolotl.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.trashelemental.dracolotl.Dracolotl;
import net.trashelemental.dracolotl.entity.custom.DracolotlEntity;

/**
 * Dracolotl 模组配置管理类。
 * 双端通用，负责持久化至 config/dracolotl.json 并支持热重载。
 */
public class DracolotlConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("dracolotl.json").toFile();
    private static DracolotlConfig INSTANCE = new DracolotlConfig();

    // ==================== 1. 喂食与掉落配置 ====================
    /** 喂食紫松果（紫颂花/紫颂果）掉落龙蛋的概率（百分比，0.0 - 100.0%） */
    public double eggDropChance = 3.0;

    /** 可作为食物给龙螈回血的物品标识列表（默认紫颂花与紫颂果） */
    public List<String> healingFoods = new ArrayList<>(List.of(
        "minecraft:chorus_flower",
        "minecraft:chorus_fruit"
    ));

    /** 每次喂食回复的生命值（4点生命 = 2颗心） */
    public float foodHealAmount = 4.0f;

    // ==================== 2. 基础属性配置 ====================
    /** 龙螈最大生命值（范围 1.0 ~ 1024.0） */
    public double maxHealth = 40.0;

    /** 龙螈地面移动速度（范围 0.01 ~ 2.0） */
    public double groundSpeed = 0.15;

    /** 龙螈飞行及水中速度（范围 0.05 ~ 5.0） */
    public double flyingSpeed = 0.6;

    /** 龙螈攻击伤害（范围 0.0 ~ 1024.0） */
    public double attackDamage = 5.0;

    /** 龙螈护甲值（范围 0.0 ~ 100.0） */
    public double armor = 4.0;

    // ==================== 3. 行为与拓展机制配置 ====================
    /** 驯服成功概率（百分比，0.0 - 100.0%） */
    public double tameChance = 20.0;

    /** 用于驯服龙螈的物品标识列表（默认末影之眼） */
    public List<String> tameItems = new ArrayList<>(List.of(
        "minecraft:ender_eye"
    ));

    /** 触发装死回血的生命值阈值（当生命值低于此值时触发装死，设为 0 可关闭） */
    public float playDeadThreshold = 8.0f;

    /** 是否允许主人使用空玻璃瓶右键采集龙息 */
    public boolean enableDragonBreathCollection = true;

    /**
     * 获取全局配置实例
     */
    public static DracolotlConfig get() {
        return INSTANCE;
    }

    /**
     * 初始化加载配置
     */
    public static void init() {
        load();
    }

    /**
     * 从文件读取配置，若文件不存在则创建默认配置
     */
    public static synchronized void load() {
        if (!CONFIG_FILE.exists()) {
            INSTANCE = new DracolotlConfig();
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            DracolotlConfig loaded = GSON.fromJson(reader, DracolotlConfig.class);
            if (loaded != null) {
                loaded.validate();
                INSTANCE = loaded;
            } else {
                INSTANCE = new DracolotlConfig();
                save();
            }
        } catch (Exception e) {
            Dracolotl.LOGGER.error("Failed to load Dracolotl config, using defaults", e);
            INSTANCE = new DracolotlConfig();
        }
    }

    /**
     * 保存当前配置到文件
     */
    public static synchronized void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            Dracolotl.LOGGER.error("Failed to save Dracolotl config", e);
        }
    }

    /**
     * 数值合理性校验与约束
     */
    public void validate() {
        this.eggDropChance = Math.max(0.0, Math.min(100.0, this.eggDropChance));
        this.foodHealAmount = Math.max(0.1f, Math.min(1024.0f, this.foodHealAmount));
        this.maxHealth = Math.max(1.0, Math.min(1024.0, this.maxHealth));
        this.groundSpeed = Math.max(0.01, Math.min(2.0, this.groundSpeed));
        this.flyingSpeed = Math.max(0.05, Math.min(5.0, this.flyingSpeed));
        this.attackDamage = Math.max(0.0, Math.min(1024.0, this.attackDamage));
        this.armor = Math.max(0.0, Math.min(100.0, this.armor));
        this.tameChance = Math.max(0.0, Math.min(100.0, this.tameChance));
        this.playDeadThreshold = Math.max(0.0f, Math.min(1024.0f, this.playDeadThreshold));

        if (this.healingFoods == null) {
            this.healingFoods = new ArrayList<>(List.of("minecraft:chorus_flower", "minecraft:chorus_fruit"));
        }
        if (this.tameItems == null) {
            this.tameItems = new ArrayList<>(List.of("minecraft:ender_eye"));
        }
    }

    /**
     * 检查给定的物品是否属于配置的回血食物
     */
    public boolean isHealingFood(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return this.healingFoods.contains(id.toString());
    }

    /**
     * 检查给定的物品是否能触发掉落龙蛋（默认紫颂花与紫颂果）
     */
    public boolean isEggDroppingFood(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String idStr = id.toString();
        return "minecraft:chorus_flower".equals(idStr) || "minecraft:chorus_fruit".equals(idStr);
    }

    /**
     * 检查给定的物品是否属于配置的驯服道具
     */
    public boolean isTameItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return this.tameItems.contains(id.toString());
    }

    /**
     * 将当前配置热应用到指定服务器中所有已加载的龙螈实体
     */
    public static void applyToAllEntities(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            for (DracolotlEntity entity : level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(DracolotlEntity.class), e -> true)) {
                entity.applyConfigAttributes();
            }
        }
    }
}

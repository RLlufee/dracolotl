# Dracolotl (Fabric 1.21.1)

> 把末影龙蛋变成一只「龙蝾螈（Dracolotl）」——会飞、可被驯服、可被装桶的龙系宠物。

本模组是 NeoForge 1.21.1 版 **Dracolotl** 向 **Fabric 1.21.1** 平台的完整移植，作者 `TrashElemental`，许可协议 MIT。
Mod ID：`dracolotl`，运行环境：`*`（客户端与服务端通用）。

---

## 目录

- [1. 项目简介](#1-项目简介)
- [2. 功能特性](#2-功能特性)
- [3. 目录结构说明](#3-目录结构说明)
- [4. 整体架构](#4-整体架构)
- [5. 各核心机制详解](#5-各核心机制详解)
  - [5.1 注册系统](#51-注册系统)
  - [5.2 实体核心 DracolotlEntity](#52-实体核心-dracolotlentity)
  - [5.3 水桶装载机制](#53-水桶装载机制)
  - [5.4 仪式召唤](#54-仪式召唤)
  - [5.5 服务端延迟任务调度](#55-服务端延迟任务调度)
  - [5.6 客户端渲染与 GeckoLib 动画](#56-客户端渲染与-geckolib-动画)
  - [5.7 JEI 集成](#57-jei-集成)
  - [5.8 数据与资源](#58-数据与资源)
- [6. 构建与配置方式](#6-构建与配置方式)
- [7. 开发 / 使用注意事项](#7-开发--使用注意事项)
- [8. 快速上手（开发者）](#8-快速上手开发者)
- [9. 修改日志 / 移植记录](#9-修改日志--移植记录-changelog)

---

## 1. 项目简介

Dracolotl 为 Minecraft 1.21.1（Fabric 加载器）添加一个可驯服的飞行生物 **Dracolotl（龙蝾螈）**。
它结合了蝾螈的萌系外形与末影龙的龙系特性：可飞行、免疫火焰与摔落、可装桶携带、可右键切换行为模式，
并通过「末影水晶环绕龙蛋」的仪式召唤一只已驯服的个体。

项目使用 **Fabric Loom** 构建，依赖 **Fabric API**、**GeckoLib 4**（骨骼动画）与可选 **JEI**（物品说明）。
源码按 Fabric 惯例拆分为 `main`（双端）与 `client`（仅客户端）两个 source set。

---

## 2. 功能特性

- **龙蝾螈实体**：继承 `TamableAnimal`，可飞行（自带飞行 AI 与 `FLYING_SPEED` 属性），使用末影之眼驯服。
- **行为模式切换**：右键（主人）在 `跟随 / 漫游 / 原地停留` 三态间循环，并通过 Action Bar 本地化提示。
- **特殊交互**：
  - 手持 **玻璃瓶** 右键已驯服龙蝾螈 → 获得 **龙息（Dragon Breath）**。
  - 喂食 **紫颂花** → 有概率掉落 **龙蛋** 并伴有吞食烟熏粒子。
  - 手持 **水桶** 右键 → 将龙蝾螈装入「龙蝾螈桶」（仅限未驯服或主人操作）。
- **装死回血机制**：生命值被打到 ≤8 时触发「装死」，获得再生并短暂脱离战斗，结束后给予力量增益（主人同享）。
- **仪式召唤**：在龙蛋四周按特定布局放置 4 个末影水晶并右键龙蛋，触发光束与爆炸仪式，召唤驯服龙蝾螈。
- **下界红龙彩蛋**：将龙蝾螈重命名为 `Hellkite` → 切换为专属下界红龙贴图（`dracolotl_nether.png`）。
- **GeckoLib 动画**：使用 GeckoLib 4 驱动高品质骨骼动画（地面/飞行 idle 与 move、装死共 5 套）。
- **JEI 支持**：以 `compileOnly` 接入，提供龙蝾螈桶与生成蛋的物品说明与召唤教学。
- **Mod Menu 图形配置与热重载**：支持通过 Mod Menu（Cloth Config）可视化配置界面或 `/dracolotl reload` 指令即时热重载配置，无需重启游戏即可令所有维度的龙螈属性即时生效。
- **进度与标签**：内置「获得龙蝾螈桶」挑战进度；把实体加入 `minecraft:can_breathe_under_water` 与 `minecraft:fall_damage_immune` 标签（可在水下呼吸、免疫摔落）。

---

## 3. 目录结构说明

```
dracolotl-fabric-1.21.1/
├── build.gradle                      # Gradle 构建脚本（Loom + 依赖 + 资源变量替换）
├── gradle.properties                 # 版本与依赖版本集中配置
├── settings.gradle                   # 项目名 / 仓库声明
├── gradlew / gradlew.bat             # Gradle Wrapper（跨平台构建入口）
├── gradle/wrapper/                   # Wrapper jar 与 properties（Gradle 9.5.1）
├── .github/workflows/build.yml       # CI：推送 / PR 自动构建并上传产物
├── LICENSE                           # MIT 许可证
├── README.md
└── src/
    ├── main/                         # 双端代码与资源
    │   ├── java/net/trashelemental/dracolotl/
    │   │   ├── Dracolotl.java                 # 主入口 ModInitializer（注册 + 事件 + 延迟任务）
    │   │   ├── entity/
    │   │   │   ├── ModEntities.java           # 实体类型注册
    │   │   │   └── custom/DracolotlEntity.java# 实体核心逻辑（AI / 交互 / 动画 / 持久化）
    │   │   ├── item/
    │   │   │   ├── ModItems.java              # 物品注册（桶 / 生成蛋）
    │   │   │   └── custom/DracolotlBucketItem.java  # 龙蝾螈桶（放置即生成实体）
    │   │   ├── util/
    │   │   │   ├── ModBucketableInterface.java # 桶装载接口与静态工具方法
    │   │   │   └── event/SummonDracolotlEvent.java  # 龙蛋仪式右键事件
    │   │   └── compat/JEI/JEIDracolotlPlugin.java   # JEI 插件（compileOnly）
    │   └── resources/
    │       ├── fabric.mod.json                # 模组元数据 / 入口点 / 依赖 / Mixin 声明
    │       ├── dracolotl.mixins.json          # Mixin 配置（当前为空，见 §7）
    │       ├── assets/dracolotl/
    │       │   ├── animations/dracolotl.animation.json  # 骨骼动画定义（GeckoLib）
    │       │   ├── geo/models/dracolotl.geo.json        # 方块模型（Blockbench 导出）
    │       │   ├── icon.png                    # 模组图标
    │       │   ├── lang/{en_us,zh_cn}.json     # 本地化文本
    │       │   ├── models/block/{cobweb_trap,spinneret}.json  # ⚠ 残留资源（见 §7）
    │       │   ├── models/item/{bucket_of_dracolotl,dracolotl_spawn_egg}.json
    │       │   ├── textures/entity/{dracolotl,dracolotl_nether}.png
    │       │   └── textures/item/bucket_of_dracolotl.png
    │       └── data/
    │           ├── dracolotl/
    │           │   ├── advancement/dracolotl_bucket_get.json     # 挑战进度
    │           │   ├── advancement/recipes/misc/bucket_of_dracolotl.json # 进度配方解锁（见 §7）
    │           │   ├── loot_table/entities/dracolotl.json        # 击杀掉落龙蛋
    │           │   └── tags/block/dragon_eggs.json               # 仪式可识别的「龙蛋」方块标签
    │           └── minecraft/tags/entity_type/
    │               ├── can_breathe_under_water.json  # 加入龙蝾螈
    │               └── fall_damage_immune.json       # 加入龙蝾螈
    └── client/                        # 仅客户端代码与资源
        ├── java/net/trashelemental/dracolotl/client/
        │   ├── DracolotlClient.java            # 客户端入口 ClientModInitializer
        │   ├── models/DracolotlModel.java       # GeoModel 实现（模型/贴图/动画资源定位 + 头部追踪）
        │   └── renderers/DracolotlRenderer.java # GeoEntityRenderer 实现（注册渲染器 + 幼体缩放）
        └── resources/dracolotl.client.mixins.json   # 客户端 Mixin 配置（当前为空）
```

> 注：`.gradle/`、`build/` 为构建缓存与产物，不纳入版本管理；`gradle.properties` 中的
> `org.gradle.java.home` 等为本机专用，提交时一般加入 `.gitignore`。

---

## 4. 整体架构

```
                ┌─────────────────────────────── fabric.mod.json ───────────────────────────────┐
                │  entrypoints: main / client / jei_mod_plugin                                   │
                └───────────┬───────────────────────────┬───────────────────────┬───────────────┘
                            │                            │                       │
               [main] Dracolotl            [client] DracolotlClient      [jei] JEIDracolotlPlugin
              ModInitializer                 ClientModInitializer           IModPlugin
                │  onInitialize()            │  onInitializeClient()         │ registerRecipes()
                ├─ ModEntities.register()    └─ EntityRendererRegistry        └─ addIngredientInfo(...)
                ├─ ModItems.register()           .register(DRACOLOTL,
                ├─ FabricDefaultAttributeRegistry       DracolotlRenderer::new)            │
                │     .register(DRACOLOTL,                                         ┌────────┘
                │        createAttributes())                                      │
                ├─ ItemGroupEvents（创造栏注入）                                    │
                ├─ UseBlockCallback ──► SummonDracolotlEvent.onRightClickBlock()  │
                └─ ServerTickEvents.END_SERVER_TICK ─► queueServerWork 调度器     │
                                                                                  │
        DracolotlEntity (TamableAnimal + GeoEntity + ModBucketableInterface) ◄────┘
           │  AI 目标链 / 飞行导航 / 行为模式 / 装死 / 交互 / NBT / 动画控制器
           ├── DracolotlBucketItem（放置生成实体）
           └── ModBucketableInterface（装桶 / 存读桶内 NBT）
```

**调用关系要点**

- 模组启动：`Dracolotl.onInitialize()` 先触发 `ModEntities` / `ModItems` 的**类加载**（静态字段中的
  `Registry.register` 在类加载时即完成注册），再注册实体属性、创造栏、方块右键事件与 Tick 调度器。
- 客户端启动：`DracolotlClient.onInitializeClient()` 把 `DracolotlRenderer` 注册到 `DRACOLOTL` 实体类型。
- 渲染链路：实体 → `GeoEntityRenderer` → `DracolotlModel`（定位 `geo` / 贴图 / 动画资源）。
- 事件链路：玩家右键龙蛋（方块）→ `UseBlockCallback` → `SummonDracolotlEvent` → 校验仪式布局 →
  用 `queueServerWork` 编排多段延时任务 → 最终生成驯服实体。
- 桶链路：玩家手持水桶右键实体 → `DracolotlEntity.mobInteract` → `ModBucketableInterface.bucketMobPickup`
  → 生成「龙蝾螈桶」物品；放置桶 → `DracolotlBucketItem.useOn` → 从桶内 NBT 还原实体。

---

## 5. 各核心机制详解

### 5.1 注册系统

| 注册对象 | 类 | 方式 | 说明 |
|---|---|---|---|
| 实体类型 `dracolotl` | `ModEntities` | `BuiltInRegistries.ENTITY_TYPE` + `EntityType.Builder` | 尺寸 `0.75×1.0`，分类 `CREATURE` |
| 物品（桶 / 生成蛋） | `ModItems` | `BuiltInRegistries.ITEM` | `DracolotlBucketItem` 与 `SpawnEggItem` |
| 实体属性 | `Dracolotl.onInitialize` | `FabricDefaultAttributeRegistry.register` | 绑定 `DracolotlEntity.createAttributes()` |
| 创造栏 | `Dracolotl.onInitialize` | `ItemGroupEvents.modifyEntriesEvent` | 桶插入「工具与实用工具」栏（在美西螈桶之后），生成蛋插入「生成蛋」栏 |

- `ModEntities.register()` / `ModItems.register()` 方法体为空，仅用于**触发类加载**，使其中静态字段的
  `Registry.register(...)` 语句执行（Fabric 常见的「静态初始化即注册」模式）。
- **顺序约束**：必须先在 `ModEntities` 中完成实体类型注册，才能调用
  `FabricDefaultAttributeRegistry.register(ModEntities.DRACOLOTL, ...)`，否则属性注册会因类型未就绪而失败。

### 5.2 实体核心 DracolotlEntity

继承关系：`TamableAnimal`（可驯服动物） + `GeoEntity`（GeckoLib 动画实体） + `ModBucketableInterface`（可装桶）。

**(a) 飞行能力**
- 构造器中设置 `FlyingMoveControl(this, 20, true)`（飞行移动控制）。
- 重写 `createNavigation` 返回 `FlyingPathNavigation`，并覆写 `isStableDestination`：只要脚下不是空气即可落脚（允许悬停）。
- 属性 `Attributes.FLYING_SPEED = 0.6`，`MOVEMENT_SPEED` 在 `travel()` 中按状态切换：飞行 / 水中 / 熔岩时为 `0.6`，否则 `0.15`。

**(b) 行为模式 `BEHAVIOR`**
- 取值：`"FOLLOW"` / `"WANDER"` / `"STAY"`，初始 `"WANDER"`。
- 切换：主人右键 → `cycleBehavior(player)`，按 `FOLLOW→WANDER→STAY→FOLLOW` 循环，并通过
  `Component.translatable("message.dracolotl.behavior.*")` 在 Action Bar 提示。
- 各 AI 目标通过 `follow()` / `wander()` 静态判断决定是否启用（`STAY` 时仅保留看人 / 环顾等低优先级目标）。
- 持久化：仅写入 NBT（`addAdditionalSaveData` / `readAdditionalSaveData`），**不**走 `SynchedEntityData`
  （AI 主要在服务端运行，详见 §7）。

**(c) AI 目标链（`registerGoals`）**
| 优先级 | 目标 | 条件 |
|---|---|---|
| 1 | `OwnerHurtByTargetGoal` | 跟随中且未装死 |
| 2 | `OwnerHurtTargetGoal` | 跟随中且未装死 |
| 3 | `MeleeAttackGoal` | 未装死 |
| 4 | `HurtByTargetGoal` | 未装死 |
| 5 | `FollowOwnerGoal` | 跟随中且未装死 |
| 7 | `TemptGoal`（末影之眼） | 漫游中且未装死 |
| 8 | `RandomStrollGoal` | 漫游中且未装死 |
| 9 | `LookAtPlayerGoal` | 未装死 |
| 10 | `RandomLookAroundGoal` | 未装死 |

> 大量目标覆写 `canUse()` 以排除「装死」状态，保证装死时完全脱离战斗。

**(d) 装死回血机制（`hurt`）**
- 对火焰类 / 龙息类伤害直接 `return false`（完全免疫）：坠落、营火、火、火球、岩浆、龙息等。
- 当 `getHealth() - amount <= 8` 且未装死时：置 `playingDead=true`，附加 `REGENERATION`（200 tick，幅度 3），
  并通过 `queueServerWork(200, ...)` 在 10 秒后解除装死、附加 `DAMAGE_BOOST`（100 tick，幅度 1）；
  若已驯服且主人在场，主人同享力量增益。
- 装死期间：`canBeSeenAsEnemy` / `canAttack` 返回 false，`setTarget(null)`，并在 `tick()` 中清除附近以自身为目标的生物目标；
  同时施加向下速度（悬停下沉）。

**(e) 特殊交互（`mobInteract`）**
- **末影之眼**：消耗物品；未驯服时有 1/5 概率驯服（广播 `(byte)7` 爱心 / `(byte)6` 失败粒子），置 `BEHAVIOR=FOLLOW`。
- **玻璃瓶**：仅主人可操作 → 获得 1 个龙息（创造模式不消耗瓶子）。
- **紫颂花**：3% 概率掉落龙蛋，播放进食音效与烟雾粒子。
- **水桶**：未驯服或主人手持 → `ModBucketableInterface.bucketMobPickup` 装入桶。
- 其余情况下主人右键触发 `cycleBehavior`，非主人走父类默认交互。

**(f) 数据同步与持久化**
- `SynchedEntityData` 仅同步两个布尔：`DATA_PLAYING_DEAD`、`FROM_BUCKET`。
- 行为 / 驯服 / 主人 / 健康等字段经 `addAdditionalSaveData` / `readAdditionalSaveData` 写入实体 NBT；
  桶内数据经 `saveToBucketTag` / `loadFromBucketTag` 写入物品 NBT（含 `IsTame` / `OwnerUUID`）。

**(g) 下界红龙彩蛋**
- `ShouldUseRedDragonSkin()`：当实体存在自定义名称且为 `"Hellkite"` 时返回 true，渲染器与模型据此切换为
  `dracolotl_nether.png` 贴图。

### 5.3 水桶装载机制

- **接口 `ModBucketableInterface`**：定义 `fromBucket` / `setFromBucket` / `saveToBucketTag` / `loadFromBucketTag` /
  `getBucketItemStack` / `getPickupSound`，并提供静态工具：
  - `saveDefaultDataToBucketTag` / `loadDefaultDataFromBucketTag`：通用字段（NoAI / Silent / NoGravity / Glowing /
    Invulnerable / Health）的存读。
  - `bucketMobPickup`：玩家持桶右键时的拾取流程（播放音效、写入桶、触发 `FILLED_BUCKET` 进度、丢弃原实体）。
- **`DracolotlEntity` 实现**：`saveToBucketTag` 额外保存 `IsTame` / `OwnerUUID`；`loadFromBucketTag` 还原驯服状态，
  若已驯服则 `BEHAVIOR=FOLLOW`。
- **`DracolotlBucketItem`**：`useOn` 在空气方块处生成实体（`MobSpawnType.BUCKET`），从其 `BUCKET_ENTITY_DATA`
  组件读取并还原 NBT，置 `fromBucket=true`；非创造模式消耗桶、返还空桶。

**使用方式（玩家）**：手持水桶右键龙蝾螈 → 得到「龙蝾螈桶」；手持桶右键空气/水 → 放出龙蝾螈。

### 5.4 仪式召唤

- 监听：`UseBlockCallback.EVENT.register(SummonDracolotlEvent::onRightClickBlock)`（仅主手触发）。
- 触发条件：被点方块属于 `dracolotl:dragon_eggs` 标签（默认含 `minecraft:dragon_egg`，并以 `required:false`
  兼容 `dragonmounts:dragon_egg`、`rediscovered:red_dragon_egg`），且龙蛋相对位置
  `(-2,-2,0)`、`(2,-2,0)`、`(0,-2,-2)`、`(0,-2,2)` 处各有 1 个 `EndCrystal`（用 AABB 检测）。
- 流程（用 `queueServerWork` 编排延时）：
  1. 立即：四根水晶向龙蛋发射光束（`setBeamTarget`），播放 `END_PORTAL_SPAWN`，移除龙蛋方块。
  2. +40 tick：移除 1 号水晶并播放爆炸音。
  3. +20 / +20 / +20 tick：依次移除其余 3 个水晶并播放爆炸音；最后播放末影龙环境音，**生成驯服龙蝾螈**
     （`setTame(true)`、`setOwnerUUID(player)`、`BEHAVIOR=FOLLOW`，位于龙蛋处上方）。

> 几何含义：四颗末影水晶需环绕龙蛋，位于其**下方 2 格、水平 ±2 格**的四个方位。JEI 说明文本描述的是
> 「3 格高黑曜石柱 + 四周各隔一格放置末影水晶」的等效搭建思路，实际以代码中的相对坐标判定为准。

### 5.5 服务端延迟任务调度

- `Dracolotl` 维护一个 `ConcurrentLinkedQueue<SimpleEntry<Runnable, Integer>>` 作为延迟任务队列。
- `queueServerWork(tickDelay, action)`：入队一个 `tickDelay` 刻后执行的动作。
- `ServerTickEvents.END_SERVER_TICK` 每刻遍历队列：剩余刻数 `-1`，归零即执行并出队。
- 这是 NeoForge `DeferredWorkQueue` / `Level#tick` 思路在 Fabric 下的等价实现，仪式召唤与装死解除以此驱动。

### 5.6 客户端渲染与 GeckoLib 动画

- `DracolotlModel extends GeoModel<DracolotlEntity>`：
  - `getModelResource` → `geo/models/dracolotl.geo.json`
  - `getTextureResource` → 按 `ShouldUseRedDragonSkin()` 返回普通 / 下界红龙贴图
  - `getAnimationResource` → `animations/dracolotl.animation.json`
  - `setCustomAnimations`：读取 `EntityModelData` 驱动 `Head` 骨骼做俯仰 / 偏航（头部追踪）。
- `DracolotlRenderer extends GeoEntityRenderer<DracolotlEntity>`：构造绑定 `DracolotlModel`；`render` 中对
  `isBaby()` 实体额外缩放 `0.4`。
- 动画控制器（`DracolotlEntity.registerControllers`）：单控制器 `predicate` 按状态选择动画：
  `PLAY_DEAD`（装死）/ `MOVE_AIR`·`IDLE_AIR`（飞行移动/悬停）/ `MOVE_GROUND`·`IDLE_GROUND`（地面）。
- 动画资源（`dracolotl.animation.json`，`geckolib_format_version: 2`）包含上述 5 套循环动画，骨骼涵盖
  `Dracolotl` / `Head` / `gills` 系 / `lwing`·`rwing` 及翼尖 / `leg1~4` / `tail` 等。

### 5.7 JEI 集成

- 依赖以 `compileOnly` 引入（`jei-1.21.1-common-api` / `jei-1.21.1-fabric-api`），运行时作为**可选**模组（`fabric.mod.json` 的 `suggests.jei`）。
- `JEIDracolotlPlugin`（标注 `@JeiPlugin`）在 `registerRecipes` 中用 `addIngredientInfo` 为
  `BUCKET_OF_DRACOLOTL` 与 `DRACOLOTL_SPAWN_EGG` 提供说明文本（`jei.dracolotl.dracolotl_info`，含召唤教学）。
- 入口点 `jei_mod_plugin` 在 `fabric.mod.json` 中声明，确保仅当 JEI 存在时加载。

### 5.8 数据与资源

- **本地化**：`lang/zh_cn.json` 与 `en_us.json` 提供实体名、物品名、行为提示、进度标题/描述、JEI 说明。
- **标签**：
  - `data/dracolotl/tags/block/dragon_eggs.json`：仪式可识别的龙蛋方块。
  - `data/minecraft/tags/entity_type/can_breathe_under_water.json` 与 `fall_damage_immune.json`：把
    `dracolotl:dracolotl` 加入，使其水下呼吸、免疫摔落。
- **战利品表**：`loot_table/entities/dracolotl.json` 击杀必掉 1 个 `minecraft:dragon_egg`。
- **进度**：`advancement/dracolotl_bucket_get.json`（隐藏挑战，父进度 `husbandry/axolotl_in_a_bucket`）。

---

## 6. 构建与配置方式

### 版本与依赖（gradle.properties）

| 配置项 | 值 | 说明 |
|---|---|---|
| `minecraft_version` | `1.21.1` | MC 版本 |
| `loader_version` | `0.19.5` | Fabric Loader |
| `loom_version` | `1.17-SNAPSHOT` | Fabric Loom（remap 插件） |
| `fabric_api_version` | `0.116.17+1.21.1` | Fabric API |
| `geckolib_version` | `4.7.4` | GeckoLib（**必需**，fabric.mod.json 声明 `>=4.7.0`） |
| `jei_version` | `19.21.0.246` | JEI（可选，compileOnly） |
| `version` | `fabric-1.0.3` | 模组版本 |
| `mod_name` / `group` | `Dracolotl` / `net.trashelemental.dracolotl` | 名称与包名 |

### 构建命令

```bash
./gradlew build        # 构建，产物在 build/libs/（已 remap 的 jar + sources jar）
./gradlew runClient    # 启动带模组的客户端进行调试（需本地安装 MC 资产）
```

- `build.gradle` 使用 `fabric-loom-remap` 插件，开启 `splitEnvironmentSourceSets()` 并声明
  `mods.dracolotl` 同时挂载 `main` 与 `client` 两个 source set。
- `processResources` 会对 `fabric.mod.json` 做版本变量替换（`${version}`）。
- 编译目标 Java 21，强制 UTF-8。
- 额外仓库：GeckoLib（Cloudsmith）、JEI（blamejared maven）、ModMaven。

### 运行依赖（`fabric.mod.json` 的 `depends`）

`fabricloader >=0.19.5`、`minecraft ~1.21.1`、`java >=21`、`fabric-api`、`geckolib >=4.7.0`；`jei`、`cloth-config` 与 `modmenu` 为建议（可选）。

### 5.9 配置系统与热重载机制 (DracolotlConfig)

模组采用独立解耦的架构，数据层面使用 Minecraft 内置的 Gson 管理 `config/dracolotl.json`，客户端采用 Cloth Config + Mod Menu 呈现可视化配置屏幕：

- **核心配置项清单**：
  1. `eggDropChance`（默认 `3.0%`，范围 `0.0 ~ 100.0%`）：喂食紫松果（紫颂花/紫颂果）时生成龙蛋的概率。
  2. `healingFoods`（默认 `["minecraft:chorus_flower", "minecraft:chorus_fruit"]`）：可作为食物投喂给龙螈回血的物品标识列表，支持任意模组自定义物品。
  3. `foodHealAmount`（默认 `4.0` 点，即 2 颗心，范围 `0.1 ~ 1024.0`）：单次投喂回血量。
  4. `maxHealth`（默认 `40.0`，范围 `1.0 ~ 1024.0`）：龙螈的最大生命值上限，最高可拉满至 1024。
  5. `groundSpeed`（默认 `0.15`，范围 `0.01 ~ 2.0`）：地面爬行速度，解耦了原本在 `travel()` 中的硬编码。
  6. `flyingSpeed`（默认 `0.6`，范围 `0.05 ~ 5.0`）：飞行与水中的移动速度。
  7. `attackDamage`（默认 `5.0`，范围 `0.0 ~ 1024.0`）：近战攻击造成的伤害点数。
  8. `armor`（默认 `4.0`，范围 `0.0 ~ 100.0`）：龙螈自带的基础护甲值。
  9. `tameChance`（默认 `20.0%`，范围 `0.0 ~ 100.0%`）：使用驯服道具成功驯服的概率。
  10. `tameItems`（默认 `["minecraft:ender_eye"]`）：驯服道具列表。
  11. `playDeadThreshold`（默认 `8.0`，范围 `0.0 ~ 1024.0`）：触发装死回血的生命值阈值（设为 0 可关闭）。
  12. `enableDragonBreathCollection`（默认 `true`）：是否允许主人使用空玻璃瓶采集龙息。
- **热重载途径**：
  1. **Mod Menu 图形界面**：在模组菜单中选择 Dracolotl 进入配置页面，调整后点击「保存并退出」，配置自动持久化并即时刷新当前世界所有龙螈实体的各项属性。
  2. **游戏内/控制台指令**：管理员或单人游戏可通过 `/dracolotl reload` 指令热重载配置文件并应用。
  3. **双端隔离安全**：服务端纯 JSON 运作，无任何客户端类交叉引用，无论单人还是独立服务器均安全稳定。

---

## 7. 开发 / 使用注意事项

> 以下为移植 / 维护过程中识别出的**残留或不一致项**，不影响当前运行，但新开发者应知晓。

1. **空 Mixin 配置**：`src/main/resources/dracolotl.mixins.json` 与 `src/client/resources/dracolotl.client.mixins.json`
   已声明但 `mixins` / `client` 数组为空，**没有任何 Mixin 类**（`net.trashelemental.dracolotl.mixin` 与
   `client.mixin` 包下无实现）。两者来自 NeoForge 移植模板，建议确认无注入需求后移除，避免误导。
2. **残留方块模型**：`models/block/cobweb_trap.json` 与 `spinneret.json` 引用了**其他模组**
   `infested_swarms_spiders:` 命名空间的贴图，且代码中**没有任何对应方块注册**。它们来自原模组的关联内容，
   当前属无效资源，可清理（否则打包后会产生悬空引用）。
3. **进度与配方不一致**：
   - 仓库中**不存在** `bucket_of_dracolotl` 的合成配方 JSON（`data/dracolotl/recipe/` 目录缺失）。
   - `advancement/recipes/misc/bucket_of_dracolotl.json` 的 `has_the_recipe` 条件要求解锁该配方，因配方不存在，
     该条件永不达成，导致「获得龙蝾螈桶」进度（`dracolotl_bucket_get`）**实际不可获得**。
   - 龙蝾螈桶在游戏内通过「手持水桶右键龙蝾螈」获得，并不靠合成。
4. **行为字段未同步**：`BEHAVIOR` 仅存于 NBT、未进入 `SynchedEntityData`，客户端不可见。由于行为仅影响服务端 AI，
   目前无功能问题；但若将来要在客户端（如渲染/UI）读取行为，需要改为同步数据。
5. **仪式几何**：四颗末影水晶必须位于龙蛋的 `(-2,-2,0)`、`(2,-2,0)`、`(0,-2,-2)`、`(0,-2,2)` 相对位置，
   即龙蛋**下方 2 格、水平 ±2 格**的四个方位；JEI 文本描述的是等效搭建思路，以代码坐标判定为准。
6. **JDK 版本差异**：`gradle.properties` 指定本地 `org.gradle.java.home=C:/Program Files/Zulu/zulu-21`
   （Java 21），而 `.github/workflows/build.yml` 使用 `java-version: '25'`。建议在 CI 统一为 Java 21
   以避免未来 Loom/工具链不兼容。
7. **装死增益幅度**：`REGENERATION` 幅度 3 = 再生 IV（持续 10s），`DAMAGE_BOOST` 幅度 1 = 力量 II（5s），
   数值调参时需注意「幅度 = 等级 - 1」。
8. **数据目录残留空文件夹**：`data/dracolotl/` 下存在 `enchantment`、`neoforge/biome_modifier`、
   `tags/item/enchantable` 以及 `data/minecraft/tags/block/mineable`、`data/minecraft/tags/item` 等空目录
   （来自 NeoForge 模板），不含任何资源文件，可忽略或清理。

---

## 8. 快速上手（开发者）

```bash
# 1) 安装 JDK 21 与对应 Fabric 开发环境
# 2) 克隆仓库，确保 gradle.properties 的 org.gradle.java.home 指向 JDK 21
./gradlew build          # 产出 build/libs/dracolotl-fabric-1.0.3.jar
# 3) 将 jar 放入客户端的 mods/ 目录（需同时安装 fabric-api 与 geckolib 4.7.x；JEI 可选）
# 4) 调试：./gradlew runClient
```

**在游戏内体验核心机制**
1. 用末影之眼右键龙蝾螈驯服它（约 1/5 概率）。
2. 右键它在 `跟随 / 漫游 / 原地停留` 间切换。
3. 玻璃瓶取龙息、紫颂花喂食有几率掉龙蛋、水桶装桶携带。
4. 搭建「龙蛋 + 四末影水晶」仪式召唤驯服个体；重命名为 `Hellkite` 解锁下界红龙皮肤。

---

## 9. 修改日志 / 移植记录 (Changelog)

### [1.0.2] - 配置系统与 Mod Menu 热重载接入 (2026-09-05)

1. **配置核心与双端解耦 (`DracolotlConfig`)**：
   - 使用 Minecraft 内置 Gson 构建配置持久化层（`config/dracolotl.json`），纯 Java 实现保障双端安全。
   - 实现紫松果掉落龙蛋概率（`eggDropChance`）、可回血食物列表（`healingFoods`，默认紫颂花与紫颂果）、食物单次回血量（`foodHealAmount`，默认 4.0 点）、最大生命值上限（`maxHealth`，支持 1.0~1024.0）、地面移动速度（`groundSpeed`）、飞行速度（`flyingSpeed`）、攻击伤害（`attackDamage`）、基础护甲（`armor`）、驯服成功概率（`tameChance`）、驯服道具列表（`tameItems`）、装死生命阈值（`playDeadThreshold`）以及龙息采集开关（`enableDragonBreathCollection`）的集中配置与校验。
2. **速度机制解耦与实体热属性更新**：
   - 修复并解耦 `DracolotlEntity#travel` 中硬编码的飞行速度（`0.6`）与地面速度（`0.15`），使速度配置真正对实体动态生效。
   - 新增 `DracolotlEntity#applyConfigAttributes()`，在实体生成、装载桶生成、配置保存或指令热重载时动态同步更新 `MAX_HEALTH`、`ATTACK_DAMAGE`、`FLYING_SPEED` 与 `ARMOR`。
   - 重构 `DracolotlEntity#mobInteract`，支持受伤时投喂可回血食物列表回血并播放爱心粒子；支持投喂紫松果判定龙蛋生成概率。
3. **Mod Menu 与 Cloth Config 界面集成**：
   - 接入 Cloth Config 15.0.140 与 Mod Menu 11.0.3 API。
   - 创建 `DracolotlConfigScreen` 提供分类化图形配置界面（基础属性、喂食与掉落、行为与机制），支持数值滑块、浮点输入与列表增删改。
   - 编写 `ModMenuCompat` 实现 `ModMenuApi`，注册在 `fabric.mod.json` 的 `"modmenu"` entrypoint。
   - 点击保存时自动写入配置并热更新当前世界的全部龙螈。
4. **管理指令与本地化**：
   - 注册 `/dracolotl reload` 管理指令，支持无需重启即时热重载配置并广播更新实体。
   - 在 `zh_cn.json` 与 `en_us.json` 中为所有配置类别、配置项目、悬停提示及命令反馈提供完整的双语本地化。

### [文档] - 2026-09-05 README 重写

- 在原有「移植记录」基础上重写为**结构化完整 README**：补充项目简介、功能特性、目录结构、整体架构图、
  各核心机制（注册 / 实体 / 水桶 / 仪式 / 延迟任务 / 渲染动画 / JEI / 数据资源）详解、构建配置与开发注意事项。
- 新增「开发 / 使用注意事项」一节，记录：空 Mixin 配置、残留方块模型（引用 `infested_swarms_spiders`）、
  缺失 `bucket_of_dracolotl` 合成配方导致进度不可达、行为字段未同步、仪式几何坐标、JDK 版本不一致、
  数据目录空文件夹残留等现状。

### [1.0.1] - 本地化与提示信息优化

1. **行为切换提示信息本地化**：
   - 将 `DracolotlEntity#cycleBehavior` 中硬编码的 Action Bar 提示文本转换为 `Component.translatable`。
   - 在语言文件（`en_us.json`、`zh_cn.json`）中新增对应行为模式的本地化键（`message.dracolotl.behavior.wander`、`message.dracolotl.behavior.follow`、`message.dracolotl.behavior.stay`）。

### [1.0.0] - 从 1.21.1 NeoForge 移植到 1.21.1 Fabric

1. **构建与依赖环境配置**：
   - 适配 Fabric Loom 1.17 与 Minecraft 1.21.1。
   - 引入 GeckoLib Fabric 1.21.1（`software.bernie.geckolib:geckolib-fabric-1.21.1:4.7.4`）。
   - 引入 JEI Fabric API（`compileOnly`）并在 `fabric.mod.json` 中配置 `jei_mod_plugin` 入口点。
   - 配置 Java 21 编译与 UTF-8 编码。
2. **注册机制转换**：
   - 实体注册：从 NeoForge 的 `DeferredRegister` 迁移至原生 `BuiltInRegistries.ENTITY_TYPE`。
   - 物品注册：迁移至原生 `BuiltInRegistries.ITEM`，包含 `bucket_of_dracolotl` 与 `dracolotl_spawn_egg`。
   - 实体属性注册：通过 `FabricDefaultAttributeRegistry.register` 注册 `DracolotlEntity.createAttributes()`。
   - 创造模式物品栏：通过 Fabric API 的 `ItemGroupEvents.modifyEntriesEvent` 注入工具栏与刷怪蛋栏。
3. **事件与核心逻辑**：
   - 龙蛋末影水晶召唤仪式：使用 Fabric API 的 `UseBlockCallback.EVENT` 监听右键点击方块并处理仪式与粒子生成。
   - 服务端延时任务：使用 `ServerTickEvents.END_SERVER_TICK` 实现 `Dracolotl.queueServerWork` 延时调度。
   - 实体 NBT 持久化：移除 NeoForge 特有的 `getPersistentData()`，完全依赖实体状态字段与 `addAdditionalSaveData`/`readAdditionalSaveData`。
4. **客户端与渲染**：
   - 使用 Fabric 独立的 `src/client/java` 源码集。
   - 迁移 `DracolotlModel` 与 `DracolotlRenderer`，通过 `EntityRendererRegistry.register` 在客户端初始化时挂载。
5. **资源文件迁移**：
   - 完整复制并对齐模型（`geo`）、动画（`animations`）、实体与物品贴图（`textures`）、语言文件（`en_us.json`, `zh_cn.json`）、战利品表（`loot_tables`）、标签（`tags`）以及进度（`advancements`）。

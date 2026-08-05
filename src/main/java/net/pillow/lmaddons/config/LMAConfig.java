package net.pillow.lmaddons.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class LMAConfig {

    public static final ForgeConfigSpec.IntValue SOUL_FRACTURE_MAX_LEVEL;
    public static final ForgeConfigSpec.IntValue SOUL_FRACTURE_DURATION_SECONDS;

    public static final ForgeConfigSpec.IntValue PARRY_WINDOW_TICKS;
    public static final ForgeConfigSpec.IntValue PARRY_SUCCESS_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue PARRY_FAILURE_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.BooleanValue PARRY_IGNORE_BYPASS_TAGS;

    public static final ForgeConfigSpec.IntValue PARRY_INVUL_TICKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PARRY_INVUL_BYPASS_DAMAGE_TYPES;

    public static final ForgeConfigSpec.IntValue PARRY_PERFECT_WINDOW_TICKS;
    public static final ForgeConfigSpec.IntValue PARRY_PERFECT_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PARRY_PERFECT_EFFECTS;

    public static final ForgeConfigSpec.IntValue PARRY_SOUL_RAGE_DURATION_SECONDS;
    public static final ForgeConfigSpec.IntValue PARRY_SOUL_RAGE_MAX_LEVEL;

    public static final ForgeConfigSpec.DoubleValue DAGGER_RAY_RANGE;
    public static final ForgeConfigSpec.BooleanValue DAGGER_AUTO_TARGET;
    public static final ForgeConfigSpec.DoubleValue DAGGER_AUTO_TARGET_RANGE;
    public static final ForgeConfigSpec.DoubleValue DAGGER_AIM_HEIGHT_RATIO;
    public static final ForgeConfigSpec.DoubleValue DAGGER_VERTICAL_TRACKING_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue DAGGER_HORIZONTAL_TRACKING_STRENGTH;
    public static final ForgeConfigSpec.IntValue DAGGER_TRACKING_DURATION_TICKS;
    public static final ForgeConfigSpec.BooleanValue DAGGER_LIFESTEAL_ENABLED;
    public static final ForgeConfigSpec.DoubleValue DAGGER_LIFESTEAL_FLAT_RATIO;
    public static final ForgeConfigSpec.DoubleValue DAGGER_LIFESTEAL_MAXHP_RATIO;
    public static final ForgeConfigSpec.IntValue DAGGER_RAY_COUNT;
    public static final ForgeConfigSpec.IntValue DAGGER_AUTO_TARGET_COUNT;

    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment(
                "Soul Fracture effect configuration applied by Soul Great Sword attacks.",
                "灵魂巨剑攻击附加碎魂效果的配置。"
        ).push("soul_fracture");

        SOUL_FRACTURE_MAX_LEVEL = b
                .comment(
                        "Maximum level of Soul Fracture (1-5). Default 3.",
                        "碎魂最高等级（1-5）。默认 3 级。")
                .defineInRange("soulFractureMaxLevel", 3, 1, 5);

        SOUL_FRACTURE_DURATION_SECONDS = b
                .comment(
                        "Duration of Soul Fracture effect (seconds). Default 10s.",
                        "碎魂持续时间（秒）。默认 10 秒。")
                .defineInRange("soulFractureDurationSeconds", 10, 0, Integer.MAX_VALUE);

        b.pop();

        b.comment(
                "Parry enhancement configuration for Soul Great Sword.",
                "灵魂巨剑格挡强化配置").push("parry");

        PARRY_WINDOW_TICKS = b
                .comment(
                        "Parry window duration (ticks). 20 ticks = 1 second. Default 10 = 0.5s.",
                        "Set to 0 to disable parry (immediate cooldown on right-click).",
                        "格挡判定窗口时长时长（ticks），20 tick = 1 秒。默认 10 = 0.5 秒。",
                        "设为 0 表示空手右键立刻进入冷却，无法格挡。")
                .defineInRange("windowTicks", 10, 0, Integer.MAX_VALUE);

        PARRY_SUCCESS_COOLDOWN_TICKS = b
                .comment(
                        "Item cooldown after a successful parry (ticks). Default 10 = 0.5s.",
                        "格挡成功后的物品冷却时间（ticks）。默认 10 = 0.5 秒。")
                .defineInRange("successCooldownTicks", 10, 0, Integer.MAX_VALUE);

        PARRY_FAILURE_COOLDOWN_TICKS = b
                .comment(
                        "Item cooldown after a failed parry (ticks). Default 40 = 2s.",
                        "格挡失败后的物品冷却时间（ticks）。默认 40 = 2 秒。")
                .defineInRange("failureCooldownTicks", 40, 0, Integer.MAX_VALUE);

        PARRY_IGNORE_BYPASS_TAGS = b
                .comment(
                        "If enabled, parry can block damage with 'bypasses invulnerability' or 'bypasses armor' tags. Default false.",
                        "如果开启，格挡可以阻挡带有「无视无敌」或「穿甲」标签的伤害。默认关闭。")
                .define("ignoreBypassTags", false);

        PARRY_INVUL_TICKS = b
                .comment(
                        "Invulnerability frames after a successful parry (ticks). Default 0.5s. Set to 0 to disable.",
                        "格挡成功后玩家无敌帧持续时间（ticks）。默认 10 = 0.5 秒。设为 0 关闭无敌帧。")
                .defineInRange("invulTicks", 10, 0, Integer.MAX_VALUE);

        PARRY_INVUL_BYPASS_DAMAGE_TYPES = b
                .comment(
                        "Damage types that can bypass parry invulnerability frames (backdoor).",
                        "Format: ''minecraft:damage_type' or 'modid:damage_type'.",
                        "Each value MUST be enclosed in quotes in lmaddons-commmon.toml!",
                        "Default: empty list (invulnerability frames blocks all damage).",
                        "可穿透格挡无敌帧的伤害类型（后门）。",
                        "格式：'minecraft:伤害注册名' 或 '模组id:伤害注册名'",
                        "配置文件中每个值必须用英文双引号包裹！",
                        "默认：空列表（无敌帧阻挡所有伤害）。")
                .defineList("invulBypassDamageTypes", ArrayList::new, obj -> true);


        b.pop();

        b.comment(
                "Perfect parry reward configuration.",
                "完美格挡奖励配置。"
        ).push("perfectParry");

        PARRY_PERFECT_WINDOW_TICKS = b
                .comment(
                        "Perfect parry window (ticks). If the player parries within this duration after right-click, it counts as a perfect parry.",
                        "Default 3 = 0.15s. Set to 0 to disable perfect parry.",
                        "完美格挡判定窗口时长（ticks）。玩家右键后在此时长内受击并格挡成功，视为完美格挡。",
                        "默认 3 = 0.15 秒。设为 0 则关闭完美格挡。")
                .defineInRange("perfectWindowTicks", 3, 0, Integer.MAX_VALUE);

        PARRY_PERFECT_COOLDOWN_TICKS = b
                .comment(
                        "Item cooldown after a perfect parry (ticks). Default 3 = 0.15s.",
                        "完美格挡成功后的冷却时间（ticks）。默认 3 = 0.15 秒。")
                .defineInRange("perfectCooldownTicks", 3, 0, Integer.MAX_VALUE);

        PARRY_PERFECT_EFFECTS = b
                .comment(
                        "Perfect parry reward effects. Each entry format: 'potion_id;seconds;level'",
                        "Each value MUST be enclosed in quotes in lmaddons-commmon.toml!",
                        "Default: Speed I (minecraft:speed;5;1) and Strength I (minecraft:strength;5;1).",
                        "完美格挡奖励的药水效果。每个条目格式：\"药水ID;秒;等级\"。",
                        "配置文件中每个值必须用英文双引号包裹！",
                        "默认：速度 I（minecraft:speed;5;1）和力量 I（minecraft:strength;5;1）。"
                )
                .defineList("perfectEffects", () -> {
                    List<String> defaults = new ArrayList<>();
                    defaults.add("minecraft:speed;5;1");
                    defaults.add("minecraft:strength;5;1");
                    return defaults;
                }, obj -> obj instanceof String && ((String) obj).contains(";"));

        b.pop();

        b.comment(
                "Soul Rage effect configuration granted on successful parry.",
                "格挡成功附加灵魂之怒效果的配置").push("soul_rage");

        PARRY_SOUL_RAGE_DURATION_SECONDS = b
                .comment(
                        "Duration of Soul Rage effect (seconds). Default 5s.",
                        "灵魂之怒持续时间（秒）。默认 5 秒。")
                .defineInRange("soulRageDurationSeconds", 5, 0, Integer.MAX_VALUE);

        PARRY_SOUL_RAGE_MAX_LEVEL = b
                .comment(
                        "Maximum level of Soul Rage (1-255). Default 1.",
                        "灵魂之怒最高等级（1-255）。默认 1 级。")
                .defineInRange("soulRageMaxLevel", 1, 1, 255);

        b.pop();

        b.comment(
                "Soul Great Sword dagger skill configuration.",
                "追踪幻影匕首技能配置。"
        ).push("dagger");

        DAGGER_RAY_RANGE = b
                .comment(
                        "Range (blocks) for the ray-target detection. Default 30.0.",
                        "视线追踪的最大距离（格）。默认 30.0。"
                )
                .defineInRange("rayRange", 30.0D, 10.0D, Double.MAX_VALUE);

        DAGGER_AUTO_TARGET = b
                .comment(
                        "If enabled, daggers automatically seek the nearest entity instead of requiring line-of-sight.",
                        "若开启，幻影匕首自动追踪最近实体，无需视线瞄准。"
                )
                .define("autoTarget", false);

        DAGGER_AUTO_TARGET_RANGE = b
                .comment(
                        "Maximum range (blocks) for auto-targeting. Default 30.0.",
                        "自动追踪的最大范围（格）。默认 30.0。"
                )
                .defineInRange("autoTargetRange", 30.0D, 10.0D, Double.MAX_VALUE);

        DAGGER_AIM_HEIGHT_RATIO = b
                .comment(
                        "Ratio of the target's hitbox height to aim at (0.0 = feet, 0.5 = mid-body, 1.0 = head). Default 0.5.",
                        "瞄准高度系数。目标碰撞箱高度的比例（0.0 = 脚底，0.5 = 身体一半，1.0 = 头顶）。默认 0.5。"
                )
                .defineInRange("aimHeightRatio", 0.5D, 0.0D, 1.0D);

        DAGGER_VERTICAL_TRACKING_STRENGTH = b
                .comment(
                        "Vertical tracking strength multiplier for phantom daggers. Higher values make daggers descend/ascend more quickly towards the target.",
                        "Default 2.0. Set to 1.0 to keep original behavior.",
                        "幻影匕首垂直追踪强度乘数。数值越高，匕首向目标俯冲/抬升的速度越快。",
                        "默认 2.0。设为 1.0 则保持原版行为。"
                )
                .defineInRange("verticalTrackingStrength", 2.0D, 0.0D, Double.MAX_VALUE);

        DAGGER_HORIZONTAL_TRACKING_STRENGTH = b
                .comment(
                        "Horizontal tracking strength multiplier for phantom daggers" +
                                ". Default 2.0. Set to 1.0 to keep original behavior.",
                        "幻影匕首水平追踪加速度乘数。默认 2.0。设为 1.0 则保持原版行为。"
                )
                .defineInRange("horizontalTrackingStrength", 2.0D, 0.0D, Double.MAX_VALUE);

        DAGGER_TRACKING_DURATION_TICKS = b
                .comment(
                        "Tracking phase duration for phantom daggers (ticks). Default 100 = 5s.",
                        "幻影匕首追踪阶段持续时长（tick）。默认 100 = 5 秒。"
                )
                .defineInRange("trackingDurationTicks", 100, 10, Integer.MAX_VALUE);

        DAGGER_LIFESTEAL_ENABLED = b
                .comment(
                        "If enabled, phantom daggers heal the owner on hit. Default true.",
                        "若开启，幻影匕首命中后为发射者回血。默认开启。"
                )
                .define("lifestealEnabled", true);

        DAGGER_LIFESTEAL_FLAT_RATIO = b
                .comment(
                        "Heal amount = damage * this + targetMaxHP * maxHP ratio. Default 0.5.",
                        "回血量 = 伤害 × 此值 + 目标最大生命 × 最大生命系数。默认 0.5。"
                )
                .defineInRange("lifestealFlatRatio", 0.5D, 0.0D, 10.0D);

        DAGGER_LIFESTEAL_MAXHP_RATIO = b
                .comment(
                        "Heal amount = damage * flatRatio + targetMaxHP * this. Default 0.05 (5%).",
                        "回血量 = 伤害 × 伤害系数 + 目标最大生命 × 此值。默认 0.05（5%）。"
                )
                .defineInRange("lifestealMaxHPRatio", 0.05D, 0.0D, 1.0D);

        DAGGER_RAY_COUNT = b
                .comment(
                        "Number of phantom daggers in ray-target mode. Default 6.",
                        "视线追踪模式下幻影匕首的数量。默认 6。"
                )
                .defineInRange("rayCount", 6, 1, Integer.MAX_VALUE);

        DAGGER_AUTO_TARGET_COUNT = b
                .comment(
                        "Number of phantom daggers in auto-target mode. Default 6.",
                        "自动追踪模式下幻影匕首的数量。默认 6。"
                )
                .defineInRange("autoTargetCount", 6, 1, Integer.MAX_VALUE);

        b.pop();

        COMMON_SPEC = b.build();
    }
}
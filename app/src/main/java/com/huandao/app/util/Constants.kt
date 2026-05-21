package com.huandao.app.util

/**
 * 缓岛全局常量定义。
 */
object Constants {

    // ── 数据库 ──
    /** 数据库文件名 */
    const val DATABASE_NAME = "huandao.db"

    // ── 默认冷静期（小时） ──
    const val DEFAULT_COOL_PERIOD_HOURS = 72

    // ── 通知渠道 ID ──
    const val NOTIFICATION_CHANNEL_EXPIRY = "cooling_expiry"
    const val NOTIFICATION_CHANNEL_WEEKLY = "weekly_report"
    const val NOTIFICATION_CHANNEL_ACHIEVEMENT = "achievement"

    // ── 免打扰时段 ──
    const val QUIET_HOURS_START = 22  // 22:00
    const val QUIET_HOURS_END = 8     // 08:00

    // ── 自定义标签上限 ──
    const val MAX_CUSTOM_TAGS = 20

    // ── 每条条目最多可选标签数 ──
    const val MAX_TAGS_PER_ITEM = 3

    // ── 标题最大长度 ──
    const val MAX_TITLE_LENGTH = 50

    // ── 标签名称长度限制 ──
    const val MIN_TAG_NAME_LENGTH = 2
    const val MAX_TAG_NAME_LENGTH = 6

    // ── 8 个预设标签 ──
    val PRESET_TAGS: List<PresetTagDef> = listOf(
        PresetTagDef("tag_preset_stress", "压力大", "😫"),
        PresetTagDef("tag_preset_influenced", "被种草了", "🌱"),
        PresetTagDef("tag_preset_reward", "奖励自己", "🎁"),
        PresetTagDef("tag_preset_compare", "社交比较", "👀"),
        PresetTagDef("tag_preset_browse", "习惯性浏览", "📱"),
        PresetTagDef("tag_preset_bored", "无聊", "😴"),
        PresetTagDef("tag_preset_happy", "开心", "😊"),
        PresetTagDef("tag_preset_other", "其他", "🤔"),
    )

    // ── 冷静期选项 ──
    val COOL_PERIOD_OPTIONS: List<CoolPeriodOption> = listOf(
        CoolPeriodOption(24, "24 小时"),
        CoolPeriodOption(72, "72 小时"),
        CoolPeriodOption(168, "7 天"),
    )

    // ── 品类列表 ──
    val CATEGORIES: List<String> = listOf(
        "服饰",
        "数码",
        "美食",
        "家居",
        "美妆",
        "娱乐",
        "书籍",
        "其他",
    )

    // ── 决策结果 ──
    const val DECISION_BUY = "我决定买了"
    const val DECISION_PASS = "不再考虑"

    // ── WorkManager 任务名 ──
    const val WORK_EXPIRY_CHECK = "cooling_expiry_check"
    const val WORK_ARCHIVE = "archive_completed"

    // ── 实价锚点（元） ──
    data class PriceAnchor(val name: String, val price: Double)
    val PRICE_ANCHORS: List<PriceAnchor> = listOf(
        PriceAnchor("一杯奶茶", 15.0),
        PriceAnchor("一顿工作餐", 35.0),
        PriceAnchor("一张电影票", 60.0),
        PriceAnchor("一本好书", 50.0),
        PriceAnchor("一斤车厘子", 80.0),
        PriceAnchor("一顿火锅", 150.0),
        PriceAnchor("一张健身月卡", 300.0),
    )
}

/**
 * 预设标签定义。
 */
data class PresetTagDef(
    val id: String,
    val name: String,
    val emoji: String,
)

/**
 * 冷静期选项定义。
 */
data class CoolPeriodOption(
    val hours: Int,
    val label: String,
)

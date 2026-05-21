package com.huandao.app.ui.navigation

/**
 * 导航图常量定义。
 * BottomNavigation 三 Tab 的路由 ID 与内部导航路径的 action ID。
 *
 * 导航结构：
 * - Tab 1: 冷静池（列表 → 决策引导 / 历史记录）
 * - Tab 2: 洞察（P1 占位）
 * - Tab 3: 个人中心（P1 占位 + 数据导出）
 */
object NavGraph {

    // ── BottomNavigation Tab 目的地 ──
    const val DEST_COOL_POOL = "cool_pool"
    const val DEST_INSIGHTS = "insights"
    const val DEST_PROFILE = "profile"

    // ── 内部导航目的地 ──
    const val DEST_DECISION = "decision/{itemId}"
    const val DEST_HISTORY = "history"

    // ── 参数名 ──
    const val ARG_ITEM_ID = "itemId"

    /**
     * 构造决策页导航路径（含实参）。
     */
    fun decisionRoute(itemId: String): String = "decision/$itemId"
}

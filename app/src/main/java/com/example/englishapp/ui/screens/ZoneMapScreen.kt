package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit

fun ZoneMapScreen(context: Context, zone: String, navigate: (String) -> Unit): View {
    val route = when (zone.lowercase()) {
        "listening" -> "listening"
        "speaking" -> "speaking"
        "writing" -> "writing"
        else -> "reading"
    }
    val icon = when (zone.lowercase()) {
        "Speaking" -> "🎙️"
        "Reading" -> "📖"
        "Writing" -> "✏️"
        else -> "🔊"
    }
    val page = UiKit.column(context, 11).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("home") }, LinearLayout.LayoutParams(UiKit.dp(context, 46), UiKit.dp(context, 44)))
        addView(UiKit.text(context, "$icon  $zone Zone", 19, UiKit.INK, true, Gravity.CENTER))
    })
    page.addView(UiKit.text(context, "Follow the path!", 25, UiKit.INK, true))
    page.addView(UiKit.text(context, "Little steps make big adventures.", 14, UiKit.MUTED))
    val levels = listOf(
        Triple("1", "⭐", UiKit.GREEN),
        Triple("2", "⭐", UiKit.GREEN),
        Triple("3", "▶", UiKit.GOLD),
        Triple("4", "🔒", 0xFFD8E1E9.toInt()),
        Triple("5", "🔒", 0xFFD8E1E9.toInt())
    )
    levels.forEachIndexed { index, level ->
        page.addView(UiKit.row(context).apply {
            gravity = Gravity.CENTER
            setPadding(
                UiKit.dp(context, if (index % 2 == 0) 28 else 94),
                UiKit.dp(context, 5),
                UiKit.dp(context, if (index % 2 == 0) 94 else 28),
                UiKit.dp(context, 5)
            )
            addView(UiKit.button(context, "${level.first}  ${level.second}", level.third, UiKit.WHITE) {
                navigate(route)
            }, LinearLayout.LayoutParams(UiKit.dp(context, 70), UiKit.dp(context, 64)))
        })
        if (index < levels.lastIndex) {
            page.addView(UiKit.text(context, "·     ·     ·     ·     ·", 17, 0xFF8BB9D6.toInt(), gravity = Gravity.CENTER))
        }
    }
    page.addView(UiKit.button(context, "Start Lesson  ✨", UiKit.GOLD) {
        navigate(route)
    })
    return UiKit.scrollContent(context, page)
}

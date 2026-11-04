package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun ZoneMapScreen(
    context: Context,
    viewModel: MainViewModel,
    zone: String,
    navigate: (String) -> Unit
): View {
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
    page.addView(UiKit.text(context, "Choose a level · ${viewModel.vocabularyMessage}", 14, UiKit.MUTED))
    val levels = viewModel.availableVocabularyLevels
    levels.forEachIndexed { index, level ->
        page.addView(UiKit.row(context).apply {
            gravity = Gravity.CENTER
            setPadding(
                UiKit.dp(context, if (index % 2 == 0) 28 else 94),
                UiKit.dp(context, 5),
                UiKit.dp(context, if (index % 2 == 0) 94 else 28),
                UiKit.dp(context, 5)
            )
            val isSelected = level == viewModel.selectedVocabularyLevel
            addView(UiKit.button(
                context,
                "$level  ${if (isSelected) "▶" else "⭐"}",
                if (isSelected) UiKit.GOLD else UiKit.GREEN,
                UiKit.WHITE
            ) {
                viewModel.loadVocabulary(level)
                navigate(route)
            }, LinearLayout.LayoutParams(UiKit.dp(context, 70), UiKit.dp(context, 64)))
        })
        if (index < levels.lastIndex) {
            page.addView(UiKit.text(context, "·     ·     ·     ·     ·", 17, 0xFF8BB9D6.toInt(), gravity = Gravity.CENTER))
        }
    }
    page.addView(UiKit.button(context, "Start Lesson  ✨", UiKit.GOLD) {
        viewModel.loadVocabulary(viewModel.selectedVocabularyLevel)
        navigate(route)
    })
    return UiKit.scrollContent(context, page)
}

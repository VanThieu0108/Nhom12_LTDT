package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit

fun ArcadeScreen(context: Context, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.text(context, "🎮  Minigame Arcade", 24, UiKit.INK, true))
    page.addView(UiKit.text(context, "Pick a game and play!", 14, UiKit.MUTED))
    listOf(
        Triple("🧩", "Word Match", "Match words to meanings  •  Easy"),
        Triple("🏃", "Vocabulary Run", "Run to the right word  •  Medium"),
        Triple("☄️", "Meteor Spelling", "Spell fast to save the island  •  Hard")
    ).forEachIndexed { index, game ->
        page.addView(UiKit.row(context, 12).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(UiKit.dp(context, 14), UiKit.dp(context, 15), UiKit.dp(context, 14), UiKit.dp(context, 15))
            UiKit.background(this, listOf(UiKit.BLUE, UiKit.GREEN, UiKit.PURPLE)[index], 22)
            addView(UiKit.text(context, game.first, 37))
            addView(UiKit.column(context, 3).apply {
                addView(UiKit.text(context, game.second, 17, UiKit.WHITE, true))
                addView(UiKit.text(context, game.third, 12, 0xFFF3F8FF.toInt()))
            }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(UiKit.text(context, "›", 27, UiKit.WHITE, true))
            isClickable = true
            setOnClickListener { navigate(listOf("match", "runner", "meteor")[index]) }
        })
    }
    return UiKit.scrollContent(context, page)
}

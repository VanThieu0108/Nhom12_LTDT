package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel
import kotlin.random.Random

fun MinigameMatchScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "Quit", UiKit.WHITE, UiKit.MUTED) { navigate("arcade") })
        addView(UiKit.text(context, "⏱ 00:45", 15, UiKit.INK, true, Gravity.CENTER), LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "⭐ ${viewModel.score}", 15, UiKit.GOLD, true))
    })
    page.addView(UiKit.text(context, "Word Match", 25, UiKit.INK, true, Gravity.CENTER))
    page.addView(UiKit.text(context, "Tap a word, then its meaning", 14, UiKit.MUTED, gravity = Gravity.CENTER))
    val pairs = listOf(
        "Apple" to "Quả táo",
        "Cat" to "Con mèo",
        "Dog" to "Con chó",
        "Sun" to "Mặt trời"
    )
    val activePairs = pairs.filterNot { viewModel.matchedWords.contains(it.first) }
    val vietnamese = activePairs.map { it.second }.shuffled(Random(2026 + viewModel.matchedWords.size))
    activePairs.forEachIndexed { index, pair ->
        page.addView(UiKit.row(context, 9).apply {
            val left = UiKit.button(
                context,
                pair.first,
                if (viewModel.matchSelection == pair.first) 0xFFB7F1D2.toInt() else 0xFFDDF2FF.toInt(),
                UiKit.INK
            ) {
                viewModel.chooseMatch(pair.first)
                navigate("match")
            }
            val rightWord = vietnamese[index]
            val right = UiKit.button(
                context,
                rightWord,
                if (viewModel.matchSelection == rightWord) 0xFFFFE0BD.toInt() else 0xFFFFEBD8.toInt(),
                UiKit.INK
            ) {
                viewModel.chooseMatch(rightWord)
                navigate("match")
            }
            addView(left, LinearLayout.LayoutParams(0, UiKit.dp(context, 58), 1f))
            addView(UiKit.text(context, "↔", 20, UiKit.MUTED, true, Gravity.CENTER), LinearLayout.LayoutParams(UiKit.dp(context, 27), UiKit.dp(context, 56)))
            addView(right, LinearLayout.LayoutParams(0, UiKit.dp(context, 58), 1f))
        })
    }
    if (viewModel.feedback.isNotBlank()) page.addView(UiKit.text(context, viewModel.feedback, 14, UiKit.GREEN, true, Gravity.CENTER))
    if (viewModel.matchedWords.size == pairs.size) {
        viewModel.completeGame("Word Match")
        page.addView(UiKit.text(context, "🎉 All pairs matched!", 20, UiKit.GREEN, true, Gravity.CENTER))
        page.addView(UiKit.button(context, "Play again", UiKit.GOLD) {
            viewModel.resetMatch()
            navigate("match")
        })
    }
    return UiKit.scrollContent(context, page)
}

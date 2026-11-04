package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun WritingGameScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 14).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("home") }, LinearLayout.LayoutParams(UiKit.dp(context, 46), UiKit.dp(context, 44)))
        addView(UiKit.text(context, "Writing Quest", 21, UiKit.INK, true))
    })
    page.addView(UiKit.pill(context, "❤️".repeat(viewModel.hearts) + "     ⭐ ${viewModel.score}", 0xFFFFE9AE.toInt()))
    page.addView(UiKit.text(context, viewModel.vocabularyMessage, 11, UiKit.MUTED, gravity = Gravity.CENTER))
    page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
        addView(UiKit.text(context, "Write this word in English", 14, UiKit.MUTED, gravity = Gravity.CENTER))
        addView(UiKit.text(context, viewModel.currentWord.vietnamese, 25, UiKit.INK, true, Gravity.CENTER))
    })
    page.addView(UiKit.text(context, "Your answer", 14, UiKit.MUTED))
    page.addView(UiKit.text(
        context,
        if (viewModel.selectedLetters.isEmpty()) "_  ".repeat(viewModel.currentWord.english.length) else viewModel.selectedLetters.toList().joinToString("  "),
        27,
        UiKit.BLUE,
        true,
        Gravity.CENTER
    ).apply {
        setPadding(UiKit.dp(context, 10), UiKit.dp(context, 17), UiKit.dp(context, 10), UiKit.dp(context, 17))
        UiKit.background(this, UiKit.WHITE, 18)
    })
    viewModel.currentWord.scrambledLetters.indices.chunked(3).forEach { tileIndices ->
        page.addView(UiKit.row(context, 8).apply {
            tileIndices.forEach { tileIndex ->
                val letter = viewModel.currentWord.scrambledLetters[tileIndex]
                addView(UiKit.button(context, letter.toString(), 0xFFDDF2FF.toInt(), UiKit.INK) {
                    viewModel.selectLetterAt(tileIndex)
                    navigate("writing")
                }.apply {
                    isEnabled = tileIndex !in viewModel.selectedLetterIndices && !viewModel.spellingCorrect
                    alpha = if (isEnabled) 1f else 0.45f
                }, LinearLayout.LayoutParams(0, UiKit.dp(context, 52), 1f))
            }
        })
    }
    page.addView(UiKit.row(context, 10).apply {
        addView(UiKit.button(context, "⌫ Undo", UiKit.WHITE, UiKit.INK) {
            viewModel.removeLastLetter()
            navigate("writing")
        }, LinearLayout.LayoutParams(0, UiKit.dp(context, 52), 1f))
        addView(UiKit.button(context, "Clear", 0xFFEDE7FF.toInt(), UiKit.PURPLE) {
            viewModel.clearSpelling()
            navigate("writing")
        }, LinearLayout.LayoutParams(0, UiKit.dp(context, 52), 1f))
    })
    if (viewModel.feedback.isNotBlank()) {
        page.addView(UiKit.text(context, viewModel.feedback, 14, UiKit.GREEN, true, Gravity.CENTER))
    }
    if (viewModel.spellingCorrect) {
        page.addView(UiKit.button(context, "Next Word  →", UiKit.GOLD) {
            viewModel.nextQuestion()
            navigate("writing")
        })
    } else {
        page.addView(UiKit.button(context, "Check Answer  ✓", UiKit.GOLD) {
            viewModel.checkSpelling()
            navigate("writing")
        })
    }
    return UiKit.scrollContent(context, page)
}

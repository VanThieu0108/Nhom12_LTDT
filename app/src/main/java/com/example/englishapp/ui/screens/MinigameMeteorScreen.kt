package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun MinigameMeteorScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 14), UiKit.dp(context, 12), UiKit.dp(context, 14), UiKit.dp(context, 18))
        setBackgroundColor(0xFF192344.toInt())
    }
    page.addView(UiKit.row(context, 8).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "Quit", 0xFF343E67.toInt(), UiKit.WHITE) { navigate("arcade") })
        addView(UiKit.text(context, "☄️ Meteor Spelling", 18, UiKit.WHITE, true, Gravity.CENTER), LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "⭐ ${viewModel.score}", 14, UiKit.GOLD, true))
    })
    page.addView(UiKit.row(context, 8).apply {
        addView(UiKit.text(context, "Wave 1", 14, UiKit.WHITE, true), LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "💚".repeat(viewModel.hearts), 16))
    })
    page.addView(UiKit.text(context, "✦       ·        ✨       ·       ✦", 22, 0xFFAAA9FF.toInt(), gravity = Gravity.CENTER))
    page.addView(UiKit.text(context, "       🔥☄️🔥", 52, gravity = Gravity.CENTER))
    page.addView(UiKit.card(context, 0xFF343E67.toInt(), 15).apply {
        addView(UiKit.text(context, "Translate:  ${viewModel.currentWord.vietnamese}", 16, UiKit.WHITE, true, Gravity.CENTER))
        val targetDisplay = viewModel.meteorTarget.mapIndexed { index, letter ->
            if (index < viewModel.meteorInput.length) letter else '_'
        }.joinToString("  ")
        addView(UiKit.text(context, targetDisplay, 24, UiKit.GOLD, true, Gravity.CENTER))
        addView(UiKit.text(context, viewModel.meteorInput.uppercase(), 17, UiKit.WHITE, true, Gravity.CENTER))
    })
    page.addView(UiKit.text(context, viewModel.vocabularyMessage, 11, 0xFFD6DDF7.toInt(), gravity = Gravity.CENTER))
    page.addView(UiKit.text(context, "Tap letters to destroy the meteor!", 13, 0xFFD6DDF7.toInt(), gravity = Gravity.CENTER))
    listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { letters ->
        page.addView(UiKit.row(context, 3).apply {
            letters.forEach { letter ->
                addView(UiKit.button(context, letter.toString(), 0xFF3A456F.toInt(), UiKit.WHITE) {
                    viewModel.submitMeteorLetter(letter)
                    navigate("meteor")
                }, LinearLayout.LayoutParams(0, UiKit.dp(context, 43), 1f))
            }
        })
    }
    if (viewModel.feedback.isNotBlank()) page.addView(UiKit.text(context, viewModel.feedback, 14, UiKit.GOLD, true, Gravity.CENTER))
    return UiKit.scrollContent(context, page)
}

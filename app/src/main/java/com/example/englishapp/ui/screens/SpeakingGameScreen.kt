package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun SpeakingGameScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 15).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 10).apply {
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("home") })
        addView(UiKit.text(context, "Speaking Quest", 20, UiKit.INK, true))
    })
    page.addView(UiKit.pill(context, "QUESTION 1 / 10      ❤️❤️❤️", 0xFFDFF7EB.toInt(), 0xFF3B9C70.toInt()))
    page.addView(UiKit.text(context, "Say this word out loud", 15, UiKit.MUTED, gravity = Gravity.CENTER))
    page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
        addView(UiKit.text(context, "🗣️", 35, gravity = Gravity.CENTER))
        addView(UiKit.text(context, viewModel.currentWord.english.uppercase(), 34, UiKit.INK, true, Gravity.CENTER))
        addView(UiKit.text(context, viewModel.currentWord.vietnamese, 16, UiKit.MUTED, gravity = Gravity.CENTER))
    })
    page.addView(UiKit.button(context, "🎙️", UiKit.GREEN, UiKit.WHITE) {
        viewModel.markSpeakingSuccess()
        navigate("speaking")
    }.apply {
        textSize = 42f
        layoutParams = android.widget.LinearLayout.LayoutParams(-1, UiKit.dp(context, 150))
    })
    page.addView(UiKit.text(
        context,
        if (viewModel.feedback.isNotBlank()) "✅ Perfect pronunciation!" else "Tap the microphone and say the word",
        15,
        if (viewModel.feedback.isNotBlank()) UiKit.GREEN else UiKit.MUTED,
        true,
        Gravity.CENTER
    ))
    page.addView(UiKit.button(context, "Continue  →", UiKit.GOLD) {
        viewModel.nextQuestion()
        navigate("speaking")
    })
    page.addView(UiKit.text(context, viewModel.vocabularyMessage, 11, UiKit.MUTED, gravity = Gravity.CENTER))
    return UiKit.scrollContent(context, page)
}

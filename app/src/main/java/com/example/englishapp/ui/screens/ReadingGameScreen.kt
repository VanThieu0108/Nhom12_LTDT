package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import kotlin.random.Random
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun ReadingGameScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 13).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("home") }, LinearLayout.LayoutParams(UiKit.dp(context, 46), UiKit.dp(context, 44)))
        addView(UiKit.column(context, 4).apply {
            addView(UiKit.text(context, "Reading Quest", 19, bold = true))
            addView(UiKit.text(context, "Progress  ${viewModel.score} points", 12, UiKit.MUTED))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "❤️".repeat(viewModel.hearts), 18))
    })
    page.addView(UiKit.column(context, 3).apply {
        UiKit.background(this, 0xFFE0EAF1.toInt(), 20)
        setPadding(UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4))
        addView(View(context).apply {
            UiKit.background(this, UiKit.GOLD, 20)
        }, LinearLayout.LayoutParams(UiKit.dp(context, (viewModel.activeWordIndex + 1) * 55), UiKit.dp(context, 10)))
    })
    page.addView(UiKit.text(context, "Choose the meaning", 16, UiKit.MUTED, gravity = Gravity.CENTER))
    page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
        addView(UiKit.text(context, "📖  Read this word", 13, UiKit.MUTED, gravity = Gravity.CENTER))
        addView(UiKit.text(context, viewModel.currentWord.english.uppercase(), 34, UiKit.INK, true, Gravity.CENTER))
    })
    val choices = (viewModel.currentWord.distractors + viewModel.currentWord.vietnamese)
        .shuffled(Random(viewModel.activeWordIndex))
    choices.forEach { answer ->
        val selected = viewModel.selectedAnswer == answer
        page.addView(UiKit.button(
            context,
            answer,
            if (selected && answer == viewModel.currentWord.vietnamese) 0xFFC9F2DC.toInt() else if (selected) 0xFFFFDADA.toInt() else UiKit.WHITE,
            UiKit.INK
        ) {
            viewModel.selectAnswer(answer)
            navigate("reading")
        }.apply { isEnabled = viewModel.selectedAnswer == null })
    }
    if (viewModel.feedback.isNotBlank()) {
        page.addView(UiKit.text(
            context,
            viewModel.feedback,
            15,
            if (viewModel.selectedAnswer == viewModel.currentWord.vietnamese) UiKit.GREEN else UiKit.ORANGE,
            true,
            Gravity.CENTER
        ))
        page.addView(UiKit.button(context, "Continue  →", UiKit.GOLD) {
            viewModel.nextQuestion()
            navigate("reading")
        })
    }
    page.addView(UiKit.button(context, "✏️  Practice Writing", 0xFFEDE7FF.toInt(), UiKit.PURPLE) {
        navigate("writing")
    })
    page.addView(UiKit.button(context, "🎙️  Speaking Practice", 0xFFDFF7EB.toInt(), 0xFF278459.toInt()) {
        navigate("speaking")
    })
    return UiKit.scrollContent(context, page)
}

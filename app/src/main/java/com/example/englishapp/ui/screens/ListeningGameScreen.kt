package com.example.englishapp.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel
import java.util.Locale

fun ListeningGameScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 13).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    var speechEngine: TextToSpeech? = null
    var speechReady = false
    speechEngine = TextToSpeech(context) { status ->
        speechReady = status == TextToSpeech.SUCCESS
        if (speechReady) speechEngine?.language = Locale.US
    }
    page.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) = Unit
        override fun onViewDetachedFromWindow(view: View) {
            speechEngine?.stop()
            speechEngine?.shutdown()
            speechEngine = null
        }
    })

    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("home") }, LinearLayout.LayoutParams(UiKit.dp(context, 46), UiKit.dp(context, 44)))
        addView(UiKit.column(context, 3).apply {
            addView(UiKit.text(context, "Listening Quest", 20, bold = true))
            addView(UiKit.text(context, "Question ${viewModel.activeWordIndex + 1} / ${viewModel.words.size}", 12, UiKit.MUTED))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "❤️".repeat(viewModel.hearts), 17))
    })
    page.addView(UiKit.column(context, 0).apply {
        UiKit.background(this, 0xFFE0EAF1.toInt(), 20)
        setPadding(UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4))
        addView(View(context).apply { UiKit.background(this, UiKit.GOLD, 20) },
            LinearLayout.LayoutParams(UiKit.dp(context, (viewModel.activeWordIndex + 1) * 55), UiKit.dp(context, 10)))
    })
    page.addView(UiKit.text(context, "Listen & Choose", 27, UiKit.INK, true, Gravity.CENTER))
    page.addView(UiKit.text(context, "Tap the speaker, then choose the word you heard.", 14, UiKit.MUTED, gravity = Gravity.CENTER))
    page.addView(UiKit.button(context, "🔊  Play Audio", UiKit.BLUE, UiKit.WHITE) {
        val engine = speechEngine
        if (speechReady && engine != null) {
            engine.speak(viewModel.currentWord.english, TextToSpeech.QUEUE_FLUSH, null, "listening-word")
        } else {
            Toast.makeText(context, "Audio is still loading. Tap again in a moment.", Toast.LENGTH_SHORT).show()
        }
    }.apply {
        textSize = 21f
        layoutParams = LinearLayout.LayoutParams(-1, UiKit.dp(context, 94))
    })
    page.addView(UiKit.text(context, "Which word did you hear?", 15, UiKit.MUTED, gravity = Gravity.CENTER))
    val choices = viewModel.words.map { it.english }.shuffled(java.util.Random(viewModel.activeWordIndex.toLong()))
    choices.forEach { answer ->
        val selected = viewModel.selectedAnswer == answer
        page.addView(UiKit.button(
            context,
            answer,
            if (selected && answer == viewModel.currentWord.english) 0xFFC9F2DC.toInt()
            else if (selected) 0xFFFFDADA.toInt() else UiKit.WHITE,
            UiKit.INK
        ) {
            viewModel.selectListeningAnswer(answer)
            navigate("listening")
        }.apply { isEnabled = viewModel.selectedAnswer == null })
    }
    if (viewModel.selectedAnswer != null) {
        page.addView(UiKit.text(
            context,
            if (viewModel.selectedAnswer == viewModel.currentWord.english) "✅ Correct! +10 points" else "Almost! Listen once more next round.",
            15,
            if (viewModel.selectedAnswer == viewModel.currentWord.english) UiKit.GREEN else UiKit.ORANGE,
            true,
            Gravity.CENTER
        ))
        page.addView(UiKit.button(context, "Continue  →", UiKit.GOLD) {
            viewModel.nextQuestion()
            navigate("listening")
        })
    }
    page.addView(UiKit.text(context, "⭐ ${viewModel.score} points", 14, UiKit.MUTED, true, Gravity.CENTER))
    return UiKit.scrollContent(context, page)
}

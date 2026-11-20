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
    if (!viewModel.isReadingLessonReady) {
        page.addView(UiKit.button(context, "‹  Back to levels", UiKit.WHITE, UiKit.INK) { navigate("map:Reading") })
        page.addView(UiKit.text(context, viewModel.vocabularyMessage, 16, UiKit.MUTED, gravity = Gravity.CENTER))
        page.addView(UiKit.button(context, "Retry Level ${viewModel.selectedReadingLevel}", UiKit.ORANGE) {
            viewModel.startReadingLesson()
            navigate("reading")
        }.apply { isEnabled = !viewModel.isVocabularyLoading })
        return UiKit.scrollContent(context, page)
    }

    if (viewModel.isReadingLessonComplete) {
        page.addView(UiKit.text(
            context,
            if (viewModel.wasReadingAttemptPassed) "🌟 Level ${viewModel.selectedReadingLevel} complete!" else "Let's try again!",
            25,
            if (viewModel.wasReadingAttemptPassed) UiKit.GREEN else UiKit.ORANGE,
            true,
            Gravity.CENTER
        ))
        page.addView(UiKit.text(
            context,
            "${viewModel.readingCorrectCount}/${MainViewModel.READING_QUESTIONS_PER_LEVEL} correct · 8 needed to pass",
            16,
            UiKit.MUTED,
            gravity = Gravity.CENTER
        ))
        page.addView(UiKit.button(
            context,
            if (viewModel.wasReadingAttemptPassed) "Continue to level map" else "Retry level",
            UiKit.GOLD
        ) {
            if (viewModel.wasReadingAttemptPassed) {
                navigate("map:Reading")
            } else {
                viewModel.startReadingLesson()
                navigate("reading")
            }
        })
        return UiKit.scrollContent(context, page)
    }

    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("map:Reading") }, LinearLayout.LayoutParams(UiKit.dp(context, 46), UiKit.dp(context, 44)))
        addView(UiKit.column(context, 4).apply {
            addView(UiKit.text(context, "Reading Quest", 19, bold = true))
            addView(UiKit.text(
                context,
                "Level ${viewModel.selectedReadingLevel} · ${viewModel.activeWordIndex + 1}/${MainViewModel.READING_QUESTIONS_PER_LEVEL}",
                12,
                UiKit.MUTED
            ))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "❤️".repeat(viewModel.hearts), 18))
    })
    page.addView(UiKit.column(context, 3).apply {
        UiKit.background(this, 0xFFE0EAF1.toInt(), 20)
        setPadding(UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4))
        addView(View(context).apply {
            UiKit.background(this, UiKit.ORANGE, 20)
        }, LinearLayout.LayoutParams(UiKit.dp(context, (viewModel.activeWordIndex + 1) * 55), UiKit.dp(context, 10)))
    })
    page.addView(UiKit.text(
        context,
        "Reading Level ${viewModel.selectedReadingLevel} · Choose the meaning",
        16,
        UiKit.MUTED,
        gravity = Gravity.CENTER
    ))
    page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
        val passage = viewModel.currentWord.readingPassage
        addView(UiKit.text(
            context,
            if (passage == null) "📖  Read this word" else "📖  Read the sentence",
            13,
            UiKit.MUTED,
            gravity = Gravity.CENTER
        ))
        if (passage != null) {
            addView(UiKit.text(context, passage, 16, UiKit.INK, gravity = Gravity.CENTER))
        }
        addView(UiKit.text(context, viewModel.currentWord.english.uppercase(), 34, UiKit.INK, true, Gravity.CENTER))
    })
    val choices = (viewModel.currentWord.distractors.take(3) + viewModel.currentWord.vietnamese)
        .shuffled(Random(viewModel.activeWordIndex))
    choices.forEach { answer ->
        val selected = viewModel.selectedAnswer == answer
        page.addView(UiKit.button(
            context,
            answer,
            if (selected && answer == viewModel.currentWord.vietnamese) 0xFFC9F2DC.toInt() else if (selected) 0xFFFFDADA.toInt() else UiKit.WHITE,
            UiKit.INK
        ) {
            viewModel.selectReadingAnswer(answer)
            navigate("reading")
        }.apply { isEnabled = viewModel.selectedAnswer == null && !viewModel.isReadingLessonComplete })
    }
    page.addView(UiKit.text(
        context,
        "${viewModel.readingCorrectCount} correct · 8 needed to pass",
        11,
        UiKit.MUTED,
        gravity = Gravity.CENTER
    ))
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
            viewModel.continueReadingLesson()
            navigate("reading")
        })
    }
    return UiKit.scrollContent(context, page)
}

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

    if (!viewModel.isWritingLessonReady) {
        page.addView(UiKit.button(context, "‹  Back to levels", UiKit.WHITE, UiKit.INK) { navigate("map:Writing") })
        page.addView(UiKit.text(context, viewModel.vocabularyMessage, 16, UiKit.MUTED, gravity = Gravity.CENTER))
        page.addView(UiKit.button(context, "Retry Level ${viewModel.selectedWritingLevel}", UiKit.PURPLE) {
            viewModel.startWritingLesson()
            navigate("writing")
        }.apply { isEnabled = !viewModel.isVocabularyLoading })
        return UiKit.scrollContent(context, page)
    }

    if (viewModel.isWritingLessonComplete) {
        page.addView(UiKit.text(
            context,
            if (viewModel.wasWritingAttemptPassed) "🌟 Level ${viewModel.writingAttemptLevel} complete!" else "Let's practice again!",
            25,
            if (viewModel.wasWritingAttemptPassed) UiKit.GREEN else UiKit.PURPLE,
            true,
            Gravity.CENTER
        ))
        page.addView(UiKit.text(
            context,
            "${viewModel.writingCorrectCount}/${MainViewModel.WRITING_QUESTIONS_PER_LEVEL} correct · 8 needed to pass",
            16,
            UiKit.MUTED,
            gravity = Gravity.CENTER
        ))
        page.addView(UiKit.button(
            context,
            if (viewModel.wasWritingAttemptPassed) "Continue to level map" else "Retry level",
            UiKit.GOLD
        ) {
            if (viewModel.wasWritingAttemptPassed) {
                navigate("map:Writing")
            } else {
                viewModel.startWritingLesson()
                navigate("writing")
            }
        })
        return UiKit.scrollContent(context, page)
    }

    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("map:Writing") }, LinearLayout.LayoutParams(UiKit.dp(context, 46), UiKit.dp(context, 44)))
        addView(UiKit.column(context, 3).apply {
            addView(UiKit.text(context, "Writing Quest", 19, bold = true))
            addView(UiKit.text(
                context,
                "Level ${viewModel.selectedWritingLevel} · ${viewModel.activeWordIndex + 1}/${MainViewModel.WRITING_QUESTIONS_PER_LEVEL}",
                12,
                UiKit.MUTED
            ))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.pill(context, "⭐ ${viewModel.writingCorrectCount}", 0xFFFFE9AE.toInt()))
    })
    page.addView(UiKit.pill(context, "❤️".repeat(viewModel.hearts) + "     ⭐ ${viewModel.score}", 0xFFFFE9AE.toInt()))
    page.addView(UiKit.text(
        context,
        when (viewModel.selectedWritingLevel) {
            in 1..10 -> "Short, familiar words"
            in 11..20 -> "Build spelling confidence with longer words"
            in 21..30 -> "Practice more challenging spelling patterns"
            in 31..40 -> "Spell longer words and phrases"
            else -> "Write a complete short sentence"
        },
        12,
        UiKit.MUTED,
        gravity = Gravity.CENTER
    ))
    page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
        addView(UiKit.text(
            context,
            if (viewModel.selectedWritingLevel >= 41) "Write this sentence in English" else "Write this word in English",
            14,
            UiKit.MUTED,
            gravity = Gravity.CENTER
        ))
        addView(UiKit.text(context, viewModel.currentWord.vietnamese, 23, UiKit.INK, true, Gravity.CENTER))
    })
    page.addView(UiKit.text(context, "Your answer", 14, UiKit.MUTED))
    val target = viewModel.currentWord.english
    var selectedIndex = 0
    val answerSlots = buildString {
        target.forEach { character ->
            if (character.isLetter()) {
                append(viewModel.selectedLetters.getOrNull(selectedIndex)?.toString() ?: "＿")
                append("  ")
                selectedIndex += 1
            } else {
                append("   ")
            }
        }
    }.trimEnd()
    page.addView(UiKit.text(
        context,
        answerSlots,
        25,
        UiKit.PURPLE,
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
                addView(UiKit.button(context, letter.toString().uppercase(), 0xFFEDE7FF.toInt(), UiKit.INK) {
                    viewModel.selectLetterAt(tileIndex)
                    navigate("writing")
                }.apply {
                    isEnabled = tileIndex !in viewModel.selectedLetterIndices &&
                        !viewModel.isWritingQuestionAnswered &&
                        viewModel.selectedLetters.length < target.count(Char::isLetter)
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
        page.addView(UiKit.text(
            context,
            viewModel.feedback,
            14,
            if (viewModel.spellingCorrect) UiKit.GREEN else UiKit.PURPLE,
            true,
            Gravity.CENTER
        ))
    }
    if (viewModel.isWritingQuestionAnswered) {
        page.addView(UiKit.button(context, "Continue  →", UiKit.GOLD) {
            viewModel.continueWritingLesson()
            navigate("writing")
        })
    } else {
        page.addView(UiKit.button(context, "Check Answer  ✓", UiKit.PURPLE) {
            viewModel.checkSpelling()
            navigate("writing")
        }.apply {
            isEnabled = viewModel.selectedLetters.length == target.count(Char::isLetter)
            alpha = if (isEnabled) 1f else 0.55f
        })
    }
    return UiKit.scrollContent(context, page)
}

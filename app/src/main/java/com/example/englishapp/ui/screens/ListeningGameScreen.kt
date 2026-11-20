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
        addView(UiKit.button(context, "‹ Map", UiKit.WHITE, UiKit.INK) {
            navigate("map:Listening")
        })
        addView(UiKit.text(
            context,
            "Listening · Level ${viewModel.selectedListeningLevel} / ${MainViewModel.LISTENING_LEVEL_COUNT}",
            16,
            UiKit.INK,
            true,
            Gravity.CENTER
        ), LinearLayout.LayoutParams(0, -2, 1f))
    })

    when {
        viewModel.isVocabularyLoading -> {
            page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, "🔊", 36, gravity = Gravity.CENTER))
                addView(UiKit.text(context, "Loading listening lesson...", 16, UiKit.MUTED, true, Gravity.CENTER))
            }, LinearLayout.LayoutParams(-1, UiKit.dp(context, 190)))
        }

        !viewModel.isListeningLessonReady -> {
            page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, "🔌", 36, gravity = Gravity.CENTER))
                addView(UiKit.text(context, viewModel.vocabularyMessage, 14, UiKit.MUTED, true, Gravity.CENTER))
                addView(UiKit.button(context, "Retry", UiKit.GOLD) {
                    viewModel.startListeningLesson()
                    navigate("listening")
                })
                addView(UiKit.button(context, "Back to level map", UiKit.WHITE, UiKit.PURPLE) {
                    navigate("map:Listening")
                })
            }, LinearLayout.LayoutParams(-1, -2))
        }

        viewModel.isListeningLessonComplete -> {
            val passed = viewModel.wasListeningAttemptPassed
            page.addView(UiKit.card(
                context,
                if (passed) 0xFFE6F7ED.toInt() else 0xFFFFF1D9.toInt(),
                22
            ).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, if (passed) "🌟" else "💪", 42, gravity = Gravity.CENTER))
                addView(UiKit.text(
                    context,
                    if (passed) "Listening level complete!" else "Almost there!",
                    20,
                    if (passed) UiKit.GREEN else UiKit.ORANGE,
                    true,
                    Gravity.CENTER
                ))
                addView(UiKit.text(
                    context,
                    "${viewModel.listeningCorrectCount}/${MainViewModel.LISTENING_QUESTIONS_PER_LEVEL} correct · 8 needed to pass",
                    14,
                    UiKit.MUTED,
                    gravity = Gravity.CENTER
                ))
            })
            if (passed && viewModel.unlockedListeningLevel > viewModel.selectedListeningLevel) {
                page.addView(UiKit.button(context, "Next level ${viewModel.unlockedListeningLevel}  →", UiKit.GOLD) {
                    viewModel.selectListeningLevel(viewModel.unlockedListeningLevel)
                    navigate("map:Listening")
                })
            } else if (passed && viewModel.selectedListeningLevel == MainViewModel.LISTENING_LEVEL_COUNT) {
                page.addView(UiKit.text(context, "🎉 You completed all 50 Listening levels!", 15, UiKit.GREEN, true, Gravity.CENTER))
            } else {
                page.addView(UiKit.button(context, "Try this level again", UiKit.GOLD) {
                    viewModel.startListeningLesson()
                    navigate("listening")
                })
            }
            page.addView(UiKit.button(context, "Back to Listening map", UiKit.WHITE, UiKit.PURPLE) {
                navigate("map:Listening")
            })
        }

        else -> {
            page.addView(UiKit.row(context, 10).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(UiKit.column(context, 3).apply {
                    addView(UiKit.text(
                        context,
                        "Question ${viewModel.activeWordIndex + 1} / ${viewModel.words.size}",
                        14,
                        UiKit.INK,
                        true
                    ))
                    addView(UiKit.text(
                        context,
                        "${viewModel.listeningCorrectCount} correct · ${viewModel.listeningQuestionCount} answered",
                        11,
                        UiKit.MUTED
                    ))
                }, LinearLayout.LayoutParams(0, -2, 1f))
                addView(UiKit.text(context, "❤️".repeat(viewModel.hearts), 17))
            })
            page.addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                UiKit.background(this, 0xFFE0EAF1.toInt(), 20)
                val progress = (viewModel.activeWordIndex + 1).toFloat() /
                    MainViewModel.LISTENING_QUESTIONS_PER_LEVEL
                addView(View(context).apply { UiKit.background(this, UiKit.GOLD, 20) },
                    LinearLayout.LayoutParams(0, UiKit.dp(context, 10), progress))
                addView(View(context), LinearLayout.LayoutParams(
                    0,
                    UiKit.dp(context, 10),
                    1f - progress
                ))
            })
            page.addView(UiKit.text(context, "Listen & Choose", 27, UiKit.INK, true, Gravity.CENTER))
            page.addView(UiKit.text(
                context,
                "Tap the speaker, then choose the word or phrase you heard.",
                14,
                UiKit.MUTED,
                gravity = Gravity.CENTER
            ))
            page.addView(UiKit.button(context, "🔊  Play Audio", UiKit.BLUE, UiKit.WHITE) {
                val engine = speechEngine
                if (speechReady && engine != null) {
                    val rate = 0.8f + (viewModel.selectedListeningLevel - 1) *
                        (0.45f / (MainViewModel.LISTENING_LEVEL_COUNT - 1))
                    engine.setSpeechRate(rate)
                    engine.speak(viewModel.currentWord.english, TextToSpeech.QUEUE_FLUSH, null, "listening-word")
                } else {
                    Toast.makeText(context, "Audio is still loading. Tap again in a moment.", Toast.LENGTH_SHORT).show()
                }
            }.apply {
                textSize = 21f
                layoutParams = LinearLayout.LayoutParams(-1, UiKit.dp(context, 94))
            })
            page.addView(UiKit.text(context, "Which word or phrase did you hear?", 15, UiKit.MUTED, gravity = Gravity.CENTER))
            val choices = (viewModel.currentWord.distractors.take(viewModel.listeningOptionDistractorCount) +
                viewModel.currentWord.english)
                .distinct()
                .shuffled(java.util.Random(
                    viewModel.selectedListeningLevel * 100L + viewModel.activeWordIndex
                ))
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
            page.addView(UiKit.text(context, viewModel.vocabularyMessage, 11, UiKit.MUTED, gravity = Gravity.CENTER))
            if (viewModel.selectedAnswer != null) {
                page.addView(UiKit.text(
                    context,
                    if (viewModel.selectedAnswer == viewModel.currentWord.english) "✅ Correct! +10 points" else "Almost! Keep listening and try the next one.",
                    15,
                    if (viewModel.selectedAnswer == viewModel.currentWord.english) UiKit.GREEN else UiKit.ORANGE,
                    true,
                    Gravity.CENTER
                ))
                page.addView(UiKit.button(context, "Continue  →", UiKit.GOLD) {
                    viewModel.continueListeningLesson()
                    navigate("listening")
                })
            }
            page.addView(UiKit.text(context, "⭐ ${viewModel.score} points", 14, UiKit.MUTED, true, Gravity.CENTER))
        }
    }
    return UiKit.scrollContent(context, page)
}

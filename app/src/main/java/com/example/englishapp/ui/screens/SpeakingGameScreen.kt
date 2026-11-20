package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun SpeakingGameScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 15).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹ Map", UiKit.WHITE, UiKit.INK) { navigate("map:Speaking") })
        addView(UiKit.text(
            context,
            "Speaking · Level ${viewModel.selectedSpeakingLevel} / ${MainViewModel.SPEAKING_LEVEL_COUNT}",
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
                addView(UiKit.text(context, "🎙️", 38, gravity = Gravity.CENTER))
                addView(UiKit.text(context, "Loading speaking prompts...", 16, UiKit.MUTED, true, Gravity.CENTER))
            }, LinearLayout.LayoutParams(-1, UiKit.dp(context, 190)))
        }

        !viewModel.isSpeakingLessonReady -> {
            page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, "🔌", 36, gravity = Gravity.CENTER))
                addView(UiKit.text(context, viewModel.vocabularyMessage, 14, UiKit.MUTED, true, Gravity.CENTER))
                addView(UiKit.button(context, "Retry", UiKit.GOLD) {
                    viewModel.startSpeakingLesson()
                    navigate("speaking")
                })
                addView(UiKit.button(context, "Back to Speaking map", UiKit.WHITE, UiKit.PURPLE) {
                    navigate("map:Speaking")
                })
            }, LinearLayout.LayoutParams(-1, -2))
        }

        viewModel.isSpeakingLessonComplete -> {
            val passed = viewModel.wasSpeakingAttemptPassed
            page.addView(UiKit.card(
                context,
                if (passed) 0xFFE6F7ED.toInt() else 0xFFFFF1D9.toInt(),
                22
            ).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, if (passed) "🌟" else "💪", 42, gravity = Gravity.CENTER))
                addView(UiKit.text(
                    context,
                    if (passed) "Speaking level complete!" else "Keep practicing!",
                    20,
                    if (passed) UiKit.GREEN else UiKit.ORANGE,
                    true,
                    Gravity.CENTER
                ))
                addView(UiKit.text(
                    context,
                    "${viewModel.speakingCorrectCount}/${MainViewModel.SPEAKING_QUESTIONS_PER_LEVEL} prompts practiced · 8 needed to pass",
                    14,
                    UiKit.MUTED,
                    gravity = Gravity.CENTER
                ))
            })
            if (passed && viewModel.unlockedSpeakingLevel > viewModel.selectedSpeakingLevel) {
                page.addView(UiKit.button(context, "Next level ${viewModel.unlockedSpeakingLevel}  →", UiKit.GOLD) {
                    viewModel.selectSpeakingLevel(viewModel.unlockedSpeakingLevel)
                    navigate("map:Speaking")
                })
            } else if (passed && viewModel.selectedSpeakingLevel == MainViewModel.SPEAKING_LEVEL_COUNT) {
                page.addView(UiKit.text(context, "🎉 You completed all 50 Speaking levels!", 15, UiKit.GREEN, true, Gravity.CENTER))
            } else {
                page.addView(UiKit.button(context, "Try this level again", UiKit.GOLD) {
                    viewModel.startSpeakingLesson()
                    navigate("speaking")
                })
            }
            page.addView(UiKit.button(context, "Back to Speaking map", UiKit.WHITE, UiKit.PURPLE) {
                navigate("map:Speaking")
            })
        }

        else -> {
            page.addView(UiKit.row(context, 10).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(UiKit.column(context, 3).apply {
                    addView(UiKit.text(
                        context,
                        "Prompt ${viewModel.activeWordIndex + 1} / ${MainViewModel.SPEAKING_QUESTIONS_PER_LEVEL}",
                        14,
                        UiKit.INK,
                        true
                    ))
                    addView(UiKit.text(
                        context,
                        "${viewModel.speakingCorrectCount} practiced · ${viewModel.speakingQuestionCount} prompts answered",
                        11,
                        UiKit.MUTED
                    ))
                }, LinearLayout.LayoutParams(0, -2, 1f))
                addView(UiKit.text(context, "❤️❤️❤️", 17))
            })
            page.addView(UiKit.text(
                context,
                viewModel.speakingDifficultyDescription,
                13,
                UiKit.MUTED,
                true,
                Gravity.CENTER
            ))
            page.addView(UiKit.text(context, "Say this out loud", 15, UiKit.MUTED, gravity = Gravity.CENTER))
            page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
                addView(UiKit.text(context, "🗣️", 35, gravity = Gravity.CENTER))
                addView(UiKit.text(context, viewModel.currentWord.english, 25, UiKit.INK, true, Gravity.CENTER))
                val pronunciation = viewModel.currentWord.phonetic
                if (!pronunciation.isNullOrBlank()) {
                    val target = viewModel.currentWord.pronunciationText
                        ?.takeIf(String::isNotBlank)
                        ?: viewModel.currentWord.english
                    addView(UiKit.text(
                        context,
                        "🔤 $target  /$pronunciation/",
                        16,
                        UiKit.PURPLE,
                        true,
                        Gravity.CENTER
                    ))
                } else {
                    addView(UiKit.text(
                        context,
                        "🔤 Phonetic not available for this word",
                        12,
                        UiKit.MUTED,
                        gravity = Gravity.CENTER
                    ))
                }
                addView(UiKit.text(context, viewModel.currentWord.vietnamese, 15, UiKit.MUTED, gravity = Gravity.CENTER))
            })
            page.addView(UiKit.button(
                context,
                if (viewModel.isSpeakingQuestionAnswered) "✅  Practice marked" else "🎙️",
                if (viewModel.isSpeakingQuestionAnswered) 0xFFBFEBD3.toInt() else UiKit.GREEN,
                UiKit.WHITE
            ) {
                viewModel.markSpeakingSuccess()
                navigate("speaking")
            }.apply {
                textSize = if (viewModel.isSpeakingQuestionAnswered) 17f else 42f
                isEnabled = !viewModel.isSpeakingQuestionAnswered
                layoutParams = LinearLayout.LayoutParams(-1, UiKit.dp(context, 120))
            })
            page.addView(UiKit.text(
                context,
                if (viewModel.isSpeakingQuestionAnswered) "Nice work! Continue when you're ready."
                else "Say the prompt aloud, then tap the microphone to mark it practiced.",
                14,
                if (viewModel.isSpeakingQuestionAnswered) UiKit.GREEN else UiKit.MUTED,
                true,
                Gravity.CENTER
            ))
            if (viewModel.isSpeakingQuestionAnswered) {
                page.addView(UiKit.button(context, "Continue  →", UiKit.GOLD) {
                    viewModel.continueSpeakingLesson()
                    navigate("speaking")
                })
            }
            page.addView(UiKit.text(context, viewModel.vocabularyMessage, 11, UiKit.MUTED, gravity = Gravity.CENTER))
        }
    }
    return UiKit.scrollContent(context, page)
}

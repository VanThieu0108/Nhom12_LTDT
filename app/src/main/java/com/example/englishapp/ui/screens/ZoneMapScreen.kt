package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun ZoneMapScreen(
    context: Context,
    viewModel: MainViewModel,
    zone: String,
    navigate: (String) -> Unit
): View {
    val route = when (zone.lowercase()) {
        "listening" -> "listening"
        "speaking" -> "speaking"
        "writing" -> "writing"
        else -> "reading"
    }
    val isListening = zone.equals("listening", ignoreCase = true)
    val isSpeaking = zone.equals("speaking", ignoreCase = true)
    val isReading = zone.equals("reading", ignoreCase = true)
    val isWriting = zone.equals("writing", ignoreCase = true)
    val hasLevelPath = isListening || isSpeaking || isReading || isWriting
    val icon = when (zone.lowercase()) {
        "speaking" -> "🎙️"
        "reading" -> "📖"
        "writing" -> "✏️"
        else -> "🔊"
    }
    val page = UiKit.column(context, 11).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "‹", UiKit.WHITE, UiKit.INK) { navigate("home") }, LinearLayout.LayoutParams(UiKit.dp(context, 46), UiKit.dp(context, 44)))
        addView(UiKit.text(context, "$icon  $zone Zone", 19, UiKit.INK, true, Gravity.CENTER))
    })
    page.addView(UiKit.text(context, "Follow the path!", 25, UiKit.INK, true))
    page.addView(UiKit.text(
        context,
        if (isListening) "50 listening adventures · Complete 8/10 to unlock the next level."
        else if (isSpeaking) "50 speaking adventures · Complete 8/10 to unlock the next level."
        else if (isReading) "50 reading adventures · Complete 8/10 to unlock the next level."
        else if (isWriting) "50 writing adventures · Spell 8/10 correctly to unlock the next level."
        else "Choose a level · ${viewModel.vocabularyMessage}",
        14,
        UiKit.MUTED
    ))
    if (hasLevelPath) {
        val completedCount = when {
            isListening -> viewModel.completedListeningLevels.size
            isSpeaking -> viewModel.completedSpeakingLevels.size
            isReading -> viewModel.completedReadingLevels.size
            else -> viewModel.completedWritingLevels.size
        }
        val unlockedLevel = when {
            isListening -> viewModel.unlockedListeningLevel
            isSpeaking -> viewModel.unlockedSpeakingLevel
            isReading -> viewModel.unlockedReadingLevel
            else -> viewModel.unlockedWritingLevel
        }
        val selected = when {
            isListening -> viewModel.selectedListeningLevel
            isSpeaking -> viewModel.selectedSpeakingLevel
            isReading -> viewModel.selectedReadingLevel
            else -> viewModel.selectedWritingLevel
        }
        page.addView(UiKit.row(context, 7).apply {
            addView(UiKit.pill(context, "⭐ $completedCount / 50 complete", 0xFFE1F5E9.toInt(), UiKit.GREEN))
            addView(UiKit.pill(context, "🔓 Level $unlockedLevel", 0xFFFFF1CE.toInt(), 0xFF9A6900.toInt()))
        })
        page.addView(UiKit.button(
            context,
            "Start ${when {
                isListening -> "Listening"
                isSpeaking -> "Speaking"
                isReading -> "Reading"
                else -> "Writing"
            }} Level $selected  ✨",
            when {
                isReading -> UiKit.ORANGE
                else -> UiKit.GOLD
            }
        ) {
            val started = when {
                isListening -> viewModel.startListeningLesson(selected)
                isSpeaking -> viewModel.startSpeakingLesson(selected)
                isReading -> viewModel.startReadingLesson(selected)
                else -> viewModel.startWritingLesson(selected)
            }
            if (started) navigate(route)
        })
    }
    val levels: List<Int> = if (hasLevelPath) {
        (1..MainViewModel.WRITING_LEVEL_COUNT).toList()
    } else {
        viewModel.availableVocabularyLevels
    }
    levels.forEachIndexed { index, level ->
        page.addView(UiKit.row(context).apply {
            gravity = Gravity.CENTER
            setPadding(
                UiKit.dp(context, if (index % 2 == 0) 28 else 94),
                UiKit.dp(context, 5),
                UiKit.dp(context, if (index % 2 == 0) 94 else 28),
                UiKit.dp(context, 5)
            )
            val isSelected = if (isListening) {
                level == viewModel.selectedListeningLevel
            } else if (isSpeaking) {
                level == viewModel.selectedSpeakingLevel
            } else if (isReading) {
                level == viewModel.selectedReadingLevel
            } else if (isWriting) {
                level == viewModel.selectedWritingLevel
            } else {
                level == viewModel.selectedVocabularyLevel
            }
            val isCompleted = when {
                isListening -> viewModel.isListeningLevelCompleted(level)
                isSpeaking -> viewModel.isSpeakingLevelCompleted(level)
                isReading -> viewModel.isReadingLevelCompleted(level)
                isWriting -> viewModel.isWritingLevelCompleted(level)
                else -> false
            }
            val isUnlocked = when {
                isListening -> viewModel.isListeningLevelUnlocked(level)
                isSpeaking -> viewModel.isSpeakingLevelUnlocked(level)
                isReading -> viewModel.isReadingLevelUnlocked(level)
                isWriting -> viewModel.isWritingLevelUnlocked(level)
                else -> true
            }
            val nodeText = when {
                isCompleted -> "⭐\n$level"
                isSelected && isUnlocked -> "▶\n$level"
                !isUnlocked -> "🔒\n$level"
                else -> "$level"
            }
            addView(UiKit.button(
                context,
                nodeText,
                when {
                    isCompleted -> UiKit.GREEN
                    isSelected -> UiKit.GOLD
                    !isUnlocked -> 0xFFE0E6EC.toInt()
                    else -> 0xFFFFF0D8.toInt()
                },
                UiKit.WHITE
            ) {
                if (isListening) {
                    viewModel.selectListeningLevel(level)
                } else if (isSpeaking) {
                    viewModel.selectSpeakingLevel(level)
                } else if (isReading) {
                    viewModel.selectReadingLevel(level)
                } else if (isWriting) {
                    viewModel.selectWritingLevel(level)
                } else {
                    viewModel.loadVocabulary(level)
                    navigate(route)
                }
            }.apply {
                isEnabled = isUnlocked || isCompleted
                alpha = if (isUnlocked || isCompleted) 1f else 0.75f
                textSize = if (hasLevelPath) 13f else 15f
            }, LinearLayout.LayoutParams(UiKit.dp(context, if (hasLevelPath) 76 else 70), UiKit.dp(context, if (hasLevelPath) 68 else 64)))
        })
        if (index < levels.lastIndex) {
            val connector = if (index % 2 == 0) "·   ·   ·   ·   ·" else "·   ·   ·   ·   ·"
            page.addView(UiKit.text(context, connector, 17, 0xFF8BB9D6.toInt(), gravity = Gravity.CENTER))
        }
    }
    if (hasLevelPath) {
        val selected = when {
            isListening -> viewModel.selectedListeningLevel
            isSpeaking -> viewModel.selectedSpeakingLevel
            isReading -> viewModel.selectedReadingLevel
            else -> viewModel.selectedWritingLevel
        }
        val isCompleted = when {
            isListening -> viewModel.isListeningLevelCompleted(selected)
            isSpeaking -> viewModel.isSpeakingLevelCompleted(selected)
            isReading -> viewModel.isReadingLevelCompleted(selected)
            else -> viewModel.isWritingLevelCompleted(selected)
        }
        if (isCompleted) {
            page.addView(UiKit.text(context, "Completed levels stay unlocked for replay.", 12, UiKit.GREEN, true, Gravity.CENTER))
        }
    } else {
        page.addView(UiKit.button(context, "Start Lesson  ✨", UiKit.GOLD) {
            viewModel.loadVocabulary(viewModel.selectedVocabularyLevel)
            navigate(route)
        })
    }
    return UiKit.scrollContent(context, page)
}

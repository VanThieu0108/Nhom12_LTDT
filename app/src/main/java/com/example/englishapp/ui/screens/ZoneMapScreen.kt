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
    val hasLevelPath = isListening || isSpeaking
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
        else "Choose a level · ${viewModel.vocabularyMessage}",
        14,
        UiKit.MUTED
    ))
    if (hasLevelPath) {
        val completedCount = if (isListening) {
            viewModel.completedListeningLevels.size
        } else {
            viewModel.completedSpeakingLevels.size
        }
        val unlockedLevel = if (isListening) {
            viewModel.unlockedListeningLevel
        } else {
            viewModel.unlockedSpeakingLevel
        }
        val selected = if (isListening) viewModel.selectedListeningLevel else viewModel.selectedSpeakingLevel
        page.addView(UiKit.row(context, 7).apply {
            addView(UiKit.pill(context, "⭐ $completedCount / 50 complete", 0xFFE1F5E9.toInt(), UiKit.GREEN))
            addView(UiKit.pill(context, "🔓 Level $unlockedLevel", 0xFFFFF1CE.toInt(), 0xFF9A6900.toInt()))
        })
        page.addView(UiKit.button(
            context,
            "Start ${if (isListening) "Listening" else "Speaking"} Level $selected  ✨",
            UiKit.GOLD
        ) {
            val started = if (isListening) {
                viewModel.startListeningLesson(selected)
            } else {
                viewModel.startSpeakingLesson(selected)
            }
            if (started) navigate(route)
        })
    }
    val levels: List<Int> = if (hasLevelPath) {
        (1..MainViewModel.SPEAKING_LEVEL_COUNT).toList()
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
            } else {
                level == viewModel.selectedVocabularyLevel
            }
            val isCompleted = when {
                isListening -> viewModel.isListeningLevelCompleted(level)
                isSpeaking -> viewModel.isSpeakingLevelCompleted(level)
                else -> false
            }
            val isUnlocked = when {
                isListening -> viewModel.isListeningLevelUnlocked(level)
                isSpeaking -> viewModel.isSpeakingLevelUnlocked(level)
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
        val selected = if (isListening) viewModel.selectedListeningLevel else viewModel.selectedSpeakingLevel
        val isCompleted = if (isListening) {
            viewModel.isListeningLevelCompleted(selected)
        } else {
            viewModel.isSpeakingLevelCompleted(selected)
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

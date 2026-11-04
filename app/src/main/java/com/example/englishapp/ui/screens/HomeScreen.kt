package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun HomeScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 13).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    val header = UiKit.row(context, 8).apply {
        gravity = Gravity.CENTER_VERTICAL
        setPadding(UiKit.dp(context, 13), UiKit.dp(context, 11), UiKit.dp(context, 13), UiKit.dp(context, 11))
        UiKit.background(this, 0xFFFFCF63.toInt(), 24)
        elevation = UiKit.dp(context, 4).toFloat()
        addView(UiKit.text(context, "🧑🏻‍🚀", 30, gravity = Gravity.CENTER).apply {
            UiKit.background(this, UiKit.WHITE, 40)
            setPadding(UiKit.dp(context, 7), UiKit.dp(context, 4), UiKit.dp(context, 7), UiKit.dp(context, 4))
        })
        addView(UiKit.column(context, 1).apply {
            addView(UiKit.text(context, "Hi, ${viewModel.user.explorerName}!", 15, bold = true))
            addView(UiKit.text(context, "🥈 SILVER RANK", 10, bold = true))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.pill(context, "⚡ ${viewModel.user.stamina}/5", 0xFFFFF1C7.toInt()))
        addView(UiKit.pill(context, "🪙 ${viewModel.user.coins}", 0xFFFFF8E0.toInt()))
    }
    page.addView(header)
    page.addView(UiKit.row(context, 5).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.column(context, 2).apply {
            addView(UiKit.text(context, "Study Island", 27, bold = true))
            addView(UiKit.text(context, "Your next big adventure starts here!", 13, UiKit.MUTED))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.pill(context, "🔥 ${viewModel.user.streakDays} DAY STREAK!", 0xFFFFEBD8.toInt(), 0xFFEB8245.toInt()))
    })
    page.addView(UiKit.row(context, 8).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.text(context, viewModel.vocabularyMessage, 11, UiKit.MUTED), LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.button(context, if (viewModel.isVocabularyLoading) "Loading..." else "Sync", UiKit.WHITE, UiKit.BLUE) {
            if (!viewModel.isVocabularyLoading) viewModel.loadVocabulary()
        })
    })

    val zones = listOf(
        Triple("Listening", "🔊", UiKit.BLUE),
        Triple("Speaking", "🎙️", UiKit.GREEN),
        Triple("Reading", "📖", UiKit.ORANGE),
        Triple("Writing", "✏️", UiKit.PURPLE)
    )
    zones.chunked(2).forEach { pair ->
        val zoneRow = UiKit.row(context, 11).apply {
            pair.forEach { (name, icon, color) ->
                val zone = UiKit.column(context, 5).apply {
                    setPadding(UiKit.dp(context, 13), UiKit.dp(context, 9), UiKit.dp(context, 13), UiKit.dp(context, 9))
                    UiKit.background(this, color, 22)
                    elevation = UiKit.dp(context, 4).toFloat()
                    addView(UiKit.text(context, icon, 31))
                    addView(UiKit.text(context, name, 16, UiKit.WHITE, true))
                    addView(UiKit.text(context, "0 / 5 lessons", 11, 0xFFEAF7FF.toInt()))
                    isClickable = true
                    setOnClickListener { navigate("map:$name") }
                }
                addView(zone, LinearLayout.LayoutParams(0, UiKit.dp(context, 112), 1f))
            }
        }
        page.addView(zoneRow, LinearLayout.LayoutParams(-1, -2))
    }

    page.addView(UiKit.column(context, 9).apply {
        setPadding(UiKit.dp(context, 15), UiKit.dp(context, 13), UiKit.dp(context, 15), UiKit.dp(context, 13))
        UiKit.gradient(this, intArrayOf(0xFFE86FC2.toInt(), 0xFF8664E9.toInt()), 24)
        elevation = UiKit.dp(context, 4).toFloat()
        addView(UiKit.row(context, 8).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(UiKit.text(context, "🎮", 38))
            addView(UiKit.column(context, 2).apply {
                addView(UiKit.text(context, "Minigame Arcade", 18, UiKit.WHITE, true))
                addView(UiKit.text(context, "Play, practice & earn!", 12, 0xFFF9EFFF.toInt()))
            }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(UiKit.text(context, "›", 28, UiKit.WHITE, true))
        })
        addView(UiKit.row(context, 6).apply {
            listOf("🧩 Match", "🏃 Run", "☄️ Meteor").forEachIndexed { index, label ->
                addView(UiKit.button(context, label, 0x55FFFFFF, UiKit.WHITE) {
                    navigate(listOf("match", "runner", "meteor")[index])
                }, LinearLayout.LayoutParams(0, UiKit.dp(context, 37), 1f))
            }
        })
        isClickable = true
        setOnClickListener { navigate("arcade") }
    })
    return UiKit.scrollContent(context, page)
}

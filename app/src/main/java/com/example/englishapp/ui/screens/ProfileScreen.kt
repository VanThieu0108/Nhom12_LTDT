package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun ProfileScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 6).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.text(context, "Profile & Stats", 26, UiKit.INK, true), LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "⚙️", 22).apply {
            isClickable = true
            setOnClickListener { navigate("login") }
        })
    })
    page.addView(UiKit.row(context, 12).apply {
        gravity = Gravity.CENTER_VERTICAL
        setPadding(UiKit.dp(context, 15), UiKit.dp(context, 16), UiKit.dp(context, 15), UiKit.dp(context, 16))
        UiKit.gradient(this, intArrayOf(0xFFFFBC54.toInt(), 0xFFFF8B4F.toInt()), 24)
        addView(UiKit.text(context, "🧑🏻‍🚀", 41, gravity = Gravity.CENTER).apply {
            UiKit.background(this, UiKit.WHITE, 18)
            setPadding(UiKit.dp(context, 8), UiKit.dp(context, 6), UiKit.dp(context, 8), UiKit.dp(context, 6))
        })
        addView(UiKit.column(context, 4).apply {
            addView(UiKit.text(context, viewModel.user.explorerName, 19, UiKit.WHITE, true))
            addView(UiKit.text(context, "🥈 Silver Rank  •  Level ${viewModel.user.level}", 13, UiKit.WHITE, true))
            addView(UiKit.text(context, "✨ +10% Gold from matches", 11, 0xFFFFF4D8.toInt()))
        })
    })
    page.addView(UiKit.column(context, 5).apply {
        UiKit.background(this, 0xFFE0EAF1.toInt(), 20)
        setPadding(UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4))
        addView(View(context).apply { UiKit.background(this, UiKit.GREEN, 20) }, LinearLayout.LayoutParams(UiKit.dp(context, 85), UiKit.dp(context, 9)))
    })
    page.addView(UiKit.text(context, "My Learning Journey", 19, UiKit.INK, true))
    val stats = listOf(
        Triple("📚", "Words Learned", viewModel.user.wordsLearned.toString()),
        Triple("🎯", "Perfect Scores", viewModel.user.perfectScores.toString()),
        Triple("🔥", "Current Streak", "${viewModel.user.streakDays} days"),
        Triple("🪙", "Gold Earned", viewModel.user.coins.toString())
    )
    stats.chunked(2).forEach { pair ->
        page.addView(UiKit.row(context, 9).apply {
            pair.forEach { stat ->
                addView(UiKit.card(context, UiKit.WHITE, 11).apply {
                    addView(UiKit.text(context, stat.first, 26))
                    addView(UiKit.text(context, stat.third, 20, UiKit.INK, true))
                    addView(UiKit.text(context, stat.second, 12, UiKit.MUTED))
                }, LinearLayout.LayoutParams(0, UiKit.dp(context, 105), 1f))
            }
        })
    }
    page.addView(UiKit.button(context, "Account & Sign Out", 0xFFE7F1F8.toInt()) { navigate("login") })
    return UiKit.scrollContent(context, page)
}

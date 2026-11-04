package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun TrophiesScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.text(context, "Adventure Leaderboard 🏆", 24, UiKit.INK, true))
    page.addView(UiKit.text(context, "Practice, play, and climb the ranks!", 14, UiKit.MUTED))
    page.addView(UiKit.row(context, 8).apply {
        gravity = Gravity.BOTTOM
        val podium = listOf(
            Triple("🥈", "Mia", "2nd"),
            Triple("👑", "Leo", "1st"),
            Triple("🥉", "Ava", "3rd")
        )
        podium.forEachIndexed { index, entry ->
            addView(UiKit.column(context, 3).apply {
                gravity = Gravity.CENTER
                setPadding(UiKit.dp(context, 5), UiKit.dp(context, 12), UiKit.dp(context, 5), UiKit.dp(context, 10))
                UiKit.background(this, listOf(0xFFDCEBFA.toInt(), 0xFFFFE9AE.toInt(), 0xFFFFE1CE.toInt())[index], 20)
                addView(UiKit.text(context, entry.first, 29, gravity = Gravity.CENTER))
                addView(UiKit.text(context, entry.second, 14, UiKit.INK, true, Gravity.CENTER))
                addView(UiKit.text(context, entry.third, 11, UiKit.MUTED, gravity = Gravity.CENTER))
            }, LinearLayout.LayoutParams(0, UiKit.dp(context, if (index == 1) 145 else 124), 1f))
        }
    })
    page.addView(UiKit.card(context, 0xFFFFF0CB.toInt(), 12).apply {
        addView(UiKit.text(context, "#${viewModel.user.rank}  ${viewModel.user.explorerName}", 17, UiKit.INK, true))
        addView(UiKit.text(context, "⭐ ${viewModel.score} points  •  Silver Rank", 13, UiKit.MUTED))
    })
    page.addView(UiKit.button(context, "⚔️  1v1 BATTLES MATCH", UiKit.PURPLE, UiKit.WHITE) {
        viewModel.awardBattleCoins()
        navigate("battle")
    })
    listOf("Mia  ·  980 pts", "Leo  ·  850 pts", "Ava  ·  720 pts", "${viewModel.user.explorerName}  ·  ${viewModel.score} pts").forEachIndexed { index, player ->
        page.addView(UiKit.row(context, 10).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(UiKit.dp(context, 12), UiKit.dp(context, 10), UiKit.dp(context, 12), UiKit.dp(context, 10))
            UiKit.background(this, UiKit.WHITE, 17)
            addView(UiKit.text(context, "${index + 1}", 15, UiKit.GOLD, true))
            addView(UiKit.text(context, player, 14, UiKit.INK, true), LinearLayout.LayoutParams(0, -2, 1f))
            addView(UiKit.text(context, "🏅", 17))
        })
    }
    return UiKit.scrollContent(context, page)
}

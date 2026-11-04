package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun BattleScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 15).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(UiKit.dp(context, 18), UiKit.dp(context, 24), UiKit.dp(context, 18), UiKit.dp(context, 18))
    }
    page.addView(UiKit.text(context, "⚔️  1v1 BATTLE", 26, UiKit.PURPLE, true, Gravity.CENTER))
    page.addView(UiKit.text(context, "Great effort, Explorer!", 18, UiKit.INK, true, Gravity.CENTER))
    page.addView(UiKit.card(context, 0xFFEDE7FF.toInt(), 20).apply {
        gravity = Gravity.CENTER
        addView(UiKit.text(context, "🧑🏻‍🚀   VS   🐲", 39, gravity = Gravity.CENTER))
        addView(UiKit.text(context, "You earned 10 battle coins!", 16, UiKit.INK, true, Gravity.CENTER))
        addView(UiKit.text(context, "Total coins: ${viewModel.user.coins}", 13, UiKit.MUTED, gravity = Gravity.CENTER))
    })
    page.addView(UiKit.button(context, "Back to Trophies", UiKit.GOLD) { navigate("trophies") })
    return UiKit.scrollContent(context, page)
}

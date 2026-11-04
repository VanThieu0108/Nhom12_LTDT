package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun MinigameRunnerScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 14), UiKit.dp(context, 12), UiKit.dp(context, 14), UiKit.dp(context, 18))
    }
    page.addView(UiKit.row(context, 8).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "Quit", UiKit.WHITE, UiKit.MUTED) { navigate("arcade") })
        addView(UiKit.text(context, "🏁 Vocabulary Run", 19, UiKit.INK, true, Gravity.CENTER), LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.text(context, "${viewModel.runnerDistance} m", 14, UiKit.INK, true))
    })
    page.addView(UiKit.pill(context, "Best: ${maxOf(1_500, viewModel.runnerBestDistance)} m     ❤️".plus("❤️".repeat(viewModel.hearts)), 0xFFFFE8B1.toInt()))
    page.addView(UiKit.text(context, "Find the English word for", 15, UiKit.MUTED, gravity = Gravity.CENTER))
    page.addView(UiKit.text(context, "🍎  QUẢ TÁO", 27, UiKit.INK, true, Gravity.CENTER))
    page.addView(UiKit.row(context, 6).apply {
        listOf("🍌 BANANA", "🍎 APPLE", "🍊 ORANGE").forEachIndexed { lane, label ->
            addView(UiKit.card(context, if (lane == viewModel.runnerLane) 0xFFFFE9AE.toInt() else 0xFFE7F4FE.toInt(), 8).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, "🚧", 26, gravity = Gravity.CENTER))
                addView(UiKit.text(context, label, 11, UiKit.INK, true, Gravity.CENTER))
            }, LinearLayout.LayoutParams(0, UiKit.dp(context, 90), 1f))
        }
    })
    page.addView(UiKit.column(context, 0).apply {
        gravity = Gravity.CENTER
        setPadding(0, UiKit.dp(context, 10), 0, UiKit.dp(context, 10))
        UiKit.background(this, 0xFFB8DCB0.toInt(), 26)
        addView(UiKit.text(context, "🌲      🌿       🌳       🌿      🌲", 25, 0xFF438B50.toInt(), gravity = Gravity.CENTER))
        addView(UiKit.text(context, "━━━━━━ 🛣️ ━━━━━━", 25, UiKit.WHITE, true, Gravity.CENTER))
        addView(UiKit.text(context, "           🏃🏻‍♂️", 43))
        addView(UiKit.text(context, "🌳      🍃       🌲       🍃      🌳", 25, 0xFF438B50.toInt(), gravity = Gravity.CENTER))
    })
    page.addView(UiKit.text(context, "Choose a lane", 14, UiKit.MUTED, gravity = Gravity.CENTER))
    page.addView(UiKit.row(context, 12).apply {
        addView(UiKit.button(context, "⬅ Left", UiKit.BLUE, UiKit.WHITE) {
            viewModel.moveRunner(0)
            navigate("runner")
        }, LinearLayout.LayoutParams(0, UiKit.dp(context, 60), 1f))
        addView(UiKit.button(context, "⬆ Middle", UiKit.GREEN, UiKit.WHITE) {
            viewModel.moveRunner(1)
            if (viewModel.runnerDistance >= 150) viewModel.completeGame("Vocabulary Run")
            navigate("runner")
        }, LinearLayout.LayoutParams(0, UiKit.dp(context, 60), 1f))
        addView(UiKit.button(context, "Right ➡", UiKit.ORANGE, UiKit.WHITE) {
            viewModel.moveRunner(2)
            navigate("runner")
        }, LinearLayout.LayoutParams(0, UiKit.dp(context, 60), 1f))
    })
    if (viewModel.feedback.isNotBlank()) page.addView(UiKit.text(context, viewModel.feedback, 14, UiKit.GREEN, true, Gravity.CENTER))
    return UiKit.scrollContent(context, page)
}

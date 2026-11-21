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
    page.addView(UiKit.text(
        context,
        " ${viewModel.currentWord.vietnamese.uppercase()}",
        27,
        UiKit.INK,
        true,
        Gravity.CENTER
    ))
    val laneLabels = viewModel.runnerOptions.map(String::uppercase)
    page.addView(UiKit.row(context, 6).apply {
        laneLabels.forEachIndexed { lane, label ->
            val selected = lane == viewModel.runnerLane && viewModel.runnerRoundResolved
            addView(UiKit.card(context, if (selected) {
                if (viewModel.runnerLastAnswerCorrect == true) 0xFFD8F6E5.toInt() else 0xFFFFDCD6.toInt()
            } else 0xFFE7F4FE.toInt(), 8).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, if (selected) {
                    if (viewModel.runnerLastAnswerCorrect == true) "✅" else "❌"
                } else "🚧", 26, gravity = Gravity.CENTER))
                addView(UiKit.text(context, label, 11, UiKit.INK, true, Gravity.CENTER))
            }, LinearLayout.LayoutParams(0, UiKit.dp(context, 90), 1f))
        }
    })
    page.addView(UiKit.text(context, if (viewModel.runnerRoundResolved) {
        if (viewModel.isRunnerGameOver) {
            if (viewModel.hearts == 0) "Run over — you ran out of hearts." else "Finish line reached! Great run!"
        } else "Round paused — check your answer, then continue."
    } else "Choose the lane with the correct English word.", 12, UiKit.MUTED, gravity = Gravity.CENTER))
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
        val enabled = !viewModel.runnerRoundResolved && !viewModel.isRunnerGameOver
        val leftButton = UiKit.button(context, "⬅ Left", UiKit.BLUE, UiKit.WHITE) {
            viewModel.moveRunner(0)
            navigate("runner")
        }.apply { isEnabled = enabled }
        val middleButton = UiKit.button(context, "⬆ Middle", UiKit.GREEN, UiKit.WHITE) {
            viewModel.moveRunner(1)
            navigate("runner")
        }.apply { isEnabled = enabled }
        val rightButton = UiKit.button(context, "Right ➡", UiKit.ORANGE, UiKit.WHITE) {
            viewModel.moveRunner(2)
            navigate("runner")
        }.apply { isEnabled = enabled }
        addView(leftButton, LinearLayout.LayoutParams(0, UiKit.dp(context, 60), 1f))
        addView(middleButton, LinearLayout.LayoutParams(0, UiKit.dp(context, 60), 1f))
        addView(rightButton, LinearLayout.LayoutParams(0, UiKit.dp(context, 60), 1f))
    })
    if (viewModel.feedback.isNotBlank()) {
        page.addView(UiKit.card(context, if (viewModel.runnerLastAnswerCorrect == false) 0xFFFFF0ED.toInt() else 0xFFEAF9F0.toInt(), 12).apply {
            addView(UiKit.text(context, viewModel.feedback, 14, if (viewModel.runnerLastAnswerCorrect == false) UiKit.ORANGE else UiKit.GREEN, true, Gravity.CENTER))
        })
    }
    if (viewModel.runnerRoundResolved) {
        page.addView(UiKit.button(
            context,
            if (viewModel.isRunnerGameOver) "🔁 Play again" else "Continue ➜",
            if (viewModel.isRunnerGameOver) UiKit.PURPLE else UiKit.GREEN,
            UiKit.WHITE
        ) {
            if (viewModel.isRunnerGameOver) {
                viewModel.resetRunnerGame()
            } else {
                viewModel.continueRunner()
            }
            navigate("runner")
        })
    }
    return UiKit.scrollContent(context, page)
}

package com.example.englishapp.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

private fun RunnerBackView(context: Context, collision: Boolean): View =
    object : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(
                UiKit.dp(context, 72),
                UiKit.dp(context, 78)
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val density = context.resources.displayMetrics.density
            paint.style = Paint.Style.FILL

            paint.color = if (collision) 0xFFE87979.toInt() else 0xFFFFC875.toInt()
            canvas.drawRoundRect(
                RectF(24 * density, 30 * density, 48 * density, 58 * density),
                8 * density,
                8 * density,
                paint
            )

            paint.color = 0xFFF0B57A.toInt()
            canvas.drawCircle(36 * density, 18 * density, 10 * density, paint)
            paint.color = 0xFF5C3825.toInt()
            canvas.drawArc(
                RectF(26 * density, 7 * density, 46 * density, 27 * density),
                180f,
                180f,
                true,
                paint
            )

            paint.color = 0xFF44A6F4.toInt()
            canvas.drawRoundRect(
                RectF(18 * density, 35 * density, 54 * density, 57 * density),
                7 * density,
                7 * density,
                paint
            )
            paint.color = 0xFF26405C.toInt()
            canvas.drawRoundRect(
                RectF(21 * density, 29 * density, 51 * density, 50 * density),
                5 * density,
                5 * density,
                paint
            )

            paint.strokeWidth = 5 * density
            paint.style = Paint.Style.STROKE
            paint.color = 0xFF26405C.toInt()
            canvas.drawLine(29 * density, 56 * density, 24 * density, 72 * density, paint)
            canvas.drawLine(43 * density, 56 * density, 48 * density, 72 * density, paint)
            paint.strokeWidth = 4 * density
            canvas.drawLine(21 * density, 39 * density, 12 * density, 53 * density, paint)
            canvas.drawLine(51 * density, 39 * density, 60 * density, 53 * density, paint)
        }
    }.apply {
        layoutParams = LinearLayout.LayoutParams(UiKit.dp(context, 72), UiKit.dp(context, 78))
    }

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
    page.addView(UiKit.row(context, 8).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.pill(context, "${"❤️".repeat(viewModel.hearts)}", 0xFFFFE8B1.toInt()), LinearLayout.LayoutParams(-2, -2))
        addView(UiKit.card(context, 0xFFF7FBFF.toInt(), 8).apply {
            val progress = (viewModel.runnerDistance * 100 / 300).coerceIn(0, 100)
            val milestones = listOf(
                Triple(100, "⭐", "+5"),
                Triple(200, "💎", "+10"),
                Triple(300, "🏆", "+20")
            )
            addView(UiKit.text(context, "Hành trình $progress%", 11, UiKit.INK, true, Gravity.CENTER))
            addView(UiKit.row(context, 4).apply {
                gravity = Gravity.CENTER
                milestones.forEach { (distance, icon, reward) ->
                    val reached = distance <= viewModel.runnerDistance
                    addView(UiKit.pill(
                        context,
                        "$icon ${distance}m · $reward 🪙",
                        if (reached) 0xFFFFD65A.toInt() else 0xFFE3EDF6.toInt()
                    ), LinearLayout.LayoutParams(0, -2, 1f))
                }
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))
    })
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
    page.addView(UiKit.card(context, 0xFFB8DCB0.toInt(), 10).apply {
        addView(UiKit.text(context, "🏁  RUNNER TRACK  🏁", 13, 0xFF438B50.toInt(), true, Gravity.CENTER))
        addView(UiKit.row(context, 5).apply {
            for (lane in 0..2) {
                val isPlayerLane = lane == viewModel.runnerLane
                val collision = viewModel.runnerRoundResolved &&
                    viewModel.runnerLastAnswerCorrect == false && isPlayerLane
                addView(UiKit.column(context, 0).apply {
                    gravity = Gravity.CENTER
                    setPadding(UiKit.dp(context, 3), UiKit.dp(context, 7), UiKit.dp(context, 3), UiKit.dp(context, 7))
                    UiKit.background(this, if (isPlayerLane) 0xFFEAF7E7.toInt() else 0xFF8FBE91.toInt(), 14)
                    addView(UiKit.text(context, if (collision) "💥" else "🚧", 25, gravity = Gravity.CENTER))
                    addView(UiKit.text(context, if (collision) "🧱" else "│", 25, UiKit.WHITE, true, Gravity.CENTER))
                    if (isPlayerLane) {
                        addView(RunnerBackView(context, collision))
                    } else {
                        addView(UiKit.text(context, "  ", 38, gravity = Gravity.CENTER))
                    }
                    addView(UiKit.text(context, when (lane) {
                        0 -> "LEFT"
                        1 -> "MIDDLE"
                        else -> "RIGHT"
                    }, 10, UiKit.INK, true, Gravity.CENTER))
                }, LinearLayout.LayoutParams(0, UiKit.dp(context, 150), 1f))
            }
        })
    })
    page.addView(UiKit.text(context, "Choose a lane", 14, UiKit.MUTED, gravity = Gravity.CENTER))
    page.addView(UiKit.row(context, 12).apply {
        val enabled = !viewModel.runnerRoundResolved && !viewModel.isRunnerGameOver
        val leftButton = UiKit.card(context, UiKit.BLUE, 16).apply {
            gravity = Gravity.CENTER
            isClickable = enabled
            isFocusable = enabled
            alpha = if (enabled) 1f else 0.45f
            setPadding(UiKit.dp(context, 8), UiKit.dp(context, 10), UiKit.dp(context, 8), UiKit.dp(context, 10))
            addView(UiKit.text(context, "⬅", 25, UiKit.WHITE, true, Gravity.CENTER))
            addView(UiKit.text(context, "LEFT", 12, UiKit.WHITE, true, Gravity.CENTER))
            setOnClickListener {
                if (enabled) {
                    viewModel.moveRunner(0)
                    navigate("runner")
                }
            }
        }
        val middleButton = UiKit.card(context, UiKit.GREEN, 16).apply {
            gravity = Gravity.CENTER
            isClickable = enabled
            isFocusable = enabled
            alpha = if (enabled) 1f else 0.45f
            setPadding(UiKit.dp(context, 8), UiKit.dp(context, 10), UiKit.dp(context, 8), UiKit.dp(context, 10))
            addView(UiKit.text(context, "⬆", 25, UiKit.WHITE, true, Gravity.CENTER))
            addView(UiKit.text(context, "MIDDLE", 12, UiKit.WHITE, true, Gravity.CENTER))
            setOnClickListener {
                if (enabled) {
                    viewModel.moveRunner(1)
                    navigate("runner")
                }
            }
        }
        val rightButton = UiKit.card(context, UiKit.ORANGE, 16).apply {
            gravity = Gravity.CENTER
            isClickable = enabled
            isFocusable = enabled
            alpha = if (enabled) 1f else 0.45f
            setPadding(UiKit.dp(context, 8), UiKit.dp(context, 10), UiKit.dp(context, 8), UiKit.dp(context, 10))
            addView(UiKit.text(context, "➡", 25, UiKit.WHITE, true, Gravity.CENTER))
            addView(UiKit.text(context, "RIGHT", 12, UiKit.WHITE, true, Gravity.CENTER))
            setOnClickListener {
                if (enabled) {
                    viewModel.moveRunner(2)
                    navigate("runner")
                }
            }
        }
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

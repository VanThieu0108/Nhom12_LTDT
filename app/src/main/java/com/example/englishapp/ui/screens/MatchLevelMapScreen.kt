package com.example.englishapp.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun MatchLevelMapScreen(
    context: Context,
    viewModel: MainViewModel,
    navigate: (String) -> Unit
): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 12), UiKit.dp(context, 16), UiKit.dp(context, 20))
    }
    page.addView(UiKit.button(context, "‹  Arcade", UiKit.WHITE, UiKit.PURPLE) {
        navigate("arcade")
    }, LinearLayout.LayoutParams(-2, -2))
    page.addView(UiKit.text(context, "Word Match Trail", 25, UiKit.INK, true))
    page.addView(UiKit.text(
        context,
        "Finish each level to unlock the next one.",
        14,
        UiKit.MUTED
    ))
    page.addView(UiKit.row(context, 8).apply {
        addView(UiKit.pill(context, "✅ ${viewModel.completedMatchLevels.size} completed", 0xFFE1F5E9.toInt(), UiKit.GREEN))
        addView(UiKit.pill(context, "🔓 Level ${viewModel.unlockedMatchLevel} unlocked", 0xFFFFF1CE.toInt(), 0xFF9A6900.toInt()))
    })

    val rowHeight = UiKit.dp(context, LEVEL_ROW_HEIGHT_DP)
    val trailHeight = rowHeight * MainViewModel.MATCH_LEVEL_COUNT
    val trail = FrameLayout(context)
    trail.addView(
        MatchLevelPathView(context, viewModel.completedMatchLevels.maxOrNull() ?: 0),
        FrameLayout.LayoutParams(-1, trailHeight)
    )
    val levelRows = UiKit.column(context)
    repeat(MainViewModel.MATCH_LEVEL_COUNT) { index ->
        val level = index + 1
        val lane = levelLane(index)
        val row = UiKit.row(context).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        repeat(3) { column ->
            val cell = FrameLayout(context)
            if (column == lane) {
                val complete = viewModel.isMatchLevelCompleted(level)
                val unlocked = viewModel.isMatchLevelUnlocked(level)
                val label = when {
                    complete -> "✓\n$level"
                    unlocked -> "★\n$level"
                    else -> "🔒\n$level"
                }
                val node = UiKit.button(
                    context,
                    label,
                    when {
                        complete -> 0xFF38B574.toInt()
                        unlocked -> UiKit.GOLD
                        else -> 0xFFE2E9EF.toInt()
                    },
                    if (unlocked) UiKit.WHITE else UiKit.MUTED
                ) {
                    if (viewModel.selectMatchLevel(context, level)) navigate("match")
                }.apply {
                    textSize = 14f
                    setPadding(UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4), UiKit.dp(context, 4))
                    isEnabled = unlocked
                    alpha = if (unlocked) 1f else 0.8f
                    UiKit.background(this, when {
                        complete -> 0xFF38B574.toInt()
                        unlocked -> UiKit.GOLD
                        else -> 0xFFE2E9EF.toInt()
                    }, 36)
                }
                cell.addView(node, FrameLayout.LayoutParams(
                    UiKit.dp(context, LEVEL_NODE_SIZE_DP),
                    UiKit.dp(context, LEVEL_NODE_SIZE_DP),
                    Gravity.CENTER
                ))
            }
            row.addView(cell, LinearLayout.LayoutParams(0, -1, 1f))
        }
        levelRows.addView(row, LinearLayout.LayoutParams(-1, rowHeight))
    }
    trail.addView(levelRows, FrameLayout.LayoutParams(-1, trailHeight))
    page.addView(trail, LinearLayout.LayoutParams(-1, trailHeight))
    return UiKit.scrollContent(context, page)
}

private fun levelLane(index: Int): Int = when (index % 4) {
    0 -> 1
    1 -> 0
    2 -> 1
    else -> 2
}

private class MatchLevelPathView(
    context: Context,
    private val completedLevel: Int
) : View(context) {
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = UiKit.dp(context, 5).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val step = UiKit.dp(context, LEVEL_ROW_HEIGHT_DP).toFloat()
        val xPositions = (0 until MainViewModel.MATCH_LEVEL_COUNT)
            .map { (it * step + step / 2f) to laneX(levelLane(it)) }
        for (index in 0 until xPositions.lastIndex) {
            val start = xPositions[index]
            val end = xPositions[index + 1]
            val path = Path().apply {
                moveTo(width * start.second, start.first)
                cubicTo(
                    width * start.second, (start.first + end.first) / 2f,
                    width * end.second, (start.first + end.first) / 2f,
                    width * end.second, end.first
                )
            }
            pathPaint.color = if (index + 1 <= completedLevel) 0xFF38B574.toInt() else 0xFFD8E1E9.toInt()
            canvas.drawPath(path, pathPaint)
        }
    }

    private fun laneX(lane: Int): Float = (lane + 0.5f) / 3f
}

private const val LEVEL_ROW_HEIGHT_DP = 76
private const val LEVEL_NODE_SIZE_DP = 62

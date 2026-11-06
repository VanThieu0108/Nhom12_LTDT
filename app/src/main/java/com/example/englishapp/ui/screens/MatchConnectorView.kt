package com.example.englishapp.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

class MatchConnectorView(
    context: Context,
    private val rightOrder: List<Int>,
    private val matchedIndices: Set<Int>,
    private val selectedKey: String?,
    private val wrongKeys: Set<String>
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = UiKit.dp(context, 4).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rightOrder.size != MainViewModel.MATCH_PAIR_COUNT) return

        matchedIndices.forEach { pairIndex ->
            drawConnection(
                canvas,
                pairIndex,
                rightOrder.indexOf(pairIndex),
                MATCHED_LINE,
                UiKit.dp(context, 4).toFloat()
            )
        }

        val selected = selectedKey?.let(::parseKey)
        if (selected != null) {
            val rightRow = rightOrder.indexOf(selected.first)
            if (rightRow >= 0) {
                drawConnection(
                    canvas,
                    if (selected.second) selected.first else selected.first,
                    rightRow,
                    SELECTED_LINE,
                    UiKit.dp(context, 3).toFloat()
                )
            }
        }

        val wrongCards = wrongKeys.mapNotNull(::parseKey)
        if (wrongCards.size == 2) {
            val englishIndex = wrongCards.firstOrNull { it.second }?.first
            val vietnameseIndex = wrongCards.firstOrNull { !it.second }?.first
            if (englishIndex != null && vietnameseIndex != null) {
                drawConnection(
                    canvas,
                    englishIndex,
                    rightOrder.indexOf(vietnameseIndex),
                    WRONG_LINE,
                    UiKit.dp(context, 4).toFloat()
                )
            }
        }
    }

    private fun drawConnection(canvas: Canvas, leftRow: Int, rightRow: Int, color: Int, width: Float) {
        if (leftRow !in 0 until MainViewModel.MATCH_PAIR_COUNT ||
            rightRow !in 0 until MainViewModel.MATCH_PAIR_COUNT
        ) return
        val rowHeight = UiKit.dp(context, ROW_HEIGHT_DP).toFloat()
        val rowGap = UiKit.dp(context, ROW_GAP_DP).toFloat()
        val centerGap = UiKit.dp(context, CENTER_GAP_DP).toFloat()
        val leftX = this.width / 2f - centerGap / 2f
        val rightX = this.width / 2f + centerGap / 2f
        val leftY = leftRow * (rowHeight + rowGap) + rowHeight / 2f
        val rightY = rightRow * (rowHeight + rowGap) + rowHeight / 2f
        val bend = UiKit.dp(context, 20).toFloat()
        val path = Path().apply {
            moveTo(leftX, leftY)
            cubicTo(leftX + bend, leftY, rightX - bend, rightY, rightX, rightY)
        }
        paint.color = color
        paint.strokeWidth = width
        canvas.drawPath(path, paint)
    }

    private fun parseKey(key: String): Pair<Int, Boolean>? {
        val parts = key.split(':', limit = 2)
        val index = parts.firstOrNull()?.toIntOrNull() ?: return null
        val side = parts.getOrNull(1) ?: return null
        if (index !in 0 until MainViewModel.MATCH_PAIR_COUNT || side !in setOf("en", "vi")) return null
        return index to (side == "en")
    }

    companion object {
        const val ROW_HEIGHT_DP = 54
        const val ROW_GAP_DP = 8
        const val CENTER_GAP_DP = 34
        private const val MATCHED_LINE = 0xFF31A66A.toInt()
        private const val SELECTED_LINE = 0xFFFFB52E.toInt()
        private const val WRONG_LINE = 0xFFE44855.toInt()
    }
}

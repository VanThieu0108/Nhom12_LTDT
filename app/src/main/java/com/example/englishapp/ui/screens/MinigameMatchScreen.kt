package com.example.englishapp.ui.screens

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun MinigameMatchScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    viewModel.ensureMatchStageLoaded()
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 12), UiKit.dp(context, 16), UiKit.dp(context, 20))
    }

    page.addView(UiKit.row(context, 10).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.button(context, "Quit", UiKit.WHITE, UiKit.MUTED) { navigate("arcade") })
        addView(
            UiKit.text(context, "⭐ ${viewModel.score}", 15, UiKit.GOLD, true, Gravity.END or Gravity.CENTER_VERTICAL),
            LinearLayout.LayoutParams(0, -2, 1f)
        )
    })

    page.addView(UiKit.row(context, 8).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.column(context, 2).apply {
            addView(UiKit.text(context, "Word Match", 23, UiKit.INK, true))
            addView(UiKit.text(context, "Connect all 8 English–Vietnamese pairs", 12, UiKit.MUTED))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.pill(
            context,
            "LEVEL ${viewModel.matchGameLevel} / ${MainViewModel.MATCH_LEVEL_COUNT}",
            0xFFEDE7FF.toInt(),
            UiKit.PURPLE
        ))
    })

    page.addView(LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        UiKit.background(this, 0xFFE1EAF1.toInt(), 10)
        addView(View(context).apply {
            UiKit.background(
                this,
                UiKit.GREEN,
                10
            )
            layoutParams = LinearLayout.LayoutParams(
                0,
                UiKit.dp(context, 8),
                viewModel.matchGameLevel / MainViewModel.MATCH_LEVEL_COUNT.toFloat()
            )
        })
    }, LinearLayout.LayoutParams(-1, UiKit.dp(context, 8)))

    page.addView(UiKit.text(
        context,
        "${viewModel.matchedWords.size} / ${MainViewModel.MATCH_PAIR_COUNT} pairs matched",
        13,
        UiKit.MUTED,
        true,
        Gravity.CENTER
    ))

    when {
        viewModel.isMatchLoading -> {
            page.addView(UiKit.card(context, UiKit.WHITE, 24).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, "✨", 38, gravity = Gravity.CENTER))
                addView(UiKit.text(context, "Loading 8 word pairs...", 16, UiKit.MUTED, true, Gravity.CENTER))
            }, LinearLayout.LayoutParams(-1, UiKit.dp(context, 230)))
        }

        viewModel.matchPairs.size == MainViewModel.MATCH_PAIR_COUNT -> {
            page.addView(UiKit.row(context, 6).apply {
                addView(UiKit.pill(context, "🇬🇧 ENGLISH", 0xFFDDF2FF.toInt(), UiKit.BLUE), LinearLayout.LayoutParams(0, -2, 1f))
                addView(View(context), LinearLayout.LayoutParams(UiKit.dp(context, MatchConnectorView.CENTER_GAP_DP), 1))
                addView(UiKit.pill(context, "🇻🇳 MEANING", 0xFFFFEBD8.toInt(), UiKit.ORANGE), LinearLayout.LayoutParams(0, -2, 1f))
            })

            val boardHeight = UiKit.dp(
                context,
                MainViewModel.MATCH_PAIR_COUNT *
                    (MatchConnectorView.ROW_HEIGHT_DP + MatchConnectorView.ROW_GAP_DP)
            )
            val board = FrameLayout(context)
            board.addView(
                MatchConnectorView(
                    context = context,
                    rightOrder = viewModel.matchRightOrder,
                    matchedIndices = viewModel.matchMatchedIndices(),
                    selectedKey = viewModel.matchSelection,
                    wrongKeys = viewModel.matchWrongSelections
                ),
                FrameLayout.LayoutParams(-1, -1)
            )
            val columns = UiKit.row(context, MatchConnectorView.CENTER_GAP_DP).apply {
                gravity = Gravity.TOP
                val left = UiKit.column(context, MatchConnectorView.ROW_GAP_DP)
                val right = UiKit.column(context, MatchConnectorView.ROW_GAP_DP)

                viewModel.matchPairs.forEachIndexed { index, pair ->
                    val englishKey = viewModel.matchCardKeyForUi(index, true)
                    val isMatched = pair.english in viewModel.matchedWords
                    val isSelected = viewModel.matchSelection == englishKey
                    val isWrong = englishKey in viewModel.matchWrongSelections
                    left.addView(
                        matchCard(
                            context = context,
                            label = pair.english,
                            baseColor = 0xFFDDF2FF.toInt(),
                            selected = isSelected,
                            matched = isMatched,
                            wrong = isWrong,
                            onClick = {
                                viewModel.selectMatchCard(index, true)
                                navigate("match")
                            }
                        ),
                        LinearLayout.LayoutParams(-1, UiKit.dp(context, MatchConnectorView.ROW_HEIGHT_DP))
                    )
                }

                viewModel.matchRightOrder.forEach { pairIndex ->
                    val pair = viewModel.matchPairs[pairIndex]
                    val vietnameseKey = viewModel.matchCardKeyForUi(pairIndex, false)
                    val isMatched = pair.english in viewModel.matchedWords
                    val isSelected = viewModel.matchSelection == vietnameseKey
                    val isWrong = vietnameseKey in viewModel.matchWrongSelections
                    right.addView(
                        matchCard(
                            context = context,
                            label = pair.vietnamese,
                            baseColor = 0xFFFFEBD8.toInt(),
                            selected = isSelected,
                            matched = isMatched,
                            wrong = isWrong,
                            onClick = {
                                viewModel.selectMatchCard(pairIndex, false)
                                navigate("match")
                            }
                        ),
                        LinearLayout.LayoutParams(-1, UiKit.dp(context, MatchConnectorView.ROW_HEIGHT_DP))
                    )
                }

                addView(left, LinearLayout.LayoutParams(0, -2, 1f))
                addView(right, LinearLayout.LayoutParams(0, -2, 1f))
            }
            board.addView(
                columns,
                FrameLayout.LayoutParams(-1, -1)
            )
            page.addView(board, LinearLayout.LayoutParams(-1, boardHeight))
        }
    }

    if (viewModel.feedback.isNotBlank()) {
        page.addView(UiKit.text(
            context,
            viewModel.feedback,
            14,
            if (viewModel.matchWrongSelections.isNotEmpty()) 0xFFE44855.toInt() else UiKit.GREEN,
            true,
            Gravity.CENTER
        ))
    }
    page.addView(UiKit.text(context, viewModel.vocabularyMessage, 11, UiKit.MUTED, gravity = Gravity.CENTER))

    if (viewModel.matchStagePassed) {
        page.addView(UiKit.card(context, 0xFFE8F7EF.toInt(), 14).apply {
            gravity = Gravity.CENTER
            addView(UiKit.text(context, "🎉", 34, gravity = Gravity.CENTER))
            addView(UiKit.text(context, "Amazing! All 8 pairs matched!", 17, UiKit.GREEN, true, Gravity.CENTER))
            addView(UiKit.text(context, "You earned 80 stars!", 13, UiKit.MUTED, gravity = Gravity.CENTER))
        })
        if (viewModel.matchGameLevel < MainViewModel.MATCH_LEVEL_COUNT) {
            page.addView(UiKit.button(context, "Next Level  →", UiKit.GOLD) {
                viewModel.advanceMatchLevel(context)
                navigate("match")
            })
        } else {
            page.addView(UiKit.button(context, "🎊  All 50 levels complete!", UiKit.GOLD) {
                viewModel.replayMatchStage()
                navigate("match")
            })
        }
        page.addView(UiKit.button(context, "Replay this level", UiKit.WHITE, UiKit.PURPLE) {
            viewModel.replayMatchStage()
            navigate("match")
        })
    }
    return UiKit.scrollContent(context, page)
}

private fun matchCard(
    context: Context,
    label: String,
    baseColor: Int,
    selected: Boolean,
    matched: Boolean,
    wrong: Boolean,
    onClick: () -> Unit
): View = UiKit.button(
    context,
    label,
    when {
        wrong -> 0xFFE44855.toInt()
        matched -> 0xFFC9F2DC.toInt()
        selected -> 0xFFFFE7A8.toInt()
        else -> baseColor
    },
    if (wrong) Color.WHITE else UiKit.INK,
    onClick
).apply {
    textSize = 13f
    setPadding(UiKit.dp(context, 5), UiKit.dp(context, 4), UiKit.dp(context, 5), UiKit.dp(context, 4))
    isEnabled = !matched
    alpha = if (matched) 1f else 0.9f
    if (wrong) {
        ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0.35f, 1f).apply {
            duration = 180L
            repeatCount = 2
            start()
        }
    }
}

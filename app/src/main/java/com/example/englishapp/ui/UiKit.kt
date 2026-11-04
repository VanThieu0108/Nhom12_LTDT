package com.example.englishapp.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

object UiKit {
    const val SKY = 0xFFF0F9FF.toInt()
    const val INK = 0xFF26405C.toInt()
    const val MUTED = 0xFF7089A1.toInt()
    const val BLUE = 0xFF44A6F4.toInt()
    const val GREEN = 0xFF43BE85.toInt()
    const val GOLD = 0xFFFFBE39.toInt()
    const val PURPLE = 0xFF9774E5.toInt()
    const val ORANGE = 0xFFFF924D.toInt()
    const val WHITE = Color.WHITE

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun column(context: Context, gap: Int = 0): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        if (gap > 0) {
            dividerDrawable = GradientDrawable().apply { setSize(1, dp(context, gap)); setColor(Color.TRANSPARENT) }
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        }
    }

    fun row(context: Context, gap: Int = 0): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        if (gap > 0) {
            dividerDrawable = GradientDrawable().apply { setSize(dp(context, gap), 1); setColor(Color.TRANSPARENT) }
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        }
    }

    fun text(
        context: Context,
        value: String,
        size: Int = 16,
        color: Int = INK,
        bold: Boolean = false,
        gravity: Int = Gravity.START or Gravity.CENTER_VERTICAL
    ): TextView = TextView(context).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        this.gravity = gravity
        typeface = Typeface.create("sans-serif-rounded", if (bold) Typeface.BOLD else Typeface.NORMAL)
        includeFontPadding = true
    }

    fun button(
        context: Context,
        title: String,
        color: Int = WHITE,
        textColor: Int = INK,
        onClick: () -> Unit
    ): TextView = text(context, title, 15, textColor, true, Gravity.CENTER).apply {
        background(this, color, 18)
        elevation = dp(context, 3).toFloat()
        setPadding(dp(context, 10), dp(context, 11), dp(context, 10), dp(context, 11))
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
    }

    fun pill(context: Context, title: String, color: Int, textColor: Int = INK): TextView =
        text(context, title, 12, textColor, true, Gravity.CENTER).apply {
            background(this, color, 30)
            setPadding(dp(context, 11), dp(context, 7), dp(context, 11), dp(context, 7))
        }

    fun card(context: Context, color: Int = WHITE, padding: Int = 14): LinearLayout =
        column(context, 6).apply {
            setPadding(dp(context, padding), dp(context, padding), dp(context, padding), dp(context, padding))
            background(this, color, 22)
            elevation = dp(context, 3).toFloat()
        }

    fun spacer(context: Context, height: Int): View =
        View(context).apply { layoutParams = LinearLayout.LayoutParams(1, dp(context, height)) }

    fun input(context: Context, hint: String, password: Boolean = false): EditText =
        EditText(context).apply {
            this.hint = hint
            textSize = 15f
            setTextColor(INK)
            setHintTextColor(MUTED)
            typeface = Typeface.create("sans-serif-rounded", Typeface.NORMAL)
            inputType = if (password) {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT
            }
            setSingleLine(true)
            setPadding(dp(context, 14), dp(context, 9), dp(context, 14), dp(context, 9))
            background(this, 0xFFF3F8FC.toInt(), 16)
            layoutParams = LinearLayout.LayoutParams(-1, dp(context, 52))
        }

    fun background(view: View, color: Int, radius: Int) {
        view.background = GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(view.context, radius).toFloat()
        }
    }

    fun gradient(view: View, colors: IntArray, radius: Int) {
        view.background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors).apply {
            cornerRadius = dp(view.context, radius).toFloat()
        }
    }

    fun scrollContent(context: Context, content: LinearLayout): ScrollView =
        ScrollView(context).apply {
            clipToPadding = false
            isFillViewport = true
            setBackgroundColor(SKY)
            setPadding(0, 0, 0, 0)
            addView(content)
        }
}

package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit

fun LoginScreen(
    context: Context,
    login: (String, String) -> String?,
    navigate: (String) -> Unit
): View {
    val page = UiKit.column(context, 13).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(UiKit.dp(context, 22), UiKit.dp(context, 28), UiKit.dp(context, 22), UiKit.dp(context, 20))
    }
    page.addView(UiKit.text(context, "☁️     ✨     🔤", 27, UiKit.BLUE, gravity = Gravity.CENTER))
    val name = UiKit.input(context, "👤  Explorer Name")
    val pin = UiKit.input(context, "🔒  Secret PIN", true)
    page.addView(UiKit.column(context, 10).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(UiKit.dp(context, 20), UiKit.dp(context, 22), UiKit.dp(context, 20), UiKit.dp(context, 20))
        UiKit.background(this, 0xEEFFFFFF.toInt(), 28)
        elevation = UiKit.dp(context, 6).toFloat()
        addView(UiKit.text(context, "🦉", 54, gravity = Gravity.CENTER))
        addView(UiKit.text(context, "Kids English", 27, UiKit.INK, true, Gravity.CENTER))
        addView(UiKit.text(context, "Adventure", 27, UiKit.PURPLE, true, Gravity.CENTER))
        addView(UiKit.text(context, "Your next adventure awaits!", 14, UiKit.MUTED, gravity = Gravity.CENTER))
        addView(UiKit.spacer(context, 6))
        addView(name)
        addView(pin)
        val error = UiKit.text(context, "", 12, 0xFFCE4848.toInt())
        addView(error)
        addView(UiKit.button(context, "Start Adventure!  🚀", UiKit.GOLD) {
            val result = login(name.text.toString(), pin.text.toString())
            error.text = result.orEmpty()
            if (result == null) navigate("home")
        })
        addView(UiKit.button(context, "Explore as Guest", 0xFFE7F4FE.toInt(), UiKit.BLUE) {
            navigate("home")
        })
        addView(UiKit.text(context, "New here? Create an explorer account!", 14, UiKit.PURPLE, true, Gravity.CENTER).apply {
            isClickable = true
            setOnClickListener { navigate("register") }
        })
    })
    page.addView(UiKit.text(context, "🌈  Learn a little. Grow a lot.  🌈", 14, UiKit.MUTED, gravity = Gravity.CENTER))
    return UiKit.scrollContent(context, page)
}

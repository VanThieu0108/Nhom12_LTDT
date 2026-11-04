package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import com.example.englishapp.ui.UiKit

fun RegisterScreen(
    context: Context,
    register: (String, String, String) -> String?,
    navigate: (String) -> Unit
): View {
    val page = UiKit.column(context, 13).apply {
        setPadding(UiKit.dp(context, 22), UiKit.dp(context, 28), UiKit.dp(context, 22), UiKit.dp(context, 20))
    }
    page.addView(UiKit.text(context, "✨  Start your adventure!  ✨", 23, UiKit.INK, true, Gravity.CENTER))
    val name = UiKit.input(context, "👤  Explorer Name")
    val pin = UiKit.input(context, "🔒  Secret PIN (4+ digits)", true)
    val confirm = UiKit.input(context, "🔒  Confirm PIN", true)
    page.addView(UiKit.card(context, UiKit.WHITE, 20).apply {
        addView(UiKit.text(context, "🦉", 48, gravity = Gravity.CENTER))
        addView(UiKit.text(context, "Create Explorer Account", 22, UiKit.INK, true, Gravity.CENTER))
        addView(UiKit.text(context, "Pick a name and a secret PIN.", 14, UiKit.MUTED, gravity = Gravity.CENTER))
        addView(name)
        addView(pin)
        addView(confirm)
        val error = UiKit.text(context, "", 12, 0xFFCE4848.toInt())
        addView(error)
        addView(UiKit.button(context, "Create Account  ✨", UiKit.GOLD) {
            val result = register(name.text.toString(), pin.text.toString(), confirm.text.toString())
            error.text = result.orEmpty()
            if (result == null) navigate("home")
        })
        addView(UiKit.text(context, "Already an explorer?  Log in", 14, UiKit.PURPLE, true, Gravity.CENTER).apply {
            isClickable = true
            setOnClickListener { navigate("login") }
        })
    })
    return UiKit.scrollContent(context, page)
}

package com.example.englishapp.ui.screens

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

fun PetCollectionScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {
    val page = UiKit.column(context, 12).apply {
        setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 18))
    }
    page.addView(UiKit.text(context, "My Pet Collection 🐾", 25, UiKit.INK, true))
    page.addView(UiKit.text(context, "Hatch eggs to collect them all!", 14, UiKit.MUTED))
    page.addView(UiKit.row(context, 8).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(UiKit.column(context, 3).apply {
            addView(UiKit.text(context, "🥚 Egg Incubator", 18, UiKit.INK, true))
            addView(UiKit.text(context, "A surprise is on its way!", 12, UiKit.MUTED))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.pill(context, "00:10", 0xFFFFE9AE.toInt()))
    })
    page.addView(UiKit.text(context, "✨   🥚   ✨", 42, gravity = Gravity.CENTER).apply {
        setPadding(0, UiKit.dp(context, 8), 0, UiKit.dp(context, 8))
        UiKit.background(this, 0xFFE0F4FF.toInt(), 25)
    })
    page.addView(UiKit.row(context, 8).apply {
        addView(UiKit.text(context, "Your Egg Backpack", 18, UiKit.INK, true), LinearLayout.LayoutParams(0, -2, 1f))
        addView(UiKit.pill(context, "2 eggs", 0xFFFFE9AE.toInt()))
    })
    page.addView(UiKit.row(context, 8).apply {
        listOf(Triple("🥚", "Common", UiKit.GREEN), Triple("🌟", "Rare", UiKit.GOLD), Triple("🥚", "Egg", UiKit.PURPLE)).forEach { egg ->
            addView(UiKit.card(context, UiKit.WHITE, 8).apply {
                gravity = Gravity.CENTER
                addView(UiKit.text(context, egg.first, 28, gravity = Gravity.CENTER))
                addView(UiKit.text(context, egg.second, 11, UiKit.INK, true, Gravity.CENTER))
                addView(UiKit.button(context, "Hatch", egg.third, UiKit.WHITE) {
                    viewModel.hatchEgg()
                    navigate("pets")
                })
            }, LinearLayout.LayoutParams(0, UiKit.dp(context, 127), 1f))
        }
    })
    page.addView(UiKit.text(context, "Your buddies", 18, UiKit.INK, true))
    viewModel.pets.chunked(3).forEach { pets ->
        page.addView(UiKit.row(context, 8).apply {
            pets.forEach { pet ->
                val color = when (pet.id) {
                    "bunny" -> 0xFFE7F5FF.toInt()
                    "fox" -> 0xFFFFE8D8.toInt()
                    else -> 0xFFEAE7F9.toInt()
                }
                addView(UiKit.column(context, 3).apply {
                    gravity = Gravity.CENTER
                    setPadding(UiKit.dp(context, 5), UiKit.dp(context, 9), UiKit.dp(context, 5), UiKit.dp(context, 7))
                    UiKit.background(this, if (pet.unlocked) color else 0xFFE4E8EC.toInt(), 18)
                    addView(UiKit.text(context, if (pet.unlocked) pet.emoji else "❔", 30, gravity = Gravity.CENTER))
                    addView(UiKit.text(context, if (pet.unlocked) pet.name else "Mystery", 11, UiKit.INK, true, Gravity.CENTER))
                    addView(UiKit.text(context, if (pet.unlocked) pet.rarity else "Locked", 9, UiKit.MUTED, gravity = Gravity.CENTER))
                }, LinearLayout.LayoutParams(0, UiKit.dp(context, 92), 1f))
            }
        })
    }
    return UiKit.scrollContent(context, page)
}

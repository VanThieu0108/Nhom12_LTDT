package com.example.englishapp.ui.screens

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.englishapp.ui.UiKit
import com.example.englishapp.viewmodel.MainViewModel

// ─── Models Dữ Liệu ──────────────────────────────────────────────────────────

private data class EggInfo(
    val typeId: String,          // 'common', 'rare', 'epic' (khớp database)
    val name: String,
    val price: Int,
    val primaryColor: Int,
    val gradientColors: IntArray,
    val rates: List<Pair<String, Int>>, // Độ hiếm -> %
    val hatchTimeStr: String,
    val description: String
)

private data class PetCardItem(
    val id: String,
    val name: String,
    val title: String,          // Danh hiệu sang trọng
    val emoji: String,
    val rarity: String,
    val rarityColor: Int,
    val cardBgColor: Int,
    var unlocked: Boolean = false,
    var duplicateCount: Int = 0 // Số lượng trùng lặp
)

// ─── Dữ Liệu Cố Định Trứng Shop ───────────────────────────────────────────────

private val SHOP_EGG_LIST = listOf(
    EggInfo(
        typeId = "common",
        name = "Trứng Thường",
        price = 50,
        primaryColor = 0xFF38B278.toInt(),
        gradientColors = intArrayOf(0xFF7BE495.toInt(), 0xFF38B278.toInt()),
        rates = listOf("Common" to 70, "Rare" to 25, "Epic" to 4, "Legendary" to 1),
        hatchTimeStr = "5 phút",
        description = "Trứng cỏ mùa xuân tươi tắn, thích hợp cho các nhà thám hiểm mới."
    ),
    EggInfo(
        typeId = "rare",
        name = "Trứng Hiếm",
        price = 150,
        primaryColor = 0xFFF5A623.toInt(),
        gradientColors = intArrayOf(0xFFFFE082.toInt(), 0xFFF5A623.toInt()),
        rates = listOf("Common" to 20, "Rare" to 55, "Epic" to 20, "Legendary" to 5),
        hatchTimeStr = "30 phút",
        description = "Trứng hoàng gia lấp lánh ánh kim, tỏa ra hào quang ấm áp."
    ),
    EggInfo(
        typeId = "epic",
        name = "Trứng Sử Thi",
        price = 400,
        primaryColor = 0xFF8E44AD.toInt(),
        gradientColors = intArrayOf(0xFFC39BD3.toInt(), 0xFF8E44AD.toInt()),
        rates = listOf("Common" to 5, "Rare" to 20, "Epic" to 55, "Legendary" to 20),
        hatchTimeStr = "2 giờ",
        description = "Trứng ma thuật vũ trụ huyền bí, mang linh hồn rồng thần & kỳ lân."
    )
)

// Toàn bộ pet chuẩn từ database backend (001_initial_schema.sql) với đồ họa sang trọng
private fun createPetCatalog(): MutableList<PetCardItem> = mutableListOf(
    PetCardItem(
        id = "bunny",
        name = "Sunny Bunny",
        title = "Thỏ Mặt Trời",
        emoji = "🐰",
        rarity = "Common",
        rarityColor = 0xFF38B278.toInt(),
        cardBgColor = 0xFFEDFBF3.toInt(),
        unlocked = true,
        duplicateCount = 2
    ),
    PetCardItem(
        id = "milo_fox",
        name = "Milo Fox",
        title = "Cáo Tinh Anh",
        emoji = "🦊",
        rarity = "Common",
        rarityColor = 0xFF38B278.toInt(),
        cardBgColor = 0xFFEDFBF3.toInt(),
        unlocked = true,
        duplicateCount = 1
    ),
    PetCardItem(
        id = "cloud_cat",
        name = "Cloud Cat",
        title = "Miêu Bạch Vân",
        emoji = "🐱",
        rarity = "Common",
        rarityColor = 0xFF38B278.toInt(),
        cardBgColor = 0xFFEDFBF3.toInt(),
        unlocked = false,
        duplicateCount = 0
    ),
    PetCardItem(
        id = "golden_fox",
        name = "Golden Fox",
        title = "Hồ Ly Kim Quang",
        emoji = "🦊",
        rarity = "Rare",
        rarityColor = 0xFFF5A623.toInt(),
        cardBgColor = 0xFFFFF9E6.toInt(),
        unlocked = true,
        duplicateCount = 1
    ),
    PetCardItem(
        id = "moon_panda",
        name = "Moon Panda",
        title = "Nguyệt Dạ Hùng",
        emoji = "🐼",
        rarity = "Rare",
        rarityColor = 0xFFF5A623.toInt(),
        cardBgColor = 0xFFFFF9E6.toInt(),
        unlocked = false,
        duplicateCount = 0
    ),
    PetCardItem(
        id = "star_dragon",
        name = "Star Dragon",
        title = "Long Thần Tinh Tú",
        emoji = "🐲",
        rarity = "Epic",
        rarityColor = 0xFF8E44AD.toInt(),
        cardBgColor = 0xFFF5EEF8.toInt(),
        unlocked = false,
        duplicateCount = 0
    ),
    PetCardItem(
        id = "rainbow_unicorn",
        name = "Rainbow Unicorn",
        title = "Kỳ Lân Cầu Vồng",
        emoji = "🦄",
        rarity = "Legendary",
        rarityColor = 0xFFE67E22.toInt(),
        cardBgColor = 0xFFFEF5E7.toInt(),
        unlocked = false,
        duplicateCount = 0
    )
)

// State toàn cục phiên làm việc (mô phỏng)
private object PetWorldState {
    var coins: Int = 350
    // Số lượng mỗi loại trứng trong kho (key: typeId)
    val eggInventoryCounts = mutableMapOf(
        "common" to 3,
        "rare" to 1,
        "epic" to 0
    )
    // Chỉ có DUY NHẤT 1 chỗ ấp trứng
    var activeEggTypeId: String? = "common" // null nếu lồng trống
    var activeEggTimeRemaining: String = "03:45"

    val catalog: MutableList<PetCardItem> = createPetCatalog()
}

// ─── Helper Vẽ Đồ Họa Trứng Nghệ Thuật (Egg Shape 3D Gradient) ────────────────

/**
 * Tạo một quả trứng hình Oval với dải màu gradient sắc sảo theo độ hiếm,
 * có bóng sáng nghệ thuật tạo cảm giác nổi khối 3D sang trọng.
 */
private fun createGraphicEggView(
    context: Context,
    gradientColors: IntArray,
    widthDp: Int = 46,
    heightDp: Int = 58,
    isGrayscale: Boolean = false
): View {
    val eggShape = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        if (isGrayscale) intArrayOf(0xFFD3DCE6.toInt(), 0xFF9FB1C3.toInt())
        else gradientColors
    ).apply {
        shape = GradientDrawable.OVAL
        if (isGrayscale) {
            setStroke(UiKit.dp(context, 2), 0xFFB9C8D6.toInt(), UiKit.dp(context, 4).toFloat(), UiKit.dp(context, 3).toFloat())
        } else {
            setStroke(UiKit.dp(context, 2), Color.WHITE)
        }
    }

    val container = FrameLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(UiKit.dp(context, widthDp), UiKit.dp(context, heightDp)).apply {
            gravity = Gravity.CENTER
        }
        background = eggShape
        elevation = if (isGrayscale) 0f else UiKit.dp(context, 3).toFloat()
    }

    // Đốm sáng phản chiếu ánh sáng 3D trên đỉnh quả trứng
    if (!isGrayscale) {
        val shineDot = View(context).apply {
            val shineShape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x88FFFFFF.toInt())
            }
            background = shineShape
            val dotW = UiKit.dp(context, (widthDp * 0.32f).toInt())
            val dotH = UiKit.dp(context, (heightDp * 0.22f).toInt())
            layoutParams = FrameLayout.LayoutParams(dotW, dotH).apply {
                gravity = Gravity.TOP or Gravity.START
                setMargins(UiKit.dp(context, (widthDp * 0.2f).toInt()), UiKit.dp(context, (heightDp * 0.15f).toInt()), 0, 0)
            }
        }
        container.addView(shineDot)
    }

    return container
}

// ─── Helper Bong Bóng Nút "!" Nhỏ Ở Góc Quả Trứng ────────────────────────────

private fun createBubbleInfoButton(context: Context, onClick: () -> Unit): View =
    TextView(context).apply {
        text = "!"
        textSize = 10f
        setTextColor(Color.WHITE)
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
        gravity = Gravity.CENTER
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xFF2F80ED.toInt()) // Xanh bóng biển rực rỡ
            setStroke(UiKit.dp(context, 1), Color.WHITE)
        }
        elevation = UiKit.dp(context, 4).toFloat()
        val size = UiKit.dp(context, 18)
        layoutParams = FrameLayout.LayoutParams(size, size).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 0, 0, 0)
        }
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
    }

// ─── Helper Badge Đếm Số Lượng Trứng Trong Kho (Góc Trên Phải) ────────────────

private fun createEggCountBadge(context: Context, count: Int): TextView =
    TextView(context).apply {
        text = if (count > 9) "9+" else count.toString()
        textSize = 10f
        setTextColor(Color.WHITE)
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
        gravity = Gravity.CENTER
        val badgeColor = if (count > 0) 0xFFFF5722.toInt() else 0xFF8FA3B8.toInt()
        background = GradientDrawable().apply {
            cornerRadius = UiKit.dp(context, 10).toFloat()
            setColor(badgeColor)
            setStroke(UiKit.dp(context, 1), Color.WHITE)
        }
        elevation = UiKit.dp(context, 3).toFloat()
        setPadding(UiKit.dp(context, 5), UiKit.dp(context, 1), UiKit.dp(context, 5), UiKit.dp(context, 1))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            UiKit.dp(context, 18)
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 0, 0, 0)
        }
    }

// ─── Helper Popup Note Chuẩn UiKit ───────────────────────────────────────────

private fun showHarmonyDialog(
    context: Context,
    titleEmoji: String,
    titleText: String,
    buildContent: LinearLayout.() -> Unit
) {
    val cardContent = UiKit.column(context, 8).apply {
        setPadding(UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 16))
        UiKit.background(this, UiKit.WHITE, 22)
        elevation = UiKit.dp(context, 8).toFloat()

        // Tiêu đề popup
        addView(UiKit.row(context, 8).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(UiKit.text(context, titleEmoji, 24))
            addView(UiKit.text(context, titleText, 17, UiKit.INK, true))
        })

        // Vạch phân cách
        addView(View(context).apply {
            setBackgroundColor(0x14000000)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UiKit.dp(context, 1)).apply {
                setMargins(0, UiKit.dp(context, 4), 0, UiKit.dp(context, 6))
            }
        })

        buildContent()
    }

    val dialogContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 18))
        addView(cardContent)
    }

    val dialog = AlertDialog.Builder(context).setView(dialogContainer).create()

    cardContent.addView(UiKit.spacer(context, 8))
    cardContent.addView(UiKit.button(context, "Đã hiểu ✨", UiKit.BLUE, UiKit.WHITE) {
        dialog.dismiss()
    }.apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UiKit.dp(context, 42))
        textSize = 13f
    })

    dialog.show()
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
}

// ─── Màn Hình Chính PetCollectionScreen ────────────────────────────────────────

fun PetCollectionScreen(context: Context, viewModel: MainViewModel, navigate: (String) -> Unit): View {

    if (viewModel.user.coins > 0 && PetWorldState.coins < viewModel.user.coins) {
        PetWorldState.coins = viewModel.user.coins
    }

    val rootScroll = UiKit.scrollContent(context, UiKit.column(context, 0))
    val mainPage = UiKit.column(context, 0).apply {
        setPadding(UiKit.dp(context, 14), UiKit.dp(context, 12), UiKit.dp(context, 14), UiKit.dp(context, 75))
    }

    // Hàm render động toàn bộ giao diện
    fun renderUi() {
        mainPage.removeAllViews()

        // ══════════════════════════════════════════════════════════════════════
        // 1. HEADER CHÍNH & VÍ XU
        // ══════════════════════════════════════════════════════════════════════
        val header = UiKit.row(context, 8).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(
                UiKit.column(context, 2).apply {
                    addView(UiKit.text(context, "🐣 Thế Giới Thú Cưng", 21, UiKit.INK, true))
                    addView(UiKit.text(context, "Ấp trứng thần thoại & sưu tầm linh thú", 12, UiKit.MUTED))
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )

            // Huy hiệu xu
            addView(UiKit.row(context, 4).apply {
                gravity = Gravity.CENTER_VERTICAL
                UiKit.background(this, 0xFFFFFBE8.toInt(), 20)
                elevation = UiKit.dp(context, 2).toFloat()
                setPadding(UiKit.dp(context, 10), UiKit.dp(context, 6), UiKit.dp(context, 10), UiKit.dp(context, 6))
                addView(UiKit.text(context, "🪙", 15))
                addView(UiKit.text(context, "${PetWorldState.coins}", 14, UiKit.INK, true))
            })
        }
        mainPage.addView(header)
        mainPage.addView(UiKit.spacer(context, 12))

        // ══════════════════════════════════════════════════════════════════════
        // 2. KHU VỰC CỬA HÀNG (SHOP)
        // Yêu cầu:
        // - 3 ô nằm trên 1 hàng ngang vừa vặn màn hình (không cần vuốt ngang)
        // - Nút "!" bong bóng nhỏ nằm trên góc bên phải của quả trứng
        // - Quả trứng được đổi màu theo từng độ hiếm (đồ họa gradient 3D)
        // ══════════════════════════════════════════════════════════════════════
        val shopSection = UiKit.column(context, 8).apply {

            // Tiêu đề shop + trang trí nhỏ
            addView(UiKit.row(context, 6).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(UiKit.text(context, "🏪 Cửa Hàng Trứng", 17, UiKit.INK, true),
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                )
                addView(UiKit.pill(context, "Độ hiếm tăng dần ✨", 0xFFEBF7FF.toInt(), UiKit.BLUE).apply {
                    textSize = 10f
                })
            })

            // Hàng ngang gồm đúng 3 ô vừa khít màn hình (weight = 1f cho mỗi ô)
            val shopCardsRow = UiKit.row(context, 8)

            SHOP_EGG_LIST.forEach { egg ->
                val card = UiKit.column(context, 6).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    UiKit.background(this, UiKit.WHITE, 16)
                    elevation = UiKit.dp(context, 2).toFloat()
                    setPadding(UiKit.dp(context, 6), UiKit.dp(context, 10), UiKit.dp(context, 6), UiKit.dp(context, 8))

                    // Khung chứa quả trứng đồ họa 3D + Bong bóng nút "!" ở góc trên phải
                    val eggFrame = FrameLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(UiKit.dp(context, 54), UiKit.dp(context, 64))

                        // Quả trứng đồ họa đổi màu theo độ hiếm
                        val eggGraphic = createGraphicEggView(context, egg.gradientColors, widthDp = 42, heightDp = 52)
                        addView(eggGraphic)

                        // Nút "!" nhỏ như 1 bong bóng nổi ở góc trên bên phải
                        val bubbleBtn = createBubbleInfoButton(context) {
                            showHarmonyDialog(context, "🥚", egg.name) {
                                addView(UiKit.text(context, egg.description, 13, UiKit.MUTED))
                                addView(UiKit.spacer(context, 6))
                                addView(UiKit.text(context, "📊 Tỷ lệ nở ra thú cưng:", 13, UiKit.INK, true))
                                addView(UiKit.spacer(context, 4))

                                egg.rates.forEach { (rarity, pct) ->
                                    val rColor = when (rarity) {
                                        "Common" -> 0xFF38B278.toInt()
                                        "Rare" -> 0xFFF5A623.toInt()
                                        "Epic" -> 0xFF8E44AD.toInt()
                                        else -> 0xFFE67E22.toInt()
                                    }
                                    addView(UiKit.row(context, 6).apply {
                                        gravity = Gravity.CENTER_VERTICAL
                                        addView(UiKit.pill(context, rarity, rColor, UiKit.WHITE).apply {
                                            layoutParams = LinearLayout.LayoutParams(UiKit.dp(context, 80), LinearLayout.LayoutParams.WRAP_CONTENT)
                                        })
                                        addView(UiKit.text(context, "$pct%", 13, UiKit.INK, true))
                                    })
                                    addView(UiKit.spacer(context, 3))
                                }

                                addView(UiKit.spacer(context, 4))
                                addView(UiKit.text(context, "⏱ Thời gian ấp nở: ${egg.hatchTimeStr}", 12, UiKit.MUTED))
                            }
                        }
                        addView(bubbleBtn)
                    }
                    addView(eggFrame)

                    // Tên trứng nhỏ gọn
                    addView(UiKit.text(context, egg.name, 12, UiKit.INK, true, Gravity.CENTER))

                    // Giá tiền
                    addView(UiKit.row(context, 2).apply {
                        gravity = Gravity.CENTER
                        addView(UiKit.text(context, "🪙", 11))
                        addView(UiKit.text(context, "${egg.price}", 12, UiKit.INK, true))
                    })

                    // Nút Mua gọn gàng
                    addView(UiKit.button(context, "Mua", egg.primaryColor, UiKit.WHITE) {
                        if (PetWorldState.coins >= egg.price) {
                            PetWorldState.coins -= egg.price
                            val currentCount = PetWorldState.eggInventoryCounts[egg.typeId] ?: 0
                            PetWorldState.eggInventoryCounts[egg.typeId] = currentCount + 1

                            showHarmonyDialog(context, "🎉", "Mua Thành Công!") {
                                addView(UiKit.text(context, "Đã thêm 1 ${egg.name} vào kho đồ của bạn!", 13, UiKit.INK))
                                addView(UiKit.text(context, "Bạn có thể vào khu vực Kho Đồ bên dưới để đưa vào lồng ấp.", 12, UiKit.MUTED))
                            }
                            renderUi()
                        } else {
                            showHarmonyDialog(context, "😅", "Chưa Đủ Xu!") {
                                addView(UiKit.text(context, "Bạn cần ${egg.price} 🪙 nhưng hiện tại chỉ có ${PetWorldState.coins} 🪙.", 13, UiKit.INK))
                                addView(UiKit.text(context, "Hãy luyện tập từ vựng hoặc chơi minigame để nhận thêm xu nhé!", 12, UiKit.MUTED))
                            }
                        }
                    }.apply {
                        textSize = 11f
                        setPadding(0, UiKit.dp(context, 5), 0, UiKit.dp(context, 5))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    })
                }

                // Mỗi card chiếm đúng 1/3 chiều ngang màn hình
                shopCardsRow.addView(card, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }
            addView(shopCardsRow)
        }
        mainPage.addView(shopSection)

        // Phân cách nhẹ
        mainPage.addView(UiKit.spacer(context, 14))
        mainPage.addView(View(context).apply {
            setBackgroundColor(0x1226405C)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UiKit.dp(context, 1))
        })
        mainPage.addView(UiKit.spacer(context, 14))

        // ══════════════════════════════════════════════════════════════════════
        // 3. KHU VỰC KHO ĐỒ (INVENTORY)
        // Yêu cầu:
        // - Bỏ hẳn combobox
        // - Hiện 3 loại trứng như shop và có SỐ LƯỢNG ở góc phải quả trứng (0 đến 9+)
        // - Bấm vào trứng có thể đem đi ấp
        // ══════════════════════════════════════════════════════════════════════
        val inventorySection = UiKit.column(context, 8).apply {

            val totalOwnedEggs = PetWorldState.eggInventoryCounts.values.sum()

            addView(UiKit.row(context, 6).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(UiKit.text(context, "🎒 Kho Đồ Trứng", 17, UiKit.INK, true),
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                )
                addView(UiKit.pill(context, "Tổng: $totalOwnedEggs quả", 0xFFFFF2D6.toInt(), 0xFF9C6500.toInt()).apply {
                    textSize = 10f
                })
            })

            // Hàng ngang gồm 3 loại trứng tương ứng như Shop
            val invCardsRow = UiKit.row(context, 8)

            SHOP_EGG_LIST.forEach { egg ->
                val count = PetWorldState.eggInventoryCounts[egg.typeId] ?: 0

                val invCard = UiKit.column(context, 6).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    val cardBg = if (count > 0) UiKit.WHITE else 0xFFF7FAFD.toInt()
                    UiKit.background(this, cardBg, 16)
                    elevation = UiKit.dp(context, 2).toFloat()
                    setPadding(UiKit.dp(context, 6), UiKit.dp(context, 10), UiKit.dp(context, 6), UiKit.dp(context, 8))

                    // Quả trứng có số lượng badge ở góc trên bên phải (0 đến 9+)
                    val eggFrame = FrameLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(UiKit.dp(context, 54), UiKit.dp(context, 64))

                        // Nếu count = 0 thì hiển thị trứng mờ/đen trắng nhẹ, nếu > 0 hiển thị đầy đủ màu sắc
                        val eggGraphic = createGraphicEggView(
                            context,
                            egg.gradientColors,
                            widthDp = 42,
                            heightDp = 52,
                            isGrayscale = (count == 0)
                        )
                        addView(eggGraphic)

                        // Huy hiệu số lượng ở góc trên bên phải (0 -> 9+)
                        val countBadge = createEggCountBadge(context, count)
                        addView(countBadge)
                    }
                    addView(eggFrame)

                    // Tên trứng
                    addView(UiKit.text(context, egg.name, 12, if (count > 0) UiKit.INK else UiKit.MUTED, true, Gravity.CENTER))

                    // Nút "Đem Ấp" hoặc "Hết trứng"
                    if (count > 0) {
                        addView(UiKit.button(context, "Đem Ấp", UiKit.BLUE, UiKit.WHITE) {
                            if (PetWorldState.activeEggTypeId != null) {
                                showHarmonyDialog(context, "⚠️", "Lồng Đang Bận!") {
                                    addView(UiKit.text(context, "Hiện tại chỗ ấp đang có 1 quả trứng đang ấp dở.", 13, UiKit.INK))
                                    addView(UiKit.text(context, "Hãy chờ hoặc ấp nở quả trứng hiện tại trước khi ấp quả mới nhé!", 12, UiKit.MUTED))
                                }
                            } else {
                                // Đưa trứng này vào lồng ấp
                                PetWorldState.eggInventoryCounts[egg.typeId] = count - 1
                                PetWorldState.activeEggTypeId = egg.typeId
                                PetWorldState.activeEggTimeRemaining = when (egg.typeId) {
                                    "common" -> "05:00"
                                    "rare" -> "30:00"
                                    else -> "02:00:00"
                                }
                                showHarmonyDialog(context, "🥚", "Đã Đặt Vào Chỗ Ấp!") {
                                    addView(UiKit.text(context, "Đã đưa 1 ${egg.name} vào lồng ấp duy nhất.", 13, UiKit.INK))
                                }
                                renderUi()
                            }
                        }.apply {
                            textSize = 10f
                            setPadding(0, UiKit.dp(context, 5), 0, UiKit.dp(context, 5))
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        })
                    } else {
                        // Nếu số lượng = 0
                        addView(UiKit.text(context, "Đã hết", 11, UiKit.MUTED, false, Gravity.CENTER).apply {
                            setPadding(0, UiKit.dp(context, 4), 0, UiKit.dp(context, 4))
                        })
                    }
                }

                invCardsRow.addView(invCard, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }

            addView(invCardsRow)
        }
        mainPage.addView(inventorySection)

        // Phân cách nhẹ
        mainPage.addView(UiKit.spacer(context, 14))
        mainPage.addView(View(context).apply {
            setBackgroundColor(0x1226405C)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UiKit.dp(context, 1))
        })
        mainPage.addView(UiKit.spacer(context, 14))

        // ══════════════════════════════════════════════════════════════════════
        // 4. KHU VỰC ẤP TRỨNG (INCUBATOR)
        // Yêu cầu:
        // - Chỉ có ĐÚNG 1 CHỖ ẤP TRỨNG
        // - Có dấu "!" ngay trên tiêu đề để xem thời gian ấp
        // - Khi trống: có hình quả trứng mất màu và có chữ "ôi chỗ ấp trống rồi"
        // ══════════════════════════════════════════════════════════════════════
        val incubatorSection = UiKit.column(context, 8).apply {

            // Tiêu đề có nút bong bóng "!" ngay cạnh
            addView(UiKit.row(context, 6).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(UiKit.text(context, "🥚 Chỗ Ấp Trứng", 17, UiKit.INK, true))

                // Dấu "!" trên tiêu đề
                addView(TextView(context).apply {
                    text = "!"
                    textSize = 11f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(UiKit.BLUE)
                    }
                    val sz = UiKit.dp(context, 20)
                    layoutParams = LinearLayout.LayoutParams(sz, sz)
                    isClickable = true
                    setOnClickListener {
                        showHarmonyDialog(context, "⏱", "Thời Gian Ấp Trứng") {
                            addView(UiKit.text(context, "Thời gian cần thiết để mỗi loại trứng nở:", 13, UiKit.MUTED))
                            addView(UiKit.spacer(context, 6))

                            listOf(
                                Triple("Trứng Thường", "5 phút", 0xFF38B278.toInt()),
                                Triple("Trứng Hiếm", "30 phút", 0xFFF5A623.toInt()),
                                Triple("Trứng Sử Thi", "2 giờ", 0xFF8E44AD.toInt())
                            ).forEach { (tName, tTime, tColor) ->
                                addView(UiKit.row(context, 6).apply {
                                    gravity = Gravity.CENTER_VERTICAL
                                    addView(UiKit.pill(context, tName, tColor, UiKit.WHITE).apply {
                                        layoutParams = LinearLayout.LayoutParams(UiKit.dp(context, 100), LinearLayout.LayoutParams.WRAP_CONTENT)
                                    })
                                    addView(UiKit.text(context, "⏳  $tTime", 13, UiKit.INK, true))
                                })
                                addView(UiKit.spacer(context, 4))
                            }
                        }
                    }
                })

                addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
            })

            // KHUNG DUY NHẤT CHO 1 CHỖ ẤP TRỨNG
            val incubatorCard = UiKit.column(context, 8).apply {
                gravity = Gravity.CENTER
                setPadding(UiKit.dp(context, 16), UiKit.dp(context, 16), UiKit.dp(context, 16), UiKit.dp(context, 16))
                UiKit.background(this, UiKit.WHITE, 20)
                elevation = UiKit.dp(context, 2).toFloat()

                val activeTypeId = PetWorldState.activeEggTypeId
                if (activeTypeId == null) {
                    // ── Trạng thái TRỐNG: quả trứng mất màu + chữ "Ôi chỗ ấp trống rồi!" ──
                    val grayscaleEgg = createGraphicEggView(
                        context,
                        intArrayOf(0xFFD3DCE6.toInt(), 0xFF9FB1C3.toInt()),
                        widthDp = 50,
                        heightDp = 64,
                        isGrayscale = true
                    )
                    addView(grayscaleEgg)
                    addView(UiKit.spacer(context, 2))
                    addView(UiKit.text(context, "Ôi chỗ ấp trống rồi!", 15, UiKit.INK, true, Gravity.CENTER))
                    addView(UiKit.text(context, "Hãy chọn 1 quả trứng từ Kho Đồ ở trên rồi bấm \"Đem Ấp\" nhé ✨", 12, UiKit.MUTED, false, Gravity.CENTER))
                } else {
                    // ── Trạng thái ĐANG ẤP ──
                    val currentEgg = SHOP_EGG_LIST.firstOrNull { it.typeId == activeTypeId } ?: SHOP_EGG_LIST[0]

                    val eggGraphic = createGraphicEggView(context, currentEgg.gradientColors, widthDp = 54, heightDp = 68)
                    addView(eggGraphic)

                    addView(UiKit.text(context, "Đang ấp: ${currentEgg.name}", 15, UiKit.INK, true, Gravity.CENTER))

                    // Đồng hồ đếm ngược
                    addView(UiKit.pill(context, "⏱ Còn lại: ${PetWorldState.activeEggTimeRemaining}", 0xFFFFF2D6.toInt(), 0xFF9C6500.toInt()))

                    // Nút Ấp Nở
                    addView(UiKit.button(context, "Ấp Nở Ngay ✨", UiKit.GREEN, UiKit.WHITE) {
                        // Xác định pet nở ra dựa theo độ hiếm
                        val candidates = PetWorldState.catalog
                        val hatchedPet = when (activeTypeId) {
                            "epic" -> candidates.firstOrNull { it.rarity == "Epic" || it.rarity == "Legendary" } ?: candidates.random()
                            "rare" -> candidates.firstOrNull { it.rarity == "Rare" } ?: candidates.random()
                            else -> candidates.firstOrNull { it.rarity == "Common" } ?: candidates.first()
                        }

                        hatchedPet.unlocked = true
                        hatchedPet.duplicateCount += 1
                        PetWorldState.activeEggTypeId = null // Chỗ ấp trở lại trống

                        showHarmonyDialog(context, "🎊", "Trứng Đã Nở!") {
                            addView(UiKit.column(context, 6).apply {
                                gravity = Gravity.CENTER
                                addView(UiKit.text(context, hatchedPet.emoji, 48, gravity = Gravity.CENTER))
                                addView(UiKit.text(context, "${hatchedPet.name} (${hatchedPet.title})", 15, UiKit.INK, true, Gravity.CENTER))
                                addView(UiKit.pill(context, hatchedPet.rarity, hatchedPet.rarityColor, UiKit.WHITE))
                                addView(UiKit.spacer(context, 4))
                                val note = if (hatchedPet.duplicateCount > 1) {
                                    "Bạn đã sở hữu thêm 1 bạn ${hatchedPet.title} (Tổng: ${hatchedPet.duplicateCount})!"
                                } else {
                                    "Linh thú mới toanh đã được mở khóa trong bộ sưu tập!"
                                }
                                addView(UiKit.text(context, note, 12, UiKit.MUTED, false, Gravity.CENTER))
                            })
                        }
                        renderUi()
                    }.apply {
                        textSize = 13f
                        setPadding(UiKit.dp(context, 24), UiKit.dp(context, 8), UiKit.dp(context, 24), UiKit.dp(context, 8))
                    })
                }
            }
            addView(incubatorCard)
        }
        mainPage.addView(incubatorSection)

        // Phân cách nhẹ
        mainPage.addView(UiKit.spacer(context, 14))
        mainPage.addView(View(context).apply {
            setBackgroundColor(0x1226405C)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UiKit.dp(context, 1))
        })
        mainPage.addView(UiKit.spacer(context, 14))

        // ══════════════════════════════════════════════════════════════════════
        // 5. BỘ SƯU TẬP (COLLECTION) - ĐỒ HỌA SANG TRỌNG & BÓNG BÍ ẨN
        // Yêu cầu:
        // - Đồ họa pet nhìn SANG TRỌNG (Card VIP, vòng hào quang, danh hiệu)
        // - Pet chưa có: dạng bóng mờ silhouette với dấu hỏi "?"
        // - Pet đã mở: hiển thị số lượng bị trùng
        // ══════════════════════════════════════════════════════════════════════
        val collectionSection = UiKit.column(context, 8).apply {

            val unlockedCount = PetWorldState.catalog.count { it.unlocked }
            val totalCount = PetWorldState.catalog.size

            addView(UiKit.row(context, 6).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(UiKit.text(context, "🏆 Bộ Sưu Tập Linh Thú", 17, UiKit.INK, true),
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                )
                addView(UiKit.pill(context, "$unlockedCount/$totalCount đã mở", UiKit.PURPLE, UiKit.WHITE).apply {
                    textSize = 10f
                })
            })

            // Lưới hiển thị các thẻ Pet sang trọng (chia làm hàng 4 ô)
            val petsGrid = UiKit.column(context, 8)

            PetWorldState.catalog.chunked(4).forEach { rowPets ->
                petsGrid.addView(UiKit.row(context, 6).apply {
                    rowPets.forEach { pet ->
                        addView(UiKit.column(context, 4).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                            setPadding(UiKit.dp(context, 4), UiKit.dp(context, 8), UiKit.dp(context, 4), UiKit.dp(context, 8))

                            if (pet.unlocked) {
                                // ── PET ĐÃ MỞ KHÓA: Thiết kế Card Sang Trọng ──
                                background = GradientDrawable().apply {
                                    cornerRadius = UiKit.dp(context, 14).toFloat()
                                    setColor(pet.cardBgColor)
                                    setStroke(UiKit.dp(context, 1.5f.toInt()), pet.rarityColor)
                                }
                                elevation = UiKit.dp(context, 2).toFloat()

                                // Vòng hào quang tròn (halo avatar circle)
                                val avatarCircle = FrameLayout(context).apply {
                                    val haloSize = UiKit.dp(context, 38)
                                    layoutParams = LinearLayout.LayoutParams(haloSize, haloSize)
                                    background = GradientDrawable().apply {
                                        shape = GradientDrawable.OVAL
                                        setColor(Color.WHITE)
                                        setStroke(UiKit.dp(context, 1), pet.rarityColor)
                                    }
                                    addView(UiKit.text(context, pet.emoji, 22, gravity = Gravity.CENTER).apply {
                                        layoutParams = FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT
                                        )
                                    })
                                }
                                addView(avatarCircle)

                                // Tên linh thú
                                addView(UiKit.text(context, pet.title, 10, UiKit.INK, true, Gravity.CENTER))

                                // Nhãn độ hiếm
                                addView(UiKit.pill(context, pet.rarity, pet.rarityColor, UiKit.WHITE).apply {
                                    textSize = 8f
                                    setPadding(UiKit.dp(context, 5), UiKit.dp(context, 1), UiKit.dp(context, 5), UiKit.dp(context, 1))
                                })

                                // Số lượng bị trùng nếu > 1
                                if (pet.duplicateCount > 1) {
                                    addView(UiKit.pill(context, "×${pet.duplicateCount}", 0xFF26405C.toInt(), UiKit.WHITE).apply {
                                        textSize = 8.5f
                                        setPadding(UiKit.dp(context, 6), 0, UiKit.dp(context, 6), 0)
                                    })
                                } else {
                                    addView(UiKit.spacer(context, 4))
                                }
                            } else {
                                // ── PET CHƯA MỞ KHÓA: Thiết kế Dạng Bóng Huyền Bí (Silhouette Shadow) ──
                                background = GradientDrawable().apply {
                                    cornerRadius = UiKit.dp(context, 14).toFloat()
                                    setColor(0xFF26354A.toInt()) // Nền đen bóng đêm sang trọng
                                    setStroke(UiKit.dp(context, 1), 0xFF40536A.toInt(), UiKit.dp(context, 4).toFloat(), UiKit.dp(context, 2).toFloat())
                                }
                                elevation = UiKit.dp(context, 1).toFloat()

                                // Khung tròn bóng mờ chứa dấu hỏi
                                val shadowCircle = FrameLayout(context).apply {
                                    val haloSize = UiKit.dp(context, 38)
                                    layoutParams = LinearLayout.LayoutParams(haloSize, haloSize)
                                    background = GradientDrawable().apply {
                                        shape = GradientDrawable.OVAL
                                        setColor(0xFF1E2B3E.toInt())
                                    }
                                    addView(UiKit.text(context, "?", 18, 0xFF88A0BA.toInt(), true, Gravity.CENTER).apply {
                                        layoutParams = FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT
                                        )
                                    })
                                }
                                addView(shadowCircle)

                                addView(UiKit.text(context, "???", 10, 0xFF88A0BA.toInt(), true, Gravity.CENTER))

                                addView(UiKit.pill(context, "Chưa có", 0xFF35465C.toInt(), 0xFF9FB1C3.toInt()).apply {
                                    textSize = 8f
                                    setPadding(UiKit.dp(context, 5), UiKit.dp(context, 1), UiKit.dp(context, 5), UiKit.dp(context, 1))
                                })
                                addView(UiKit.spacer(context, 4))
                            }
                        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    }

                    // Bù cột trống nếu hàng cuối thiếu
                    repeat(4 - rowPets.size) {
                        addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
                    }
                })
            }

            addView(petsGrid)
        }
        mainPage.addView(collectionSection)
    }

    // Render khởi tạo
    renderUi()

    return rootScroll.apply {
        (this.getChildAt(0) as LinearLayout).addView(mainPage)
    }
}

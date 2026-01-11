package com.github.libretube.test.obj

data class BottomSheetItem(
    var title: String,
    val drawable: Int? = null,
    val iconDrawable: android.graphics.drawable.Drawable? = null,
    val getCurrent: () -> String? = { null },
    val onClick: () -> Unit = {}
)


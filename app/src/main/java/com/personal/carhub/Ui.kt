package com.personal.carhub

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/** Small helpers for building the screens in code (large, car-friendly touch targets). */
object Ui {
    fun Activity.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    fun Activity.color(id: Int): Int = getColor(id)

    fun Activity.title(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 26f
        setTextColor(color(R.color.text))
        setPadding(dp(20), dp(20), dp(20), dp(8))
    }

    fun Activity.body(text: String, sizeSp: Float = 16f): TextView = TextView(this).apply {
        this.text = text
        textSize = sizeSp
        setTextColor(color(R.color.muted))
        setPadding(dp(20), dp(4), dp(20), dp(8))
    }

    /** Number of tile columns that fit the current screen width. */
    fun Activity.columns(): Int {
        val widthDp = resources.configuration.screenWidthDp
        return (widthDp / 170).coerceIn(2, 6)
    }

    fun Activity.grid(): GridLayout = GridLayout(this).apply {
        columnCount = columns()
        setPadding(dp(12), dp(8), dp(12), dp(24))
    }

    fun Activity.tile(label: String, icon: Drawable?, iconRes: Int?, dim: Boolean, onClick: () -> Unit): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = getDrawable(R.drawable.tile_bg)
            isClickable = true
            isFocusable = true
            setPadding(dp(8), dp(16), dp(8), dp(16))
            alpha = if (dim) 0.55f else 1f
            setOnClickListener { onClick() }
            contentDescription = label
        }
        val image = ImageView(this).apply {
            when {
                icon != null -> setImageDrawable(icon)
                iconRes != null -> setImageResource(iconRes)
                else -> setImageResource(R.drawable.ic_app)
            }
        }
        box.addView(image, LinearLayout.LayoutParams(dp(64), dp(64)))
        box.addView(TextView(this).apply {
            text = label
            textSize = 16f
            gravity = Gravity.CENTER
            maxLines = 2
            setTextColor(color(R.color.text))
            setPadding(0, dp(10), 0, 0)
        })
        box.layoutParams = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
        ).apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setMargins(dp(8), dp(8), dp(8), dp(8))
        }
        return box
    }
}

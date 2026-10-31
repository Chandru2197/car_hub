package com.personal.carhub

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import com.personal.carhub.Ui.body
import com.personal.carhub.Ui.color
import com.personal.carhub.Ui.dp
import com.personal.carhub.Ui.title

/** Pick exactly which apps appear on the home screen. Changes save immediately. */
class PickAppsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pinned = Apps.pinned(this).toMutableList()

        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(title("Choose apps"))
        column.addView(body("Only the apps ticked here appear on the CarHub home screen."))

        // Not-installed defaults stay listed so they can be unticked.
        val installed = Apps.launchable(this)
        val missingDefaults = Apps.DEFAULT_OTT
            .filterKeys { pkg -> installed.none { it.packageName == pkg } }
            .map { (pkg, label) -> AppTile(pkg, "$label (not installed)", null, false) }

        for (app in missingDefaults + installed) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(20), dp(10), dp(20), dp(10))
            }
            row.addView(ImageView(this).apply {
                if (app.icon != null) setImageDrawable(app.icon) else setImageResource(R.drawable.ic_app)
            }, LinearLayout.LayoutParams(dp(40), dp(40)))
            row.addView(CheckBox(this).apply {
                text = app.label
                textSize = 18f
                setTextColor(color(R.color.text))
                setPadding(dp(12), 0, 0, 0)
                isChecked = app.packageName in pinned
                setOnCheckedChangeListener { _, checked ->
                    if (checked) pinned += app.packageName else pinned -= app.packageName
                    Apps.setPinned(this@PickAppsActivity, pinned)
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            column.addView(row)
        }

        setContentView(ScrollView(this).apply { addView(column) })
    }
}

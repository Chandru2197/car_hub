package com.personal.carhub

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import android.widget.ScrollView
import com.personal.carhub.Ui.body
import com.personal.carhub.Ui.grid
import com.personal.carhub.Ui.tile
import com.personal.carhub.Ui.title

/**
 * The main screen: big tiles for the apps you chose (your OTT apps by default),
 * plus "My Streams" and "Choose apps".
 *
 * On a phone it is a normal app. On an Android "CarPlay AI box" it can be chosen as the
 * home screen, so the car display shows only your apps.
 */
class HomeActivity : Activity() {

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(title(getString(R.string.app_name)))
        column.addView(body("Video is for when the car is parked. While driving, use audio only."))

        val tiles = grid()
        for (app in Apps.tiles(this)) {
            val label = if (app.installed) app.label else "${app.label}\n(install)"
            tiles.addView(tile(label, app.icon, null, dim = !app.installed) {
                Apps.open(this, app.packageName)
            })
        }
        tiles.addView(tile("My Streams", null, R.drawable.ic_app, dim = false) {
            startActivity(Intent(this, StreamsActivity::class.java))
        })
        tiles.addView(tile("Choose apps", null, android.R.drawable.ic_menu_manage, dim = false) {
            startActivity(Intent(this, PickAppsActivity::class.java))
        })
        column.addView(tiles)

        setContentView(ScrollView(this).apply { addView(column) })
    }
}

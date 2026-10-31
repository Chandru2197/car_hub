package com.personal.carhub

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri

/** An app shown as a tile on the home screen. */
data class AppTile(val packageName: String, val label: String, val icon: Drawable?, val installed: Boolean)

object Apps {
    /** Your OTT services. Shown by default; tapping opens the official app (or its Play Store page). */
    val DEFAULT_OTT = linkedMapOf(
        "com.google.android.youtube" to "YouTube",
        "com.amazon.avod.thirdpartyclient" to "Prime Video",
        "com.netflix.mediaclient" to "Netflix",
        "in.startv.hotstar" to "JioHotstar",
        "tv.accedo.airtel.wynk" to "Airtel Xstream",
    )

    private const val PREFS = "apps"
    private const val KEY_PINNED = "pinned"

    /** Packages you chose to show, in order. Defaults to the OTT list. */
    fun pinned(context: Context): List<String> {
        val saved = prefs(context).getString(KEY_PINNED, null)
            ?: return DEFAULT_OTT.keys.toList()
        return saved.split(',').filter { it.isNotBlank() }
    }

    fun setPinned(context: Context, packages: List<String>) {
        prefs(context).edit().putString(KEY_PINNED, packages.distinct().joinToString(",")).apply()
    }

    fun tiles(context: Context): List<AppTile> {
        val pm = context.packageManager
        return pinned(context).map { pkg ->
            val info = try {
                pm.getApplicationInfo(pkg, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            if (info != null) {
                AppTile(pkg, pm.getApplicationLabel(info).toString(), pm.getApplicationIcon(info), true)
            } else {
                AppTile(pkg, DEFAULT_OTT[pkg] ?: pkg, null, false)
            }
        }
    }

    /** All apps that have a launcher icon, excluding CarHub itself. */
    fun launchable(context: Context): List<AppTile> {
        val pm = context.packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(query, 0)
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != context.packageName }
            .map { AppTile(it.packageName, pm.getApplicationLabel(it).toString(), pm.getApplicationIcon(it), true) }
            .sortedBy { it.label.lowercase() }
    }

    /** Opens the app, or its Play Store page if it is not installed. */
    fun open(context: Context, packageName: String) {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: context.packageManager.getLeanbackLaunchIntentForPackage(packageName)
        val intent = launch ?: Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // No Play Store/browser available (possible on some boxes); nothing more to do.
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

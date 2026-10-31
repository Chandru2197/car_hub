package com.personal.carhub

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.personal.carhub.Ui.body
import com.personal.carhub.Ui.color
import com.personal.carhub.Ui.dp
import com.personal.carhub.Ui.title
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Your own streams: add a single link, or import an M3U channel list (pasted or from a link).
 * Tap a stream to play video on this screen. The same list appears in Android Auto as audio.
 */
class StreamsActivity : Activity() {

    private lateinit var store: StreamStore
    private lateinit var list: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = StreamStore(this)

        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(title("My Streams"))
        column.addView(body("Add links you are allowed to watch: HLS (.m3u8), MP4, MP3 or an M3U channel list. Netflix, Prime and other subscription apps can't be added here; open them from the home screen."))

        val name = field("Name (optional)", multiLine = false)
        val url = field("https://… stream or .m3u link", multiLine = false).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        }
        column.addView(name)
        column.addView(url)
        column.addView(button("Add stream") {
            val stream = Playlist.newStream(name.text.toString(), url.text.toString())
            if (stream == null) {
                toast("Enter a full link starting with https://")
            } else {
                toast(if (store.addAll(listOf(stream)) > 0) "Added" else "Already in your list")
                name.setText("")
                url.setText("")
                refresh()
            }
        })
        column.addView(button("Import M3U channel list from this link") {
            importFromLink(url.text.toString())
        })

        val pasted = field("…or paste M3U playlist text here", multiLine = true)
        column.addView(pasted)
        column.addView(button("Import pasted list") {
            val found = Playlist.parseM3u(pasted.text.toString())
            toast("Added ${store.addAll(found)} of ${found.size} streams")
            pasted.setText("")
            refresh()
        })

        column.addView(title("Saved"))
        column.addView(body("Tap to play. Press and hold to delete."))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(list)

        setContentView(ScrollView(this).apply { addView(column) })
        refresh()
    }

    private fun refresh() {
        list.removeAllViews()
        val streams = store.all()
        if (streams.isEmpty()) {
            list.addView(body("No streams yet."))
            return
        }
        for (s in streams) {
            list.addView(TextView(this).apply {
                text = "▶  ${s.name}"
                textSize = 18f
                setTextColor(color(R.color.text))
                background = getDrawable(R.drawable.tile_bg)
                isFocusable = true
                setPadding(dp(20), dp(18), dp(20), dp(18))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { setMargins(dp(16), dp(6), dp(16), dp(6)) }
                setOnClickListener {
                    startActivity(Intent(this@StreamsActivity, PlayerActivity::class.java)
                        .putExtra(PlayerActivity.EXTRA_STREAM_ID, s.id))
                }
                setOnLongClickListener {
                    AlertDialog.Builder(this@StreamsActivity)
                        .setMessage("Delete \"${s.name}\"?")
                        .setPositiveButton("Delete") { _, _ -> store.remove(s.id); refresh() }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
            })
        }
    }

    private fun importFromLink(raw: String) {
        val link = Playlist.cleanUrl(raw) ?: return toast("Enter a full link starting with https://")
        toast("Downloading list…")
        thread {
            val message = try {
                val text = download(link)
                val found = Playlist.parseM3u(text)
                if (found.isEmpty()) "No streams found in that list" else "Added ${store.addAll(found)} of ${found.size} streams"
            } catch (e: Exception) {
                "Could not download the list (${e.javaClass.simpleName})"
            }
            runOnUiThread { toast(message); refresh() }
        }
    }

    /** Downloads at most 2 MB of playlist text. */
    private fun download(link: String): String {
        val conn = URL(link).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        try {
            if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
            val bytes = conn.inputStream.use { input ->
                val buffer = ByteArray(2 * 1024 * 1024)
                var total = 0
                while (total < buffer.size) {
                    val n = input.read(buffer, total, buffer.size - total)
                    if (n < 0) break
                    total += n
                }
                buffer.copyOf(total)
            }
            return String(bytes, Charsets.UTF_8)
        } finally {
            conn.disconnect()
        }
    }

    private fun field(hint: String, multiLine: Boolean) = EditText(this).apply {
        this.hint = hint
        setTextColor(color(R.color.text))
        setHintTextColor(color(R.color.muted))
        if (multiLine) {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            gravity = Gravity.TOP
        } else {
            isSingleLine = true
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { setMargins(dp(16), dp(4), dp(16), dp(4)) }
    }

    private fun button(text: String, onClick: () -> Unit) = Button(this).apply {
        this.text = text
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { setMargins(dp(16), dp(4), dp(16), dp(4)) }
    }

    private fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

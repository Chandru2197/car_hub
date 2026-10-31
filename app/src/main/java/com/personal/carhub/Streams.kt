package com.personal.carhub

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** One stream you added yourself: an HLS (.m3u8), MP4, MP3 or similar http(s) address. */
data class Stream(val id: String, val name: String, val url: String)

/** Pure parsing and validation, kept free of Android classes so it can be unit tested. */
object Playlist {
    const val MAX_STREAMS = 500
    private const val MAX_NAME = 80
    private const val MAX_URL = 2048

    /** Returns the cleaned URL, or null if it is not an http(s) address we can play. */
    fun cleanUrl(raw: String): String? {
        val url = raw.trim()
        if (url.isEmpty() || url.length > MAX_URL || url.any { it.isWhitespace() }) return null
        val lower = url.lowercase()
        if (!lower.startsWith("https://") && !lower.startsWith("http://")) return null
        if (url.substringAfter("://").substringBefore('/').isEmpty()) return null
        return url
    }

    fun cleanName(raw: String, fallbackUrl: String): String {
        val name = raw.trim().replace(Regex("\\s+"), " ")
        val chosen = name.ifEmpty { fallbackUrl.substringAfter("://").substringBefore('/') }
        return chosen.take(MAX_NAME)
    }

    fun newStream(name: String, url: String): Stream? {
        val clean = cleanUrl(url) ?: return null
        return Stream(UUID.randomUUID().toString(), cleanName(name, clean), clean)
    }

    /**
     * Parses M3U / M3U8 channel-list text (the "#EXTINF:-1 ...,Channel name" + URL format).
     * Lines that are not valid http(s) URLs are skipped.
     */
    fun parseM3u(text: String): List<Stream> {
        val result = mutableListOf<Stream>()
        var pendingName = ""
        for (rawLine in text.lineSequence()) {
            val line = rawLine.trim()
            when {
                line.isEmpty() -> Unit
                line.startsWith("#EXTINF", ignoreCase = true) ->
                    pendingName = line.substringAfterLast(',', "")
                line.startsWith("#") -> Unit
                else -> {
                    newStream(pendingName, line)?.let { result += it }
                    pendingName = ""
                    if (result.size >= MAX_STREAMS) return result
                }
            }
        }
        return result
    }

    fun toJson(streams: List<Stream>): String {
        val array = JSONArray()
        streams.forEach {
            array.put(JSONObject().put("id", it.id).put("name", it.name).put("url", it.url))
        }
        return array.toString()
    }

    fun fromJson(json: String?): List<Stream> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val o = array.optJSONObject(i) ?: return@mapNotNull null
                val url = cleanUrl(o.optString("url")) ?: return@mapNotNull null
                val id = o.optString("id").ifEmpty { return@mapNotNull null }
                Stream(id, cleanName(o.optString("name"), url), url)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/** Saves your streams on the phone only. Nothing is uploaded anywhere. */
class StreamStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("streams", Context.MODE_PRIVATE)

    @Synchronized
    fun all(): List<Stream> = Playlist.fromJson(prefs.getString(KEY, null))

    fun find(id: String): Stream? = all().firstOrNull { it.id == id }

    /** Adds streams, skipping URLs already saved. Returns how many were added. */
    @Synchronized
    fun addAll(newOnes: List<Stream>): Int {
        val current = all().toMutableList()
        val known = current.map { it.url }.toMutableSet()
        var added = 0
        for (s in newOnes) {
            if (current.size >= Playlist.MAX_STREAMS) break
            if (known.add(s.url)) {
                current += s
                added++
            }
        }
        save(current)
        return added
    }

    @Synchronized
    fun remove(id: String) = save(all().filterNot { it.id == id })

    private fun save(list: List<Stream>) {
        prefs.edit().putString(KEY, Playlist.toJson(list)).apply()
    }

    private companion object {
        const val KEY = "list"
    }
}

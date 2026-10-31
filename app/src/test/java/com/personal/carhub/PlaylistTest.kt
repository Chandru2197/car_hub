package com.personal.carhub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistTest {

    @Test
    fun acceptsHttpAndHttpsOnly() {
        assertEquals("https://a.example/x.m3u8", Playlist.cleanUrl("  https://a.example/x.m3u8 "))
        assertEquals("http://a.example/x.mp3", Playlist.cleanUrl("http://a.example/x.mp3"))
        assertNull(Playlist.cleanUrl("file:///sdcard/x.mp4"))
        assertNull(Playlist.cleanUrl("javascript:alert(1)"))
        assertNull(Playlist.cleanUrl("https://"))
        assertNull(Playlist.cleanUrl("https://a.example/with space"))
        assertNull(Playlist.cleanUrl(""))
    }

    @Test
    fun nameFallsBackToHost() {
        assertEquals("a.example", Playlist.cleanName("   ", "https://a.example/live"))
        assertEquals("News 24", Playlist.cleanName("  News   24 ", "https://a.example/live"))
    }

    @Test
    fun parsesM3uChannelList() {
        val text = """
            #EXTM3U
            #EXTINF:-1 tvg-id="one" group-title="News",News One
            https://a.example/one.m3u8
            #EXTINF:-1,Bad entry
            rtmp://a.example/not-supported
            #EXTINF:-1,Radio
            http://b.example/radio.mp3
            https://c.example/no-name.mp4
        """.trimIndent()
        val streams = Playlist.parseM3u(text)
        assertEquals(listOf("News One", "Radio", "c.example"), streams.map { it.name })
        assertEquals("https://a.example/one.m3u8", streams[0].url)
    }

    @Test
    fun jsonRoundTripDropsInvalidEntries() {
        val streams = listOf(Playlist.newStream("A", "https://a.example/a")!!)
        assertEquals(streams, Playlist.fromJson(Playlist.toJson(streams)))
        assertEquals(emptyList<Stream>(), Playlist.fromJson("""[{"id":"1","name":"x","url":"ftp://x"}]"""))
        assertEquals(emptyList<Stream>(), Playlist.fromJson("not json"))
    }
}

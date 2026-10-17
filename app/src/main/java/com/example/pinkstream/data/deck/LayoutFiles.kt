package com.example.pinkstream.data.deck

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Reads and writes backup files the user picked with the system file picker (the
 * Storage Access Framework). The picker hands us a content:// Uri, which works for
 * Downloads, Google Drive and so on, and needs no storage permission.
 */
class LayoutFiles(private val contentResolver: ContentResolver) {

    /** Throws IOException (or SecurityException if the file went away). */
    suspend fun write(uri: Uri, text: String) = withContext(Dispatchers.IO) {
        // "wt" = write and truncate, so a longer old file doesn't leave junk at the end.
        val stream = contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Can't open $uri")
        stream.bufferedWriter().use { it.write(text) }
    }

    /** Throws IOException (or SecurityException), also for files over [MAX_BYTES]. */
    suspend fun read(uri: Uri): String = withContext(Dispatchers.IO) {
        val stream = contentResolver.openInputStream(uri) ?: throw IOException("Can't open $uri")
        stream.use {
            val bytes = it.readNBytesCompat(MAX_BYTES + 1)
            if (bytes.size > MAX_BYTES) throw IOException("File too big for a layout")
            bytes.toString(Charsets.UTF_8)
        }
    }

    // InputStream.readNBytes needs API 33; this does the same on older phones.
    private fun InputStream.readNBytesCompat(limit: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (out.size() < limit) {
            val read = read(buffer, 0, minOf(buffer.size, limit - out.size()))
            if (read < 0) break
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }

    private companion object {
        /** A big layout is a few tens of KB; anything this size is the wrong file. */
        const val MAX_BYTES = 1024 * 1024
    }
}

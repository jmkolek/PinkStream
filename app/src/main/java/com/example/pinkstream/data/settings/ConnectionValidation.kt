package com.example.pinkstream.data.settings

/** Checks for the PC address and port fields. Each `…Error` returns null when the value is fine. */
object ConnectionValidation {

    fun hostError(host: String): String? {
        val h = host.trim()
        return when {
            h.isEmpty() -> "Enter your PC's address."
            // People paste whole URLs ("ws://192.168.1.20:8080/"); the field wants just the address.
            h.contains("://") || h.any { it.isWhitespace() || it == '/' } ->
                "Enter just the address, like 192.168.1.20."
            else -> null
        }
    }

    // Short on purpose: it shows under the narrow Port field.
    fun portError(port: String): String? = if (parsePort(port) == null) "Use 1–65535." else null

    fun parsePort(port: String): Int? = port.trim().toIntOrNull()?.takeIf { it in 1..65535 }
}

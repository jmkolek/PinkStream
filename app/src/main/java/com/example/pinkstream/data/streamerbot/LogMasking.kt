package com.example.pinkstream.data.streamerbot

// Matches "authentication": "<anything>" (a string value only, so the server's
// Hello with its salt/challenge object is logged as is).
private val AuthenticationValue = Regex("\"authentication\"\\s*:\\s*\"[^\"]*\"")

/** Hides the authentication string before a message goes to Logcat. */
fun maskSecrets(json: String): String =
    AuthenticationValue.replace(json, "\"authentication\":\"***\"")

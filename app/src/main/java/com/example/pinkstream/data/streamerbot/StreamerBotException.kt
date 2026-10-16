package com.example.pinkstream.data.streamerbot

/**
 * A request didn't work. [message] is written for the user.
 * [rejected] = Streamer.bot answered with an error (as opposed to no answer at all).
 */
class StreamerBotException(message: String, val rejected: Boolean = false) : Exception(message)

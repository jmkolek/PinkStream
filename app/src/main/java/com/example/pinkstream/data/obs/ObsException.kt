package com.example.pinkstream.data.obs

/** A request to OBS didn't work. [message] is written for the user. */
class ObsException(message: String) : Exception(message)

package com.example.pinkstream.data.obs

import kotlinx.serialization.json.JsonObject

/** One OBS event, e.g. type "InputMuteStateChanged" with data {"inputName": "Mic/Aux", "inputMuted": true}. */
data class ObsEvent(val type: String, val data: JsonObject)

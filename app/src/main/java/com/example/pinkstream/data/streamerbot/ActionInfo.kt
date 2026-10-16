package com.example.pinkstream.data.streamerbot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One action from GetActions. */
@Serializable
data class ActionInfo(
    val id: String,
    val name: String,
    val group: String? = null,
    val enabled: Boolean = true,
    @SerialName("subaction_count") val subactionCount: Int = 0,
)

@Serializable
data class GetActionsResponse(
    val id: String? = null,
    val status: String? = null,
    val count: Int? = null,
    val actions: List<ActionInfo> = emptyList(),
)

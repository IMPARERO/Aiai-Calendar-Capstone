package com.miguel.apps.aiaialpha.ui.model

import java.util.UUID

enum class Role {
    YOU, AIAI, ERROR
}

data class ChatMessage(
    val cId: String = UUID.randomUUID().toString(),
    val id: String,
    var text: String = "",
    val participant: Role = Role.YOU,
    var isPending: Boolean = false,
    val imageUris: List<String>,
)

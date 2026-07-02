package com.laszlo.tienda_app.model

import java.io.Serializable

data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String
) : Serializable

data class ChatRequest(
    val messages: List<ChatMessage>
) : Serializable

data class ChatResponse(
    val status: String,
    val message: String,
    val data: ChatData
) : Serializable

data class ChatData(
    val response: String,
    val products: List<ProductAnalysis>
) : Serializable

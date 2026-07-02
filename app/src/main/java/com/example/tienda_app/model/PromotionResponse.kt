package com.laszlo.tienda_app.model

import java.io.Serializable

data class Promotion(
    val id: Int,
    val title: String,
    val description: String?,
    val discount_code: String?,
    val qr_code_url: String
) : Serializable

data class PromotionResponse(
    val status: String,
    val message: String,
    val data: Promotion
) : Serializable

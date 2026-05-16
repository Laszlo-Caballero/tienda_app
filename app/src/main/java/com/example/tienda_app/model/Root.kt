package com.example.tienda_app.model

import java.io.Serializable

data class Root<T>(
    val data: List<T>,
    val message: String,
    val status: String
) : Serializable

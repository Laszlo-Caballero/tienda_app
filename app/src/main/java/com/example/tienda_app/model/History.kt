package com.laszlo.tienda_app.model

data class History(
    val id: Int,
    val title: String,
    val time: String,
    val description: String,
    val tags: List<String>,
    val image: String,
    val category: String
)

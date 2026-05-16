package com.example.tienda_app.model

import java.io.Serializable

data class ProductAnalysis(
    val productoId: Int,
    val nombre: String,
    val precios: List<Double>,
    val vendido_por: String,
    val marca: String,
    val url_venta: String,
    val caracteristicas: List<String>,
    val categoria: String,
    val sub_categoria: String,
    val especificaciones: List<String>,
    val imagenes: List<Imagen>,
    val similitud: Double
) : Serializable

data class Imagen(
    val imagenId: Int,
    val url: String
) : Serializable

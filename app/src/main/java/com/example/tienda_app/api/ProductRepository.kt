package com.laszlo.tienda_app.api

import com.laszlo.tienda_app.model.ProductAnalysis
import com.laszlo.tienda_app.model.Imagen
import com.laszlo.tienda_app.model.ChatMessage
import com.laszlo.tienda_app.model.ChatResponse
import com.laszlo.tienda_app.model.ChatData

object ProductRepository {

    private val LOCAL_PRODUCTS = listOf(
        ProductAnalysis(
            productoId = 1,
            nombre = "Laptop Pro 15",
            precios = listOf(1200.0, 1150.0),
            vendido_por = "TechStore",
            marca = "PowerBrand",
            url_venta = "https://example.com/laptop",
            caracteristicas = listOf("16GB RAM", "512GB SSD"),
            categoria = "Electrónica",
            sub_categoria = "Laptops",
            especificaciones = listOf("Procesador i7", "Pantalla 4K"),
            imagenes = emptyList<Imagen>(),
            similitud = 1.0
        ),
        ProductAnalysis(
            productoId = 2,
            nombre = "Smartphone X",
            precios = listOf(800.0, 750.0),
            vendido_por = "MobileWorld",
            marca = "NextGen",
            url_venta = "https://example.com/phone",
            caracteristicas = listOf("128GB", "Cámara 48MP"),
            categoria = "Electrónica",
            sub_categoria = "Celulares",
            especificaciones = listOf("OLED 6.1\"", "5G"),
            imagenes = emptyList<Imagen>(),
            similitud = 1.0
        )
    )

    /**
     * Searches for products in the local dataset.
     */
    fun searchLocal(query: String): List<ProductAnalysis> {
        val trimmedQuery = query.trim().lowercase()
        if (trimmedQuery.isEmpty()) return LOCAL_PRODUCTS
        
        return LOCAL_PRODUCTS.filter {
            it.nombre.lowercase().contains(trimmedQuery) ||
            it.marca.lowercase().contains(trimmedQuery) ||
            it.productoId.toString() == trimmedQuery
        }
    }

    suspend fun getProducts(query: String): List<ProductAnalysis> {
        return try {
            val response = ApiController.api.getProducts(query.ifBlank { null })
            if (response.data.isEmpty()) {
                searchLocal(query)
            } else {
                response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            searchLocal(query)
        }
    }

    suspend fun voiceSearch(query: String): List<ProductAnalysis> {
        return try {
            val response = ApiController.api.voiceSearch(query)
            if (response.data.isEmpty()) {
                searchLocal(query)
            } else {
                response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            searchLocal(query)
        }
    }

    suspend fun chat(messages: List<ChatMessage>): ChatResponse {
        return try {
            ApiController.api.chat(com.laszlo.tienda_app.model.ChatRequest(messages))
        } catch (e: Exception) {
            e.printStackTrace()
            ChatResponse(
                status = "error",
                message = "Error de red: ${e.message}",
                data = ChatData(
                    response = "Lo siento, no pude conectarme con el asistente en este momento. Por favor, verifica tu conexión.",
                    products = emptyList()
                )
            )
        }
    }
}

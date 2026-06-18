package com.laszlo.tienda_app.api

import com.laszlo.tienda_app.model.ProductAnalysis

object ProductRepository {

    suspend fun getProducts(query: String): List<ProductAnalysis> {
        return try {
            val response = ApiController.api.getProducts(query.ifBlank { null })
            response.data
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun voiceSearch(query: String): List<ProductAnalysis> {
        return try {
            val response = ApiController.api.voiceSearch(query)
            response.data
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

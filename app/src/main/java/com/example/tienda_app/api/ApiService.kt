package com.example.tienda_app.api

import com.example.tienda_app.model.History
import retrofit2.http.GET


interface ApiService {
    @GET("history")
    suspend fun getHistory(): List<History>
}
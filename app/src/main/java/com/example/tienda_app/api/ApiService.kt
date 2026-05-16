package com.example.tienda_app.api

import com.example.tienda_app.model.History
import com.example.tienda_app.model.ProductAnalysis
import com.example.tienda_app.model.Root
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiService {
    @GET("history")
    suspend fun getHistory(): List<History>

    @Multipart
    @POST("api/products/identify")
    suspend fun identifyProduct(
        @Part file: MultipartBody.Part
    ): Root<ProductAnalysis>
}
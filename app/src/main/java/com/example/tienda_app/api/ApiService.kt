package com.laszlo.tienda_app.api

import com.laszlo.tienda_app.model.History
import com.laszlo.tienda_app.model.ProductAnalysis
import com.laszlo.tienda_app.model.Root
import com.laszlo.tienda_app.model.LoginRequest
import com.laszlo.tienda_app.model.RegisterRequest
import com.laszlo.tienda_app.model.AuthResponse
import com.laszlo.tienda_app.model.PushTokenRequest
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Body

interface ApiService {
    @GET("history")
    suspend fun getHistory(): List<History>

    @Multipart
    @POST("api/products/identify")
    suspend fun identifyProduct(
        @Part file: MultipartBody.Part
    ): Root<ProductAnalysis>

    @GET("api/products/voice")
    suspend fun voiceSearch(
        @Query("query") query: String
    ): Root<ProductAnalysis>

    @POST("api/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): AuthResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): AuthResponse

    @POST("api/notifications/register-token")
    suspend fun registerPushToken(
        @Body body: PushTokenRequest
    ): Root<Unit>
}

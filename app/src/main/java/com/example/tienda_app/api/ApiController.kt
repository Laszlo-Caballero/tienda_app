package com.laszlo.tienda_app.api

import android.content.Context
import com.laszlo.tienda_app.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiController {
    private var retrofitInstance: Retrofit? = null
    private var apiServiceInstance: ApiService? = null

    fun init(context: Context) {
        if (retrofitInstance == null) {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .build()

            retrofitInstance = Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            apiServiceInstance = retrofitInstance!!.create(ApiService::class.java)
        }
    }

    val api: ApiService
        get() {
            return apiServiceInstance ?: throw IllegalStateException("ApiController must be initialized by calling init(context) first.")
        }
}

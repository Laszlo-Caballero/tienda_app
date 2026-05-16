package com.example.tienda_app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.example.tienda_app.Constants

object ApiController {
    private val retrofit = Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    val api: ApiService = retrofit.create(ApiService::class.java)
}
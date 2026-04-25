package com.example.tienda_app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiController {
    private val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    val api: ApiService = retrofit.create(ApiService::class.java)
}
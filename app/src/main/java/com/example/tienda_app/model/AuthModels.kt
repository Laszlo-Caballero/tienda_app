package com.example.tienda_app.model

import java.io.Serializable

data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val role: String
) : Serializable

data class AuthData(
    val accessToken: String,
    val user: User
) : Serializable

data class AuthResponse(
    val status: String,
    val message: String,
    val data: AuthData?
) : Serializable

data class LoginRequest(
    val username: String,
    val password: String
) : Serializable

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
) : Serializable

data class PushTokenRequest(
    val token: String,
    val platform: String = "android"
) : Serializable

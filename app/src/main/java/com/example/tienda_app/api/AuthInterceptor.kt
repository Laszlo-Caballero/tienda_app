package com.laszlo.tienda_app.api

import android.content.Context
import android.content.Intent
import android.util.Log
import com.laszlo.tienda_app.ui.auth.AuthActivity
import com.laszlo.tienda_app.util.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    
    private val TAG = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val authManager = AuthManager.getInstance(context)
        val originalRequest = chain.request()
        
        // Build request, add Bearer token if it exists
        val requestBuilder = originalRequest.newBuilder()
        authManager.getAccessToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        val response = chain.proceed(requestBuilder.build())
        
        // If server responds with 401, session has expired/invalidated
        if (response.code() == 401) {
            Log.e(TAG, "Unauthorized response (401) received. Clearing session and redirecting to AuthActivity.")
            authManager.logout()
            
            // Redirect to Login/Registration flow, clearing the backstack
            val intent = Intent(context, AuthActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
        
        return response
    }
}

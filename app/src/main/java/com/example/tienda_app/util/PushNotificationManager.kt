package com.laszlo.tienda_app.util

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.laszlo.tienda_app.api.ApiController
import com.laszlo.tienda_app.model.PushTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushNotificationManager {
    private const val TAG = "PushNotificationManager"
    const val CHANNEL_ID = "tienda_app_notifications"
    private const val CHANNEL_NAME = "Notificaciones Tienda"
    private const val CHANNEL_DESC = "Canal para notificaciones de ofertas y actualizaciones de la tienda"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel initialized: $CHANNEL_ID")
        }
    }

    fun requestNotificationPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    requestCode
                )
            }
        }
    }

    fun registerCurrentToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "Current FCM Token: $token")
            sendTokenToServer(context, token)
        }
    }

    fun sendTokenToServer(context: Context, token: String) {
        // Save token locally in SharedPreferences
        val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        val authManager = AuthManager.getInstance(context)
        if (!authManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in. Token saved locally, will send after authentication.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = PushTokenRequest(token = token, platform = "android")
                val response = ApiController.api.registerPushToken(request)
                Log.d(TAG, "Push token successfully registered on server: $response")
            } catch (e: Exception) {
                Log.e(TAG, "Error registering push token on server. Will retry later.", e)
            }
        }
    }
}

package com.example.tienda_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tienda_app.api.ApiController
import com.example.tienda_app.ui.auth.AuthActivity
import com.example.tienda_app.util.AuthManager
import com.example.tienda_app.util.PushNotificationManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize API controller
        ApiController.init(applicationContext)

        // Session check guard
        if (!AuthManager.getInstance(this).isLoggedIn()) {
            val intent = Intent(this, AuthActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
            return
        }

        // Initialize Push Notifications
        PushNotificationManager.initNotificationChannel(applicationContext)
        PushNotificationManager.registerCurrentToken(applicationContext)
        PushNotificationManager.requestNotificationPermission(this, 1001)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cargarFragment(ScanFragment())

        val btnAnalyzer = findViewById<LinearLayout>(R.id.analyze)
        btnAnalyzer.isSelected = true

        val btnhistory = findViewById<LinearLayout>(R.id.history)
        val btnSettings = findViewById<LinearLayout>(R.id.config)
        val btnGallery = findViewById<LinearLayout>(R.id.gallery)

        val items = listOf(btnAnalyzer, btnGallery, btnhistory, btnSettings)

        btnAnalyzer.setOnClickListener {
            val analyzerFragment = ScanFragment()
            selectItem(btnAnalyzer, items)
            cargarFragment(analyzerFragment)
        }

        btnGallery.setOnClickListener {
            val searchFragment = com.example.tienda_app.ui.search.SearchFragment()
            selectItem(btnGallery, items)
            cargarFragment(searchFragment)
        }

        btnhistory.setOnClickListener {
            val historyFragment = HistoryFrangment()
            selectItem(btnhistory, items)
            cargarFragment(historyFragment)
        }

        btnSettings.setOnClickListener {
            val settingsFragment = ConfigFragment()
            selectItem(btnSettings, items)
            cargarFragment(settingsFragment)
        }
    }

    private fun selectItem(selected: View, items: List<View>){
        items.forEach {
            val density = it.resources.displayMetrics.density;
            it.isSelected = false
            it.layoutParams.width = (81 * density).toInt()
            it.setPadding(
                0,
                (8 * density).toInt(),
                0,
                (8 * density).toInt()
            )
            it.requestLayout()
        }
        selected.isSelected = true
        val density = selected.resources.displayMetrics.density;
        selected.layoutParams.width = (112 * density).toInt()
        selected.setPadding(
            (24 * density).toInt(),
            (8 * density).toInt(),
            (24 * density).toInt(),
            (8 * density).toInt()
        )
        selected.requestLayout()
    }

    private fun cargarFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.contenedor, fragment).commit()
    }
}
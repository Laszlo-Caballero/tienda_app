package com.example.tienda_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.tienda_app.ui.auth.AuthActivity
import com.example.tienda_app.util.AccessibilityHelper
import com.example.tienda_app.util.AuthManager
import com.example.tienda_app.util.SettingsManager

class ConfigFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read and configure API Base URL and Maps key description
        view.findViewById<TextView>(R.id.tvApiBaseUrl).text = Constants.API_BASE_URL
        
        // Use reflection/safe check or direct reference to BuildConfig since we enabled buildConfig
        val mapsStatusText = if (BuildConfig.MAPS_API_KEY.isNotEmpty()) {
            "Configurado con llave segura"
        } else {
            "FALTA API KEY (configurar en local.properties)"
        }
        view.findViewById<TextView>(R.id.tvMapsStatus).text = mapsStatusText

        // Configure switches
        val switchHighContrast = view.findViewById<SwitchCompat>(R.id.switchHighContrast)
        val switchAudioAssistant = view.findViewById<SwitchCompat>(R.id.switchAudioAssistant)

        switchHighContrast.isChecked = settingsManager.highContrastMode
        switchAudioAssistant.isChecked = settingsManager.audioAssistant

        switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.highContrastMode = isChecked
            applyAccessibilitySettings(view)
            val msg = if (isChecked) "Modo alto contraste activado" else "Modo alto contraste desactivado"
            AccessibilityHelper.announce(switchHighContrast, msg)
        }

        switchAudioAssistant.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.audioAssistant = isChecked
            val msg = if (isChecked) "Asistente de voz activado" else "Asistente de voz desactivado"
            AccessibilityHelper.announce(switchAudioAssistant, msg)
        }

        // Configure system TalkBack status
        val tvTalkBackStatus = view.findViewById<TextView>(R.id.tvTalkBackStatus)
        val isSystemTalkBack = AccessibilityHelper.isSystemTalkBackEnabled(requireContext())
        if (isSystemTalkBack) {
            tvTalkBackStatus.text = "ACTIVO"
            tvTalkBackStatus.setTextColor(requireContext().getColor(android.R.color.white))
            tvTalkBackStatus.setBackgroundColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            tvTalkBackStatus.text = "INACTIVO"
        }

        // Configure Logout Button
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            AuthManager.getInstance(requireContext()).logout()
            AccessibilityHelper.announce(btnLogout, "Cierre de sesión exitoso. Redirigiendo...")
            val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            requireActivity().finish()
        }

        applyAccessibilitySettings(view)

        // Initial accessibility announcement for settings page entry
        AccessibilityHelper.announce(view, "Pantalla de configuración cargada. Puedes cerrar sesión, configurar alto contraste y asistente de voz.")
    }

    private fun applyAccessibilitySettings(rootView: View) {
        val tvTitle = rootView.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = rootView.findViewById<TextView>(R.id.tvSubtitle)
        if (settingsManager.highContrastMode) {
            tvTitle.setTextColor(rootView.context.getColor(android.R.color.black))
            tvSubtitle.setTextColor(rootView.context.getColor(android.R.color.black))
            tvTitle.paint.isFakeBoldText = true
            tvSubtitle.paint.isFakeBoldText = true
        } else {
            tvTitle.setTextColor(rootView.context.getColor(R.color.black))
            tvSubtitle.setTextColor(rootView.context.getColor(R.color.gray))
            tvTitle.paint.isFakeBoldText = false
            tvSubtitle.paint.isFakeBoldText = false
        }
        tvTitle.invalidate()
        tvSubtitle.invalidate()
    }
}
package com.laszlo.tienda_app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.laszlo.tienda_app.MainActivity
import com.laszlo.tienda_app.R
import com.laszlo.tienda_app.api.ApiController
import com.laszlo.tienda_app.model.LoginRequest
import com.laszlo.tienda_app.model.User
import com.laszlo.tienda_app.util.AccessibilityHelper
import com.laszlo.tienda_app.util.AuthManager
import com.laszlo.tienda_app.util.PushNotificationManager
import com.laszlo.tienda_app.util.SettingsManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginFragment : Fragment() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnTestLogin: Button
    private lateinit var layoutRegisterLink: LinearLayout
    private lateinit var tvErrorMsg: TextView
    private lateinit var layoutLoading: FrameLayout
    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsManager.getInstance(requireContext())

        etUsername = view.findViewById(R.id.etUsername)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnTestLogin = view.findViewById(R.id.btnTestLogin)
        layoutRegisterLink = view.findViewById(R.id.layoutRegisterLink)
        tvErrorMsg = view.findViewById(R.id.tvErrorMsg)
        layoutLoading = view.findViewById(R.id.layoutLoading)

        applyAccessibilitySettings(view)

        btnLogin.setOnClickListener {
            attemptLogin()
        }

        btnTestLogin.setOnClickListener {
            loginAsTestUser()
        }

        layoutRegisterLink.setOnClickListener {
            (activity as? AuthActivity)?.showFragment(RegisterFragment())
        }

        AccessibilityHelper.announce(view, "Pantalla de inicio de sesión cargada.")
    }

    private fun attemptLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty()) {
            showError("Por favor, ingresa tu usuario.")
            etUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            showError("Por favor, ingresa tu contraseña.")
            etPassword.requestFocus()
            return
        }

        hideError()
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = LoginRequest(username = username, password = password)
                val response = ApiController.api.login(request)

                if (response.data != null) {
                    val authData = response.data
                    AuthManager.getInstance(requireContext()).saveSession(
                        authData.accessToken,
                        authData.user
                    )

                    // Register FCM push token now that we are authenticated
                    PushNotificationManager.registerCurrentToken(requireContext())

                    AccessibilityHelper.announce(btnLogin, "Inicio de sesión exitoso. Bienvenido.")

                    // Redirect to MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    showError(response.message ?: "Credenciales incorrectas.")
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Usuario o contraseña incorrectos."
                    404 -> "Servidor de autenticación no encontrado."
                    else -> "Error en el servidor: ${e.message()}"
                }
                showError(errorMsg)
            } catch (e: Exception) {
                showError("Error de conexión. Verifica tu internet e intenta de nuevo.")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun loginAsTestUser() {
        val mockUser = User(
            userId = 999,
            username = "usuario_prueba",
            email = "prueba@tienda.com",
            role = "ADMIN"
        )
        AuthManager.getInstance(requireContext()).saveSession(
            "mock_access_token_12345",
            mockUser
        )

        try {
            PushNotificationManager.registerCurrentToken(requireContext())
        } catch (e: Exception) {
            // Ignorar errores de registro de token en desarrollo
        }

        AccessibilityHelper.announce(btnTestLogin, "Inicio de sesión de prueba exitoso. Bienvenido.")

        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showError(message: String) {
        tvErrorMsg.text = message
        tvErrorMsg.visibility = View.VISIBLE
        AccessibilityHelper.announce(tvErrorMsg, message)
    }

    private fun hideError() {
        tvErrorMsg.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun applyAccessibilitySettings(rootView: View) {
        if (settingsManager.highContrastMode) {
            val tvTitle = rootView.findViewById<TextView>(R.id.tvLoginTitle)
            val tvSubtitle = rootView.findViewById<TextView>(R.id.tvLoginSubtitle)
            val lblUsername = rootView.findViewById<TextView>(R.id.lblUsername)
            val lblPassword = rootView.findViewById<TextView>(R.id.lblPassword)
            val tvNoAccount = rootView.findViewById<TextView>(R.id.tvNoAccount)
            val tvRegisterLink = rootView.findViewById<TextView>(R.id.tvRegisterLink)

            tvTitle.setTextColor(rootView.context.getColor(android.R.color.white))
            tvSubtitle.setTextColor(rootView.context.getColor(android.R.color.white))
            lblUsername.setTextColor(rootView.context.getColor(android.R.color.white))
            lblPassword.setTextColor(rootView.context.getColor(android.R.color.white))
            tvNoAccount.setTextColor(rootView.context.getColor(android.R.color.white))
            tvRegisterLink.setTextColor(rootView.context.getColor(R.color.yellow))

            tvTitle.paint.isFakeBoldText = true
            tvSubtitle.paint.isFakeBoldText = true
            lblUsername.paint.isFakeBoldText = true
            lblPassword.paint.isFakeBoldText = true
            tvNoAccount.paint.isFakeBoldText = true
            tvRegisterLink.paint.isFakeBoldText = true
        }
    }
}

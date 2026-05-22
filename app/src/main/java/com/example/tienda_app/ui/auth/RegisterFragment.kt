package com.example.tienda_app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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
import com.example.tienda_app.MainActivity
import com.example.tienda_app.R
import com.example.tienda_app.api.ApiController
import com.example.tienda_app.model.RegisterRequest
import com.example.tienda_app.util.AccessibilityHelper
import com.example.tienda_app.util.AuthManager
import com.example.tienda_app.util.PushNotificationManager
import com.example.tienda_app.util.SettingsManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterFragment : Fragment() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var layoutLoginLink: LinearLayout
    private lateinit var tvErrorMsg: TextView
    private lateinit var layoutLoading: FrameLayout
    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsManager.getInstance(requireContext())

        etUsername = view.findViewById(R.id.etRegisterUsername)
        etEmail = view.findViewById(R.id.etRegisterEmail)
        etPassword = view.findViewById(R.id.etRegisterPassword)
        etConfirmPassword = view.findViewById(R.id.etRegisterConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        layoutLoginLink = view.findViewById(R.id.layoutLoginLink)
        tvErrorMsg = view.findViewById(R.id.tvRegisterErrorMsg)
        layoutLoading = view.findViewById(R.id.layoutRegisterLoading)

        applyAccessibilitySettings(view)

        btnRegister.setOnClickListener {
            attemptRegister()
        }

        layoutLoginLink.setOnClickListener {
            (activity as? AuthActivity)?.showFragment(LoginFragment())
        }

        AccessibilityHelper.announce(view, "Pantalla de registro cargada.")
    }

    private fun attemptRegister() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (username.isEmpty()) {
            showError("Por favor, ingresa un nombre de usuario.")
            etUsername.requestFocus()
            return
        }

        if (email.isEmpty()) {
            showError("Por favor, ingresa tu correo electrónico.")
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("El correo electrónico ingresado no es válido.")
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            showError("Por favor, ingresa una contraseña.")
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.")
            etPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            showError("Por favor, confirma tu contraseña.")
            etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            showError("Las contraseñas no coinciden.")
            etConfirmPassword.requestFocus()
            return
        }

        hideError()
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = RegisterRequest(username = username, email = email, password = password)
                val response = ApiController.api.register(request)

                if (response.data != null) {
                    val authData = response.data
                    AuthManager.getInstance(requireContext()).saveSession(
                        authData.accessToken,
                        authData.user
                    )

                    // Register FCM push token now that we are authenticated
                    PushNotificationManager.registerCurrentToken(requireContext())

                    AccessibilityHelper.announce(btnRegister, "Registro exitoso. Bienvenido.")

                    // Redirect to MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    showError(response.message ?: "Ocurrió un error al registrarse.")
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    400 -> "Datos inválidos o el usuario ya existe."
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
            val tvTitle = rootView.findViewById<TextView>(R.id.tvRegisterTitle)
            val tvSubtitle = rootView.findViewById<TextView>(R.id.tvRegisterSubtitle)
            val lblUsername = rootView.findViewById<TextView>(R.id.lblRegisterUsername)
            val lblEmail = rootView.findViewById<TextView>(R.id.lblRegisterEmail)
            val lblPassword = rootView.findViewById<TextView>(R.id.lblRegisterPassword)
            val lblConfirmPassword = rootView.findViewById<TextView>(R.id.lblRegisterConfirmPassword)
            val tvHasAccount = rootView.findViewById<TextView>(R.id.tvHasAccount)
            val tvLoginLink = rootView.findViewById<TextView>(R.id.tvLoginLink)

            tvTitle.setTextColor(rootView.context.getColor(android.R.color.white))
            tvSubtitle.setTextColor(rootView.context.getColor(android.R.color.white))
            lblUsername.setTextColor(rootView.context.getColor(android.R.color.white))
            lblEmail.setTextColor(rootView.context.getColor(android.R.color.white))
            lblPassword.setTextColor(rootView.context.getColor(android.R.color.white))
            lblConfirmPassword.setTextColor(rootView.context.getColor(android.R.color.white))
            tvHasAccount.setTextColor(rootView.context.getColor(android.R.color.white))
            tvLoginLink.setTextColor(rootView.context.getColor(R.color.yellow))

            tvTitle.paint.isFakeBoldText = true
            tvSubtitle.paint.isFakeBoldText = true
            lblUsername.paint.isFakeBoldText = true
            lblEmail.paint.isFakeBoldText = true
            lblPassword.paint.isFakeBoldText = true
            lblConfirmPassword.paint.isFakeBoldText = true
            tvHasAccount.paint.isFakeBoldText = true
            tvLoginLink.paint.isFakeBoldText = true
        }
    }
}

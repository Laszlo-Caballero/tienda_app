package com.laszlo.tienda_app.ui.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.laszlo.tienda_app.AnalysisResultsFragment
import com.laszlo.tienda_app.R
import com.laszlo.tienda_app.api.ProductRepository
import com.laszlo.tienda_app.util.AccessibilityHelper
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceSearchDialog : DialogFragment() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private lateinit var tvVoiceStatus: TextView
    private lateinit var tvVoiceQuery: TextView
    private lateinit var viewVoicePulse: View
    private lateinit var ivVoiceMic: ImageView
    private lateinit var pbVoiceLoading: ProgressBar
    private lateinit var btnVoiceRetry: Button
    private lateinit var btnVoiceClose: View

    private var onQueryRecognizedListener: ((String) -> Unit)? = null

    fun setOnQueryRecognizedListener(listener: (String) -> Unit) {
        this.onQueryRecognizedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light theme / transparent sheet styling
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_voice_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Views
        tvVoiceStatus = view.findViewById(R.id.tvVoiceStatus)
        tvVoiceQuery = view.findViewById(R.id.tvVoiceQuery)
        viewVoicePulse = view.findViewById(R.id.viewVoicePulse)
        ivVoiceMic = view.findViewById(R.id.ivVoiceMic)
        pbVoiceLoading = view.findViewById(R.id.pbVoiceLoading)
        btnVoiceRetry = view.findViewById(R.id.btnVoiceRetry)
        btnVoiceClose = view.findViewById(R.id.btnVoiceClose)

        btnVoiceClose.setOnClickListener {
            dismiss()
        }

        btnVoiceRetry.setOnClickListener {
            startListeningFlow()
        }

        startListeningFlow()
    }

    private fun startListeningFlow() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            updateStateToError("Permiso de micrófono no concedido")
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            updateStateToError("El reconocimiento de voz no está disponible en este dispositivo")
            return
        }

        // Reset UI states
        tvVoiceStatus.text = "Escuchando..."
        tvVoiceQuery.text = "Di algo..."
        viewVoicePulse.visibility = View.VISIBLE
        pbVoiceLoading.visibility = View.GONE
        ivVoiceMic.visibility = View.VISIBLE
        btnVoiceRetry.visibility = View.GONE
        
        AccessibilityHelper.announce(requireView(), "Escuchando. Hable ahora para buscar.")

        // Start Speech Recognition
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext()).apply {
            setRecognitionListener(SpeechListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        isListening = true
    }

    private fun processVoiceQuery(query: String) {
        isListening = false
        speechRecognizer?.destroy()
        speechRecognizer = null

        // Visual State: Procesando
        tvVoiceStatus.text = "Procesando..."
        tvVoiceQuery.text = "\"$query\""
        viewVoicePulse.visibility = View.GONE
        pbVoiceLoading.visibility = View.VISIBLE
        ivVoiceMic.visibility = View.GONE
        btnVoiceRetry.visibility = View.GONE

        AccessibilityHelper.announce(requireView(), "Texto reconocido: $query. Procesando búsqueda en el servidor.")

        lifecycleScope.launch {
            try {
                // Call voice search API
                val results = ProductRepository.voiceSearch(query)
                if (results.isNotEmpty()) {
                    // Success state
                    AccessibilityHelper.announce(requireView(), "Éxito. Se encontraron ${results.size} productos.")
                    
                    // Route to results list (reusing existing identification results layout)
                    val fragment = AnalysisResultsFragment.newInstance(ArrayList(results))
                    val containerId = (requireView().parent as ViewGroup).id
                    parentFragmentManager.beginTransaction()
                        .replace(containerId, fragment)
                        .addToBackStack(null)
                        .commit()
                    
                    // Call caller listener to fill search box text
                    onQueryRecognizedListener?.invoke(query)
                    dismiss()
                } else {
                    // Success API response, but empty results list. Fallback to setting query in SearchFragment
                    AccessibilityHelper.announce(requireView(), "No se encontraron coincidencias. Volviendo a la pantalla de búsqueda.")
                    onQueryRecognizedListener?.invoke(query)
                    dismiss()
                }
            } catch (e: Exception) {
                updateStateToError("Error en la red: ${e.message}")
            }
        }
    }

    private fun updateStateToError(errorMessage: String) {
        isListening = false
        speechRecognizer?.destroy()
        speechRecognizer = null

        tvVoiceStatus.text = "Error"
        tvVoiceQuery.text = errorMessage
        viewVoicePulse.visibility = View.GONE
        pbVoiceLoading.visibility = View.GONE
        ivVoiceMic.visibility = View.VISIBLE
        btnVoiceRetry.visibility = View.VISIBLE

        AccessibilityHelper.announce(requireView(), "Ocurrió un error. $errorMessage. Presiona Reintentar para volver a hablar.")
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        super.onDestroy()
    }

    inner class SpeechListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {
            isListening = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Pulse wave scaling effect
            if (isListening) {
                val scale = 1.0f + (rmsdB.coerceAtLeast(0f) / 12f)
                viewVoicePulse.scaleX = scale
                viewVoicePulse.scaleY = scale
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            isListening = false
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                SpeechRecognizer.ERROR_CLIENT -> "Error del cliente de voz"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
                SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de red excedido"
                SpeechRecognizer.ERROR_NO_MATCH -> "No se detectó voz. Intenta de nuevo."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Servicio de voz ocupado"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se escuchó nada"
                else -> "Error de reconocimiento"
            }
            updateStateToError(message)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                processVoiceQuery(matches[0])
            } else {
                updateStateToError("No se detectó texto claro")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                tvVoiceQuery.text = matches[0]
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}

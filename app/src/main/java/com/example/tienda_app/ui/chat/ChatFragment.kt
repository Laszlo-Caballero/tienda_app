package com.laszlo.tienda_app.ui.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laszlo.tienda_app.ProductDetailFragment
import com.laszlo.tienda_app.R
import com.laszlo.tienda_app.api.ProductRepository
import com.laszlo.tienda_app.model.ChatMessage
import com.laszlo.tienda_app.util.AccessibilityHelper
import kotlinx.coroutines.launch
import java.util.Locale

class ChatFragment : Fragment() {

    private lateinit var rvChatHistory: RecyclerView
    private lateinit var pbChatLoading: ProgressBar
    private lateinit var etChatMessage: EditText
    private lateinit var btnSendMessage: ImageView
    private lateinit var btnVoiceInput: ImageView
    private lateinit var btnClearChat: TextView

    private lateinit var adapter: ChatAdapter
    private val displayMessages = mutableListOf<ChatAdapter.DisplayMessage>()
    private val apiMessages = mutableListOf<ChatMessage>()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val requestRecordAudioLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toggleVoiceListening()
        } else {
            Toast.makeText(requireContext(), "Permiso de micrófono requerido para voz.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        rvChatHistory = view.findViewById(R.id.rvChatHistory)
        pbChatLoading = view.findViewById(R.id.pbChatLoading)
        etChatMessage = view.findViewById(R.id.etChatMessage)
        btnSendMessage = view.findViewById(R.id.btnSendMessage)
        btnVoiceInput = view.findViewById(R.id.btnVoiceInput)
        btnClearChat = view.findViewById(R.id.btnClearChat)

        // Setup Adapter
        adapter = ChatAdapter(displayMessages) { product ->
            // Navigate to product details
            val detailFragment = ProductDetailFragment.newInstance(product)
            val containerId = (requireView().parent as ViewGroup).id
            parentFragmentManager.beginTransaction()
                .replace(containerId, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        rvChatHistory.layoutManager = LinearLayoutManager(requireContext())
        rvChatHistory.adapter = adapter

        // Welcome message if chat is empty
        if (displayMessages.isEmpty()) {
            addAssistantMessage("¡Hola! Soy tu Asistente DeepSeek. ¿Qué productos estás buscando hoy? Puedes decirme cosas como \"Hola, busco zapatillas nike rojas\" o usar tu voz.")
        }

        // Send Button Click
        btnSendMessage.setOnClickListener {
            sendMessageFlow()
        }

        // Clear Chat Click
        btnClearChat.setOnClickListener {
            displayMessages.clear()
            apiMessages.clear()
            adapter.notifyDataSetChanged()
            addAssistantMessage("Historial limpio. ¿En qué te puedo ayudar ahora?")
            AccessibilityHelper.announce(view, "Historial de conversación borrado.")
        }

        // IME Keyboard Action Send
        etChatMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessageFlow()
                true
            } else {
                false
            }
        }

        // Voice Input Click
        btnVoiceInput.setOnClickListener {
            checkAndLaunchVoiceInput()
        }
    }

    private fun checkAndLaunchVoiceInput() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            toggleVoiceListening()
        } else {
            requestRecordAudioLauncher.launch(permission)
        }
    }

    private fun toggleVoiceListening() {
        if (isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Reconocimiento de voz no disponible.", Toast.LENGTH_SHORT).show()
            return
        }

        isListening = true
        btnVoiceInput.setImageResource(R.drawable.ic_mic)
        btnVoiceInput.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow))
        etChatMessage.setHint("Escuchando...")

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext()).apply {
            setRecognitionListener(SpeechListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        AccessibilityHelper.announce(requireView(), "Escuchando. Habla ahora.")
    }

    private fun stopListening() {
        isListening = false
        btnVoiceInput.setImageResource(R.drawable.ic_mic)
        btnVoiceInput.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
        etChatMessage.setHint("Pregunta sobre algún producto...")
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun sendMessageFlow() {
        val content = etChatMessage.text.toString().trim()
        if (content.isEmpty()) return

        etChatMessage.text.clear()
        hideKeyboard(etChatMessage)

        // Add user message
        addUserMessage(content)

        // Call API
        pbChatLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ProductRepository.chat(apiMessages)
                pbChatLoading.visibility = View.GONE
                if (response.status == "success" || response.status == "error") {
                    addAssistantMessage(response.data.response, response.data.products)
                } else {
                    addAssistantMessage("Lo siento, ocurrió un problema al procesar tu solicitud.")
                }
            } catch (e: Exception) {
                pbChatLoading.visibility = View.GONE
                addAssistantMessage("Ocurrió un error al conectar con el servidor.")
            }
        }
    }

    private fun addUserMessage(content: String) {
        displayMessages.add(ChatAdapter.DisplayMessage(role = "user", content = content))
        apiMessages.add(ChatMessage(role = "user", content = content))
        adapter.notifyItemInserted(displayMessages.size - 1)
        rvChatHistory.scrollToPosition(displayMessages.size - 1)
    }

    private fun addAssistantMessage(content: String, products: List<com.laszlo.tienda_app.model.ProductAnalysis> = emptyList()) {
        displayMessages.add(ChatAdapter.DisplayMessage(role = "assistant", content = content, products = products))
        apiMessages.add(ChatMessage(role = "assistant", content = content))
        adapter.notifyItemInserted(displayMessages.size - 1)
        rvChatHistory.scrollToPosition(displayMessages.size - 1)
        
        val announcement = if (products.isNotEmpty()) {
            "$content. Se muestran ${products.size} productos recomendados."
        } else {
            content
        }
        AccessibilityHelper.announce(requireView(), announcement)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        super.onDestroy()
    }

    inner class SpeechListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            stopListening()
        }
        override fun onError(error: Int) {
            stopListening()
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                etChatMessage.setText(matches[0])
                sendMessageFlow()
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}

package com.example.tienda_app.ui.search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tienda_app.ProductDetailFragment
import com.example.tienda_app.R
import com.example.tienda_app.util.AccessibilityHelper
import com.example.tienda_app.util.SettingsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchAdapter
    private lateinit var settingsManager: SettingsManager

    private lateinit var etSearch: EditText
    private lateinit var btnSearchClear: ImageView
    private lateinit var btnVoiceSearch: ImageView
    private lateinit var tvSearchCounter: TextView
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var pbSearchLoading: ProgressBar
    private lateinit var lytEmptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView

    private var searchJob: Job? = null

    private val requestRecordAudioLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceSearchDialog()
        } else {
            Toast.makeText(requireContext(), "El permiso de micrófono es necesario para buscar por voz.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager.getInstance(requireContext())
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Views
        etSearch = view.findViewById(R.id.etSearch)
        btnSearchClear = view.findViewById(R.id.btnSearchClear)
        btnVoiceSearch = view.findViewById(R.id.btnVoiceSearch)
        tvSearchCounter = view.findViewById(R.id.tvSearchCounter)
        rvSearchResults = view.findViewById(R.id.rvSearchResults)
        pbSearchLoading = view.findViewById(R.id.pbSearchLoading)
        lytEmptyState = view.findViewById(R.id.lytEmptyState)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle)

        btnVoiceSearch.setOnClickListener {
            checkAndLaunchVoiceSearch()
        }

        // Setup RecyclerView
        adapter = SearchAdapter(emptyList()) { product ->
            hideKeyboard(etSearch)
            val detailFragment = ProductDetailFragment.newInstance(product)
            val containerId = (requireView().parent as ViewGroup).id
            parentFragmentManager.beginTransaction()
                .replace(containerId, detailFragment)

                .addToBackStack(null)
                .commit()
        }
        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.adapter = adapter

        // Setup Text Change Debouncer
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                btnSearchClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    viewModel.performSearch(query)
                }
            }
        })

        // Setup Clear Button
        btnSearchClear.setOnClickListener {
            etSearch.text.clear()
            btnSearchClear.visibility = View.GONE
            etSearch.requestFocus()
            showKeyboard(etSearch)
            AccessibilityHelper.announce(btnSearchClear, "Texto de búsqueda borrado")
        }

        // Setup Keyboard IME Action search
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString()
                searchJob?.cancel()
                viewModel.performSearch(query)
                hideKeyboard(etSearch)
                true
            } else {
                false
            }
        }

        // Bind ViewModel states
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchUiState.Idle -> {
                    pbSearchLoading.visibility = View.GONE
                    rvSearchResults.visibility = View.VISIBLE
                    lytEmptyState.visibility = View.GONE
                }
                is SearchUiState.Loading -> {
                    pbSearchLoading.visibility = View.VISIBLE
                    rvSearchResults.visibility = View.GONE
                    lytEmptyState.visibility = View.GONE
                    AccessibilityHelper.announce(view, "Buscando productos, por favor espere.")
                }
                is SearchUiState.Success -> {
                    pbSearchLoading.visibility = View.GONE
                    val products = state.products
                    adapter.updateData(products)
                    
                    val resultsText = "${products.size} productos encontrados"
                    tvSearchCounter.text = resultsText
                    
                    if (products.isEmpty()) {
                        rvSearchResults.visibility = View.GONE
                        lytEmptyState.visibility = View.VISIBLE
                        tvEmptyTitle.text = "No se encontraron productos"
                        tvEmptySubtitle.text = "Intenta buscar con otros términos o códigos de barra."
                        AccessibilityHelper.announce(view, "Búsqueda finalizada. No se encontraron resultados.")
                    } else {
                        rvSearchResults.visibility = View.VISIBLE
                        lytEmptyState.visibility = View.GONE
                        AccessibilityHelper.announce(view, "Búsqueda finalizada. Se encontraron ${products.size} productos.")
                    }
                }
                is SearchUiState.Error -> {
                    pbSearchLoading.visibility = View.GONE
                    rvSearchResults.visibility = View.GONE
                    lytEmptyState.visibility = View.VISIBLE
                    tvEmptyTitle.text = "Error al buscar"
                    tvEmptySubtitle.text = state.message
                    tvSearchCounter.text = "0 productos encontrados"
                    AccessibilityHelper.announce(view, "Error en la búsqueda: ${state.message}")
                }
            }
        }

        // Apply Accessibility Contrast
        applyAccessibilityContrast(view)

        // Perform initial load/search
        viewModel.performSearch("")

        AccessibilityHelper.announce(view, "Buscador cargado. Ingresa un término en el campo de texto para buscar.")
    }

    private fun applyAccessibilityContrast(rootView: View) {
        val labelSearchCounter = rootView.findViewById<TextView>(R.id.tvSearchCounter)
        
        if (settingsManager.highContrastMode) {
            labelSearchCounter.setTextColor(rootView.context.getColor(android.R.color.black))
            labelSearchCounter.paint.isFakeBoldText = true
            etSearch.setTextColor(rootView.context.getColor(android.R.color.black))
            etSearch.setHintTextColor(rootView.context.getColor(android.R.color.black))
            tvEmptyTitle.setTextColor(rootView.context.getColor(android.R.color.black))
            tvEmptyTitle.paint.isFakeBoldText = true
            tvEmptySubtitle.setTextColor(rootView.context.getColor(android.R.color.black))
            tvEmptySubtitle.paint.isFakeBoldText = true
        } else {
            labelSearchCounter.setTextColor(rootView.context.getColor(R.color.gray))
            labelSearchCounter.paint.isFakeBoldText = false
            etSearch.setTextColor(rootView.context.getColor(R.color.black))
            etSearch.setHintTextColor(rootView.context.getColor(R.color.gray))
            tvEmptyTitle.setTextColor(rootView.context.getColor(R.color.gray))
            tvEmptyTitle.paint.isFakeBoldText = false
            tvEmptySubtitle.setTextColor(rootView.context.getColor(R.color.gray))
            tvEmptySubtitle.paint.isFakeBoldText = false
        }
        
        labelSearchCounter.invalidate()
        etSearch.invalidate()
        tvEmptyTitle.invalidate()
        tvEmptySubtitle.invalidate()
    }

    private fun checkAndLaunchVoiceSearch() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            showVoiceSearchDialog()
        } else {
            requestRecordAudioLauncher.launch(permission)
        }
    }

    private fun showVoiceSearchDialog() {
        val dialog = VoiceSearchDialog()
        dialog.setOnQueryRecognizedListener { query ->
            etSearch.setText(query)
            btnSearchClear.visibility = View.VISIBLE
            viewModel.performSearch(query)
        }
        dialog.show(childFragmentManager, "VoiceSearchDialog")
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}


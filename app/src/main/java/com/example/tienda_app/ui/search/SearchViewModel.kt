package com.example.tienda_app.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda_app.api.ProductRepository
import com.example.tienda_app.model.ProductAnalysis
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val products: List<ProductAnalysis>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel : ViewModel() {

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.Idle)
    val uiState: LiveData<SearchUiState> = _uiState

    /**
     * Executes product search matching the given query.
     * Hits repository which tries history API first, then falls back to local data.
     */
    fun performSearch(query: String) {
        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            try {
                val results = ProductRepository.getProducts(query)
                _uiState.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Error al buscar productos.")
            }
        }
    }
}

package com.laszlo.tienda_app.config

import com.google.android.gms.maps.model.LatLng

object MapConfig {
    // Default Map center: Lima, Peru
    val DEFAULT_CENTER = LatLng(-12.046374, -77.042793)
    const val DEFAULT_ZOOM = 14.0f
    
    data class StoreLocation(
        val name: String,
        val address: String,
        val latLng: LatLng,
        val sellerName: String
    )

    // Predefined locations of stores (physical endpoints) based on "vendido_por" / seller info
    val MOCK_STORES = listOf(
        StoreLocation(
            name = "Saga Falabella - Centro de Lima",
            address = "Jirón de la Unión 610, Lima",
            latLng = LatLng(-12.047113, -77.032223),
            sellerName = "Saga Falabella"
        ),
        StoreLocation(
            name = "Ripley - Jirón de la Unión",
            address = "Jirón de la Unión 701, Lima",
            latLng = LatLng(-12.048560, -77.032990),
            sellerName = "Ripley"
        ),
        StoreLocation(
            name = "Plaza Vea - Wilson",
            address = "Av. Garcilaso de la Vega 1337, Lima",
            latLng = LatLng(-12.056080, -77.036980),
            sellerName = "Plaza Vea"
        ),
        StoreLocation(
            name = "Tottus - Tacna",
            address = "Av. Tacna 665, Lima",
            latLng = LatLng(-12.043320, -77.038440),
            sellerName = "Tottus"
        ),
        StoreLocation(
            name = "Saga Falabella - Las Begonias",
            address = "Av. Begonias 550, San Isidro, Lima",
            latLng = LatLng(-12.091120, -77.025340),
            sellerName = "Saga Falabella"
        ),
        StoreLocation(
            name = "Mercado Libre - Lima Hub",
            address = "Av. República de Panamá 3420, San Isidro",
            latLng = LatLng(-12.095450, -77.022980),
            sellerName = "Mercado Libre"
        )
    )

    /**
     * Gets nearby stores for a specific seller.
     * Falls back to general stores if the seller is not explicitly matched.
     */
    fun getStoresForSeller(seller: String): List<StoreLocation> {
        val filtered = MOCK_STORES.filter { it.sellerName.equals(seller, ignoreCase = true) }
        return filtered.ifEmpty { MOCK_STORES }
    }
}

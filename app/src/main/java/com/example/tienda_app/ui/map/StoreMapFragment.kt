package com.laszlo.tienda_app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.laszlo.tienda_app.R
import com.laszlo.tienda_app.config.MapConfig
import com.laszlo.tienda_app.util.AccessibilityHelper
import com.laszlo.tienda_app.util.SettingsManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions

class StoreMapFragment : Fragment(), OnMapReadyCallback {

    private var sellerName: String = ""
    private var googleMap: GoogleMap? = null
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager.getInstance(requireContext())
        arguments?.let {
            sellerName = it.getString(ARG_SELLER_NAME, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store_map, container, false)

        view.findViewById<ImageView>(R.id.btnMapBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val tvMapSellerName = view.findViewById<TextView>(R.id.tvMapSellerName)
        tvMapSellerName.text = if (sellerName.isNotEmpty()) {
            "Tiendas de $sellerName"
        } else {
            "Todas las Tiendas"
        }

        // Apply Accessibility contrast settings
        val tvMapSubtitle = view.findViewById<TextView>(R.id.tvMapSubtitle)
        if (settingsManager.highContrastMode) {
            tvMapSellerName.setTextColor(requireContext().getColor(android.R.color.black))
            tvMapSellerName.paint.isFakeBoldText = true
            tvMapSubtitle.setTextColor(requireContext().getColor(android.R.color.black))
            tvMapSubtitle.paint.isFakeBoldText = true
        }

        // Initialize Map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val announcement = if (sellerName.isNotEmpty()) {
            "Mapa cargado. Mostrando tiendas de $sellerName"
        } else {
            "Mapa cargado. Mostrando todas las tiendas físicas disponibles"
        }
        AccessibilityHelper.announce(view, announcement)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configure map options
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false

        // Fetch stores
        val stores = MapConfig.getStoresForSeller(sellerName)

        // Clear existing markers if any
        map.clear()

        // Place markers
        if (stores.isNotEmpty()) {
            stores.forEach { store ->
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(store.latLng)
                        .title(store.name)
                        .snippet(store.address)
                )
                marker?.tag = store
            }

            // Move camera to the first store
            val targetLocation = stores.first().latLng
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, MapConfig.DEFAULT_ZOOM))
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(MapConfig.DEFAULT_CENTER, MapConfig.DEFAULT_ZOOM))
        }
    }

    companion object {
        private const val ARG_SELLER_NAME = "seller_name"

        @JvmStatic
        fun newInstance(sellerName: String) =
            StoreMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SELLER_NAME, sellerName)
                }
            }
    }
}

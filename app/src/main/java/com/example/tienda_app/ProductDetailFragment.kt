package com.laszlo.tienda_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.laszlo.tienda_app.model.ProductAnalysis
import com.laszlo.tienda_app.util.SettingsManager
import com.laszlo.tienda_app.util.AccessibilityHelper
import com.laszlo.tienda_app.ui.map.StoreMapFragment

class ProductDetailFragment : Fragment() {

    private var product: ProductAnalysis? = null
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager.getInstance(requireContext())
        arguments?.let {
            product = it.getSerializable("product") as? ProductAnalysis
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)
        
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        product?.let { p ->
            val tvDetailName = view.findViewById<TextView>(R.id.tvDetailName)
            val tvDetailBrand = view.findViewById<TextView>(R.id.tvDetailBrand)
            val tvDetailSeller = view.findViewById<TextView>(R.id.tvDetailSeller)
            val tvDetailCategory = view.findViewById<TextView>(R.id.tvDetailCategory)
            val tvDetailPrice = view.findViewById<TextView>(R.id.tvDetailPrice)
            val tvDetailFeatures = view.findViewById<TextView>(R.id.tvDetailFeatures)
            val tvDetailSpecs = view.findViewById<TextView>(R.id.tvDetailSpecs)

            tvDetailName.text = p.nombre
            tvDetailBrand.text = p.marca
            tvDetailSeller.text = p.vendido_por
            tvDetailCategory.text = "${p.categoria} > ${p.sub_categoria}"
            
            val pricesText = if (p.precios.isNotEmpty()) {
                p.precios.joinToString(" - ") { "S/ $it" }
            } else {
                "Precio no disponible"
            }
            tvDetailPrice.text = pricesText
            
            tvDetailFeatures.text = p.caracteristicas.joinToString("\n") { "• $it" }
            tvDetailSpecs.text = p.especificaciones.joinToString("\n") { "• $it" }

            val ivDetailImage = view.findViewById<ImageView>(R.id.ivDetailImage)
            if (p.imagenes.isNotEmpty()) {
                val urlPath = p.imagenes[0].url.replace("\\", "/")
                val imageUrl = "${Constants.API_BASE_URL}$urlPath"
                Glide.with(this)
                    .load(imageUrl)
                    .into(ivDetailImage)
            }

            // High contrast adjustments
            if (settingsManager.highContrastMode) {
                tvDetailName.setTextColor(requireContext().getColor(android.R.color.black))
                tvDetailName.paint.isFakeBoldText = true
                tvDetailSeller.setTextColor(requireContext().getColor(android.R.color.black))
                tvDetailSeller.paint.isFakeBoldText = true
                tvDetailCategory.setTextColor(requireContext().getColor(android.R.color.black))
                tvDetailCategory.paint.isFakeBoldText = true
                tvDetailPrice.setTextColor(requireContext().getColor(android.R.color.black))
                tvDetailPrice.paint.isFakeBoldText = true
                tvDetailFeatures.setTextColor(requireContext().getColor(android.R.color.black))
                tvDetailSpecs.setTextColor(requireContext().getColor(android.R.color.black))
            }

            // Setup map button click
            view.findViewById<View>(R.id.btnViewStores).setOnClickListener {
                val mapFragment = StoreMapFragment.newInstance(p.vendido_por)
                val containerId = (requireView().parent as ViewGroup).id
                parentFragmentManager.beginTransaction()
                    .replace(containerId, mapFragment)
                    .addToBackStack(null)
                    .commit()
            }

            // Accessibility announcement on details loaded
            AccessibilityHelper.announce(view, "Detalle del producto cargado: ${p.nombre}. Vendido por ${p.vendido_por}. Precio: $pricesText")
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(product: ProductAnalysis) =
            ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("product", product)
                }
            }
    }
}

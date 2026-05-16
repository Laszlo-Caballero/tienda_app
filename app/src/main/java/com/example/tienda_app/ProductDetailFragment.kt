package com.example.tienda_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tienda_app.model.ProductAnalysis

class ProductDetailFragment : Fragment() {

    private var product: ProductAnalysis? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            view.findViewById<TextView>(R.id.tvDetailName).text = p.nombre
            view.findViewById<TextView>(R.id.tvDetailBrand).text = p.marca
            view.findViewById<TextView>(R.id.tvDetailSeller).text = p.vendido_por
            view.findViewById<TextView>(R.id.tvDetailCategory).text = "${p.categoria} > ${p.sub_categoria}"
            
            val pricesText = if (p.precios.isNotEmpty()) {
                p.precios.joinToString(" - ") { "S/ $it" }
            } else {
                "Precio no disponible"
            }
            view.findViewById<TextView>(R.id.tvDetailPrice).text = pricesText
            
            view.findViewById<TextView>(R.id.tvDetailFeatures).text = p.caracteristicas.joinToString("\n") { "• $it" }
            view.findViewById<TextView>(R.id.tvDetailSpecs).text = p.especificaciones.joinToString("\n") { "• $it" }

            val ivDetailImage = view.findViewById<ImageView>(R.id.ivDetailImage)
            if (p.imagenes.isNotEmpty()) {
                val urlPath = p.imagenes[0].url.replace("\\", "/")
                val imageUrl = "${Constants.API_BASE_URL}$urlPath"
                Glide.with(this)
                    .load(imageUrl)
                    .into(ivDetailImage)
            }
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

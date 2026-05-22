package com.example.tienda_app.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tienda_app.Constants
import com.example.tienda_app.R
import com.example.tienda_app.model.ProductAnalysis
import com.example.tienda_app.util.SettingsManager

class SearchAdapter(
    private var products: List<ProductAnalysis>,
    private val onItemClick: (ProductAnalysis) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductCategory: TextView = view.findViewById(R.id.tvProductCategory)
        val tvSimilitud: TextView = view.findViewById(R.id.tvSimilitud)
        val ivProductIcon: ImageView = view.findViewById(R.id.ivProductIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_analysis_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context
        val settings = SettingsManager.getInstance(context)

        holder.tvProductName.text = product.nombre
        holder.tvProductCategory.text = "${product.marca} • S/ ${product.precios.firstOrNull() ?: 0.0}"
        
        holder.tvSimilitud.text = product.vendido_por
        holder.tvSimilitud.setBackgroundResource(R.drawable.bg_badge_light)

        if (product.imagenes.isNotEmpty()) {
            val urlPath = product.imagenes[0].url.replace("\\", "/")
            val imageUrl = if (urlPath.startsWith("http")) {
                urlPath
            } else {
                "${Constants.API_BASE_URL}$urlPath"
            }
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_laptop)
                .into(holder.ivProductIcon)
        } else {
            holder.ivProductIcon.setImageResource(R.drawable.ic_laptop)
        }

        // Accessibility content description for screen readers (Announces full card context cleanly)
        val pricesText = product.precios.joinToString(" o ") { "$it soles" }
        holder.itemView.contentDescription = "Producto: ${product.nombre}, Marca: ${product.marca}, Precio: $pricesText, Vendido por: ${product.vendido_por}. Toca dos veces para ver detalles y ubicación en mapa."

        // High contrast overrides for low-vision users
        if (settings.highContrastMode) {
            holder.tvProductName.setTextColor(context.getColor(android.R.color.black))
            holder.tvProductName.paint.isFakeBoldText = true
            holder.tvProductCategory.setTextColor(context.getColor(android.R.color.black))
            holder.tvProductCategory.paint.isFakeBoldText = true
            holder.tvSimilitud.setTextColor(context.getColor(android.R.color.black))
        } else {
            holder.tvProductName.setTextColor(context.getColor(R.color.black))
            holder.tvProductName.paint.isFakeBoldText = false
            holder.tvProductCategory.setTextColor(context.getColor(R.color.gray))
            holder.tvProductCategory.paint.isFakeBoldText = false
        }

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<ProductAnalysis>) {
        products = newProducts
        notifyDataSetChanged()
    }
}

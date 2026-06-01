package com.laszlo.tienda_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.laszlo.tienda_app.model.ProductAnalysis
import kotlin.math.roundToInt

class AnalysisResultAdapter(
    private val results: List<ProductAnalysis>,
    private val onItemClick: (ProductAnalysis) -> Unit
) : RecyclerView.Adapter<AnalysisResultAdapter.ViewHolder>() {

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
        val product = results[position]
        holder.tvProductName.text = product.nombre
        holder.tvProductCategory.text = product.sub_categoria ?: product.categoria
        
        val similitudPorcentaje = product.similitud.roundToInt()
        holder.tvSimilitud.text = "${similitudPorcentaje}%\nConfianza"

        if (product.imagenes.isNotEmpty()) {
            val urlPath = product.imagenes[0].url.replace("\\", "/")
            val imageUrl = "${Constants.API_BASE_URL}$urlPath"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.ivProductIcon)
        }

        // Accessibility content description
        holder.itemView.contentDescription = "Producto: ${product.nombre}, Categoría: ${product.sub_categoria ?: product.categoria}, Confianza del ${similitudPorcentaje} por ciento. Toca para ver más detalles."
        
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount() = results.size
}

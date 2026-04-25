package com.example.tienda_app.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tienda_app.R
import com.example.tienda_app.model.History

class HistoryAdapter(
    private var history: List<History>,
): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.image)
        val category = view.findViewById<TextView>(R.id.category)
        val title = view.findViewById<TextView>(R.id.title)
        val time = view.findViewById<TextView>(R.id.time)
        val description = view.findViewById<TextView>(R.id.description)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historySingle = history[position]

        holder.title.text = historySingle.title
        holder.time.text = historySingle.time
        holder.description.text = historySingle.description
        holder.category.text = historySingle.category

        Glide.with(holder.image.context)
            .load(historySingle.image)
            .into(holder.image)
    }

    override fun getItemCount(): Int = history.size

    fun updateData(newItems: List<History>){
        history = newItems
        notifyDataSetChanged()
    }
}
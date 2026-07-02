package com.laszlo.tienda_app.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laszlo.tienda_app.AnalysisResultAdapter
import com.laszlo.tienda_app.R
import com.laszlo.tienda_app.model.ChatMessage
import com.laszlo.tienda_app.model.ProductAnalysis

class ChatAdapter(
    private var messages: List<DisplayMessage>,
    private val onProductClick: (ProductAnalysis) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_ASSISTANT = 2
    }

    data class DisplayMessage(
        val role: String,
        val content: String,
        val products: List<ProductAnalysis> = emptyList()
    )

    fun updateData(newMessages: List<DisplayMessage>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") TYPE_USER else TYPE_ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_message_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_message_assistant, parent, false)
            AssistantViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.tvMessageUser.text = message.content
        } else if (holder is AssistantViewHolder) {
            holder.tvMessageAssistant.text = message.content
            
            if (message.products.isNotEmpty()) {
                holder.rvRecommendedProducts.visibility = View.VISIBLE
                holder.rvRecommendedProducts.layoutManager = LinearLayoutManager(holder.itemView.context)
                holder.rvRecommendedProducts.adapter = AnalysisResultAdapter(message.products) { product ->
                    onProductClick(product)
                }
            } else {
                holder.rvRecommendedProducts.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessageUser: TextView = view.findViewById(R.id.tvMessageUser)
    }

    class AssistantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessageAssistant: TextView = view.findViewById(R.id.tvMessageAssistant)
        val rvRecommendedProducts: RecyclerView = view.findViewById(R.id.rvRecommendedProducts)
    }
}

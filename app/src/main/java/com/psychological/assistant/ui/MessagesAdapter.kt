package com.psychological.assistant.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psychological.assistant.data.model.ChatMessage
import com.psychological.assistant.databinding.ItemMessageBinding

class MessagesAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }
    
    override fun getItemCount(): Int = messages.size
    
    class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.content
            
            val card = binding.cardMessage
            val layoutParams = card.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            
            if (message.isUser) {
                // Сообщения пользователя - справа, синий фон
                card.setCardBackgroundColor(binding.root.context.getColor(com.psychological.assistant.R.color.message_sent))
                binding.tvMessage.setTextColor(binding.root.context.getColor(com.psychological.assistant.R.color.message_sent_text))
                card.cardElevation = 3f
                layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.startToStart = -1
                layoutParams.horizontalBias = 1.0f
                layoutParams.marginEnd = 8
                layoutParams.marginStart = 80
                binding.tvMessage.gravity = android.view.Gravity.END
            } else {
                // Сообщения AI - слева, белый фон
                card.setCardBackgroundColor(binding.root.context.getColor(com.psychological.assistant.R.color.message_received))
                binding.tvMessage.setTextColor(binding.root.context.getColor(com.psychological.assistant.R.color.message_received_text))
                card.cardElevation = 1f
                layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = -1
                layoutParams.horizontalBias = 0.0f
                layoutParams.marginStart = 8
                layoutParams.marginEnd = 80
                binding.tvMessage.gravity = android.view.Gravity.START
            }
            
            card.layoutParams = layoutParams
        }
    }
}
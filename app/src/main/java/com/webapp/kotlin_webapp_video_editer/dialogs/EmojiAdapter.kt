package com.webapp.kotlin_webapp_video_editer.dialogs

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EmojiAdapter(private val onEmojiClicked: (String) -> Unit) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    private val emojis = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun setEmojis(emojiUrls: List<String>) {
        emojis.clear()
        emojis.addAll(emojiUrls)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(100, 100) // Set appropriate size
        }
        return EmojiViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emojiUrl = emojis[position]
        Glide.with(holder.itemView.context)
            .load(emojiUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onEmojiClicked(emojiUrl)
        }
    }

    override fun getItemCount() = emojis.size

    class EmojiViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}

package com.webapp.kotlin_webapp_video_editer.dialogs

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FaceAdapter(private val onFaceClicked: (String) -> Unit) : RecyclerView.Adapter<FaceAdapter.FaceViewHolder>() {

    private val faces = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun setFaces(faceNames: List<String>) {
        faces.clear()
        faces.addAll(faceNames)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(100, 100) // Set appropriate size
        }
        return FaceViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: FaceViewHolder, position: Int) {
        val faceName = faces[position]
        val faceUrl = "https://samrt-loader.com/faces/${faceName}"
        Glide.with(holder.itemView.context)
            .load(faceUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onFaceClicked(faceName)
        }
    }

    override fun getItemCount() = faces.size

    class FaceViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}

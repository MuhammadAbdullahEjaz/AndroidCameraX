package com.example.camera

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImagesRvAdapter(private val context:Context):RecyclerView.Adapter<ImagesRvAdapter.Image>() {
    private var imageData:List<Uri> = emptyList()

    inner class Image(view: View): RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.image)

        fun bind(imageSrc: Uri, position: Int){
            Glide.with(context).load(imageSrc).into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesRvAdapter.Image {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.images_rv_item, parent, false)
        return Image(view)
    }

    override fun onBindViewHolder(holder: ImagesRvAdapter.Image, position: Int) {
        val image = imageData[position]
        holder.bind(image, position)

    }

    override fun getItemCount(): Int {
        return imageData.size
    }

    fun updateData(data:List<Uri>?){
        imageData = data ?: emptyList()
        notifyDataSetChanged()
    }
}
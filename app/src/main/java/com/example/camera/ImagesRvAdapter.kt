package com.example.camera

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class ImagesRvAdapter(
    private val context: Context,
    val onImageAdd: (Uri) -> Unit,
    val onImageRemove: (Uri) -> Unit
) : RecyclerView.Adapter<ImagesRvAdapter.Image>() {
    private var imageData: List<Uri> = emptyList()
    private var selectedImages: List<Uri> = emptyList()
    val selected: List<Uri> get() = selectedImages

    inner class Image(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.image)
        private val imageCard: MaterialCardView = view.findViewById(R.id.imageCard)

        fun bind(imageSrc: Uri, position: Int) {
            Glide.with(context).load(imageSrc).into(imageView)

            if (imageSrc in selectedImages) {
                imageCard.strokeColor = Color.parseColor("#0000FF")
                imageCard.strokeWidth = 16
            } else {
                imageCard.strokeWidth = 0
            }

            itemView.setOnClickListener {
                Log.d("fetch", "selectedImages: $selectedImages")

                if (imageSrc in selectedImages) {

                    onImageRemove(imageSrc)
                    imageCard.strokeWidth = 0
                } else {
                    if (selectedImages.size < 10) {
                        onImageAdd(imageSrc)
                        imageCard.strokeColor = Color.parseColor("#0000FF")
                        imageCard.strokeWidth = 16
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesRvAdapter.Image {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.images_rv_item, parent, false)
        return Image(view)
    }

    override fun onBindViewHolder(holder: ImagesRvAdapter.Image, position: Int) {
        val image = imageData[position]
        holder.bind(image, position)
    }

    override fun getItemCount(): Int {
        return imageData.size
    }

    fun updateData(data: List<Uri>?) {
        imageData = data ?: emptyList()
        notifyDataSetChanged()
    }

    fun updateSelectedImages(data: List<Uri>?) {
        selectedImages = data ?: emptyList()
    }

}
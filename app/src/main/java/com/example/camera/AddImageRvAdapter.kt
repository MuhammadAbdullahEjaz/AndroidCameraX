package com.example.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AddImageRvAdapter(private val context:Context):RecyclerView.Adapter<AddImageRvAdapter.ItemCardView>() {

    private var imageData:List<Uri> = emptyList()

    inner class ItemCardView(view:View):RecyclerView.ViewHolder(view){
        val imageView:ImageView = view.findViewById(R.id.addImageIV)
        val cardView:MaterialCardView = view.findViewById(R.id.materialCardMC)
        val cancalButton:MaterialButton = view.findViewById(R.id.addImageIVB)

        fun bind(imageSrc:Uri, position: Int){
            if(position == 0){
                cardView.strokeWidth = 4
            }else{
                cardView.strokeWidth = 1
            }
            Glide.with(context).load(imageSrc).into(imageView)
            cancalButton.setOnClickListener {
                Log.d("cross", "cancelled clicked")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemCardView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_image_rv_item, parent, false)
        return ItemCardView(view)
    }

    override fun onBindViewHolder(holder: ItemCardView, position: Int) {
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
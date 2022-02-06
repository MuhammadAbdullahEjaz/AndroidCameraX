package com.example.camera

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.camera.databinding.FragmentImagePickerBinding


class ImagePicker : Fragment() {
    private val TAG = "imagePicker"
    private lateinit var viewBinding:FragmentImagePickerBinding
    private val viewModel:MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentImagePickerBinding.inflate(layoutInflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.imagesRV.layoutManager = GridLayoutManager(requireContext(), 3)
        val adapter = ImagesRvAdapter(requireContext())
        viewBinding.imagesRV.adapter = adapter
        adapter.updateData(getImagesFromGallery())
    }


    private fun getImagesFromGallery():List<Uri>{

        val images: MutableList<Uri> = mutableListOf()

        val query = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
            ),
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )
        query?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
            val displayNameColumn =
                it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
            Log.d("fetch", "dataCount: ${it.count}")
            while (it.moveToNext()) {

                val id = it.getLong(idColumn)
                val displayName = it.getString(displayNameColumn)
                val contentUris =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                images.add(contentUris)
                Log.d("fetch", "Images: $id, $displayName, $contentUris")
            }
            it.close()
        }
        return  images
    }
}
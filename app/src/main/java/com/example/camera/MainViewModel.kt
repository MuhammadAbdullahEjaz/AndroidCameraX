package com.example.camera

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.max

class MainViewModel : ViewModel() {
    val maxNumberOfImages = 10
    private val _images: MutableLiveData<List<Uri>> = MutableLiveData(emptyList())
    val images: LiveData<List<Uri>> get() = _images


    fun updateImages(selectedImages: List<Uri>) {
        val numberOfImages = _images.value!!.size
        val numberOfImagesToBeAdded = maxNumberOfImages + 1 - numberOfImages - selectedImages.size
        if (numberOfImagesToBeAdded >= 0) {
            _images.postValue(
                _images.value?.plus(selectedImages.take(numberOfImagesToBeAdded))?.toSet()?.toList()
            )
        }
    }

    fun removeImage(selectedImage: Uri) {
        _images.postValue(_images.value!!.filter { it != selectedImage })
    }

    fun setIntersection(selectedImage: List<Uri>) {
        val intersection = selectedImage.toSet().intersect(_images.value!!)
        _images.postValue(intersection.toList())
    }

}
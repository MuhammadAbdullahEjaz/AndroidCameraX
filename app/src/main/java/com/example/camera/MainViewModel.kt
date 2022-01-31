package com.example.camera

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {
    private val _images:MutableLiveData<List<Uri>> = MutableLiveData(emptyList())
    val images:LiveData<List<Uri>> get() = _images

    fun updateImages(images:List<Uri>){
        var list = _images.value
        list = list?.plus(images)
        _images.postValue(list)
    }

}
package com.example.nmixer.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nmixer.models.Music
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class HomeViewModel : ViewModel() {

    private val _musics = MutableLiveData<MutableList<Music>>().apply {

        doAsync {
            Music.getMyMusics {
                it?.let {
                    value = it
                }

                if (it == null)
                value = null

                uiThread {
                    value
                }
            }
        }
    }

    val musics : LiveData<MutableList<Music>>? = _musics
}
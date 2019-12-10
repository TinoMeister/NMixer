package com.example.nmixer.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nmixer.models.Music
import com.example.nmixer.models.Share
import com.example.nmixer.models.User
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SearchViewModel : ViewModel() {

    private val _shares = MutableLiveData<MutableList<Share>>().apply {

        doAsync {
            Share.getShares{
                it?.let{
                    value = it
                }

                if (it == null)
                    value = null
            }

            uiThread {
                value
            }
        }
    }

    val shares : LiveData<MutableList<Share>>? = _shares

    private val _musics = MutableLiveData<MutableList<Music>>().apply {

        doAsync {
            Music.getAllMusics {
                it?.let{
                    value = it
                }

                if (it == null)
                    value = null
            }

            uiThread {
                value
            }
        }
    }

    val musics : LiveData<MutableList<Music>>? = _musics

    private val _users = MutableLiveData<MutableList<User>>().apply {

        doAsync {
            User.getUsers {
                it?.let{
                    value = it
                }

                if (it == null)
                    value = null
            }

            uiThread {
                value
            }
        }
    }

    val users : LiveData<MutableList<User>>? = _users
}
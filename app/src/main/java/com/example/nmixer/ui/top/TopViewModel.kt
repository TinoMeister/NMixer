package com.example.nmixer.ui.top

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nmixer.models.Favorite
import com.example.nmixer.models.Music
import com.example.nmixer.models.User
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class TopViewModel : ViewModel() {

    private val _favorites = MutableLiveData<MutableList<Favorite>>().apply {

        doAsync {
            Favorite.getAllFavorites("idMusic") {
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

    val favorites : LiveData<MutableList<Favorite>>? = _favorites

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
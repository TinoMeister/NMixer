package com.example.nmixer.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nmixer.models.Music
import com.example.nmixer.models.Share
import com.example.nmixer.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.ArrayList

class SearchViewModel : ViewModel() {
    var auth : FirebaseAuth? = null

    private val _shares = MutableLiveData<MutableList<Share>>().apply {

        val sharesList : MutableList<Share> = ArrayList()

        doAsync {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Shares")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    sharesList.clear()
                    if (data.exists()){
                        for (d in data.children){
                            val share = Share(d)
                            sharesList.add(share)
                        }
                    }

                    uiThread {
                        value = if (sharesList.size > 0)
                            sharesList
                        else
                            null
                    }
                }

                override fun onCancelled(erro: DatabaseError) {
                    Log.d("Error / Share", erro.message)
                }
            })
        }
    }

    val shares : LiveData<MutableList<Share>>? = _shares

    private val _musics = MutableLiveData<MutableList<Music>>().apply {

        val musicsList : MutableList<Music> = ArrayList()

        doAsync {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Musics")
                .orderByChild("title")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    musicsList.clear()
                    if (data.exists()){
                        for (d in data.children){
                            val music = Music(d)
                            musicsList.add(music)
                        }
                    }

                    uiThread {
                        value = if (musicsList.size > 0)
                            musicsList
                        else
                            null
                    }
                }

                override fun onCancelled(erro: DatabaseError) {
                    Log.d("Error / Music", erro.message)
                }
            })
        }
    }

    val musics : LiveData<MutableList<Music>>? = _musics

    private val _users = MutableLiveData<MutableList<User>>().apply {

        val usersList : MutableList<User> = ArrayList()

        doAsync {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Users")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    usersList.clear()
                    if (data.exists()){
                        for (d in data.children){
                            val user = User(d)
                            usersList.add(user)
                        }
                    }

                    uiThread {
                        value = if (usersList.size > 0)
                            usersList
                        else
                            null
                    }
                }

                override fun onCancelled(erro: DatabaseError) {
                }
            })
        }
    }

    val users : LiveData<MutableList<User>>? = _users
}
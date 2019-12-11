package com.example.nmixer.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nmixer.models.Music
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.ArrayList

class HomeViewModel : ViewModel() {
    var auth: FirebaseAuth? = null

    private val _musics = MutableLiveData<MutableList<Music>>().apply {
        val musicsList = ArrayList<Music>()

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
                            if (d.child("idUser").value.toString() == auth?.uid.toString()){
                                val music = Music(d)
                                musicsList.add(music)
                            }
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


}
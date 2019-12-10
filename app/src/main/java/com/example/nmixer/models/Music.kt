package com.example.nmixer.models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.util.*

class Music {

    var id          : String? = null
    var title       : String? = null
    var date        : String? = null
    var time        : String? = null
    var idUser      : String? = null
    var user        : User?   = null

    constructor(id : String?, title : String?, date : String?, time : String?, idUser : String?) {
        this.id        = id
        this.title     = title
        this.date      = date
        this.time      = time
        this.idUser    = idUser
    }

    constructor(id : String?, title : String?, date : String?, time : String?, user: User?) {
        this.id        = id
        this.title     = title
        this.date      = date
        this.time      = time
        this.user      = user
    }

    constructor(data : String){
        val jsonObject = JSONObject(data)
        id          = jsonObject.getString("id"        )
        title       = jsonObject.getString("title"     )
        date        = jsonObject.getString("date"      )
        time        = jsonObject.getString("time"      )
        idUser      = jsonObject.getString("idUser"    )
    }

    constructor(data : DataSnapshot){
        id          = data.key
        title       = data.child("title"     ).value.toString()
        date        = data.child("date"      ).value.toString()
        time        = data.child("time"      ).value.toString()
        idUser      = data.child("idUser"    ).value.toString()
    }

    fun toJson () : JSONObject{
        val jsonObject = JSONObject()

        jsonObject.put("id"        , id        )
        jsonObject.put("title"     , title     )
        jsonObject.put("date"      , date      )
        jsonObject.put("type"      , time      )
        jsonObject.put("idUser"    , idUser    )

        return jsonObject
    }


    companion object {
        var auth: FirebaseAuth? = null

        fun getAllMusics(callback: (MutableList<Music>?) -> Unit ){
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

                        if (musicsList.size > 0)
                            uiThread {
                                callback(musicsList)
                            }
                        else
                            uiThread {
                                callback(null)
                            }
                    }

                    override fun onCancelled(erro: DatabaseError) {
                        Log.d("Error / Music", erro.message)
                    }
                })
            }
        }

        fun getMyMusics(callback: (MutableList<Music>?) -> Unit ){
            val musicsList : MutableList<Music> = ArrayList()

            doAsync {
                auth = FirebaseAuth.getInstance()
                getAllMusics {
                    musicsList.clear()
                    it?.let{
                        for (music in it){
                            if (music.idUser == auth?.uid.toString())
                                musicsList.add(music)
                        }
                    }

                    if (musicsList.size > 0)
                        uiThread {
                            callback(musicsList)
                        }
                    else
                        uiThread {
                            callback(null)
                        }
                }
            }
        }

        fun insertMusic(title : String?, date : String?, time : String?) {
            auth = FirebaseAuth.getInstance()

            val idMusic = UUID.randomUUID().toString()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Musics")
                .child(idMusic)

            val music = Music (
                idMusic,
                title ,
                date,
                time,
                auth?.uid.toString()
            )

            myRef.setValue(music)
        }

        fun updateMusicFirebase(music: Music?) {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Musics")

            myRef.setValue(music)
        }

        fun deleteMusicFirebase(music: Music?) {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            database.getReference("Musics")
                .child(music?.id.toString())
                .removeValue()
        }
    }
}
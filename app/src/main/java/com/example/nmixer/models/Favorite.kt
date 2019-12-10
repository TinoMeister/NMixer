package com.example.nmixer.models

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Favorite {

    var id      : String? = null
    var date    : String? = null
    var idUser  : String? = null
    var idMusic : String? = null
    var total   : String? = null

    constructor(id : String?, date : String?, idUser : String?, idMusic : String?) {
        this.id       = id
        this.date     = date
        this.idUser   = idUser
        this.idMusic  = idMusic
    }

    constructor(id : String?, date : String?, idUser : String?, idMusic : String?, total : String?) {
        this.id       = id
        this.date     = date
        this.idUser   = idUser
        this.idMusic  = idMusic
        this.total    = total
    }

    constructor(data : String){
        val jsonObject = JSONObject(data)

        this.id         = jsonObject.getString("id"      )
        this.date       = jsonObject.getString("date"    )
        this.idUser     = jsonObject.getString("idUser"  )
        this.idMusic    = jsonObject.getString("idMusic" )
        this.total      = jsonObject.getString("total"   )
    }

    constructor(data : DataSnapshot){
        this.id      = data.key
        this.date    = data.child("date"    ).value.toString()
        this.idUser  = data.child("idUser"  ).value.toString()
        this.idMusic = data.child("idMusic" ).value.toString()
    }

    fun toJson () : JSONObject {
        val jsonObject = JSONObject()

        jsonObject.put("id"      , this.id      )
        jsonObject.put("date"    , this.date    )
        jsonObject.put("idUser"  , this.idUser  )
        jsonObject.put("idMusic" , this.idMusic )
        jsonObject.put("total"   , this.total   )

        return jsonObject
    }


    companion object{
        var auth : FirebaseAuth? = null

        fun getAllFavorites(child : String, callback: (MutableList<Favorite>?) -> Unit ){
            val favoritesList : MutableList<Favorite> = ArrayList()

            doAsync {
                auth = FirebaseAuth.getInstance()
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("Favorites")
                    .orderByChild(child)

                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(data: DataSnapshot) {
                        favoritesList.clear()
                        if (data.exists()){
                            for (d in data.children){
                                val favorite = Favorite(d)
                                favoritesList.add(favorite)
                            }
                        }

                        if (favoritesList.size > 0)
                            uiThread {
                                callback(favoritesList)
                            }
                        else
                            uiThread {
                                callback(null)
                            }
                    }

                    override fun onCancelled(erro: DatabaseError) {
                    }
                })
            }
        }

        fun getMyFavorites(callback: (MutableList<Favorite>?) -> Unit ){
            val favoritesList : MutableList<Favorite> = ArrayList()

            doAsync {
                auth = FirebaseAuth.getInstance()

                getAllFavorites("date") {
                    favoritesList.clear()
                    it?.let{
                        for (favorite in it){
                            if (favorite.idUser == auth!!.uid.toString()) {
                                favoritesList.add(favorite)
                            }
                        }
                    }

                    if (favoritesList.size > 0)
                        uiThread {
                            callback(favoritesList)
                        }
                    else
                        uiThread {
                            callback(null)
                        }
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun insertFavorite(idUser : String?, idMusic : String?) {
            auth = FirebaseAuth.getInstance()
            var conf = false
            val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS")
            val currentDate = sdf.format(Date())

            val idFavorite = UUID.randomUUID().toString()
            val database = FirebaseDatabase.getInstance()
            var myRef = database.getReference("Favorites")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    if (data.exists()){
                        for (d in data.children){
                            val favorite = Favorite(d)
                            if (favorite.idUser == idUser && favorite.idMusic == idMusic){
                                conf = true
                            }
                        }
                    }

                    val run = Runnable {
                        if (!conf){
                            myRef = myRef
                                .child(idFavorite)

                            val favorite = Favorite (
                                idFavorite,
                                currentDate,
                                idUser,
                                idMusic
                            )

                            myRef.setValue(favorite)
                        }
                    }
                    android.os.Handler().postDelayed(run, 500)
                }

                override fun onCancelled(erro: DatabaseError) {
                }
            })
        }

        fun updateFavoriteFirebase(favorite: Favorite?) {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Favorites")
                .child(auth!!.uid!!)


            myRef.setValue(favorite)
        }
    }
}
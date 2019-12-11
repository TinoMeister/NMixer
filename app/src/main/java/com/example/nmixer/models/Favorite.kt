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

        @SuppressLint("SimpleDateFormat")
        fun insertFavorite(idUser : String?, idMusic : String?) {

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
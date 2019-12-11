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
}
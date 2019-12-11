package com.example.nmixer.models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class Share {
    var id      : String? = null
    var idMusic : String? = null
    var type    : String? = null
    var link    : String? = null

    constructor(id : String?, idMusic : String?, type : String?, link : String?) {
        this.id      = id
        this.idMusic = idMusic
        this.type    = type
        this.link    = link
    }

    constructor(data : DataSnapshot){
        this.id      = data.key
        this.idMusic = data.child("idMusic" ).value.toString()
        this.type    = data.child("type"    ).value.toString()
        this.link    = data.child("link"    ).value.toString()
    }
}
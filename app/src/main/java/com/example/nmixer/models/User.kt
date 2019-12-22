package com.example.nmixer.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject

class User {
    var id          : String? = null
    var name        : String? = null
    var email       : String? = null
    var imageUrl    : String? = null

    constructor(id : String?, name : String?, email : String?, imageUrl : String?) {
        this.id        = id
        this.name      = name
        this.email     = email
        this.imageUrl  = imageUrl
    }

    constructor(data : String){
        val jsonObject = JSONObject(data)
        id         = jsonObject.getString("id"       )
        name       = jsonObject.getString("name"     )
        email      = jsonObject.getString("email"    )
        imageUrl   = jsonObject.getString("imageUrl" )
    }

    constructor(data : DataSnapshot){
        id         = data.child("id"       ).value.toString()
        name       = data.child("name"     ).value.toString()
        email      = data.child("email"    ).value.toString()
        imageUrl   = data.child("imageUrl" ).value.toString()
    }

    fun toJson () : JSONObject {
        val jsonObject = JSONObject()

        jsonObject.put("id"       , id       )
        jsonObject.put("name"     , name     )
        jsonObject.put("email"    , email    )
        jsonObject.put("imageUrl" , imageUrl )

        return jsonObject
    }

    companion object{
        var auth : FirebaseAuth? = null

        fun getUser(callback: (User?) -> Unit ){

            doAsync {
                auth = FirebaseAuth.getInstance()
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("Users")
                    .child(auth!!.uid!!)

                var user : User?

                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(data: DataSnapshot) {
                        user = if (data.exists()){
                            User (data)
                        }
                        else {
                            null
                        }

                        uiThread {
                            callback(user)
                        }
                    }

                    override fun onCancelled(erro: DatabaseError) {
                    }
                })
            }
        }


        fun insertUser(name : String?, email : String?) {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Users")
                .child(auth!!.uid!!)

            val user = User (
                auth!!.uid.toString(),
                name,
                email,
                ""
            )
            myRef.setValue(user)
        }

        fun updateUser(user : User?) {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Users")
                .child(auth!!.uid!!)

            myRef.setValue(user)
        }
    }
}
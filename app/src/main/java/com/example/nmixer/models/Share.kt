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

    companion object{
        var auth : FirebaseAuth? = null

        fun getShares(callback: (MutableList<Share>?) -> Unit ){
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

                        if (sharesList.size > 0)
                            uiThread {
                                callback(sharesList)
                            }
                        else
                            uiThread {
                                callback(null)
                            }
                    }

                    override fun onCancelled(erro: DatabaseError) {
                        Log.d("Error / Share", erro.message)
                    }
                })
            }
        }

        fun insertShare(idMusic: String?, type: String?) {
            auth = FirebaseAuth.getInstance()
            var conf = false
            val idShare = UUID.randomUUID().toString()
            val database = FirebaseDatabase.getInstance()

            getShares {
                it?.let {
                    for (share in it){
                        if (share.idMusic == idMusic) {
                            conf = true
                        }
                    }
                }

                if (!conf) {
                    val myRef = database.getReference("Shares")
                        .child(idShare)

                    val share = Share(
                        idShare,
                        idMusic,
                        type,
                        ""
                    )

                    myRef.setValue(share)
                }
            }
        }
    }
}
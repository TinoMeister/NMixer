package com.example.nmixer.models

import com.google.firebase.database.DataSnapshot
import org.json.JSONObject

class Share {
    var id      : String? = null
    var idMusic : String? = null
    var type    : String? = null
    var link    : String? = null
    var music   : Music?  = null

    constructor(id : String?, idMusic : String?, type : String?, link : String?) {
        this.id      = id
        this.idMusic = idMusic
        this.type    = type
        this.link    = link
    }

    constructor(id : String?, type : String?, link : String?, music: Music) {
        this.id      = id
        this.type    = type
        this.link    = link
        this.music   = music
    }

    constructor(data:String){
        val jsonObject = JSONObject(data)
        this.id = jsonObject.getString("id" )
        this.type    = jsonObject.getString("type"    )
        this.link    = jsonObject.getString("link"    )

        val sourceObj = jsonObject.get("music")

        if (sourceObj is JSONObject)
            music  = Music(sourceObj.toString())
        else if (sourceObj is String)
            music  = Music(sourceObj)
    }

    constructor(data : DataSnapshot){
        this.id      = data.key
        this.idMusic = data.child("idMusic" ).value.toString()
        this.type    = data.child("type"    ).value.toString()
        this.link    = data.child("link"    ).value.toString()
    }

    fun toJson () : JSONObject {
        val jsonObject = JSONObject()

        jsonObject.put("id"     , id                            )
        jsonObject.put("type"   , type                          )
        jsonObject.put("link"   , link                          )
        jsonObject.put("music"  , music?.toJson().toString()    )

        return jsonObject
    }
}
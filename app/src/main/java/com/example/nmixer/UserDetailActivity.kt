package com.example.nmixer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.nmixer.models.User
import kotlinx.android.synthetic.main.activity_user_detail.*

class UserDetailActivity : AppCompatActivity() {

    var user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        var strJsonArticle = intent.getStringExtra(USER)

        strJsonArticle?.let {
            user = User(strJsonArticle)

            textViewName.text = user!!.name
            Glide.with(this@UserDetailActivity)
                .load(user!!.imageUrl)
                .into(imageViewUser)
        }
    }

    companion object{
        val USER = "com.example.nmixer.userDetailActivity"
    }
}

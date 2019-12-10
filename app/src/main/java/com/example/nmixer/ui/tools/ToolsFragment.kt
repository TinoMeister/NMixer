package com.example.nmixer.ui.tools

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.nmixer.MainActivity
import com.example.nmixer.R
import com.example.nmixer.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_tools.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class ToolsFragment : Fragment()  {

    private var auth : FirebaseAuth? = null
    private var database : FirebaseDatabase? = null
    private var storage : FirebaseStorage? = null

    private var googleSignInClient : GoogleSignInClient? = null
    var user : User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tools, container, false)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        User.getUser {
            user = it
            refresh()
            seeAll()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        load()

        buttonCImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        buttonBack.setOnClickListener {
            editTextPassword.visibility = View.GONE
            buttonBack.visibility = View.GONE
        }

        buttonCPassword.setOnClickListener {
            editTextPassword.visibility = View.VISIBLE
            buttonBack.visibility = View.VISIBLE
        }

        buttonLogOut.setOnClickListener {
            auth?.signOut()
            googleSignInClient?.signOut()
            requireActivity().finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        refresh()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            load()

            val ref = storage?.getReference("/images/${auth?.uid.toString()}")
            ref?.putFile(data.data!!)
                ?.addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        if (user?.imageUrl != it.toString() && it.toString().isNotEmpty()) {
                            user?.imageUrl = it.toString()
                            User.updateUser(user)
                            refresh()
                            seeAll()
                        }
                    }
                }
        }
    }

    private fun load() {
        llProgressBar.visibility = View.VISIBLE
        cardViewImage.visibility = View.GONE
        buttonCImage.visibility = View.GONE
        buttonCPassword.visibility = View.GONE
        buttonLogOut.visibility = View.GONE
    }

    private fun seeAll() {
        llProgressBar.visibility = View.GONE
        cardViewImage.visibility = View.VISIBLE
        buttonCImage.visibility = View.VISIBLE
        buttonCPassword.visibility = View.VISIBLE
        buttonLogOut.visibility = View.VISIBLE
    }

    private fun refresh() {
        textViewName.text = user?.name
        textViewEmail.text = user?.email


        if (user?.imageUrl != "") {
            Glide.with(context!!)
                .load(user?.imageUrl)
                .into(imageViewUser)
        }
    }
}
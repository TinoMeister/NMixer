package com.example.nmixer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nmixer.models.User
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var isSignIn = true
    var passwordForgot = false
    var count = 0
    var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        allUnSee(false)

        textViewSignIn.setOnClickListener {
            isSignIn = true
            updateView()
        }

        textViewSignUp.setOnClickListener {
            isSignIn = false
            updateView()
        }

        buttonEnter.setOnClickListener {
            if (editTextEmail.text.toString().isNotEmpty() && editTextPassword.text.toString().isNotEmpty())
            {
                if (isSignIn && !passwordForgot)
                    signIn(editTextEmail.text.toString().trimEnd(), editTextPassword.text.toString().trimEnd())
                else if (!passwordForgot && editTextName.text.toString().isNotEmpty())
                    signUp(editTextName.text.toString().trimEnd(), editTextEmail.text.toString().trimEnd(), editTextPassword.text.toString().trimEnd())
                else{
                    resetPassword(editTextEmail.text.toString().trimEnd())
                }
            }
        }

        textViewFPassword.setOnClickListener {
            passwordForgot = true
            updateView()
        }

        textViewBack.setOnClickListener {
            passwordForgot = false
            updateView()
        }

        buttonConnectGoogle.setOnClickListener {
            googleLogin()
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()
    }

    fun signIn(email : String, password : String){
        auth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    editTextEmail.text.clear()
                    editTextPassword.text.clear()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signUp(name : String, email : String, password : String){
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    User.insertUser(name, email)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    editTextName.text.clear()
                    editTextEmail.text.clear()
                    editTextPassword.text.clear()
                    Toast.makeText(baseContext, "User Created.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun resetPassword(email : String){
        auth!!.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // successful!
                    Toast.makeText(baseContext, "Email sent.",
                        Toast.LENGTH_SHORT).show()
                    editTextEmail.text.clear()
                } else {
                    Toast.makeText(baseContext, "Email failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    User.getUser {
                        if (it == null)
                            User.insertUser(account?.displayName, account?.email)
                    }

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this, task.exception?.message,
                        Toast.LENGTH_SHORT).show()
                }
            }
    }


    fun updateView() {

        if (passwordForgot){
            editTextName.visibility = View.GONE
            textViewSignIn.visibility = View.INVISIBLE
            textViewSignUp.visibility = View.INVISIBLE

            editTextPassword.visibility = View.INVISIBLE
            buttonEnter.text = "Send"

            textViewFPassword.visibility = View.INVISIBLE

            textViewBack.visibility = View.VISIBLE

            viewLeft.visibility = View.INVISIBLE
            textViewOr.visibility = View.INVISIBLE
            viewRight.visibility = View.INVISIBLE

            buttonConnectGoogle.visibility = View.INVISIBLE
            buttonConnectFacebook.visibility = View.INVISIBLE

        }
        else{
            editTextName.visibility = View.GONE
            textViewSignIn.visibility = View.VISIBLE
            textViewSignUp.visibility = View.VISIBLE

            editTextPassword.visibility = View.VISIBLE

            textViewFPassword.visibility = View.VISIBLE

            textViewBack.visibility = View.GONE

            viewLeft.visibility = View.VISIBLE
            textViewOr.visibility = View.VISIBLE
            viewRight.visibility = View.VISIBLE

            buttonConnectGoogle.visibility = View.VISIBLE
            buttonConnectFacebook.visibility = View.VISIBLE
        }

        if (isSignIn && !passwordForgot){
            buttonEnter.text = "Sing In"
        }
        else if (!passwordForgot){
            editTextName.visibility = View.VISIBLE
            textViewFPassword.visibility = View.GONE

            viewLeft.visibility = View.GONE
            textViewOr.visibility = View.GONE
            viewRight.visibility = View.GONE

            buttonConnectGoogle.visibility = View.GONE
            buttonConnectFacebook.visibility = View.GONE

            buttonEnter.text = "Sing Up"
        }

        editTextName.text.clear()
        editTextPassword.text.clear()
    }


    fun allUnSee(value : Boolean) {
        var type : Int

        if (value)
            type = View.VISIBLE
        else
            type = View.GONE

        logo.visibility = type
        textViewSignIn.visibility = type
        textViewSignUp.visibility = type
        editTextEmail.visibility = type
        editTextPassword.visibility = type
        buttonEnter.visibility = type
        textViewFPassword.visibility = type
        viewLeft.visibility = type
        textViewOr.visibility = type
        viewRight.visibility = type
        buttonConnectGoogle.visibility = type
        buttonConnectFacebook.visibility = type
    }

    override fun onStart() {
        super.onStart()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )) {

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    4
                )
            }
        }

        val mDelayHandler = Handler()

        llProgressBarLogin.visibility = View.VISIBLE

        val mRunnable = Runnable {
            if (!isFinishing){
                if (checkInternetConnection(this)) {
                    isConnected = true
                }
                else
                    count++

                onStart()
            }
        }

        if (isConnected) {
            val currentUser = auth?.currentUser

            if (currentUser != null) {

               User.getUser{
                   it?.let {
                       if (it.imageUrl != "") {
                           Glide.with(this@LoginActivity)
                               .load(it.imageUrl)
                               .diskCacheStrategy(DiskCacheStrategy.ALL)
                       }
                   }
                }

                intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
            }
            else
                allUnSee(true)

            llProgressBarLogin.visibility = View.GONE
        }
        else if (count >= 12){
            Toast.makeText(baseContext,
                "No Internet Connection",
                Toast.LENGTH_SHORT)
                .show()
            llProgressBarLogin.visibility = View.GONE
            allUnSee(true)
        }
        else
            mDelayHandler.postDelayed(mRunnable, 500)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            4 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                }
                return
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }
}

package com.example.nmixer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nmixer.models.TelnetConnection
import com.example.nmixer.models.User
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null
    private var isSignIn = true
    private var passwordForgot = false
    private var count = 0
    private var isConnected = false


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

        buttonSound.setOnClickListener {
            val socket = TelnetConnection()

            socket.connectUdp()

            if (socket.result == "ack"){
                val intent = Intent(this@LoginActivity, EspActivity::class.java)
                startActivity(intent)
            }
            else
                Toast.makeText(baseContext, "Esp is not connect.",
                    Toast.LENGTH_SHORT).show()
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

    private fun signIn(email : String, password : String){
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

    private fun signUp(name : String, email : String, password : String){
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

    private fun resetPassword(email : String){
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

    private fun googleLogin() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
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


    @SuppressLint("SetTextI18n")
    private fun updateView() {

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
            buttonSound.visibility = View.INVISIBLE

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
            buttonSound.visibility = View.VISIBLE
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
            buttonSound.visibility = View.GONE

            buttonEnter.text = "Sing Up"
        }

        editTextName.text.clear()
        editTextPassword.text.clear()
    }


    private fun allUnSee(value : Boolean) {

        val type : Int = if (value)
            View.VISIBLE
        else
            View.GONE

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
        buttonSound.visibility = type
    }

    override fun onStart() {
        super.onStart()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
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

        when {
            isConnected -> {
                val currentUser = auth?.currentUser

                if (currentUser != null) {

                    User.getUser{
                        it?.let {
                            if (it.imageUrl != "") {
                                Glide.with(this@LoginActivity)
                                    .load(it.imageUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .submit()
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
            count >= 12 -> {
                Toast.makeText(baseContext,
                    "No Internet Connection",
                    Toast.LENGTH_SHORT)
                    .show()
                llProgressBarLogin.visibility = View.GONE
                allUnSee(true)
            }
            else -> mDelayHandler.postDelayed(mRunnable, 500)
        }
    }


    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

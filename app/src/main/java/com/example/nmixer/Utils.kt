package com.example.nmixer

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager

fun checkInternetConnection(baseContext: Context) : Boolean {

    val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    val conf: Boolean

    conf = networkInfo != null && networkInfo.isConnected

    return conf
}
var mediaPlayer: MediaPlayer = MediaPlayer()

fun playSong(link : String){

    if (mediaPlayer.isPlaying){
        mediaPlayer.stop()
    }
    else{
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(link)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener { mediaPlayer.start()}
    }

}
package com.example.nmixer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import com.example.nmixer.R.*
import com.example.nmixer.R.drawable.*
import com.example.nmixer.models.Share
import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity : AppCompatActivity() {
    var share : Share? = null
    var barVolume = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_player)
        active = true

        playBtn.setOnClickListener {
            playBtnClick(it)
        }

        imageView_back.setOnClickListener {
            super.onBackPressed()
        }

        imageViewFavorite.setOnClickListener {
            imageViewFavorite.setImageResource(ic_favorite_black_10dp)
        }

        val strJsonArticle = intent.getStringExtra(MUSIC)
        share = Share(strJsonArticle!!)

        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeBar.progress = ((volumeLevel.toFloat() * 100.0F) / 15F).toInt()
        barVolume = volumeBar.progress / 100F

        positionBar.progress = playerPosition

        txtMusicName.text = share!!.music!!.title
        txtArtistName.text = share!!.music!!.user!!.name
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()

        if (mp.isPlaying && oldShare?.id == share?.id){
            playBtn.setImageResource(android.R.drawable.ic_media_pause)
        } else if (!mp.isPlaying && oldShare?.id == share?.id){
            playBtn.setImageResource(android.R.drawable.ic_media_play)
        } else {
            playBtn.setImageResource(android.R.drawable.ic_media_pause)
            mp.pause()
            start()
        }

        // Volume Bar
        volumeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val am = getSystemService(AUDIO_SERVICE) as AudioManager
                        val volumeNum = progress / 100.0f
                        val volume = ((progress * 15.0F) / 100.0F).toInt()
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

                        mp.setVolume(volumeNum, volumeNum)
                        Log.d("dsfs", volumeNum.toString())
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )

        // Position Bar
        positionBar.max = totalTime
        positionBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mp.seekTo(progress)
                        playerPosition = progress
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )

        // Thread
        Thread(Runnable {
            while (true) {
                try {
                    val msg = Message()
                    msg.what = mp.currentPosition
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()
    }

    private fun start(){
        oldShare = share
        mp = MediaPlayer.create(this, Uri.parse(share?.link))
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mp.isLooping = false
        mp.setVolume(barVolume, barVolume)
        totalTime = mp.duration
        mp.start()
    }

    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            val currentPosition = msg.what

            // Update positionBar
            positionBar.progress = currentPosition

            // Update Labels
            val elapsedTime = createTimeLabel(currentPosition)
            elapsedTimeLabel.text = elapsedTime

            val remainingTime = createTimeLabel(totalTime - currentPosition)
            remainingTimeLabel.text = "-$remainingTime"
        }
    }

    fun createTimeLabel(time: Int): String {
        var timeLabel: String
        val min = time / 1000 / 60
        val sec = time / 1000 % 60

        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }

    private fun playBtnClick(v : View) {
        if (mp.isPlaying && oldShare?.id != share?.id) {
            mp.pause()
            val uri: Uri = Uri.parse( share?.link)
            mp = MediaPlayer.create(this, uri)
            totalTime = mp.duration
            mp.start()

        } else if (mp.isPlaying){
            mp.pause()
            playBtn.setImageResource(android.R.drawable.ic_media_play)
        }else {
            mp.start()
            playBtn.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC)

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                volumeBar.progress = (((volumeLevel + 1).toFloat() * 100.0F) / 15F).toInt()
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                volumeBar.progress = (((volumeLevel - 1).toFloat() * 100.0F) / 15F).toInt()
            }
            KeyEvent.KEYCODE_BACK ->{
                super.onBackPressed()
            }
        }
        return false
    }

    override fun onStop() {
        super.onStop()
        active = false
        if (!MainActivity.active)
            mp.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        active = false
    }

    companion object{
        const val MUSIC = "music"
        var mp = MediaPlayer()
        var oldShare : Share? = null
        var totalTime = 0
        var playerPosition = 0
        var active = false
    }
}

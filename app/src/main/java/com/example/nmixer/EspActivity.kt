package com.example.nmixer

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.nmixer.models.TelnetConnection
import kotlinx.android.synthetic.main.activity_esp.*


class EspActivity : AppCompatActivity() {
    private val socket = TelnetConnection()
    var value = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esp)

        button1.setOnTouchListener { v, event ->
            value = "100"
            onTouchEvent(event)
        }

        button2.setOnTouchListener { v, event ->
            value = "200"
            onTouchEvent(event)
        }

        button3.setOnTouchListener { v, event ->
            value = "300"
            onTouchEvent(event)
        }

        button4.setOnTouchListener { v, event ->
            value = "400"
            onTouchEvent(event)
        }

        button5.setOnTouchListener { v, event ->
            value = "500"
            onTouchEvent(event)
        }

        button6.setOnTouchListener { v, event ->
            value = "600"
            onTouchEvent(event)
        }


    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event!!.action){
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                socket.sendUDP("100")
            }
            else -> {
                socket.sendUDP("0")
            }
        }

        return true
    }
}

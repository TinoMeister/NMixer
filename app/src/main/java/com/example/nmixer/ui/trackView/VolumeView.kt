package com.example.nmixer.ui.trackView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class VolumeView : View {
    var touchX = 0.0F
    var height = 0.0F
    var touchEvent : ((tempValue : Float) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attr : AttributeSet) : super(context, attr) {
        init()
    }

    constructor(context: Context, attr : AttributeSet, defStyleAttr : Int) : super(context, attr, defStyleAttr) {
        init()
    }

    fun init(){

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 4.0F
        paint.style = Paint.Style.FILL_AND_STROKE

        val paint2 = Paint()
        paint2.color = Color.GREEN
        paint2.strokeWidth = 10.0f

        canvas?.let {
            val startx = 0f
            val starty = 10f
            val endx = 250f
            val endy = 10f

            it.drawLine(startx, starty, endx, endy, paint2)

            val rect = RectF(touchX, 0.0F, touchX + 15.0F, 20.0F)

            it.drawRect(rect, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val x = event!!.getX()

        when (event!!.action){
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {

                if (x < 235.0F && x >= 0.0F)
                    touchX = x
                else {
                    if (x > 0.0F)
                        touchX = 235.0F
                    else
                        touchX = 0.0F
                }


                invalidate()
            }
        }

        return true
    }
}
package com.example.accessibilityexperience

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton

class DraggableButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isPressed = false

    init {
        //setTextColor(Color.BLACK)
        //paint.color = Color.GRAY
        textSize = 21f
        setOnClickListener {
            // Handle the button click event here
            Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the button background
        /*
        if (isPressed) {
            paint.color = Color.GRAY
        } else {
            paint.color = Color.LTGRAY
        }
        if (isPressed) {
            setBackgroundColor(paint.color)
            val colorStateList = ColorStateList.valueOf(paint.color)
            backgroundTintList = colorStateList
        } else {
            setBackgroundColor(paint.color)
            val colorStateList = ColorStateList.valueOf(paint.color)
            backgroundTintList = colorStateList
        }

         */
        //canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 20f, 20f, paint)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                invalidate()
                Log.d("XDEBUG", "qui")
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                x = event.rawX - width / 2
                y = event.rawY - height / 2
                //updateViewLayout(mLayout, params)
                Log.d("XDEBUG", "qui move")
                return true
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                invalidate()
                performClick() // Trigger the click event
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}

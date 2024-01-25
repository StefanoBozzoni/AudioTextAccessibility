package com.sbsoft.audiotextassistant

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager

object basicModeGestureListener : GestureDetector.SimpleOnGestureListener() {
    var moved = false
    private var previousTouchX: Float = 0f
    private var previousTouchY: Float = 0f
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var layoutUpdater: ILayoutUpdater

    fun setListenerParam(layoutParam: WindowManager.LayoutParams, iLayoutUpdater: ILayoutUpdater) {
        params = layoutParam
        layoutUpdater = iLayoutUpdater
    }

    override fun onDown(e: MotionEvent): Boolean {
        previousTouchX = e.rawX
        previousTouchY = e.rawY
        moved = false
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        moved = true
        params.x = params.x + (e2.rawX - previousTouchX).toInt()
        params.y = params.y + (e2.rawY - previousTouchY).toInt()
        previousTouchX = e2.rawX
        previousTouchY = e2.rawY
        layoutUpdater.onUpdateLayout(params)
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        layoutUpdater.onActivateAdvancedMode()
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        layoutUpdater.onSpeakerOnOff()
        return false
    }
}

interface ILayoutUpdater {
    fun onUpdateLayout(params: WindowManager.LayoutParams)
    fun onSpeakerOnOff()
    fun onActivateAdvancedMode()
}

/*
class Prova(): ILayoutUpdater {
    val mLayout: FrameLayout? = null
    override fun onUpdateLayout(params: WindowManager.LayoutParams) {
        TODO("Not yet implemented")
    }

    override fun onSpeakerOnOff() {
        TODO("Not yet implemented")
    }

    override fun onActivateAdvancedMode() {
        TODO("Not yet implemented")
    }
}
*/
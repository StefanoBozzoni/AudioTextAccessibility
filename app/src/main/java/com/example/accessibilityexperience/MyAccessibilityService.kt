package com.example.accessibilityexperience

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Deque
import java.util.Locale
import java.util.concurrent.Executor


enum class SpeakerState {
    SPEAKERON,
    SPEAKEROFF
}

class MyAccessibilityService : AccessibilityService() {
    private var firstTimeSpeakerOff: Boolean = false
    var mLayout: FrameLayout? = null

    private lateinit var tts: TextToSpeech

    private var speakerState : SpeakerState = SpeakerState.SPEAKERON

    override fun onServiceConnected() {

        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ITALIAN)
        val dateInString = "01-04-2024 00:00:00"
        val finalDate: Date? = sdf.parse(dateInString)

        val timestamp = System.currentTimeMillis()
        val currentDate = Date(timestamp)

        if (currentDate > finalDate) {
            disableSelf()
            return
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault())
                tts.setSpeechRate(1.2f)
            }
        }

        // Create an overlay and display the action bar
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)
        setTouchListenerforDragging()
        configureScanBtn()
    }

    private fun configureScanBtn() {
        val btnScan = mLayout?.findViewById<ImageView>(R.id.btnScan)
        btnScan?.setOnClickListener {
            if (btnScan.contentDescription=="speaker on") {
                btnScan.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.volume_off_40))
                btnScan.contentDescription = "speaker off"
                speakerState = SpeakerState.SPEAKEROFF
                firstTimeSpeakerOff = true
            } else {
                btnScan.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.text_to_speech_40))
                btnScan.contentDescription = "speaker on"
                speakerState = SpeakerState.SPEAKERON
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //TODO("Not yet implemented")
        event?.let {
            val eventoDescr = AccessibilityEvent.eventTypeToString(it.eventType)

            if (it.eventType==TYPE_VIEW_CLICKED) {
                tts.stop()
                Log.d("XDEBUG", eventoDescr)
                Log.d("XDEBUG text", it.text.joinToString())
                Log.d("XDEBUG content descr", it.contentDescription.toString())
                Log.d("XDEBUG before text", it.beforeText.toString())
                Log.d("XDEBUG describecontent", it.describeContents().toString())
                Log.d("XDEBUG source.text", it.source?.text.toString())
                Log.d("XDEBUG source.conent", it.source?.contentDescription.toString())
                Log.d("XDEBUG source present", if (it.source!=null) "present" else "not present")

                val testo = it.text.joinToString()
                if (testo=="Indietro") {
                    tts.stop()
                }
                if (testo.isNotEmpty()) {
                    speakText(testo, speakerState == SpeakerState.SPEAKEROFF && (firstTimeSpeakerOff==true))
                    firstTimeSpeakerOff = false
                } else if (it.source?.text!=null) {
                    it.source?.let {
                        printTree(it)
                    }
                }
                //printTree(rootInActiveWindow)
            }
            Log.d("XDEBUG event type", eventoDescr)
        }
    }


    private fun speakText(testo: String, overrideCheck: Boolean = false) {
        if (speakerState == SpeakerState.SPEAKERON || overrideCheck) {
            tts.speak(testo,
                TextToSpeech.QUEUE_ADD,
                null,
                null)
        }
    }

    override fun takeScreenshot(displayId: Int, executor: Executor, callback: TakeScreenshotCallback) {
        super.takeScreenshot(displayId, executor, callback)
        Log.d("XDEBUG screenshot","")
    }

    override fun onSystemActionsChanged() {
        super.onSystemActionsChanged()
        Log.d("XDEBUG Action changed","")
    }

    private fun printTree(root: AccessibilityNodeInfo) {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            Log.d("XDEBUG text", node.className.toString()+" / "+ node.text.toString()+" / "+'/'+node.contentDescription+"/"+node.isClickable+"/"+node.isContextClickable)

            if (node.isClickable) {
                speakText(node?.contentDescription.toString())
                //node.getChild(0)
            } else {
                speakText(node?.text.toString())
                for (i in 0 until node.childCount) {
                    if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
                }
            }
        }
    }

    override fun onInterrupt() {
        //TODO("Not yet implemented")
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListenerforDragging() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        val btnScan = mLayout?.findViewById<ImageView>(R.id.btnScan)

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        btnScan?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var moved: Boolean = false
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        moved = false
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(mLayout, params)
                        moved = true
                        return false
                    }

                    MotionEvent.ACTION_UP -> {
                        // Perform a click action when the touch is released
                        if (!moved) {
                            v.post {
                                v.performClick()
                            }
                            return false
                        } else
                            return true
                    }

                    else -> return false
                }
            }
        }
        )

    }
}

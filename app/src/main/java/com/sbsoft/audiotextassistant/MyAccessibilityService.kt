package com.sbsoft.audiotextassistant

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.InflateException
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.sbsoft.audiotextassistant.Constants.TIMEOUT_DATE_STR
import com.sbsoft.audiotextassistant.Utils.findNodeByCoordinates
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Deque
import java.util.Locale


enum class SpeakerState {
    SPEAKERON,
    SPEAKEROFF
}

enum class Mode {
    ADVANCED,
    NORMAL
}

class MyAccessibilityService : AccessibilityService() {
    private var firstTimeSpeakerOff: Boolean = false
    var mLayout: FrameLayout? = null
    private lateinit var tts: TextToSpeech
    private var speakerState: SpeakerState = SpeakerState.SPEAKERON
    private lateinit var mode: Mode
    private var moved: Boolean = false

    override fun onServiceConnected() {

        mode = Mode.NORMAL
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ITALIAN)
        val finalDate: Date? = sdf.parse(TIMEOUT_DATE_STR)

        val timestamp = System.currentTimeMillis()
        val currentDate = Date(timestamp)

        if (currentDate > finalDate) {
            disableSelf()
            return
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault())
                tts.setSpeechRate(1f)
            }
        }

        // Create an overlay and display the action bar
        activateBasicScreen()
        val rootNode = rootInActiveWindow

        if (rootNode != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                rootNode.getChild(0)?.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
            }, 1000)
        }

    }

    private fun configureScanBtn() {
        val btnSpeaker = mLayout?.findViewById<ImageView>(R.id.btnSpeaker)


        /*
        btnSpeaker?.setOnClickListener {
            switchSpeakerOnOff()
        }
         */

        btnSpeaker?.setOnLongClickListener {
            if (!moved) {
                activateOverlayScreen()
            }
            false
        }

    }

    private fun switchSpeakerOnOff() {
        val btnSpeaker = mLayout?.findViewById<ImageView>(R.id.btnSpeaker)

        if (btnSpeaker?.contentDescription == "speaker on") {
            btnSpeaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.volume_off_40))
            btnSpeaker.contentDescription = "speaker off"
            speakerState = SpeakerState.SPEAKEROFF
            firstTimeSpeakerOff = true
        } else {
            btnSpeaker?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.text_to_speech_40))
            btnSpeaker?.contentDescription = "speaker on"
            speakerState = SpeakerState.SPEAKERON
        }
        speakText(btnSpeaker?.contentDescription.toString(), firstTimeSpeakerOff)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun activateOverlayScreen() {
        mode = Mode.ADVANCED
        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(mLayout)
            mLayout = FrameLayout(this)
            val inflater = LayoutInflater.from(this)
            inflater.inflate(R.layout.main_overlay, mLayout)
            val lp = WindowManager.LayoutParams()
            lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            lp.format = PixelFormat.TRANSLUCENT
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            wm.addView(mLayout, lp)

            val mainOverlay = mLayout?.findViewById<ViewGroup>(R.id.mainOverlayView)


            mainOverlay?.setOnLongClickListener {
                activateBasicScreen()
                false
            }

            mainOverlay?.setOnTouchListener { _, event ->
                // Log the touch coordinates
                val x = event.x
                val y = event.y

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Handle touch down event
                        val rootNode = this.rootInActiveWindow
                        val nodefound = findNodeByCoordinates(rootNode, x.toInt(), y.toInt(), mLayout!!)
                        Log.d("XDEBUG DOWN", "x=$x, y=$y")
                        nodefound?.let {
                            Log.d("XDEBUG node ", nodefound.toString())
                            tts.stop()
                            Log.d("XDEBUG node parent", nodefound.parent.toString())
                            speakTree(it.parent)
                        }
                        false
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // Handle touch move event
                        //Log.d("XDEBUG MOVE", "x=$x, y=$y")
                        false
                    }

                    MotionEvent.ACTION_UP -> {
                        // Handle touch up event
                        //Log.d("XDEBUG UP", "x=$x, y=$y")
                        false
                    }

                    else -> false
                }

            }
        } catch (e: InflateException) {
            Log.e("YourTag", "Error inflating layout: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("YourTag", "Exception: ${e.message}", e)
        }

        speakText("modalità avanzata")
    }

    private fun activateBasicScreen() {
        mode = Mode.NORMAL
        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (mLayout != null) {
                wm.removeView(mLayout)
                tts.stop()
                speakText("modalità normale")
            }

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
        } catch (e: InflateException) {
            Log.e("YourTag", "Error inflating layout: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("YourTag", "Exception: ${e.message}", e)
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //TODO("Not yet implemented")
        event?.let {
            val eventoDescr = AccessibilityEvent.eventTypeToString(it.eventType)

            if (it.eventType == TYPE_VIEW_CLICKED && (mode != Mode.ADVANCED)) {
                tts.stop()
                Log.d("XDEBUG", eventoDescr)
                /*
                Log.d("XDEBUG", eventoDescr)
                Log.d("XDEBUG text", it.text.joinToString())
                Log.d("XDEBUG content descr", it.contentDescription.toString())
                Log.d("XDEBUG before text", it.beforeText.toString())
                Log.d("XDEBUG describecontent", it.describeContents().toString())
                Log.d("XDEBUG source.text", it.source?.text.toString())
                Log.d("XDEBUG source.conent", it.source?.contentDescription.toString())
                Log.d("XDEBUG source present", if (it.source != null) "present" else "not present")
                 */

                val testo = it.text.joinToString()
                if (testo == "Indietro") {
                    tts.stop()
                }
                if (testo.isNotEmpty() && testo.lowercase() != "null") {
                    speakText(testo, speakerState == SpeakerState.SPEAKEROFF && (firstTimeSpeakerOff == true))
                    firstTimeSpeakerOff = false
                } else if (it.source != null) {
                    it.source?.let {
                        speakTree(it)
                    }
                }
            }

            if (it.eventType == TYPE_VIEW_FOCUSED) {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        rootNode?.getChild(0)?.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
                    }, 1000)
                }
                //printTree(rootNode)
            }

            Log.d("XDEBUG", eventoDescr)
        }
    }

    private fun speakText(testo: String, overrideCheck: Boolean = false) {
        if (testo != "null" && (speakerState == SpeakerState.SPEAKERON || overrideCheck)) {
            tts.speak(
                testo,
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
        }
    }

    override fun onSystemActionsChanged() {
        super.onSystemActionsChanged()
        //Log.d("XDEBUG Action changed", "")
    }

    private fun speakTree(root: AccessibilityNodeInfo) {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            Log.d("XDEBUG text", node.toString())
            Log.d("XDEBUG text", node.className.toString() + " / " + node.text.toString() + " / " + '/' + node.contentDescription + "/" + node.isClickable + "/" + node.isContextClickable)
            val contentDescription = node?.contentDescription.toString()

            val boundsinScreen = Rect()
            node.getBoundsInScreen(boundsinScreen)
            val isAdvertiseButton = ((boundsinScreen.top < 0) || (boundsinScreen.left < 0) || boundsinScreen.right < 0 || boundsinScreen.bottom < 0)

            if (node.isClickable && contentDescription.isNotEmpty() && contentDescription != "null" && !isAdvertiseButton) {
                speakText(node?.contentDescription.toString())
                Log.d("XDEBUG isClickable", node.className.toString() + " / " + node.text.toString() + " / " + '/' + node.contentDescription + "/" + node.isClickable + "/" + node.isContextClickable)
            } else {
                if (node.isVisibleToUser) {
                    speakText(node?.text.toString())
                }
                for (i in 0 until node.childCount) {
                    if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
                }
            }
        }
    }

    override fun onInterrupt() {
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
        val btnSpeaker = mLayout?.findViewById<ImageView>(R.id.btnSpeaker)

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        btnSpeaker?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
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
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        moved = true
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(mLayout, params)
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        // Perform a click action when the touch is released
                        if (!moved) {
                            v.post {
                                switchSpeakerOnOff()
                                //v.performClick()
                            }
                            return false
                        } else {
                            moved = false
                            return true
                        }
                    }
                    else -> return false
                }
            }
        }
        )

    }
}

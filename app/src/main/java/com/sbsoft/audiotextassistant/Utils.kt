package com.sbsoft.audiotextassistant

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.hardware.display.DisplayManagerCompat
import java.util.ArrayDeque
import java.util.Deque

object Utils {
    fun findNodeByCoordinates(rootNode: AccessibilityNodeInfo, x: Int, y: Int, viewgroup: ViewGroup): AccessibilityNodeInfo? {
        val point = convertViewCoordinatesToScreen(viewgroup, x, y)
        return findNodeIterative(rootNode, point.x, point.y)
    }

    fun findScrollableNodeByCoordinates(rootNode: AccessibilityNodeInfo, x: Int, y: Int, viewgroup: ViewGroup): AccessibilityNodeInfo? {
        val point = convertViewCoordinatesToScreen(viewgroup, x, y)
        return findScrollableNodeIterative(rootNode, point.x, point.y)
    }

    private fun findScrollableNodeIterative(root: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        var smallestScrollableNode: AccessibilityNodeInfo? = root
        var smallestRect = Rect()
        root.getBoundsInScreen(smallestRect)

        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)

        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            val nodeRect = Rect()
            node.getBoundsInScreen(nodeRect)
            if (nodeRect.contains(x, y)) {
                if (
                    (nodeRect.width() <= smallestRect.width() && nodeRect.height() <= smallestRect.height()) &&
                    (nodeRect.left >= smallestRect.left && nodeRect.top >= smallestRect.top)
                ) {
                    if (node.isScrollable) {
                        smallestScrollableNode = node
                        smallestRect = nodeRect
                    }
                }

                for (i in 0 until node.childCount) {
                    if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
                }
            }
        }

        return if (smallestScrollableNode?.isScrollable == true) smallestScrollableNode else null
    }


    private fun checkFocusable(root: AccessibilityNodeInfo): Boolean {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            if (node.isAccessibilityFocused) return true
            for (i in 0 until node.childCount) {
                if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
            }
        }
        return false
    }

    private fun findNodeIterative(root: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        var smallestRectNode: AccessibilityNodeInfo? = root
        var smallestRect = Rect()
        smallestRectNode?.getBoundsInScreen(smallestRect)

        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)

        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            val nodeRect = Rect()
            node.getBoundsInScreen(nodeRect)
            if (nodeRect.contains(x, y)) {

                if (
                    (nodeRect.width() <= smallestRect.width() && nodeRect.height() <= smallestRect.height()) &&
                    (nodeRect.left >= smallestRect.left && nodeRect.top >= smallestRect.top)
                ) {
                    smallestRectNode = node
                    smallestRect = nodeRect
                }

                for (i in 0 until node.childCount) {
                    if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
                }
            }
        }

        return smallestRectNode
    }

    fun convertViewCoordinatesToScreen(view: View, x: Int, y: Int): Point {
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val screenX = location[0] + x
        val screenY = location[1] + y

        Log.d("XDEBUG loc0", location[0].toString())
        Log.d("XDEBUG loc1", location[1].toString())
        return Point(screenX, screenY)
    }

    fun printTree(root: AccessibilityNodeInfo) {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            Log.d(
                "XDEBUG tree",
                node.viewIdResourceName.toString() + " / " + node.text.toString() + " / " + '/' + node.contentDescription + "/" + node.isClickable + "/" + node.isContextClickable + "/scr/" + node.isScrollable
            )
            //Log.d("XDEBUG tree", node.actionList.toString())
            /*
            if (node.actionList.firstOrNull {it == ACTION_SCROLL_FORWARD || it == ACTION_SCROLL_BACKWARD } != null) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                Log.d("XDEBUG tree", "************, ${rect}")
                Log.d("XDEBUG tree", node.className.toString() + " / " + node.text.toString() + " / " + '/' + node.contentDescription + "/" + node.isClickable + "/" + node.isContextClickable+"/scr/"+node.isScrollable)
            }
            */
            val nodeRect = Rect()
            node.getBoundsInScreen(nodeRect)
            node?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            //Log.d("XDEBUG rect", nodeRect.toString())
            for (i in 0 until node.childCount) {
                if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
            }
        }
    }

    fun screenMetrics(context: Context): Pair<Int, Int> {
        val tag = "XDEBUG"
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val defaultDisplay =
                DisplayManagerCompat.getInstance(context).getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = context.createDisplayContext(defaultDisplay!!)

            val width = displayContext.resources.displayMetrics.widthPixels
            val height = displayContext.resources.displayMetrics.heightPixels

            Log.e(tag, "width (ANDOIRD R/ABOVE): $width")
            Log.e(tag, "height (ANDOIRD R/ABOVE) : $height")
            return Pair(width, height)

        } else {

            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels

            Log.e(tag, "width (BOTTOM ANDROID R): $width")
            Log.e(tag, "height (BOTTOM ANDROID R) : $height")
            return Pair(width, height)
        }
    }

    fun getStableInsets(rootView: View): Rect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val rootWindowsInsets = rootView.rootWindowInsets ?: return Rect(0, 0, 0, 0)
            val insets = rootWindowsInsets.getInsets(WindowInsets.Type.systemBars())
            return Rect(insets.left, insets.top, insets.right, insets.bottom)
        } else {
            val windowInsets = rootView.rootWindowInsets
            if (windowInsets != null) {
                @Suppress("DEPRECATION")
                Rect(
                    windowInsets.stableInsetLeft, windowInsets.stableInsetTop,
                    windowInsets.stableInsetRight, windowInsets.stableInsetBottom
                )
            } else {
                // TODO: Edge case, you might want to return a default value here
                Rect(0, 0, 0, 0)
            }
        }
    }


}
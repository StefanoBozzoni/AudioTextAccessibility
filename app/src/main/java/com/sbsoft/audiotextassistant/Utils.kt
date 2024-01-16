package com.sbsoft.audiotextassistant

import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque
import java.util.Deque

object Utils {
    fun findNodeByCoordinates(rootNode: AccessibilityNodeInfo, x: Int, y: Int, viewgroup: ViewGroup): AccessibilityNodeInfo? {
        val point = convertViewCoordinatesToScreen(viewgroup, x, y)
        return findNodeIterative(rootNode, point.x, point.y)
    }

    fun findScrollableNodeByCoordinates(rootNode: AccessibilityNodeInfo, x: Int, y: Int, viewgroup: ViewGroup): AccessibilityNodeInfo? {
        val point = convertViewCoordinatesToScreen(viewgroup, x, y)
        return findNodeIterative(rootNode, point.x, point.y)
    }

    private fun findScrollableNodeIterative(root: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        var smallestScrollableNode: AccessibilityNodeInfo? = null
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
                    if (node.isScrollable) smallestScrollableNode = node
                    smallestRect = nodeRect
                }

                for (i in 0 until node.childCount) {
                    if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
                }
            }
        }

        return if (smallestScrollableNode?.isScrollable == true) smallestScrollableNode else null
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
            Log.d("XDEBUG text", node.className.toString() + " / " + node.text.toString() + " / " + '/' + node.contentDescription + "/" + node.isClickable + "/" + node.isContextClickable)

            val nodeRect = Rect()
            node.getBoundsInScreen(nodeRect)
            Log.d("XDEBUG rect", nodeRect.toString())

            for (i in 0 until node.childCount) {
                if ((node != null) && (node.getChild(i) != null)) deque.addLast(node.getChild(i))
            }
        }
    }

}
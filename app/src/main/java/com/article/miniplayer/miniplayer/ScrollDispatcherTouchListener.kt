package com.article.miniplayer.miniplayer

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class ScrollDispatcherTouchListener(
    private val gestureDetector: GestureDetector? = null,
    private val config: MotionScrollConfig,
    private val onScrollDispatcher: OnScrollDispatcher
) : View.OnTouchListener {
    private val clickThreshold: Int = 10
    private var startX = 0f
    private var startY: Float? = null

    private var maxDragDistance = config.maxDragDistance

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        gestureDetector?.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                if (startY == null) {
                    startY = event.rawY
                }
                val deltaY = event.rawY - startY!!

                if (deltaY > clickThreshold) {
                    val progress = (deltaY / maxDragDistance).coerceIn(0f, 1f)
                    if (config.canScrollDown()) {
                        onScrollDispatcher.onScrollDown(progress, config)
                    }
                } else if (deltaY < -clickThreshold) {
                    // User is scrolling up (finger moving up)
                    val progress = (-deltaY / maxDragDistance).coerceIn(0f, 1f)
                    if (config.canScrollUp()) {
                        onScrollDispatcher.onScrollUp(progress, config)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (startY == null) {
                    startY = event.rawY
                }
                val deltaX = abs(event.rawX - startX)
                val deltaY = event.rawY - startY!!
                val absDeltaY = abs(deltaY)

                if (deltaX < clickThreshold && absDeltaY < clickThreshold) {
                    v.performClick()
                } else {
                    if (deltaY > clickThreshold) {
                        onScrollDispatcher.onCancelScroll(true, config)
                    } else if (deltaY < -clickThreshold) {
                        onScrollDispatcher.onCancelScroll(false, config)
                    }
                }

                startY = null // Reset startY after the touch event
            }
        }
        return false
    }
}


data class MotionScrollConfig(
    val canScrollUp: () -> Boolean = { true },
    val canScrollDown: () -> Boolean = { true },
    val maxDragDistance: Float = 1000f,
)

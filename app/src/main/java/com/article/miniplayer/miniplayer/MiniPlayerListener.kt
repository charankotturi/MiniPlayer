package com.article.miniplayer.miniplayer

import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class MiniPlayerListener(
    private val parentWidth: Int,
    private val parentHeight: Int,
    private var constraintSet: ConstraintSet?,
) : View.OnTouchListener {
    private var dX = 0f
    private var dY = 0f

    private var startX = 0f
    private var startY = 0f
    private val clickThreshold = 10 // in pixels

    private var isStartDragging = false // in pixels

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isStartDragging = true
                dX = view.x - event.rawX
                dY = view.y - event.rawY
                startX = event.rawX
                startY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                if (isStartDragging) {
                    view.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                }
            }

            MotionEvent.ACTION_UP -> {
                isStartDragging = false
                val deltaX = abs(event.rawX - startX)
                val deltaY = abs(event.rawY - startY)

                if (deltaX < clickThreshold && deltaY < clickThreshold) {
                    view.performClick()
                } else {
                    snapToNearestCorner(view)
                }
            }
        }
        return true
    }

    fun setStartPositionConstraintSet(constraintSet: ConstraintSet?) {
        this.constraintSet = constraintSet
    }

    private fun snapToNearestCorner(view: View) {
        val midX = parentWidth / 2
        val midY = parentHeight / 2

        val paddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            view.resources.displayMetrics
        ).toInt()

        val finalX = if (view.x + view.width / 2 < midX) {
            paddingPx.toFloat()
        } else {
            (parentWidth - view.width - paddingPx).toFloat()
        }

        val finalY = if (view.y + view.height / 2 < midY) {
            paddingPx.toFloat()
        } else {
            (parentHeight - view.height - paddingPx).toFloat()
        }

        view.animate().x(finalX).y(finalY).setDuration(300).start()

        view.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            delay(300)
            constraintSet?.apply {
                constrainViewToCurrentPosition(
                    this, view, finalX, finalY
                )
            }
        }
    }
}


private fun constrainViewToCurrentPosition(
    constraintSet: ConstraintSet, view: View, x: Float, y: Float
): ConstraintSet {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    constraintSet.constrainWidth(view.id, view.width)
    constraintSet.constrainHeight(view.id, view.height)
    constraintSet.setMargin(view.id, ConstraintSet.START, view.left)
    constraintSet.setMargin(view.id, ConstraintSet.TOP, view.top)
    // optionally set translationX/Y if needed
    constraintSet.setTranslationX(view.id, view.translationX)
    constraintSet.setTranslationY(view.id, view.translationY)
    return constraintSet
}

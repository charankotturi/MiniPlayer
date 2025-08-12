package com.article.miniplayer.miniplayer

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import com.article.miniplayer.R


class PlayerScreenMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), OnScrollDispatcher {

    private var videoPlayerState: VideoStates = VideoStates.Expanded()

    override fun onScrollDown(progress: Float, config: MotionScrollConfig?) {
        Log.i("GAME", "onScrollDown: $progress, config: $config")
        if (videoPlayerState is VideoStates.Collapsed) {
            setTransition(R.id.collapsed, R.id.expanded)
            setProgress(progress)
        } else if (videoPlayerState is VideoStates.Expanded) {
            setTransition(R.id.expanded, R.id.miniplayer)
            setProgress(progress)
        }
    }

    override fun onScrollUp(progress: Float, config: MotionScrollConfig?) {
        Log.i("GAME", "onScrollUp: $progress, config: $config")
        if (videoPlayerState is VideoStates.Expanded) {
            setTransition(R.id.expanded, R.id.collapsed)
            setProgress(progress)
        }
    }

    override fun onCancelScroll(isDown: Boolean, config: MotionScrollConfig?) {
        Log.i("GAME", "onCancelScroll: $isDown, config: $config")
        snapToNearestState(isDown)
    }

    /**
     * On release, snap to the "start" (expanded) or "end" (collapsed) depending on which is closer.
     * We'll use MotionLayout transitions, so it animates to the final state.
     */
    private fun snapToNearestState(dragDown: Boolean) {
        if (dragDown) {
            // If final move is downward, let's favor expanded (start)
            // e.g. if progress < 0.8 => expand; else collapse
            if (progress < 0.1f) {
                transitionToStart() // expanded
            } else {
                transitionToEnd() // collapse
            }
        } else {
            // Final move is upward => favor collapsed (end)
            // e.g. if progress > 0.2 => collapse; else expand
            if (progress > 0.1f) {
                transitionToEnd()
            } else {
                transitionToStart()
            }
        }
    }

    fun setCurrentTransition(currVideoPlayerState: VideoStates) {
        videoPlayerState = currVideoPlayerState
    }
}

fun View.bindScrollTo(
    gestureDetector: GestureDetector? = null,
    config: MotionScrollConfig = MotionScrollConfig(),
    dispatcher: OnScrollDispatcher
) {
    val touchListener = ScrollDispatcherTouchListener(gestureDetector, config, dispatcher)
    this.setOnTouchListener(touchListener)
}
package com.article.miniplayer.miniplayer

interface OnScrollDispatcher {

    fun onScrollDown(progress: Float, config: MotionScrollConfig?)

    fun onScrollUp(progress: Float, config: MotionScrollConfig?)

    fun onCancelScroll(isDown: Boolean, config: MotionScrollConfig?)
}

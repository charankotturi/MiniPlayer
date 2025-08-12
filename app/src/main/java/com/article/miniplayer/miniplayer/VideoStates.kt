package com.article.miniplayer.miniplayer

sealed class VideoStates(val value: String, val analyticValue: String) {
    class Expanded : VideoStates("Expanded", "EXPANDED")

    class Collapsed : VideoStates("Collapsed", "COLLAPSED")

    class MiniPlayer : VideoStates("MiniPlayer", "MINI_PLAYER")

    // Future states
    class Fullscreen : VideoStates("Fullscreen", "FULLSCREEN")

    class PIPMode : VideoStates("PIP", "PIP")
}

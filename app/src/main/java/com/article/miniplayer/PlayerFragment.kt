package com.article.miniplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.article.miniplayer.databinding.FragmentPlayerScreenBinding
import com.article.miniplayer.miniplayer.MiniPlayerListener
import com.article.miniplayer.miniplayer.MotionScrollConfig
import com.article.miniplayer.miniplayer.VideoStates
import com.article.miniplayer.miniplayer.bindScrollTo

class PlayerFragment : Fragment(), TransitionListener {

    private val videoUrl =
        "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"

    // Default state
    private var currVideoPlayerState: VideoStates = VideoStates.Expanded()

    private val miniPlayerTouchListener: MiniPlayerListener by lazy {
        MiniPlayerListener(
            parentWidth = binding.root.width,
            parentHeight = binding.root.height,
            constraintSet = binding.motionLayout.getConstraintSet(R.id.miniplayer),
        )
    }

    private var snapTransition = false

    companion object {
        const val TAG = "PlayScreenFragment"
        fun newInstance(): PlayerFragment {
            val args = Bundle()
            val playScreenFragment = PlayerFragment()
            playScreenFragment.arguments = args
            return playScreenFragment
        }
    }

    private lateinit var binding: FragmentPlayerScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.motionLayout.setCurrentTransition(currVideoPlayerState)

        initMiniPlayerMotionLayout()
        attachDragListeners()
        initPlayer()
    }

    private fun initPlayer() {
        val player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(videoUrl.toUri())
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    private fun initMiniPlayerMotionLayout() {
        binding.run {
            motionLayout.setTransitionListener(this@PlayerFragment)
        }
    }

    private fun attachDragListeners() {
        binding.run {
            playerView.bindScrollTo(
                config = MotionScrollConfig(
                    canScrollDown = {
                        true
                    }),
                dispatcher = motionLayout,
            )
            listView.bindScrollTo(
                config = MotionScrollConfig(
                    canScrollDown = {
                        currVideoPlayerState is VideoStates.Collapsed
                    }),
                dispatcher = motionLayout,
            )
        }
    }

    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
        snapTransition = true
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float
    ) {
        if (progress > 0.5f) {
            if (snapTransition) {
                motionLayout.post { motionLayout.transitionToEnd() }
                snapTransition = false
            }
        }
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
        binding.run {
            snapTransition = true
            setMotionVideoState(currentId)

            // Check the completed state and update the resize mode
            when (currentId) {
                R.id.miniplayer -> {
                    playerView.setOnClickListener {
                        if (currVideoPlayerState is VideoStates.MiniPlayer) {
                            motionLayout.setTransition(R.id.miniplayer, R.id.expanded)
                            motionLayout.transitionToEnd()
                        }
                    }

                    miniPlayerTouchListener.setStartPositionConstraintSet(
                        motionLayout.getConstraintSet(
                            R.id.miniplayer
                        )
                    )
                    playerView.setOnTouchListener(miniPlayerTouchListener)
                }

                else -> {
                    attachDragListeners()
                }
            }
        }
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float
    ) {
    }

    override fun onDestroy() {
        binding.playerView.player?.release()
        super.onDestroy()
    }

    private fun setMotionVideoState(currentStateId: Int) {
        when (currentStateId) {
            R.id.expanded -> {
                currVideoPlayerState = VideoStates.Expanded()
            }

            R.id.collapsed -> {
                currVideoPlayerState = VideoStates.Collapsed()
            }

            R.id.miniplayer -> {
                currVideoPlayerState = VideoStates.MiniPlayer()
            }
        }
        binding.motionLayout.setCurrentTransition(currVideoPlayerState)
    }
}
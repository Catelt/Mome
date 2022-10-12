package com.catelt.mome.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.catelt.mome.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


internal class CustomPlayerUiController(
    context: Context,
    customPlayerUi: View,
    youTubePlayer: YouTubePlayer,
    youTubePlayerView: YouTubePlayerView
) :
    AbstractYouTubePlayerListener() {
    private val playerUi: View
    private val context: Context
    private val youTubePlayer: YouTubePlayer
    private val youTubePlayerView: YouTubePlayerView
    private val playerTracker: YouTubePlayerTracker
    var isMute: Boolean = true

    private lateinit var panel: View
    private lateinit var layoutLoading: FrameLayout
    private lateinit var layoutPlayPause: View
    private lateinit var layoutSeekBar: View
    private lateinit var seekBarProgress: SeekBar
    private lateinit var seekBarProgressMini: SeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtAge: TextView
    private lateinit var txtTypeVideo: TextView
    private lateinit var btnPlay: ImageView
    private lateinit var btnPause: ImageView
    private lateinit var btnReply: ImageView
    private lateinit var btnAudio: ImageView
    private lateinit var imgBackdrop: ImageView

    private var fadeOut: Runnable = Runnable{
        setVisionControl(false)
    }

    init {
        playerUi = customPlayerUi
        this.context = context
        this.youTubePlayer = youTubePlayer
        this.youTubePlayerView = youTubePlayerView
        playerTracker = YouTubePlayerTracker()
        youTubePlayer.addListener(playerTracker)
        initViews(customPlayerUi)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        panel = view.findViewById(R.id.panel)
        layoutLoading = view.findViewById(R.id.layoutLoading)
        layoutPlayPause = view.findViewById(R.id.layoutPlayPause)
        layoutSeekBar = view.findViewById(R.id.layoutSeekBar)
        seekBarProgress = view.findViewById(R.id.seekBarProgress)
        txtCurrentTime = view.findViewById(R.id.txtCurrentTime)
        txtAge = view.findViewById(R.id.txtAge)
        txtTypeVideo = view.findViewById(R.id.txtTypeVideo)
        btnPlay = view.findViewById(R.id.btnPlay)
        btnPause = view.findViewById(R.id.btnPause)
        btnReply = view.findViewById(R.id.btnReply)
        btnAudio = view.findViewById(R.id.btnAudio)
        imgBackdrop = view.findViewById(R.id.imgBackdrop)

        seekBarProgressMini = view.findViewById(R.id.seekBarProgressMini)
        btnPlay.setOnClickListener {
            handlePlayPause()
        }
        btnPause.setOnClickListener {
            handlePlayPause()
        }

        btnReply.setOnClickListener {
            btnPlay.callOnClick()
            setUIReply(false)
        }

        btnAudio.setOnClickListener {
            isMute = !isMute
            setUIButtonAudio()
        }

        seekBarProgress.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN){
                setVisionControl(true)
                youTubePlayer.pause()
            }
            false
        }

        seekBarProgress.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){
                    if (p1.toFloat() > playerTracker.videoDuration - LIMIT_TIME_END){
                        youTubePlayer.seekTo(playerTracker.videoDuration - LIMIT_TIME_END)
                    }
                    else{
                        youTubePlayer.seekTo(p1.toFloat())
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                youTubePlayer.play()
                setAutoHideController()
            }

        })
        panel.setOnClickListener {
            setVisionControl(true)
            setAutoHideController()
        }

        setUIButtonAudio()
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        layoutLoading.visibility = View.GONE
    }

    private fun handlePlayPause(){
        val isPlaying = playerTracker.state == PlayerState.PLAYING
        if (isPlaying) {
            youTubePlayer.pause()
        } else {
            youTubePlayer.play()
        }
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
        setUIButtonPlayPause(state == PlayerState.PLAYING)

        when(state) {
            PlayerState.PLAYING -> {
                layoutLoading.visibility = View.GONE
                imgBackdrop.visibility = View.GONE
                panel.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.transparent)
                )
            }
            PlayerState.PAUSED -> {
                panel.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.transparent)
                )
            }
            PlayerState.ENDED -> {
                panel.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.black)
                )
                setUIReply(true)
                imgBackdrop.visibility = View.VISIBLE
            }
            PlayerState.BUFFERING -> {
                panel.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.black)
                )
                setVisionControl(false)
            }
            else -> {
            }
        }
    }

    private fun setUIButtonAudio() {
        if (isMute){
            btnAudio.setImageResource(R.drawable.ic_mute)
            youTubePlayer.mute()
        }else{
            btnAudio.setImageResource(R.drawable.ic_unmute)
            youTubePlayer.unMute()
        }
    }

    private fun setUIReply(isReply: Boolean){
        if (isReply){
            btnReply.visibility = View.VISIBLE
            btnAudio.visibility = View.GONE
            layoutPlayPause.visibility = View.GONE
            layoutSeekBar.visibility = View.GONE
            seekBarProgressMini.visibility = View.GONE
        }
        else{
            btnReply.visibility = View.GONE
            btnAudio.visibility = View.VISIBLE
            setVisionControl(false)
        }
    }

    private fun setAutoHideController(){
        panel.handler.removeCallbacks(fadeOut)
        panel.handler.postDelayed(fadeOut, DEFAULT_TIME_DELAY)
    }

    private fun setVisionControl(isVision: Boolean){
        if (isVision){
            panel.handler.removeCallbacks(fadeOut)
            panel.handler.removeCallbacksAndMessages(null)
            layoutPlayPause.visibility = View.VISIBLE
            layoutSeekBar.visibility = View.VISIBLE
            seekBarProgressMini.visibility = View.GONE
        }else{
            layoutPlayPause.visibility = View.GONE
            layoutSeekBar.visibility = View.GONE
            seekBarProgressMini.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        txtCurrentTime.text = formatCurrentTime(second.toInt())
        seekBarProgress.progress = second.toInt()
        seekBarProgressMini.progress = second.toInt()
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        seekBarProgress.max = duration.toInt()
        seekBarProgressMini.max = duration.toInt()
    }

    private fun setUIButtonPlayPause(isPlaying: Boolean){
        if (isPlaying){
            btnPause.visibility = View.VISIBLE
            btnPlay.visibility = View.GONE
        }
        else{
            btnPause.visibility = View.GONE
            btnPlay.visibility = View.VISIBLE
        }
    }

    private fun formatCurrentTime(value: Int): String {
        val min = value/60
        val second = value%60

        val strMin = if (min < 10){
            "0${min}"
        }
        else{
            "$min"
        }
        val strSecond = if (second < 10){
            "0${second}"
        }
        else{
            "$second"
        }
        return "$strMin:$strSecond"
    }

    companion object{
        private const val DEFAULT_TIME_DELAY = 5000L
        private const val LIMIT_TIME_END = 10
    }
}
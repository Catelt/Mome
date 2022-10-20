package com.catelt.mome.ui.videoplayer

import android.app.AlertDialog
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.*
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentVideoPlayerBinding
import com.catelt.mome.utils.BUNDLE_TITLE_MEDIA
import com.catelt.mome.utils.BUNDLE_URL_MEDIA
import com.catelt.mome.utils.brightness.BrightnessUtils
import com.catelt.mome.utils.brightness.changeAppScreenBrightnessValue
import com.catelt.mome.utils.brightness.changeBrightnessToDefault
import com.catelt.mome.utils.brightness.changeMaxBrightness
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class VideoPlayerFragment : BaseFragment<FragmentVideoPlayerBinding>(
    FragmentVideoPlayerBinding::inflate
) {
    private lateinit var exoPlayer: ExoPlayer

    private var saveBright = 0F
    private lateinit var brightnessUtils: BrightnessUtils
    private var mainReceiver: BroadcastReceiver? = null

    override var isHideBottom = true
    override var isFullScreen = true

    private var titleMedia: String = ""
    private var urlMedia: String = ""

    private var optionSpeed: Int = 2

    //Argument custom control view
    private lateinit var mainContainer: View
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var txtTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnSpeed: LinearLayout
    private lateinit var txtTitleSpeed: TextView
    private lateinit var layoutEnd: FrameLayout
    private lateinit var optionDialogSpeed: Array<String>
    private lateinit var btnPlay: View
    private lateinit var btnPause: View
    private lateinit var btnRewind: View
    private lateinit var btnForward: View

    override fun setUpArgument(bundle: Bundle) {
        bundle.apply {
            titleMedia = getString(BUNDLE_TITLE_MEDIA, "")
            urlMedia = getString(BUNDLE_URL_MEDIA, "")
        }
    }

    override fun setUpViews() {
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
            .also {
                binding.playView.player = it
            }
        brightnessUtils = BrightnessUtils.init(requireContext())
        binding.root.apply {
            mainContainer = findViewById(R.id.mainContainer)
            seekBarBrightness = findViewById(R.id.seekBarBrightness)
            txtTitle = findViewById(R.id.txtTitle)
            btnBack = findViewById(R.id.btnBack)
            btnSpeed = findViewById(R.id.btnSpeed)
            txtTitleSpeed = findViewById(R.id.txtTitleSpeed)
            layoutEnd = findViewById(R.id.layoutEnd)
            optionDialogSpeed = resources.getStringArray(R.array.speed_dialog)
            btnPlay = findViewById(com.google.android.exoplayer2.R.id.exo_play)
            btnPause = findViewById(com.google.android.exoplayer2.R.id.exo_pause)
            btnForward = findViewById(com.google.android.exoplayer2.R.id.exo_ffwd)
            btnRewind = findViewById(com.google.android.exoplayer2.R.id.exo_rew)
        }

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                setBrightness((p1.toFloat()) / 100)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        binding.playView.doOnLayout {
            updatePictureInPictureParams()
        }

        val audioAttributes = com.google.android.exoplayer2.audio.AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()


        exoPlayer.setAudioAttributes(audioAttributes, true)

        txtTitle.text = titleMedia
        txtTitleSpeed.text =
            getString(R.string.text_title_speed, optionDialogSpeed[2].split("(")[0])

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSpeed.setOnClickListener {
            exoPlayer.pause()
            val alertDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            alertDialog.setNegativeButton(getString(R.string.negative_option_speed_dialog), null)
            alertDialog.setOnDismissListener {
                exoPlayer.play()
            }
            alertDialog.setSingleChoiceItems(
                optionDialogSpeed,
                optionSpeed
            ) { dialogInterface, i ->
                setTitleSpeed(i)
                when (i) {
                    0 -> {
                        exoPlayer.setPlaybackSpeed(0.5f)
                        dialogInterface.dismiss()
                    }
                    1 -> {
                        exoPlayer.setPlaybackSpeed(0.75f)
                        dialogInterface.dismiss()
                    }
                    2 -> {
                        exoPlayer.setPlaybackSpeed(1f)
                        dialogInterface.dismiss()
                    }
                    3 -> {
                        exoPlayer.setPlaybackSpeed(1.25f)
                        dialogInterface.dismiss()
                    }
                    4 -> {
                        exoPlayer.setPlaybackSpeed(1.5f)
                        dialogInterface.dismiss()
                    }
                }
            }
            alertDialog.create().show()
        }

        if (saveBright < 0.01) {
            seekBarBrightness.progress = 100
        } else {
            saveBright = getCurrentBright()
            seekBarBrightness.progress = (saveBright * 100).toInt()
        }


        val mediaItem = MediaItem.fromUri(urlMedia.toUri())

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        if (activity?.isInPictureInPictureMode == false) {
                            binding.layoutLoading.visibility = View.VISIBLE
                        }
                        binding.playView.hideController()
                    }
                    Player.STATE_READY -> {
                        binding.layoutLoading.visibility = View.GONE
                        layoutEnd.visibility = View.GONE
                        exoPlayer.play()
                        binding.playView.showController()
                    }
                    Player.STATE_ENDED -> {
                        layoutEnd.visibility = View.VISIBLE
                    }
                    Player.STATE_IDLE -> {

                    }
                }
            }
        })
        binding.playView.doOnLayout { updatePictureInPictureParams() }
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    private fun setTitleSpeed(option: Int) {
        txtTitleSpeed.text =
            getString(R.string.text_title_speed, optionDialogSpeed[option].split("(")[0])
        optionSpeed = option
    }

    private fun getCurrentBright(): Float {
        return BrightnessUtils.currentAppBrightnessValue
    }

    private fun setBrightness(value: Float) {
        saveBright = value
        activity?.window?.changeAppScreenBrightnessValue(value)
    }

    private fun hideSystemUI() {
        activity?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            WindowInsetsControllerCompat(it, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun showSystemUI() {
        activity?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, true)
            WindowInsetsControllerCompat(
                it,
                binding.root
            ).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun setUIPictureInPicture(isPipMode: Boolean) {
        mainContainer.visibility = setVisionView(!isPipMode)
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hideSystemUI()
            if (saveBright < 0.01) {
                activity?.window?.changeMaxBrightness()
            }
        }
    }

    private fun releasePlayer() {
        exoPlayer.stop()
        exoPlayer.release()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.pause()
    }

    override fun onDestroy() {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showSystemUI()
            it.window.changeBrightnessToDefault()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.setPictureInPictureParams(
                    PictureInPictureParams.Builder().setAutoEnterEnabled(false).build()
                )
            }
        }
        releasePlayer()
        super.onDestroy()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        setUIPictureInPicture(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            activity?.window?.changeBrightnessToDefault()
            mainReceiver = makeMainReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction(BROADCAST_PLAY)
            intentFilter.addAction(BROADCAST_PAUSE)
            intentFilter.addAction(BROADCAST_REWIND)
            intentFilter.addAction(BROADCAST_FORWARD)
            activity?.registerReceiver(mainReceiver, intentFilter)
        } else {
            setBrightness(saveBright)
            mainReceiver?.let {
                activity?.unregisterReceiver(mainReceiver)
                mainReceiver = null
            }
        }
    }

    private fun updatePictureInPictureParams(isPlaying: Boolean = true): PictureInPictureParams {
        val aspectRatio = Rational(binding.playView.width, binding.playView.height)
        val sourceRectHint = Rect()
        val actions = ArrayList<RemoteAction>()

        actions.add(createAction(BROADCAST_REWIND, R.drawable.ic_skip_backward))
        if (isPlaying) actions.add(createAction(BROADCAST_PAUSE, R.drawable.ic_pause))
        else actions.add(createAction(BROADCAST_PLAY, R.drawable.ic_play))
        actions.add(createAction(BROADCAST_FORWARD, R.drawable.ic_skip_forward))

        binding.playView.getGlobalVisibleRect(sourceRectHint)
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PictureInPictureParams.Builder()
                .setAutoEnterEnabled(true)
                .setAspectRatio(aspectRatio)
                .setSourceRectHint(sourceRectHint)
                .setActions(actions)
                .build()
        } else {
            PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .setSourceRectHint(sourceRectHint)
                .setActions(actions)
                .build()
        }

        activity?.setPictureInPictureParams(params)
        return params
    }

    private fun createAction(type: String, iconId: Int): RemoteAction {
        val actionIntent = Intent(type)
        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            REQUEST_CODE, actionIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val icon = Icon.createWithResource(context, iconId)
        return RemoteAction(icon, "", "", pendingIntent)
    }

    private fun makeMainReceiver(): BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BROADCAST_PLAY -> {
                    exoPlayer.play()
                    binding.playView.doOnLayout {
                        updatePictureInPictureParams(true)
                    }
                }
                BROADCAST_PAUSE -> {
                    exoPlayer.pause()
                    binding.playView.doOnLayout {
                        updatePictureInPictureParams(false)
                    }
                }
                BROADCAST_REWIND -> {
                    btnRewind.callOnClick()
                }
                BROADCAST_FORWARD -> {
                    btnForward.callOnClick()
                }
            }
        }
    }

    companion object {
        fun newInstance(titleMedia: String, urlMedia: String) = VideoPlayerFragment().apply {
            arguments = Bundle().apply {
                putString(BUNDLE_TITLE_MEDIA, titleMedia)
                putString(BUNDLE_URL_MEDIA, urlMedia)
            }
        }

        private const val BROADCAST_PLAY = "play"
        private const val BROADCAST_PAUSE = "pause"
        private const val BROADCAST_REWIND = "rewind"
        private const val BROADCAST_FORWARD = "forward"
        private const val REQUEST_CODE = 1
    }
}
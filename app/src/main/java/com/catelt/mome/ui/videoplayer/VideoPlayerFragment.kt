package com.catelt.mome.ui.videoplayer

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player


class VideoPlayerFragment : BaseFragment<FragmentVideoPlayerBinding>(
    FragmentVideoPlayerBinding::inflate
) {
    private lateinit var exoPlayer: ExoPlayer

    private var saveBright = 0F
    private lateinit var brightnessUtils : BrightnessUtils
    override var isHideBottom = true

    private var titleMedia: String = ""
    private var urlMedia: String = ""

    private var optionSpeed: Int = 2


    //Argument custom control view
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var txtTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnSpeed: LinearLayout
    private lateinit var txtTitleSpeed: TextView
    private lateinit var layoutEnd: FrameLayout
    private lateinit var optionDialogSpeed: Array<String>

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
            seekBarBrightness = findViewById(R.id.seekBarBrightness)
            txtTitle = findViewById(R.id.txtTitle)
            btnBack = findViewById(R.id.btnBack)
            btnSpeed = findViewById(R.id.btnSpeed)
            txtTitleSpeed = findViewById(R.id.txtTitleSpeed)
            layoutEnd = findViewById(R.id.layoutEnd)
            optionDialogSpeed = resources.getStringArray(R.array.speed_dialog)
        }

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                setBrightness((p1.toFloat())/100)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        txtTitle.text = titleMedia
        txtTitleSpeed.text =
            getString(R.string.text_title_speed, optionDialogSpeed[2].split("(")[0])

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSpeed.setOnClickListener {
            exoPlayer.pause()
            val alertDialog = AlertDialog.Builder(requireContext(),R.style.DialogTheme)
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

        if (saveBright < 0.01){
            seekBarBrightness.progress = 100
        }
        else{
            saveBright = getCurrentBright()
            seekBarBrightness.progress = (saveBright*100).toInt()
        }



        val mediaItem = MediaItem.fromUri(urlMedia.toUri())

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        binding.layoutLoading.visibility = View.VISIBLE
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

    override fun onResume() {
        super.onResume()
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hideSystemUI()
            if (saveBright < 0.01){
                activity?.window?.changeMaxBrightness()
            }
        }
    }

    private fun releasePlayer() {
        exoPlayer.stop()
        exoPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showSystemUI()
            it.window.changeBrightnessToDefault()
        }
        releasePlayer()
    }

    companion object {
        fun newInstance(titleMedia: String, urlMedia: String) = VideoPlayerFragment().apply {
            arguments = Bundle().apply {
                putString(BUNDLE_TITLE_MEDIA, titleMedia)
                putString(BUNDLE_URL_MEDIA, urlMedia)
            }
        }
    }
}
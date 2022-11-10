package com.catelt.mome.ui.videoplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.core.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentVideoPlayerBinding
import com.catelt.mome.utils.*
import com.catelt.mome.utils.brightness.BrightnessUtils
import com.catelt.mome.utils.brightness.changeAppScreenBrightnessValue
import com.catelt.mome.utils.brightness.changeBrightnessToDefault
import com.catelt.mome.utils.brightness.changeMaxBrightness
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class VideoPlayerFragment : BaseFragment<FragmentVideoPlayerBinding>(
    FragmentVideoPlayerBinding::inflate
) {
    private lateinit var exoPlayer: ExoPlayer

    private var saveBright = 0F
    private lateinit var brightnessUtils: BrightnessUtils
    private var mainReceiver: BroadcastReceiver? = null

    override var isHideBottom = true
    override var isFullScreen = true

    private var isMovie = true

    private var optionSpeed: Int = 2
    private var currentPosition = 0
    private val adapter = EpisodeVideoPlayerAdapter()
    override val viewModel: VideoPlayerViewModel by viewModels()

    private var scaleGestureDetector: ScaleGestureDetector? = null

    private var thumbnail : Bitmap? = null
    //Argument custom control view
    private lateinit var mainContainer: View
    private lateinit var seekBarBrightness: SeekBar

    private lateinit var exoProgress: DefaultTimeBar
    private lateinit var previewFrameLayout: ViewGroup
    private lateinit var imgReview: ImageView

    private lateinit var txtPosition: TextView
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
    private lateinit var btnLock: View
    private lateinit var btnAudioSubtitle: View

    private lateinit var layoutEpisode: View
    private lateinit var layoutNextEp: View
    private lateinit var btnEpisode: View
    private lateinit var btnNextEp: View
    private lateinit var recycleView: RecyclerView

    override fun setUpArgument(bundle: Bundle) {
        bundle.apply {
            currentPosition = getInt(BUNDLE_CURRENT_EPISODE, 0)
        }
    }

    override fun setUpAdapter() {
        adapter.onMovieClicked = {
            viewModel.movies.value.let { list ->
                currentPosition = it
                setupVideo()
                layoutNextEp.visibility = setVisionView(currentPosition < list.size - 1)
            }
            btnBack.callOnClick()
            binding.playView.hideController()
        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            mediaTitle.observe(viewLifecycleOwner){
                txtTitle.text = it
            }
            lifecycleScope.launch {
                launch {
                    imageUrlParser.collectLatest {
                        adapter.imageUrlParser = it
                    }
                }
                launch {
//                    movies.collectLatest {
//                        if (it.isNotEmpty()) {
//                            adapter.submitList(it)
//                            isMovie = (it.size == 1 && it[0].stillPath?.isBlank() == true)
//
//                            setupVideo()
//
//                            if (!isMovie) {
//                                layoutEpisode.visibility = setVisionView(true)
//                                recycleView.adapter = adapter
//                                layoutNextEp.visibility =
//                                    setVisionView(currentPosition < it.size - 1)
//                            }
//                        }
//                    }
                    episode.collectLatest {
                        it?.let {
                            CoroutineScope(Dispatchers.IO).launch {
                                thumbnail = Glide.with(requireContext())
                                    .asBitmap()
                                    .load(it.thumbnail)
                                    .submit()
                                    .get()
                            }
                            setupVideo()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpViews() {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    btnBack.callOnClick()
                }
            })

        binding.root.apply {
            mainContainer = findViewById(R.id.mainContainer)
            seekBarBrightness = findViewById(R.id.seekBarBrightness)

            exoProgress = findViewById(com.google.android.exoplayer2.R.id.exo_progress)
            previewFrameLayout = findViewById(R.id.previewFrameLayout)
            imgReview = findViewById(R.id.imgPreview)

            txtPosition = findViewById(R.id.txtPosition)
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
            btnLock = findViewById(R.id.btnLock)
            btnAudioSubtitle = findViewById(R.id.btnAudioSubtitles)

            layoutEpisode = findViewById(R.id.layoutEpisodes)
            layoutNextEp = findViewById(R.id.layoutNextEp)
            btnEpisode = findViewById(R.id.btnEpisodes)
            btnNextEp = findViewById(R.id.btnNextEp)
            recycleView = findViewById(R.id.recyclerViewHorizontal)
        }

        exoPlayer = ExoPlayer.Builder(requireContext())
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
            .also {
                binding.playView.player = it
            }
        scaleGestureDetector =
            ScaleGestureDetector(requireContext(), CustomOnScaleGestureListener(binding.playView))

        brightnessUtils = BrightnessUtils.init(requireContext())

        mainContainer.setOnTouchListener { _, event ->
            scaleGestureDetector?.onTouchEvent(event)
            false
        }

        mainContainer.setOnClickListener {
            if (binding.playView.isControllerVisible) binding.playView.hideController()
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

        exoProgress.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                previewFrameLayout.visibility = View.VISIBLE
                val targetX = updatePreviewX(position.toInt(), exoPlayer.duration.toInt())
                previewFrameLayout.x = targetX.toFloat()
                updatePreviewFrame(position)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                previewFrameLayout.visibility = View.INVISIBLE
            }

            override fun onScrubStart(timeBar: TimeBar, position: Long) {}
        })

        binding.playView.setControllerVisibilityListener {
            if (it != 0 && recycleView.isVisible) {
                binding.playView.showController()
            }
        }

        binding.playView.doOnLayout {
            updatePictureInPictureParams()
        }

        val audioAttributes = com.google.android.exoplayer2.audio.AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()


        exoPlayer.setAudioAttributes(audioAttributes, true)

        txtTitleSpeed.text =
            getString(R.string.text_title_speed, optionDialogSpeed[2].split("(")[0])

        btnBack.setOnClickListener {
            if (recycleView.isVisible) {
                recycleView.visibility = setVisionView(false)
                exoPlayer.play()
            } else {
                findNavController().navigateUp()
            }
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

        btnLock.setOnClickListener {
            toast(getString(R.string.message_feature_coming_soon))
        }

        btnAudioSubtitle.setOnClickListener {
            toast(getString(R.string.message_feature_coming_soon))
        }

        btnEpisode.setOnClickListener {
            recycleView.visibility = setVisionView(true)
            recycleView.scrollToPosition(currentPosition)
            exoPlayer.pause()
        }

        btnNextEp.setOnClickListener {
            viewModel.movies.value.let { list ->
                if (currentPosition < list.size - 1) {
                    currentPosition++
                    setupVideo()
                }
            }
        }

        if (saveBright < 0.01) {
            seekBarBrightness.progress = 100
        } else {
            saveBright = getCurrentBright()
            seekBarBrightness.progress = (saveBright * 100).toInt()
        }

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

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying){
                    binding.playView.postDelayed(this@VideoPlayerFragment::getCurrentPlayerPosition, POLL_INTERVAL_MS);
                }
            }
        })
        binding.playView.doOnLayout { updatePictureInPictureParams() }
    }

    private fun getCurrentPlayerPosition() {
        txtPosition.text = ToString.timePosition(exoPlayer.currentPosition,exoPlayer.duration)

        if (exoPlayer.isPlaying) {
            binding.playView.postDelayed({ getCurrentPlayerPosition() }, POLL_INTERVAL_MS)
        }
    }


    private fun setupVideo() {
//        viewModel.movies.value.let { list ->
//            list[currentPosition].apply {
//                if (isMovie) {
//                    txtTitle.text = title
//                } else {
//                    val newTitle = "E$numberEpisode \"$title\""
//                    txtTitle.text = newTitle
//                }
//
//                val mediaItem = MediaItem.fromUri((url ?: "").toUri())
//                if (exoPlayer.mediaItemCount > 0) {
//                    exoPlayer.clearMediaItems()
//                }
//
//                exoPlayer.setMediaItem(mediaItem)
//                exoPlayer.prepare()
//                viewModel.getVideoFrame(url)
//            }
//        }
        viewModel.episode.value?.let { movie ->
            val mediaItem = MediaItem.fromUri((movie.url ?: "").toUri())
            if (exoPlayer.mediaItemCount > 0) {
                exoPlayer.clearMediaItems()
            }
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            lifecycleScope.launch {
                viewModel.timeAt.collectLatest {
                    if (it > 0){
                        exoPlayer.seekTo(it)
                    }
                }
            }
        }
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
            WindowInsetsControllerCompat(it, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun showSystemUI() {
        activity?.window?.let {
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

    override fun onPause() {
        requireActivity().lifecycleScope.launch {
            viewModel.addWatchTimeAt(exoPlayer.currentPosition)
        }
        super.onPause()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onDestroy() {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            showSystemUI()
            it.window.changeBrightnessToDefault()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.setPictureInPictureParams(
                    PictureInPictureParams.Builder().setAutoEnterEnabled(false).build()
                )
            }
        }
        viewModel.job?.cancel()
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
                .setSourceRectHint(sourceRectHint)
                .setActions(actions)
                .build()
        } else {
            PictureInPictureParams.Builder()
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

    private fun updatePreviewX(progress: Int, max: Int): Int {
        if (max == 0) {
            return 0
        }

        val parent = previewFrameLayout.parent as ViewGroup
        val layoutParams = previewFrameLayout.layoutParams as ViewGroup.MarginLayoutParams
        val offset = progress.toFloat() / max
        val minimumX: Int = previewFrameLayout.left
        val maximumX = (parent.width - parent.paddingRight - layoutParams.rightMargin)

        val previewPaddingRadius: Int =
            dpToPx(resources.displayMetrics, DefaultTimeBar.DEFAULT_SCRUBBER_DRAGGED_SIZE_DP).div(2)
        val previewLeftX = (exoProgress as View).left.toFloat()
        val previewRightX = (exoProgress as View).right.toFloat()
        val previewSeekBarStartX: Float = previewLeftX + previewPaddingRadius
        val previewSeekBarEndX: Float = previewRightX - previewPaddingRadius
        val currentX = (previewSeekBarStartX + (previewSeekBarEndX - previewSeekBarStartX) * offset)
        val startX: Float = currentX - previewFrameLayout.width / 2f
        val endX: Float = startX + previewFrameLayout.width

        return if (startX >= minimumX && endX <= maximumX) {
            startX.toInt()
        } else if (startX < minimumX) {
            minimumX
        } else {
            maximumX - previewFrameLayout.width
        }
    }

    private fun dpToPx(displayMetrics: DisplayMetrics, dps: Int): Int {
        return (dps * displayMetrics.density).toInt()
    }

    private fun updatePreviewFrame(position: Long) {
//        lifecycleScope.launch {
//            viewModel.mBitmapList.collectLatest{ list ->
//                list?.let {
//                    val position = (time / (VideoPlayerViewModel.TIME_PREVIEW/ 1000)).toInt()
//
//                    if (position < list.size){
//                        imgReview.load(list[position])
//                    }
//                    else{
//                        previewFrameLayout.visibility = View.INVISIBLE
//                    }
//                }
//            }
//        }
        Glide.with(imgReview)
            .load(thumbnail)
            .override(SIZE_ORIGINAL,SIZE_ORIGINAL)
            .transform(GlideThumbnailTransformation(position))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(imgReview)

    }


    companion object {
        fun newInstance(
            mediaId: Int,
            mediaSlug: String,
            currentPosition: Int = 0,
            mediaTitle: String? = null,
        ) = VideoPlayerFragment().apply {
            arguments = Bundle().apply {
                putInt(BUNDLE_ID_MEDIA, mediaId)
                putString(BUNDLE_SLUG_MEDIA, mediaSlug)
                putInt(BUNDLE_CURRENT_EPISODE, currentPosition)
                putString(BUNDLE_TITLE_MEDIA, mediaTitle)
            }
        }

        private const val POLL_INTERVAL_MS = 500L
        private const val BROADCAST_PLAY = "play"
        private const val BROADCAST_PAUSE = "pause"
        private const val BROADCAST_REWIND = "rewind"
        private const val BROADCAST_FORWARD = "forward"
        private const val REQUEST_CODE = 1
    }
}
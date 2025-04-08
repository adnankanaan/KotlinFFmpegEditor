package com.webapp.kotlin_webapp_video_editer.videoeditor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.webapp.kotlin_webapp_video_editer.R
import com.webapp.kotlin_webapp_video_editer.databinding.ActivityVideoEditorBinding
import com.webapp.kotlin_webapp_video_editer.dialogs.AddSoundDialog
import com.webapp.kotlin_webapp_video_editer.dialogs.AddTextDialog
import com.webapp.kotlin_webapp_video_editer.dialogs.AddTextDialog.Companion.textColorSet
import com.webapp.kotlin_webapp_video_editer.dialogs.AddTextDialog.Companion.textSet
import com.webapp.kotlin_webapp_video_editer.dialogs.AddTextDialog.Companion.textSizeSet
import com.webapp.kotlin_webapp_video_editer.dialogs.EmojiDialogFragment
import com.webapp.kotlin_webapp_video_editer.dialogs.FaceSwapImageDialog
import com.webapp.kotlin_webapp_video_editer.dialogs.PaintOptionsDialog
import com.webapp.kotlin_webapp_video_editer.dialogs.PaintOptionsDialog.Companion.paintColorSet
import com.webapp.kotlin_webapp_video_editer.dialogs.PaintOptionsDialog.Companion.paintSizeSet
import com.webapp.kotlin_webapp_video_editer.dialogs.ProssessingDialog
import com.webapp.kotlin_webapp_video_editer.ffmpegengin.FFmpegCanvasDraw
import com.webapp.kotlin_webapp_video_editer.ffmpegengin.FFmpegSoundSwap
import com.webapp.kotlin_webapp_video_editer.ffmpegengin.FFmpegTextAndEmoji
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.File
import java.net.URL

class VideoEditorFragment : Fragment() ,View.OnClickListener , SeekBar.OnSeekBarChangeListener {

    private var _binding: ActivityVideoEditorBinding? = null
    private val binding get() = _binding!!
    private var videoPath: Uri? = null

    private lateinit var videoPreview: VideoView
    private lateinit var videoProgress: ProgressBar
    private lateinit var seekVideoProgress: SeekBar
    private lateinit var downloadOptions: ImageView
    private val loadingDialog = ProssessingDialog()
    private val faceSwapDialog = FaceSwapImageDialog()
    private val addSoundDialog = AddSoundDialog()
    private val soundSwap = FFmpegSoundSwap()
    private lateinit var drawingCanvas:DrawingViewEditer
    private var job: Job? = null
    private var duration: Int = 0
    private var dPin: Dialog? = null
    private var emojiUrlSet: String? = null
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0
    private var downloadingProgress: ProgressBar? = null
    private var downloadingPersentProgressText: TextView? = null
    private val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    private var MAX_PROGRESS = 100
    private var msg = Message()
    private val addTextDialog = AddTextDialog()
    private val emojiDialog = EmojiDialogFragment()
    private val addPaintDialog = PaintOptionsDialog()
    private val canvasDrawer = FFmpegCanvasDraw()
    private val textEmojiDrawer = FFmpegTextAndEmoji()
    // Constants for better readability and maintainability
    private val LOCAL_VIDEO_DURATION_THRESHOLD_SECONDS = 500
    private val PROGRESS_UPDATE_DELAY_MS = 500L
    private lateinit var res: Resources
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // returns Map<String, Boolean> where String represents the
            // permission requested and boolean represents the
            // permission granted or not
            // iterate over each entry of map and take action needed for
            // each permission requested
            permissions.forEach { actionMap ->
                when (actionMap.key) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        if (actionMap.value) {
                            // permission granted continue the normal
                            // workflow of app
                            msg.message(requireContext(), "يمكنك الأن حفظ المقطع")
                            ///Log.i("DEBUG", "permission granted")
                        } else {
                            // if permission denied then check whether never
                            // ask again is selected or not by making use of
                            // !ActivityCompat.shouldShowRequest
                            // PermissionRationale(requireActivity(),
                            // Manifest.permission.CAMERA)
                            //  Log.i("DEBUG", "permission denied")
                        }
                    }
                }
            }
        }

    private fun showLoading(show: Boolean,msg:String = "") {
        // Check if the dialog is already added or showing
        ProssessingDialog.message = msg
        if (show) {
            if (!loadingDialog.isAdded) {
                loadingDialog.show(childFragmentManager, "ProssessingDialog")
            }
        } else {
            // Dismiss the dialog only if it is currently showing
            if (loadingDialog.isAdded) {
                loadingDialog.dismiss()
            }
        }
    }

    private fun startAnimation(ctx: Context, view: View, animation: Int, show: Boolean) {
        if (show) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.INVISIBLE
        }
        val anim = AnimationUtils.loadAnimation(ctx, animation)
        view.startAnimation(anim)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityVideoEditorBinding.inflate(inflater, container, false)
        UserData.current_lang = currentLang
        res = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().intent.getParcelableExtra("videoUri", Uri::class.java)
        } else {
            requireActivity().intent.getParcelableExtra("videoUri")
        }

        if (videoPath == null) {
            Toast.makeText(requireContext(), "No video selected", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
        setupListeners()
    }

    private fun setupListeners() {
        videoPreview = binding.videoView
        videoProgress = binding.zoomVideoPb
        seekVideoProgress = binding.videoProgress
        downloadOptions = binding.videoDownloadOptions
        downloadOptions.setOnClickListener(this)
        binding.closeButton.setOnClickListener(this)
        drawingCanvas = binding.drawingCanvas
        drawingCanvas.apply {
            visibility = View.GONE
        }
        videoPreview.setVideoURI(videoPath)
        downloadOptions.visibility = View.GONE
        binding.editVideoFab.apply {
            visibility = View.INVISIBLE
            setOnClickListener {
                try {
                    startAnimation(
                        requireContext(),
                        binding.editToolsLine,
                        if (binding.editToolsLine.visibility == View.INVISIBLE) R.anim.anim_slide_up else R.anim.slide_down_800,
                        binding.editToolsLine.visibility == View.INVISIBLE
                    )
                } catch (e:Exception) {
                    e.printStackTrace()
                } finally {
                    val pointsData = drawingCanvas.getFlattenedPaths()
                    if (!emojiUrlSet.isNullOrEmpty() || textSet.isNotEmpty() || pointsData.isNotEmpty()) {
                        showLoading(true,res.getString(R.string.loading_prossess_editer_video_text))
                        val outputVideoPath =
                            File(requireContext().cacheDir, "output_video_text_and_emoji.mp4").absolutePath
                        val fm = File(outputVideoPath)
                        if (fm.exists()) {
                            fm.delete()
                        }
                        if (!emojiUrlSet.isNullOrEmpty() || textSet.isNotEmpty()) {
                            try {
                                val objectsData =
                                    drawingCanvas.getObjectsDataForApiRelativeToVideo(
                                        drawingCanvas.width.toFloat(),
                                        drawingCanvas.height.toFloat(),
                                    )
                                // Extracting data from the result.
                                val emojiData = objectsData["emoji"] as Map<*, *>
                                val textData = objectsData["text"] as Map<*, *>

                                val emojiX = emojiData["x"] as Float
                                val emojiY = emojiData["y"] as Float
                                val emojiWidth = emojiData["width"] as Float
                                val emojiHeight = emojiData["height"] as Float
                                val emojiScale = emojiData["scale"] as Float

                                val textX = textData["x"] as Float
                                val textY = textData["y"] as Float
                                val textSize = textData["size"] as Float
                                val aspectRatio = emojiWidth
                                textEmojiDrawer.processVideoWithTextAndEmoji(
                                    it,
                                    requireActivity(),
                                    requireContext(),
                                    videoPath.toString(),
                                    outputVideoPath.toString(),
                                    textSet,
                                    emojiUrlSet,
                                    textX.toInt(),
                                    textY.toInt(),
                                    textSize.toInt(),
                                    textFinalColorSet,
                                    emojiX.toInt(),
                                    emojiY.toInt(),
                                    aspectRatio.toInt()
                                )
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                textEmojiDrawer.processVideoWithTextAndEmoji(
                                    it,
                                    requireActivity(),
                                    requireContext(),
                                    videoPath.toString(),
                                    outputVideoPath.toString(),
                                    textSet,
                                    emojiUrlSet
                                )
                            }
                            if (pointsData.isNotEmpty()) {
                                textEmojiDrawer.setOnSaveSelectedListener {
                                    val outputVideoPath2 =
                                        File(
                                            requireContext().cacheDir,
                                            "output_video_canvas_draw.mp4"
                                        ).absolutePath
                                    val f2 = File(outputVideoPath2)
                                    if (f2.exists()) f2.delete()
                                    requireActivity().lifecycleScope.launch(Dispatchers.IO) {
                                        canvasDrawer.generateVideoWithDrawings(
                                            requireContext(),
                                            inputVideo = outputVideoPath,
                                            outputVideo = outputVideoPath2,
                                            frameRate = 30,
                                            resolutionWidth = videoWidth,
                                            resolutionHeight = videoHeight,
                                            canvasWidth = drawingCanvas.width,
                                            canvasHeight = drawingCanvas.height,
                                            paintColor = paintColorSet,
                                            paintSize = paintSizeSet,
                                            getPoints = { pointsData } // Your method to provide points
                                        )
                                        withContext(Dispatchers.Main) {
                                            val f = File(outputVideoPath)
                                            if (f.exists()) f.delete()
                                            if (fm.exists()) {
                                                fm.delete()
                                            }
                                            copyToGallery(
                                                outputVideoPath2,
                                                videoPath.toString()
                                            )

                                            // Update UI
                                            setImageResource(if (binding.editToolsLine.visibility == View.INVISIBLE) R.drawable.baseline_edit_24 else R.drawable.baseline_check_24_green)
                                            drawingCanvas.visibility =
                                                if (binding.editToolsLine.visibility == View.INVISIBLE) View.GONE else View.VISIBLE
                                        }
                                    }
                                }
                            } else {
                                textEmojiDrawer.setOnSaveSelectedListener {
                                    requireActivity().runOnUiThread {
                                        copyToGallery(outputVideoPath, videoPath.toString())
                                        setImageResource(if (binding.editToolsLine.visibility == View.INVISIBLE) R.drawable.baseline_edit_24 else R.drawable.baseline_check_24_green)
                                        drawingCanvas.visibility =
                                            if (binding.editToolsLine.visibility == View.INVISIBLE) View.GONE else View.VISIBLE
                                    }
                                }
                            }
                        } else {
                            if (pointsData.isNotEmpty()) {
                                val outputVideoPath2 =
                                    File(requireContext().cacheDir, "output_video_canvas_draw.mp4").absolutePath
                                val f2 = File(outputVideoPath2)
                                if (f2.exists()) f2.delete()
                                requireActivity().lifecycleScope.launch(Dispatchers.IO) {
                                    canvasDrawer.generateVideoWithDrawings(
                                        requireContext(),
                                        inputVideo = videoPath.toString(),
                                        outputVideo = outputVideoPath2,
                                        frameRate = 30,
                                        resolutionWidth = videoWidth,
                                        resolutionHeight = videoHeight,
                                        canvasWidth = drawingCanvas.width,
                                        canvasHeight = drawingCanvas.height,
                                        paintColor = paintColorSet,
                                        paintSize = paintSizeSet,
                                        getPoints = { pointsData } // Your method to provide points
                                    )
                                    withContext(Dispatchers.Main) {
                                        copyToGallery(outputVideoPath2, videoPath.toString())

                                        // Update UI
                                        setImageResource(if (binding.editToolsLine.visibility == View.INVISIBLE) R.drawable.baseline_edit_24 else R.drawable.baseline_check_24_green)
                                        drawingCanvas.visibility =
                                            if (binding.editToolsLine.visibility == View.INVISIBLE) View.GONE else View.VISIBLE
                                    }
                                }
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (activity != null) {
                                setImageResource(if (binding.editToolsLine.visibility == View.INVISIBLE) R.drawable.baseline_edit_24 else R.drawable.baseline_check_24_green)
                                drawingCanvas.visibility = if (binding.editToolsLine.visibility == View.INVISIBLE) View.GONE else View.VISIBLE
                            }
                        },700)
                    }
                }
            }
        }
        binding.resetBtn.setOnClickListener {
            drawingCanvas.clear()
        }
        binding.paintBtn.setOnClickListener {
            showPaintDialog()
        }
        binding.emojiBtn.setOnClickListener {
            showEmojiDialog()
        }
        binding.textBtn.setOnClickListener {
            startAddTextDialog()
        }
        binding.faceSwapBtn.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                showFaceSwapDialog(videoPath.toString())
            }
        }
        binding.soundSwapBtn.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                showSoundSwapDialog(videoPath.toString())
            }
        }
        loadVideo("local")
    }

    private fun copyToGallery(sourcePath: String, destinationPath: String) {
        try {
            val sourceFile = File(sourcePath)
            val destinationFile = File(destinationPath)
            println("sourceFile $sourceFile")
            println("destinationFile $destinationFile")

            if (sourceFile.exists()) {
                sourceFile.copyTo(destinationFile, overwrite = true)
                //sourceFile.deleteRecursively()
                videoPreview.setVideoPath(destinationFile.absolutePath)
                msg.messageSnackBar(requireContext(), res.getString(R.string.video_saved_with_edits_text), binding.root)
                // Update UI
                println("File copied to gallery: ${destinationFile.absolutePath}")
            } else {
                println("Source file does not exist.")
            }
        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                msg.messageSnackBar(requireContext(), "Error saving video: ${e.message}", binding.root)
            }
            e.printStackTrace()
        } finally {
            drawingCanvas.clear()
            emojiUrlSet = ""
            textSet = ""
            showLoading(false)
            val sourceFile = File(sourcePath)
            if (sourceFile.exists()) sourceFile.delete()
        }
    }

    private fun showSoundSwapDialog(videoPath: String) {
        AddSoundDialog.openType = "editer"
        addSoundDialog.setOnSaveSelectedListener { audioPath, _ ->
            AddSoundDialog.openType = ""
            soundSwap.setOnSaveSelectedListener { outVideo ->
                requireActivity().runOnUiThread {
                    copyToGallery(outVideo, videoPath)

                    // Update UI
                    binding.editVideoFab.setImageResource(if (binding.editToolsLine.visibility == View.INVISIBLE) R.drawable.baseline_edit_24 else R.drawable.baseline_check_24_green)
                    drawingCanvas.visibility =
                        if (binding.editToolsLine.visibility == View.INVISIBLE) View.GONE else View.VISIBLE
                    if (addSoundDialog.isAdded) addSoundDialog.dismiss()
                }
            }
            showLoading(true,res.getString(R.string.loading_prossess_editer_video_text))
            val outputVideoPath2 =
                File(requireContext().cacheDir, "output_video_sound_swap.mp4").absolutePath
            val f2 = File(outputVideoPath2)
            if (f2.exists()) f2.delete()
            soundSwap.generateVideoWithSound(videoPath,audioPath,outputVideoPath2,videoWidth,videoHeight)
        }
        addSoundDialog.show(childFragmentManager,"AddSoundDialog")
    }

    private fun showFaceSwapDialog(videoPath:String) {
        FaceSwapImageDialog.videoPath = videoPath
        faceSwapDialog.setOnSaveDaceSwapSelectedListener { outputVideoPath ->
            // Start a coroutine to load the emoji bitmap in the background
            copyToGallery(outputVideoPath, videoPath)

            // Update UI
            binding.editVideoFab.setImageResource(if (binding.editToolsLine.visibility == View.INVISIBLE) R.drawable.baseline_edit_24 else R.drawable.baseline_check_24_green)
            drawingCanvas.visibility =
                if (binding.editToolsLine.visibility == View.INVISIBLE) View.GONE else View.VISIBLE
            if (faceSwapDialog.isAdded) faceSwapDialog.dismiss()
        }
        faceSwapDialog.show(childFragmentManager, "FaceSwapImageDialog")
    }


    private fun showPaintDialog() {
        addPaintDialog.setOnSavePaintSelectedListener { _ ->
            // Start a coroutine to load the emoji bitmap in the background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Switch to the main thread to set the emoji on the canvas
                    withContext(Dispatchers.Main) {
                        //textFinalSet = textSet
                        drawingCanvas.apply {
                            setDrawProperties(paintColorSet, paintSizeSet, Paint.Style.STROKE)
                        }
                        addPaintDialog.dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        addPaintDialog.setOnSavePaintDeletedListener { _ ->
            //textFinalSet = ""
            drawingCanvas.apply {
                setDrawProperties(Color.RED, 10f, Paint.Style.STROKE)
            }
        }
        addPaintDialog.show(childFragmentManager, "AddTextDialog")
    }

    private fun showEmojiDialog() {
        emojiDialog.setOnEmojiSelectedListener { emojiUrl ->
            // Start a coroutine to load the emoji bitmap in the background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Load emoji bitmap in the background
                    val emojiBitmap = BitmapFactory.decodeStream(URL(emojiUrl).openStream())

                    // Switch to the main thread to set the emoji on the canvas
                    withContext(Dispatchers.Main) {
                        emojiUrlSet = emojiUrl
                        drawingCanvas.setEmoji(emojiBitmap, 200f, 100f)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        emojiDialog.show(childFragmentManager, "EmojiDialog")
    }

    private fun startAddTextDialog() {
        addTextDialog.setOnSaveTextSelectedListener { _ ->
            // Start a coroutine to load the emoji bitmap in the background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Load emoji bitmap in the background
                    //val emojiBitmap = BitmapFactory.decodeStream(URL(emojiUrl).openStream())

                    // Switch to the main thread to set the emoji on the canvas
                    withContext(Dispatchers.Main) {
                        //textFinalSet = textSet
                        drawingCanvas.apply {
                            setTextProperties(textSet, textColorSet, textSizeSet)
                        }
                        addTextDialog.dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        addTextDialog.setOnSaveTextDeletedListener { _ ->
            textSet = ""
            drawingCanvas.apply {
                setTextProperties("", textColorSet, textSizeSet)
            }
        }
        addTextDialog.show(childFragmentManager, "AddTextDialog")
    }

    private fun loadVideo(openType:String) {
        videoPreview.visibility = View.VISIBLE
        videoProgress.visibility = View.VISIBLE
        seekVideoProgress.visibility = View.VISIBLE
        videoPreview.setOnPreparedListener {
            //running = true
            videoWidth = it.videoWidth
            videoHeight = it.videoHeight
            drawingCanvas.setNewVideoRes(videoWidth.toFloat(),videoHeight.toFloat())
            videoProgress.visibility = View.GONE
            //if (openType != "local") downloadOptions!!.visibility = View.VISIBLE
            if (openType != "local")  startAnimation(requireContext(),downloadOptions,R.anim.fade_in_1000, true)

            it.isLooping = false
            it.start()
            startTimer(videoPreview,seekVideoProgress,openType).start()
        }
        videoPreview.setOnCompletionListener {
            //it.start()
        }
        videoPreview.setOnErrorListener { mp, _, _ ->
            if (mp.isPlaying) mp.pause()
            msg.message(requireContext(), res.getString(R.string.video_not_supported))
            false
        }
        videoPreview.setOnClickListener(this)
        seekVideoProgress.setOnSeekBarChangeListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun startTimer(video: VideoView, videoProgress: SeekBar, openType: String): Job {
        val duration = video.duration
        val timeInSeconds = duration / 1000

        // Show animation if the video is local and shorter than 500 seconds
        if (openType == "local" && timeInSeconds < LOCAL_VIDEO_DURATION_THRESHOLD_SECONDS) {
            startAnimation(requireContext(), binding.editVideoFab, R.anim.fade_in_1000, true)
        }

        videoProgress.max = duration

        // Launch a coroutine to update the progress
        return lifecycleScope.launch {
            while (video.currentPosition < duration) {
                videoProgress.progress = video.currentPosition
                delay(PROGRESS_UPDATE_DELAY_MS)
            }
        }.also { job ->
            job.invokeOnCompletion { cause ->
                when {
                    cause == null -> Log.d("VideoTimer", "Timer completed normally")
                    cause.message == "ButtonPressed" -> Log.d("VideoTimer", "Timer cancelled by button press")
                    else -> Log.d("VideoTimer", "Timer cancelled: ${cause.message}")
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.videoView -> {
                if (videoPreview.isPlaying) {
                    videoPreview.pause()
                } else {
                    videoPreview.start()
                }
            }
            R.id.close_button -> {
                requireActivity().finish()
            }
        }
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if(p2) {
            videoPreview.seekTo(p1)
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }

    companion object {
        var textFinalSet = ""
        var textFinalColorSet = "#050505"
        var currentLang = "en"
    }
}

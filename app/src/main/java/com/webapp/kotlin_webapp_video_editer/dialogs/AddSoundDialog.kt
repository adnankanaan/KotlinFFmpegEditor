package com.webapp.kotlin_webapp_video_editer.dialogs

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.webapp.kotlin_webapp_video_editer.R
import com.webapp.kotlin_webapp_video_editer.databinding.AddSoundDialogBinding
import com.webapp.kotlin_webapp_video_editer.videoeditor.ChangeLangBase
import com.webapp.kotlin_webapp_video_editer.videoeditor.Message
import com.webapp.kotlin_webapp_video_editer.videoeditor.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AddSoundDialog : DialogFragment() {
    private lateinit var binding: AddSoundDialogBinding
    private lateinit var resourcesSet: Resources
    private lateinit var imageView: ImageView
    private lateinit var progress: ProgressBar
    private var file1: String? = null
    private val loadingDialog = ProssessingDialog()
    private var message = Message()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var pickAudioLauncher : ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher : ActivityResultLauncher<String>
    private var saveSet: ((String,String) -> Unit)? = null

    fun setOnSaveSelectedListener(listener: (String,String) -> Unit) {
        saveSet = listener
    }

    private fun checkPermissionAndOpenPicker() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    openAudioPicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openAudioPicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openAudioPicker() {
        pickAudioLauncher.launch("audio/*")
    }

    private fun playAudio(uri: Uri) {
        imageView.visibility = View.GONE
        binding.soundLineTools.visibility = View.VISIBLE
        binding.playButton.visibility = View.VISIBLE
        progress.visibility = View.GONE
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(requireContext(), uri)
            prepare()
            start()
        }
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }
    }

    private fun saveSoundToInternalStorage(context: Context, sourceUri: Uri, fileName: String): File? {
        try {
            // Open a stream to the source file
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val file = File(context.filesDir, fileName)

            // Write the file to internal storage
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun handleSoundUri(uri: Uri) {
        // Handle the selected image URI, e.g., save it or upload it to a server
        //Toast.makeText(requireContext(), "Image URI: $uri", Toast.LENGTH_SHORT).show()
        try {
            val uriString = uri.toString()
            val myFile = File(uriString)
            var displayName: String?
            if (uriString.startsWith("content://")) {
                requireActivity().contentResolver.query(uri, null, null, null, null).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        @SuppressLint("Range")
                        displayName =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        file1 = displayName
                    }
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.name
                file1 = displayName
            }
        } catch (e:Exception) {
            e.printStackTrace()
        } finally {
            if (file1 != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val file = saveSoundToInternalStorage(requireContext(),uri, file1!!)
                    if (file != null) {
                        if (openType.isNotEmpty()) {
                            saveSet?.invoke(file.absolutePath, file1!!)
                            return@launch
                        }

                        // Use the file path for your backend, or further processing
                        withContext(Dispatchers.Main) {
                            playAudio(uri)
                        }
                        Log.d("FilePath", "File path: $file")
                    } else {
                        // Use Uri directly if file path cannot be retrieved
                        withContext(Dispatchers.Main) {
                            showLoading(false)
                            message.messageSnackBar(
                                requireContext(),
                                resourcesSet.getString(R.string.pls_add_audio),
                                binding.root
                            )
                            Log.d("FilePath", "File path could not be retrieved; using Uri")
                        }
                    }
                }
            } else {
                message.messageSnackBar(requireContext(), resourcesSet.getString(R.string.pls_add_audio),binding.root)
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


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = AddSoundDialogBinding.inflate(requireActivity().layoutInflater)
        resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources
        pickAudioLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleSoundUri(uri) } ?: message.messageSnackBar(requireContext(), resourcesSet.getString(R.string.pls_add_audio), binding.root)
        }
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openAudioPicker() else message.messageSnackBar(requireContext(), "Permission to access audio files was denied.", binding.root)
        }

        return builder.setView(binding.root).create().apply {
            //d.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(this.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.window!!.attributes = lp

            binding.dialogTitle.text = resourcesSet.getString(R.string.add_sound_btn_text)
            imageView = binding.imageHandler
            progress = binding.progressAudio
            progress.visibility = View.VISIBLE
            binding.addSoundBtn.apply {
                text = resourcesSet.getString(R.string.add_sound_btn_text)
                setOnClickListener {
                    checkPermissionAndOpenPicker()
                }
            }

            binding.deleteSoundBtn.setOnClickListener {

            }
           //loadCurrentBackGround()
            binding.playButton.apply {
                visibility = View.GONE
                text = resourcesSet.getString(R.string.play_btn_text)
                setOnClickListener {
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer?.start()
                    }
                }
            }

            binding.stopButton.apply {
                text = resourcesSet.getString(R.string.stop_btn_text)
                setOnClickListener {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer?.stop()
                        mediaPlayer?.prepare()  // Prepare the MediaPlayer to restart playback if needed
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
    }

    override fun dismiss() {
        super.dismiss()
        mediaPlayer?.release()
    }

    companion object {
        var openType = ""
    }
}
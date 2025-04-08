package com.webapp.kotlin_webapp_video_editer.dialogs

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.webapp.kotlin_webapp_video_editer.R
import com.webapp.kotlin_webapp_video_editer.databinding.FaceSwapImageDialogBinding
import com.webapp.kotlin_webapp_video_editer.ffmpegengin.FFmpegFaceSwap
import com.webapp.kotlin_webapp_video_editer.videoeditor.ChangeLangBase
import com.webapp.kotlin_webapp_video_editer.videoeditor.Message
import com.webapp.kotlin_webapp_video_editer.videoeditor.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class FaceSwapImageDialog : DialogFragment() {

    private lateinit var binding: FaceSwapImageDialogBinding
    private lateinit var resourcesSet: Resources
    private lateinit var imageView: ImageView
    private var file1: String? = null
    private val loadingDialog = ProssessingDialog()
    private val cropDialog = CropBackGroundDialog()
    private val faceSwapDialog = FFmpegFaceSwap()
    private var message = Message()
    private lateinit var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    // Register traditional image picker for Android 12 and below
    private lateinit var imagePickerLauncher:ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var saveSet: ((String) -> Unit)? = null
    fun setOnSaveDaceSwapSelectedListener(listener: (String) -> Unit) {
        saveSet = listener
    }
    private fun checkAndRequestPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Check READ_MEDIA_VIDEO for Android 14+
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else -> {
                // Check READ_EXTERNAL_STORAGE for Android 13 and below
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openImagePickerLegacy()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
    private fun openImagePicker() {
        // Launch the gallery to pick an image
        //pickImageLauncher.launch("image/*")
        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openImagePickerLegacy() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveImageToInternalStorage(context: Context, sourceUri: Uri, fileName: String): File? {
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

    private fun handleImageUri(uri: Uri) {
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
                showLoading(true,resourcesSet.getString(R.string.loading_prossess_add_face_swap_image_text))
                CoroutineScope(Dispatchers.IO).launch {
                    val file = saveImageToInternalStorage(requireContext(),uri, file1!!)
                    if (file != null) {
                        // Use the file path for your backend, or further processing
                        startPostPhotoToServer(uri)
                        Log.d("FilePath", "File path: $file")
                    } else {
                        // Use Uri directly if file path cannot be retrieved
                        withContext(Dispatchers.Main) {
                            showLoading(false)
                            message.messageSnackBar(
                                requireContext(),
                                resourcesSet.getString(R.string.pls_add_photo),
                                binding.root
                            )
                            Log.d("FilePath", "File path could not be retrieved; using Uri")
                        }
                    }
                }
            } else {
                message.messageSnackBar(requireContext(), resourcesSet.getString(R.string.pls_add_photo),binding.root)
            }
        }
    }


    private fun getFaceBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // Open an InputStream from the URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            // Decode the InputStream into a Bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            // Return the Bitmap (you could process it further here)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private suspend fun startPostPhotoToServer(uri: Uri) {
            try {
                // Perform video processing here
                val faceBiturimap = getFaceBitmapFromUri(requireContext(),uri)
                val outputVideoPath =
                    File(requireContext().cacheDir, "output_video_face_swap.mp4").absolutePath
                val f = File(outputVideoPath)
                if (f.exists()) f.delete()
                if (faceBiturimap != null){
                    faceSwapDialog.processVideoWithFaceSwap(
                        requireContext(),
                        videoPath,
                        outputVideoPath.toString(),
                        faceBiturimap
                    )
                }
                withContext(Dispatchers.Main) {
                    // Update UI after processing
                    if (faceBiturimap != null){
                        saveSet?.invoke(outputVideoPath)
                        println("Video processing completed.")
                    } else {
                        message.messageSnackBar(requireContext(), resourcesSet.getString(R.string.pls_add_photo),binding.root)
                    }
                    showLoading(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    private fun startDeleteImage() {

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = FaceSwapImageDialogBinding.inflate(requireActivity().layoutInflater)
        resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            uri?.let {
                CropBackGroundDialog.uri = it
                cropDialog.show(childFragmentManager,"CropBackGroundDialog")
            }
        }
        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                CropBackGroundDialog.uri = it
                cropDialog.show(childFragmentManager,"CropBackGroundDialog")
            }
        }
        cropDialog.setOnSaveCropListener { _, cropResult ->
            if (cropResult.isSuccessful) {
                val croppedUri = cropResult.uriContent
                if (croppedUri != null) {
                    handleImageUri(croppedUri)
                } else {
                    message.messageSnackBar(requireContext(), "Failed to save cropped image", binding.root)
                }
            } else {
                message.messageSnackBar(requireContext(), "Cropping failed: ${cropResult.error?.message}", binding.root)
            }
        }
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    openImagePicker()
                } else {
                    openImagePickerLegacy()
                }
            } else {
                message.message(requireContext(), "Permission denied to access media")
            }
        }
        return builder.setView(binding.root).create().apply {
            //d.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(this.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.window!!.attributes = lp

            binding.dialogTitle.text = resourcesSet.getString(R.string.add_face_swap_options_help_title)
            imageView = binding.imageHandler
            binding.addImageBtn.apply {
                text = resourcesSet.getString(R.string.add_back_ground_btn_text)
                setOnClickListener {
                    checkAndRequestPermissions()
                }
            }

            binding.deleteImageBtn.apply {
                visibility = View.GONE
                setOnClickListener {
                    startDeleteImage()
                }
            }
        }
    }

    companion object {
        var videoPath = ""
    }
}

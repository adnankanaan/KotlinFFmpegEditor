package com.webapp.kotlin_webapp_video_editer.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.canhub.cropper.CropImageView
import com.webapp.kotlin_webapp_video_editer.R
import com.webapp.kotlin_webapp_video_editer.databinding.ConformBackgroundCropBinding
import com.webapp.kotlin_webapp_video_editer.videoeditor.ChangeLangBase
import com.webapp.kotlin_webapp_video_editer.videoeditor.UserData

class CropBackGroundDialog: DialogFragment() {
    private lateinit var binding: ConformBackgroundCropBinding
    private lateinit var resourcesSet: Resources
    private var saveSet: ((CropImageView, CropImageView.CropResult) -> Unit)? = null

    fun setOnSaveCropListener(listener: (CropImageView, CropImageView.CropResult)-> Unit) {
        saveSet = listener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false // Make dialog non-cancelable
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = ConformBackgroundCropBinding.inflate(requireActivity().layoutInflater)
        resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources
        return builder.setView(binding.root).create().apply {
            //d.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
            this.setCancelable(false)
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(this.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.window!!.attributes = lp
            val cropImageView = binding.cropImageView
            cropImageView.setImageUriAsync(uri)
            cropImageView.setOnCropImageCompleteListener { view, result ->
               saveSet?.invoke(view,result)
            }
            binding.saveBtn.apply {
                text = resourcesSet.getString(R.string.crop_text)
                setOnClickListener {
                    cropImageView.croppedImageAsync()
                    dismiss()
                }
            }
        }
    }
    companion object{
        var uri:Uri? = null
    }
}
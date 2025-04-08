package com.webapp.kotlin_webapp_video_editer.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.webapp.kotlin_webapp_video_editer.databinding.LoadingSendOrderDialogBinding
import com.webapp.kotlin_webapp_video_editer.videoeditor.ChangeLangBase
import com.webapp.kotlin_webapp_video_editer.videoeditor.UserData

class ProssessingDialog : DialogFragment() {
    private lateinit var binding: LoadingSendOrderDialogBinding
    private lateinit var resourcesSet: Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false  // Make dialog non-cancelable
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = LoadingSendOrderDialogBinding.inflate(requireActivity().layoutInflater)
        resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources

        return builder.setView(binding.root).create().apply {
            this.setCancelable(false)
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(this.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.window!!.attributes = lp
            this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            updateMessage() // Set initial text
        }
    }

    // Call this to update text dynamically
    private fun updateMessage() {
        binding.sendingText.text = message
    }

    // Function to set the message and update the text
    fun setText(newMessage: String) {
        message = newMessage
        activity?.runOnUiThread {
            if (this::binding.isInitialized) {
                updateMessage()
            }
        }
    }

    companion object {
        var message: String = ""
    }
}


package com.webapp.kotlin_webapp_video_editer.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.webapp.kotlin_webapp_video_editer.R
import com.webapp.kotlin_webapp_video_editer.databinding.AddTextDialogBinding
import com.webapp.kotlin_webapp_video_editer.videoeditor.ChangeLangBase
import com.webapp.kotlin_webapp_video_editer.videoeditor.UserData
import com.webapp.kotlin_webapp_video_editer.videoeditor.VideoEditorFragment.Companion.textFinalColorSet
import com.webapp.kotlin_webapp_video_editer.videoeditor.VideoEditorFragment.Companion.textFinalSet

class AddTextDialog : DialogFragment() {
    private lateinit var binding: AddTextDialogBinding
    private lateinit var resourcesSet: Resources
    private var saveSet: ((String) -> Unit)? = null
    fun setOnSaveTextSelectedListener(listener: (String) -> Unit) {
        saveSet = listener
    }

    private var saveDeleteSet: ((String) -> Unit)? = null
    fun setOnSaveTextDeletedListener(listener: (String) -> Unit) {
        saveDeleteSet = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = AddTextDialogBinding.inflate(requireActivity().layoutInflater)
        resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources

        return builder.setView(binding.root).create().apply {
            //d.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(this.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.window!!.attributes = lp

            binding.dialogTitle.text = resourcesSet.getString(R.string.add_text_title_text)
            binding.deleteTxtBtn.setOnClickListener {
                startDeleteText(it)
            }
            binding.addTxtBtn.apply {
                text = resourcesSet.getString(R.string.save_text)
                setOnClickListener {
                    if (textSet != ""){
                        saveSet?.invoke(it.toString())
                    }
                }
            }
            binding.textInput.apply {
                hint = resourcesSet.getText(R.string.text_hint_text)
                addTextChangedListener {
                    textSet = it.toString()
                    //binding.canvasView.setTextProperties(it.toString(), binding.canvasView.textColor, binding.canvasView.textSize)
                }
            }
            binding.textSizeSeekBar.max = 40
            binding.textSizeSeekBar.progress = 24
            binding.textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    try {
                        textSizeSet = progress.toFloat()
                        //binding.canvasView.setTextProperties(binding.canvasView.text!!, binding.canvasView.textColor, progress.toFloat())
                    } catch (e:NullPointerException){
                        e.printStackTrace()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            binding.colorPickerButton.apply {
                text = resourcesSet.getText(R.string.pick_color_text)
                setOnClickListener {
                    ColorPickerDialogBuilder
                        .with(requireContext())
                        .setTitle(resourcesSet.getString(R.string.pick_color_text))
                        .initialColor(Color.RED)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener { selectedColor ->
                            try {
                                textColorSet = selectedColor
                                //binding.canvasView.setTextProperties(binding.canvasView.text!!, selectedColor, binding.canvasView.textSize)
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
                        }
                        .setPositiveButton(
                            resourcesSet.getString(R.string.save_text)
                        ) { _, selectedColor, _ ->
                            textFinalColorSet = String.format("#%06X", 0xFFFFFF and selectedColor)
                        }
                        .setNegativeButton(
                            resourcesSet.getString(R.string.close_button_text)
                        ) { _, _ -> }
                        .build()
                        .show()
                }
            }
        }
    }

    private fun startDeleteText(v:View) {
        textSet = ""
        textFinalSet = ""
        saveDeleteSet?.invoke(v.toString())
        dismiss()
    }

    companion object{
        var textSet = ""
        var textSizeSet = 18f
        var textColorSet = Color.parseColor("#050505")
    }
}
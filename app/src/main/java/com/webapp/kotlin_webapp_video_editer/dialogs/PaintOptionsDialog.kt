package com.webapp.kotlin_webapp_video_editer.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.webapp.kotlin_webapp_video_editer.R
import com.webapp.kotlin_webapp_video_editer.databinding.PaintDialogBinding
import com.webapp.kotlin_webapp_video_editer.videoeditor.ChangeLangBase
import com.webapp.kotlin_webapp_video_editer.videoeditor.UserData

class PaintOptionsDialog : DialogFragment() {
    private lateinit var binding: PaintDialogBinding
    private lateinit var resourcesSet: Resources
    private var saveSet: ((String) -> Unit)? = null
    fun setOnSavePaintSelectedListener(listener: (String) -> Unit) {
        saveSet = listener
    }

    private var saveDeleteSet: ((String) -> Unit)? = null
    fun setOnSavePaintDeletedListener(listener: (String) -> Unit) {
        saveDeleteSet = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = PaintDialogBinding.inflate(requireActivity().layoutInflater)
        resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources

        return builder.setView(binding.root).create().apply {
            //d.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(this.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.window!!.attributes = lp

            binding.dialogTitle.text = resourcesSet.getString(R.string.paint_title_text)
            binding.deletePaintBtn.setOnClickListener {
                startDeletePaint(it)
            }
            binding.addPaintBtn.apply {
                text = resourcesSet.getString(R.string.save_text)
                setOnClickListener {
                    saveSet?.invoke(it.toString())
                }
            }
            binding.paintSizeSeekBar.max = 50 // Maximum stroke width
            binding.paintSizeSeekBar.progress = 10 // Default stroke width
            binding.paintSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    try {
                        paintSizeSet = progress.toFloat()
                        //binding.canvasView.setTextProperties(binding.canvasView.text!!, binding.canvasView.textColor, progress.toFloat())
                    } catch (e:NullPointerException){
                        e.printStackTrace()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            binding.colorPickerButton.apply {
                text = resourcesSet.getText(R.string.pick_pint_color_text)
                setOnClickListener {
                    ColorPickerDialogBuilder
                        .with(requireContext())
                        .setTitle(resourcesSet.getString(R.string.pick_color_text))
                        .initialColor(Color.RED)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener { selectedColor ->
                            try {
                                paintColorSet = selectedColor
                                paintColorFinalSet = String.format("#%06X", 0xFFFFFF and selectedColor)
                                //binding.canvasView.setTextProperties(binding.canvasView.text!!, selectedColor, binding.canvasView.textSize)
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
                        }
                        .setPositiveButton(
                            resourcesSet.getString(R.string.save_text)
                        ) { _, selectedColor, _ ->
                            try {
                                paintColorSet = selectedColor
                                paintColorFinalSet = String.format("#%06X", 0xFFFFFF and selectedColor)
                                //binding.canvasView.setTextProperties(binding.canvasView.text!!, selectedColor, binding.canvasView.textSize)
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
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

    private fun startDeletePaint(v:View) {
        saveDeleteSet?.invoke(v.toString())
        dismiss()
    }

    companion object{
        var paintSizeSet = 12f
        var paintColorSet = Color.parseColor("#050505")
        var paintColorFinalSet = "#050505"

    }
}
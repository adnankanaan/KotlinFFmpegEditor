package com.webapp.kotlin_webapp_video_editer.ffmpegengin

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.SessionState
import com.webapp.kotlin_webapp_video_editer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class FFmpegTextAndEmoji {
    private var saveSet: ((String) -> Unit)? = null
    fun setOnSaveSelectedListener(listener: (String) -> Unit) {
        saveSet = listener
    }

    @SuppressLint("ResourceType")
    fun processVideoWithTextAndEmoji(
        view:View? = null,
        activity: ComponentActivity,
        context: Context,
        inputVideo: String,
        outputVideo: String,
        text: String? = null,
        emojiUrl: String? = null, // URL for the emoji
        textPositionX: Int = 10,
        textPositionY: Int = 10,
        textSize: Int = 24,
        textColor: String = "white", // e.g., "white" or "#FFFFFF"
        emojiPositionX: Int = 100,
        emojiPositionY: Int = 100,
        emojiSize: Int = 200 // Emoji size (width and height in pixels)
    ) {
        val fontFile = File(context.cacheDir, "custom_font.ttf")
        context.resources.openRawResource(R.font.din_next_lt_bold).use { inputStream ->
            FileOutputStream(fontFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        val fontPath = fontFile.absolutePath
        val commands = mutableListOf("-i", inputVideo)
        val filterGraph = StringBuilder()

        // Handle text overlay
        if (!text.isNullOrEmpty()) {
            filterGraph.append("[0:v]drawtext=text='$text':fontfile=$fontPath:fontsize=$textSize:fontcolor=$textColor:x=$textPositionX:y=$textPositionY[drawn]")
        } else {
            filterGraph.append("[0:v]null[drawn]") // Ensure there's always a label for chaining
        }

        // Handle emoji overlay (if provided)
        if (!emojiUrl.isNullOrEmpty()) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val emojiFile = File(context.cacheDir, "emoji.png")
                if (emojiFile.exists()) {
                    emojiFile.deleteRecursively()
                }
                try {
                    val client = OkHttpClient.Builder().build()
                    val request = Request.Builder().url(emojiUrl).build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        println("Downloading emoji from $emojiUrl")
                        response.body?.byteStream()?.use { inputStream ->
                            FileOutputStream(emojiFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        success = true
                        println("Emoji downloaded successfully to ${emojiFile.absolutePath}")
                    } else {
                        println("Failed to download emoji. HTTP error: ${response.code} - ${response.message}")
                    }
                } catch (e: Exception) {
                    println("Error downloading emoji: ${e.localizedMessage}")
                    e.printStackTrace() // Prints full error stack for debugging
                }
                withContext(Dispatchers.Main) {
                    if (success) {
                        commands.add("-i")
                        commands.add(emojiFile.absolutePath)

                        // Add emoji scaling and overlay
                        filterGraph.insert(0, "[1:v]scale=$emojiSize:$emojiSize[emoji];") // Add emoji scaling
                        filterGraph.append(";[drawn][emoji]overlay=x=$emojiPositionX:y=$emojiPositionY[out]")
                    } else {
                        filterGraph.append(";[drawn]null[out]") // Pass-through if no emoji
                    }

                    if (filterGraph.isNotEmpty()) {
                        commands.add("-filter_complex")
                        commands.add(filterGraph.toString())
                        commands.addAll(listOf("-map", "[out]", "-map", "0:a", "-c:a", "copy", outputVideo)) // Ensure audio is mapped
                    } else {
                        commands.addAll(listOf("-map", "0:a", "-c:a", "copy", outputVideo)) // Ensure audio is passed through
                    }
                    // Debugging: Print the command
                    println("FFmpeg command: ${commands.joinToString(" ")}")
                    FFmpegKit.executeAsync(commands.joinToString(" ")) { session ->
                        val state = session.state
                        val returnCode = session.returnCode

                        if (state == SessionState.COMPLETED && returnCode.isValueSuccess) {
                            println("Video processed successfully! at $outputVideo")
                        } else {
                            println("Failed to process video: ${session.failStackTrace}")
                        }
                        emojiFile.delete()
                        fontFile.delete()
                        saveSet?.invoke(view.toString())
                    }
                }
            }
        } else {
            filterGraph.append(";[drawn]null[out]") // Pass-through if no emoji
        }

        // Add filter_complex and output mapping
        // Only add filter_complex if there are filters
        if (emojiUrl.isNullOrEmpty() && filterGraph.isNotEmpty()) {
            commands.add("-filter_complex")
            commands.add(filterGraph.toString())
            commands.addAll(listOf("-map", "[out]", "-map", "0:a", "-c:a", "copy", outputVideo)) // Ensure audio is mapped
        } else {
            if (emojiUrl.isNullOrEmpty()) commands.addAll(listOf("-map", "0:a", "-c:a", "copy", outputVideo)) // Pass-through if no filters
        }

        // Execute FFmpeg command
        if (emojiUrl.isNullOrEmpty()) {
            // Debugging: Print the command
            println("FFmpeg command: ${commands.joinToString(" ")}")
            FFmpegKit.executeAsync(commands.joinToString(" ")) { session ->
                val state = session.state
                val returnCode = session.returnCode

                if (state == SessionState.COMPLETED && returnCode.isValueSuccess) {
                    println("Video processed successfully! at $outputVideo")
                } else {
                    println("Failed to process video: ${session.failStackTrace}")
                }
                fontFile.delete()
                saveSet?.invoke(view.toString())
            }
        }
    }
    companion object {
        var success = false
    }
}
package com.webapp.kotlin_webapp_video_editer.ffmpegengin

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit

class FFmpegSoundSwap {
    private var saveSet: ((String) -> Unit)? = null
    fun setOnSaveSelectedListener(listener: (String) -> Unit) {
        saveSet = listener
    }

    fun generateVideoWithSound(
        inputVideo: String,  // Path to the input video
        inputAudio: String,    // Path to the input audio file
        outputVideo: String, // Path to save the output video
        resolutionWidth: Int = 720,
        resolutionHeight: Int = 1280
    ) {
        // Build the FFmpeg command
        val ffmpegCommand = listOf(
            "-i", inputVideo,                // Input video
            "-i", inputAudio,               // Input audio
            "-filter_complex", "scale=$resolutionWidth:$resolutionHeight", // Scale video
            "-c:v", "mpeg4",              // Video codec (H.264 for compatibility)
            "-c:a", "aac",                  // Audio codec (AAC)
            "-map", "0:v:0",                // Map video stream from input 0
            "-map", "1:a:0",                // Map audio stream from input 1
            "-y",                           // Overwrite the output file if it exists
            outputVideo                     // Output file
        ).joinToString(" ")
        // Execute the FFmpeg command
        FFmpegKit.execute(ffmpegCommand).apply {
            if (returnCode.isValueSuccess) {
                Log.d("FaceSwap", "Video reassembled successfully at: $outputVideo")
            } else {
                Log.e("FaceSwap", "Error during video reassembly: $output")
            }
            saveSet?.invoke(outputVideo)
        }
    }
}


package com.webapp.kotlin_webapp_video_editer.ffmpegengin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

class FFmpegCanvasDraw {
    fun generateVideoWithBlurMask(
        context: Context,
        inputVideo: String,
        outputVideo: String,
        maskImagePath: String, // Path to the blur mask (.png)
        frameRate: Int = 16,
        resolutionWidth: Int = 720,
        resolutionHeight: Int = 1280,
        canvasWidth: Int, // Original video width
        canvasHeight: Int, // Original video height
        getPoints: () -> List<Map<String, Float>> // Function to get user-drawn points
    ) {
        val framesDir = File(context.cacheDir, "canvas_frames")
        if (framesDir.exists()) framesDir.deleteRecursively()
        framesDir.mkdirs()

        val extractedFramesPattern = File(framesDir, "frame_%04d.png").absolutePath

        // Extract frames with FFmpeg
        FFmpegKit.execute("-i $inputVideo -vf scale=${resolutionWidth}:${resolutionHeight} $extractedFramesPattern")

        // Calculate scaling factors
        val scaleX = resolutionWidth.toFloat() / canvasWidth
        val scaleY = resolutionHeight.toFloat() / canvasHeight

        // Load the blur mask
        val blurMask = BitmapFactory.decodeFile(maskImagePath) ?: throw IllegalArgumentException("Invalid mask path!")

        val points = getPoints() // Retrieve user-drawn points for mask placement

        // Use coroutines to process frames in parallel
        runBlocking {
            framesDir.listFiles()?.toList()?.chunked(5)?.forEach { frameBatch ->
                frameBatch.map { frameFile ->
                    async(Dispatchers.Default) {
                        processFrameWithBlurMask(frameFile, blurMask, points, scaleX, scaleY)
                    }
                }.awaitAll()
            }
        }

        // Extract audio from original video
        val audioFile = File(context.cacheDir, "audio.aac")
        if (audioFile.exists()) audioFile.delete()
        FFmpegKit.execute("-i $inputVideo -vn -acodec copy ${audioFile.absolutePath}")

        // Reassemble video with blurred frames
        val reassembledVideoCommand = listOf(
            "-framerate", frameRate.toString(),
            "-i", "$framesDir/frame_%04d.png",
            "-i", audioFile.absolutePath,
            "-c:v", "mpeg4", "-preset", "ultrafast",
            "-map", "0:v:0", "-map", "1:a:0",
            "-pix_fmt", "yuv420p",
            "-c:a", "aac",
            "-y", outputVideo
        )
        FFmpegKit.execute(reassembledVideoCommand.joinToString(" "))

        // Cleanup temporary files
        framesDir.deleteRecursively()
        audioFile.delete()
        println("Video with blurred mask generated at: $outputVideo")
    }

    private fun processFrameWithBlurMask(
        frameFile: File,
        blurMask: Bitmap,
        points: List<Map<String, Float>>,
        scaleX: Float,
        scaleY: Float
    ) {
        val originalBitmap = BitmapFactory.decodeFile(frameFile.absolutePath) ?: return
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true).also { originalBitmap.recycle() }

        val canvas = Canvas(mutableBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Apply the mask for each point
        points.forEach { point ->
            val x = (point["x"] ?: 0f) * scaleX
            val y = (point["y"] ?: 0f) * scaleY

            // Place the blur mask at the specified position
            val maskLeft = (x - blurMask.width / 2).toInt()
            val maskTop = (y - blurMask.height / 2).toInt()

            canvas.drawBitmap(blurMask, maskLeft.toFloat(), maskTop.toFloat(), paint)
        }

        FileOutputStream(frameFile).use { outputStream ->
            mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        mutableBitmap.recycle()
    }
    fun generateVideoWithDrawings(
        context: Context,
        inputVideo: String,
        outputVideo: String,
        frameRate: Int = 16,
        resolutionWidth: Int = 720,
        resolutionHeight: Int = 1280,
        canvasWidth: Int, // Original video width
        canvasHeight: Int, // Original video height
        paintColor: Int = Color.RED,
        paintSize: Float = 10f,
        getPoints: () -> List<Map<String, Float>>
    ) {
        val framesDir = File(context.cacheDir, "canvas_frames")
        if (framesDir.exists()) framesDir.deleteRecursively()
        framesDir.mkdirs()

        val extractedFramesPattern = File(framesDir, "frame_%04d.png").absolutePath

        // Extract frames with FFmpeg
        FFmpegKit.execute("-i $inputVideo -vf scale=${resolutionWidth}:${resolutionHeight} $extractedFramesPattern")

        // Calculate scaling factors
        val scaleX = resolutionWidth.toFloat() / canvasWidth
        val scaleY = resolutionHeight.toFloat() / canvasHeight
        val points = getPoints()

        // Use coroutines to process frames in parallel
        runBlocking {
            framesDir.listFiles()?.toList()?.chunked(5)?.forEach { frameBatch ->
                frameBatch.map { frameFile ->
                    async(Dispatchers.Default) {
                        processFrameWithDrawings(frameFile, points, scaleX, scaleY, paintColor, paintSize)
                    }
                }.awaitAll()
            }
        }

        // Extract audio from original video
        val audioFile = File(context.cacheDir, "audio.aac")
        if (audioFile.exists()) audioFile.delete()
        FFmpegKit.execute("-i $inputVideo -vn -acodec copy ${audioFile.absolutePath}")

        // Reassemble video with drawings
        val reassembledVideoCommand = listOf(
            "-framerate", frameRate.toString(),
            "-i", "$framesDir/frame_%04d.png",
            "-i", audioFile.absolutePath,
            "-c:v", "mpeg4", "-preset", "ultrafast", // Faster encoding
            "-map", "0:v:0", "-map", "1:a:0",
            "-pix_fmt", "yuv420p",
            "-c:a", "aac",
            "-y", outputVideo
        )
        FFmpegKit.execute(reassembledVideoCommand.joinToString(" "))

        // Cleanup temporary files
        framesDir.deleteRecursively()
        audioFile.delete()
        println("Video with drawings generated at: $outputVideo")
    }

    private fun processFrameWithDrawings(
        frameFile: File,
        points: List<Map<String, Float>>,
        scaleX: Float,
        scaleY: Float,
        paintColor: Int,
        paintSize: Float
    ) {
        val originalBitmap = BitmapFactory.decodeFile(frameFile.absolutePath) ?: return
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true).also { originalBitmap.recycle() }

        val canvas = Canvas(mutableBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = paintColor
            style = Paint.Style.STROKE
            strokeWidth = paintSize
        }
        val path = Path()

        points.forEachIndexed { index, point ->
            val x = (point["x"] ?: 0f) * scaleX
            val y = (point["y"] ?: 0f) * scaleY
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)

        FileOutputStream(frameFile).use { outputStream ->
            mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        mutableBitmap.recycle()
    }
}

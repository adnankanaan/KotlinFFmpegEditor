package com.webapp.kotlin_webapp_video_editer.ffmpegengin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import com.arthenica.ffmpegkit.FFmpegKit
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

class FFmpegFaceSwap {
    fun processVideoWithFaceSwap(
        context: Context,
        inputVideo: String,
        outputVideo: String,
        faceImage: Bitmap,
        frameRate: Int = 30,
        resolutionWidth: Int = 720,
        resolutionHeight: Int = 1280
    ) {
        val framesDir = File(context.cacheDir, "face_swap_frames").apply { mkdirs() }
        val extractedFramesPattern = File(framesDir, "frame_%04d.png").absolutePath

        FFmpegKit.execute("-i $inputVideo -vf fps=$frameRate,scale=$resolutionWidth:$resolutionHeight $extractedFramesPattern")

        val faceDetector = FaceDetection.getClient()
        runBlocking {
            framesDir.listFiles()?.toList()?.chunked(5)?.forEach { frameGroup ->
                frameGroup.map { frameFile ->
                    async(Dispatchers.Default) {
                        processFrame(frameFile, faceDetector, faceImage)
                    }
                }.awaitAll()
            }
        }

        val audioFile = File(context.cacheDir, "audio.aac")
        FFmpegKit.execute("-i $inputVideo -vn -acodec copy ${audioFile.absolutePath}")

        FFmpegKit.execute("-framerate $frameRate -i $framesDir/frame_%04d.png -i ${audioFile.absolutePath} -c:v mpeg4 -preset ultrafast -pix_fmt yuv420p -c:a aac -y $outputVideo")

        framesDir.deleteRecursively()
        audioFile.delete()
    }

    private fun processFrame(frameFile: File, faceDetector: FaceDetector, faceImage: Bitmap) {
        val originalBitmap = BitmapFactory.decodeFile(frameFile.absolutePath) ?: return
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true).also { originalBitmap.recycle() }

        val inputImage = InputImage.fromBitmap(mutableBitmap, 0)
        val faces = Tasks.await(faceDetector.process(inputImage))
        if (faces.isNotEmpty()) {
            val canvas = Canvas(mutableBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            faces.forEach { face ->
                val faceRect = face.boundingBox
                val resizedFace = Bitmap.createScaledBitmap(faceImage, faceRect.width(), faceRect.height(), true)
                canvas.drawBitmap(resizedFace, faceRect.left.toFloat(), faceRect.top.toFloat(), paint)
                resizedFace.recycle()
            }
            mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(frameFile))
        }
        mutableBitmap.recycle()
    }
}

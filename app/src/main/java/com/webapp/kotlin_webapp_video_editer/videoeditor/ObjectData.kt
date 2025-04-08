package com.webapp.kotlin_webapp_video_editer.videoeditor

data class ObjectData(
    val x: Float,  // The X coordinate of the object relative to the original video size
    val y: Float,  // The Y coordinate of the object relative to the original video size
    val width: Float,  // The width of the object (emoji) after scaling relative to the video
    val height: Float,  // The height of the object (emoji) after scaling relative to the video
    val size: Float  // The scale factor for resizing the object (text or emoji)
)

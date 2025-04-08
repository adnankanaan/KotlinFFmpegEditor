package com.webapp.kotlin_webapp_video_editer.videoeditor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class DrawingViewEditer(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val points = mutableListOf<Pair<Float, Float>>()
    private val boundingRects = mutableListOf<RectF>()
    private val drawnPaths = mutableListOf<MutableList<Map<String, Float>>>()

    private val canvasBitmap: Bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    private val canvas: Canvas = Canvas(canvasBitmap)
    private var drawing = false  // Flag for dragging the line

    private var originalVideoWidth = 1280f  // The original video width
    private var originalVideoHeight = 720f  // The original video height

    private var emojiBitmap: Bitmap? = null
    private var emojiX = 200f
    private var emojiY = 200f
    private var emojiWidth = 100f
    private var emojiHeight = 100f
    private var emojiScale = 1f
    private var emojiDrag = false  // Flag for dragging the emoji
    private var resizingEmoji = false  // Flag for resizing the emoji

    var text:String? = null
    var textColor: Int = Color.WHITE
    private var textX = 300f
    private var textY = 300f
    var textSize = 60f
    private var textDrag = false  // Flag for dragging the text
    private var resizingText = false  // Flag for resizing the text
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = this@DrawingViewEditer.textSize
        textAlign = Paint.Align.LEFT
    }

    private val resizeBoxMargin = 30f
    private var dragStartX = 0f
    private var dragStartY = 0f

    // Paint for drawing paths
    private var paintColor = Color.RED
    private var paintStrokeWidth = 10f
    private var paintStyle = Style.STROKE
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Style.STROKE
    }
    private val path = android.graphics.Path()


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw path
        paint.apply {
            color = paintColor
            strokeWidth = paintStrokeWidth
            style = paintStyle
        }
        canvas.drawPath(path, paint)

        // Draw the emoji (scaled)
        emojiBitmap?.let {
            val scaledWidth = it.width * emojiScale
            val scaledHeight = it.height * emojiScale
            val scaledBitmap = Bitmap.createScaledBitmap(it, scaledWidth.toInt(), scaledHeight.toInt(), true)
            canvas.drawBitmap(scaledBitmap, emojiX, emojiY, null)

            // Draw resize box around the emoji
            val boxLeft = emojiX
            val boxTop = emojiY
            val boxRight = emojiX + scaledWidth
            val boxBottom = emojiY + scaledHeight
            val rectPaint = Paint().apply {
                color = Color.BLUE
                alpha = 100
                style = Style.STROKE
                strokeWidth = 5f
            }
            canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, rectPaint)

            // Draw resize handles (corners and edges)
            val handlePaint = Paint().apply {
                color = Color.RED
                style = Style.FILL
            }

            // Corner handles
            canvas.drawCircle(boxLeft, boxTop, resizeBoxMargin, handlePaint) // Top-left


            // Edge handles (center of each side)

        }

        // Draw the text
        textPaint.color = textColor
        textPaint.textSize = textSize
        text?.let {
            canvas.drawText(it, textX, textY, textPaint)

            // Draw resize box around the text
            val textBounds = Rect()
            textPaint.getTextBounds(text, 0, text!!.length, textBounds)
            val textBoxLeft = textX
            val textBoxTop = textY - textBounds.height()
            val textBoxRight = textX + textBounds.width()
            val textBoxBottom = textY

            // Draw resize box for text
            val rectPaint = Paint().apply {
                color = Color.GREEN
                alpha = 100
                style = Style.STROKE
                strokeWidth = 5f
            }
            canvas.drawRect(textBoxLeft, textBoxTop, textBoxRight, textBoxBottom, rectPaint)

            // Draw resize handles for the text (4 corners + edges)
            val handlePaint = Paint().apply {
                color = Color.RED
                style = Style.FILL
            }
            canvas.drawCircle(textBoxLeft, textBoxTop, resizeBoxMargin, handlePaint) // Top-left corner
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if touch is within text bounds
                if (isTouchingResizeHandle(event.x, event.y, emojiX, emojiY)) {
                    resizingEmoji = true
                    dragStartX = event.x
                    dragStartY = event.y
                    return true
                }

                if (isTouchingResizeHandle(event.x, event.y, textX, textY)) {
                    resizingText = true
                    dragStartX = event.x
                    dragStartY = event.y
                    return true
                }

                // Check if the user is touching the emoji or text for dragging
                if (isTouchingObject(event.x, event.y, emojiX, emojiY)) {
                    emojiDrag = true
                    dragStartX = event.x - emojiX
                    dragStartY = event.y - emojiY
                    return true
                }

                if (isTouchingObject(event.x, event.y, textX, textY)) {
                    textDrag = true
                    dragStartX = event.x - textX
                    dragStartY = event.y - textY
                    return true
                }
                if (drawing) {
                    startNewPath()
                    path.moveTo(event.x, event.y)
                    points.add(Pair(event.x, event.y))
                    boundingRects.add(RectF(event.x, event.y, event.x, event.y))
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (drawing) {
                    try {
                        path.lineTo(event.x, event.y)
                        points.add(Pair(event.x, event.y))
                        val currentRect = boundingRects.last()
                        currentRect.union(event.x, event.y)
                        addPointToPath(event.x,event.y)
                        invalidate()
                    } catch (e:Exception) {
                        return false
                    }
                    return true
                } else {
                    if (resizingEmoji) {
                        // Resize emoji based on the drag motion
                        val dx = event.x - dragStartX
                        val dy = event.y - dragStartY
                        emojiScale += (dx + dy) * 0.005f
                        emojiScale = emojiScale.coerceIn(0.5f, 3f)
                        invalidate()
                        return true
                    }

                    if (resizingText) {
                        // Resize text size based on the drag motion
                        val dx = event.x - dragStartX
                        val dy = event.y - dragStartY
                        textSize += (dx + dy) * 0.1f
                        textSize = textSize.coerceIn(20f, 150f)
                        textPaint.textSize = textSize
                        invalidate()
                        return true
                    }

                    if (emojiDrag) {
                        // Move the emoji
                        emojiX = event.x - dragStartX
                        emojiY = event.y - dragStartY
                        invalidate()
                        return true
                    }

                    if (textDrag) {
                        // Move the text
                        textX = event.x - dragStartX
                        textY = event.y - dragStartY
                        invalidate()
                        return true
                    }
                }
                return false
            }

            MotionEvent.ACTION_UP -> {
                // Reset flags when touch is lifted
                resizingEmoji = false
                resizingText = false
                emojiDrag = false
                textDrag = false
                return true
            }
        }
        if (drawing) {
            invalidate()
        }
        return true
    }

    // Clears the paths and resets the view
    fun clear() {
        drawnPaths.clear()
        emojiBitmap = null
        text = null
        path.reset()
        points.clear()
        boundingRects.clear()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }


    private fun isTouchingResizeHandle(touchX: Float, touchY: Float, handleX: Float, handleY: Float): Boolean {
        // Check if the touch is near any of the corners or edges
        return abs(touchX - handleX) < resizeBoxMargin && abs(touchY - handleY) < resizeBoxMargin
    }

    private fun isTouchingObject(touchX: Float, touchY: Float, objX: Float, objY: Float): Boolean {
        // Check if the touch is inside the object (emoji or text)
        val distanceX = abs(touchX - objX)
        val distanceY = abs(touchY - objY)
        return distanceX < emojiWidth && distanceY < emojiHeight
    }

    fun setEmoji(bitmap: Bitmap, x: Float, y: Float) {
        drawing = false
        emojiBitmap = bitmap
        emojiX = x
        emojiY = y
        emojiScale = 1f
        invalidate()
    }

    // Method to set text properties (text content, color, size)
    fun setTextProperties(newText: String, newColor: Int, newSize: Float) {
        drawing = false
        text = newText
        textColor = newColor
        textSize = newSize
        invalidate() // Redraw the view
    }

    fun setDrawProperties(newColor: Int,newStrokeWidth:Float,newStyle:Style) {
        drawing = true
        paintColor = newColor
        paintStrokeWidth = newStrokeWidth
        paintStyle = newStyle
        invalidate() // Redraw the view
    }

    fun setNewVideoRes(newWidth:Float,newHeight:Float) {
        originalVideoWidth = newWidth
        originalVideoHeight = newHeight
        invalidate()
    }

    // Function to get emoji data relative to the original video size
    private fun getEmojiDataRelativeToVideo(scaleX: Float, scaleY: Float,canvasWidth:Float,canvasHeight: Float): ObjectData {
        val scaledWidth = emojiBitmap?.width?.times(emojiScale)?.times(scaleX) ?: 0f
        val scaledHeight = emojiBitmap?.height?.times(emojiScale)?.times(scaleY) ?: 0f
        val relativeX = (emojiX * scaleX).coerceIn(0f, canvasWidth - scaledWidth)
        val relativeY = (emojiY * scaleY).coerceIn(0f, canvasHeight - scaledHeight)

        return ObjectData(relativeX, relativeY, scaledWidth, scaledHeight, emojiScale)
    }


    // Function to get text data relative to the original video size
    private fun getTextDataRelativeToVideo(scaleX:Float,scaleY:Float): ObjectData {
        val relativeX = textX * scaleX
        val relativeY = textY * scaleY

        return ObjectData(relativeX, relativeY, 0f, 0f, textSize)
    }

    // Function to get the combined data (emoji and text) for API, relative to the video size
    fun getObjectsDataForApiRelativeToVideo(canvasWidth:Float,canvasHeight:Float): Map<String, Any> {
        val scaleX = originalVideoWidth / canvasWidth
        val scaleY = originalVideoHeight / canvasHeight
        val emojiData = getEmojiDataRelativeToVideo(scaleX,scaleY,canvasWidth,canvasHeight)
        val textData = getTextDataRelativeToVideo(scaleX,scaleY)

        return mapOf(
            "emoji" to mapOf(
                "x" to emojiData.x,
                "y" to emojiData.y,
                "width" to emojiData.width,
                "height" to emojiData.height,
                "scale" to emojiData.size
            ),
            "text" to mapOf(
                "x" to textData.x,
                "y" to textData.y,
                "size" to textData.size
            )
        )
    }

    private fun startNewPath() {
        // Start a new path when drawing begins
        drawnPaths.add(mutableListOf())
    }

    private fun addPointToPath(x: Float, y: Float) {
        // Add points to the current path
        if (drawnPaths.isNotEmpty()) {
            drawnPaths.last().add(mapOf("x" to x, "y" to y))
        }
    }

    fun getFlattenedPaths(): List<Map<String, Float>> {
        return drawnPaths.flatten()
    }

    fun getFreehandPaths(): List<List<Map<String, Float>>> {
        // Return all paths
        return drawnPaths
    }

    // Method to get points with multiple bounding boxes
    fun getPoints(): List<Map<String, Float>> {
        return boundingRects.map { rect ->
            mapOf(
                "x" to rect.left,
                "y" to rect.top,
                "w" to rect.width(),
                "h" to rect.height()
            )
        }
    }

}
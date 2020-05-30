package com.pru.notes.custom_views

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout


open class SignatureDrawView(context: Context) : View(context) {
    // set the stroke width
    private val TAG = SignatureDrawView::class.java.simpleName
    private val dirtyRect = RectF()
    private val paint = Paint()
    private val path = Path()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    var counter = 0
    // set the signature bitmap

    // important for saving signature
     fun getBitmapS(): Bitmap? {
            if (path.isEmpty || counter < 1) {
                return null
            }

        // set the signature bitmap
        val signatureBitmap: Bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)

            // important for saving signature
            val canvas = Canvas(signatureBitmap)
            this.draw(canvas)
            return signatureBitmap
        }

    /**
     * clear signature canvas
     */
    fun clearSignature() {
        counter = 0
        path.reset()
        this.invalidate()
    }

    // all touch events during the drawing
    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                lastTouchX = eventX
                lastTouchY = eventY
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                counter = counter + 1
                resetDirtyRect(eventX, eventY)
                val historySize = event.historySize
                var i = 0
                while (i < historySize) {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    expandDirtyRect(historicalX, historicalY)
                    path.lineTo(historicalX, historicalY)
                    i++
                }
                path.lineTo(eventX, eventY)
            }
            else -> return false
        }
        invalidate(
            (dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt()
        )
        lastTouchX = eventX
        lastTouchY = eventY
        return true
    }

    private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY
        }
    }

    private fun resetDirtyRect(eventX: Float, eventY: Float) {
        dirtyRect.left = Math.min(lastTouchX, eventX)
        dirtyRect.right = Math.max(lastTouchX, eventX)
        dirtyRect.top = Math.min(lastTouchY, eventY)
        dirtyRect.bottom = Math.max(lastTouchY, eventY)
    }

    companion object {
        private const val STROKE_WIDTH = 10f
        private const val HALF_STROKE_WIDTH = STROKE_WIDTH / 2
    }

    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = context.resources.getColor(R.color.black)
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = STROKE_WIDTH
        paint.strokeCap = Paint.Cap.ROUND
        setBackgroundColor(Color.TRANSPARENT)
        // width and height should cover the screen
        this.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
    }
}
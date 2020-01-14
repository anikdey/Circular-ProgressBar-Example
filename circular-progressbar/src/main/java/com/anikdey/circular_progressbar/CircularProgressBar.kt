package com.anikdey.circular_progressbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Created by Anik Dey on 1/7/2020
 */
class CircularProgressBar constructor(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    private var progressRect: RectF
    private var innerRect: RectF
    private var progressBarWidth = 10f
    private var innerGap = 20f + (progressBarWidth / 2)
    private var angle: Float = 0.toFloat()
    private var innerCircleColor: Int = Color.WHITE
    private var progressCircleColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK

    private var startAngel = -90f
    private var percentCompleted = 0f
    private var calculatedHeight = 0
    private var calculatedWidth = 0

    private val textBounds = Rect()
    private var percentageCompletedText = ""
    private var textSize = 50
    private val paint = Paint()

    init {
        paint.isAntiAlias = true
        innerGap = 20f + (progressBarWidth / 2)
        progressRect = RectF(
            progressBarWidth,
            progressBarWidth,
            calculatedWidth.toFloat() - progressBarWidth,
            calculatedHeight.toFloat() - progressBarWidth
        )

        innerRect = RectF(
            progressBarWidth + innerGap,
            progressBarWidth + innerGap,
            calculatedWidth.toFloat() - progressBarWidth - innerGap,
            calculatedHeight.toFloat() - progressBarWidth - innerGap
        )

        attrs?.let { attributes ->
            val typedArray =
                context.obtainStyledAttributes(attributes, R.styleable.CircularProgressBar)
            textSize = typedArray.getDimensionPixelSize(
                R.styleable.CircularProgressBar_mtextSize,
                textSize
            )
            startAngel =
                typedArray.getFloat(R.styleable.CircularProgressBar_mstartAngle, startAngel)
            progressBarWidth = typedArray.getFloat(
                R.styleable.CircularProgressBar_progressBarWidth,
                progressBarWidth
            )
            percentCompleted = typedArray.getFloat(
                R.styleable.CircularProgressBar_mpercentCompleted,
                percentCompleted
            )
            innerCircleColor = typedArray.getColor(
                R.styleable.CircularProgressBar_innerCircleColor,
                innerCircleColor
            )
            textColor = typedArray.getColor(R.styleable.CircularProgressBar_mtextColor, textColor)
            progressCircleColor = typedArray.getColor(
                R.styleable.CircularProgressBar_progressCircleColor,
                progressCircleColor
            )
            typedArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        innerRect.right = calculatedWidth.toFloat() - progressBarWidth - innerGap
        innerRect.bottom = calculatedHeight.toFloat() - progressBarWidth - innerGap

        progressRect.right = calculatedWidth.toFloat() - progressBarWidth
        progressRect.bottom = calculatedHeight.toFloat() - progressBarWidth

        drawInnerCircle(canvas, paint)
        drawProgressCircle(canvas, paint)

        if (!percentageCompletedText.isBlank()) {
            val centerX = (innerRect.left + innerRect.right) / 2
            val centerY = (innerRect.top + innerRect.bottom) / 2
            drawPercentageCompletedText(percentageCompletedText, paint, canvas, centerX, centerY)
        }
    }

    private fun drawInnerCircle(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = innerCircleColor
        canvas.drawArc(innerRect, 0f, 360f, true, paint)
    }

    private fun drawProgressCircle(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = progressBarWidth
        paint.color = progressCircleColor
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(progressRect, startAngel, angle, false, paint)
    }

    private inline fun drawPercentageCompletedText(
        str: String,
        paint: Paint,
        canvas: Canvas,
        centerX: Float,
        centerY: Float
    ) {
        paint.textSize = textSize.toFloat()
        paint.color = textColor
        paint.style = Paint.Style.FILL
        paint.getTextBounds(str, 0, str.length, textBounds)
        canvas.drawText(
            str,
            centerX - textBounds.width() / 2,
            centerY + (textBounds.height() / 2),
            paint
        )
    }

    fun setProgress(percentCompleted: Float) {
        percentageCompletedText = "${percentCompleted}%"
        val animation = CircleAngleAnimation(this, percentCompleted)
        animation.duration = 1000
        this.startAnimation(animation)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        calculatedWidth = measureDimension(desiredWidth, widthMeasureSpec)
        calculatedHeight = measureDimension(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(calculatedWidth, calculatedHeight)
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    inner class CircleAngleAnimation(
        private val circle: CircularProgressBar,
        percentCompleted: Float
    ) : Animation() {
        private var oldAngle: Float = angle
        private var newAngle: Float = (360 * percentCompleted / 100)

        override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
            val newCalculatedAngle = oldAngle + (newAngle - oldAngle) * interpolatedTime
            angle = newCalculatedAngle
            circle.requestLayout()
        }
    }
}
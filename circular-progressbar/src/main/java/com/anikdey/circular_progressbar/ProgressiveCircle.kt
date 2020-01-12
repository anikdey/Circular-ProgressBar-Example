package com.anikdey.circular_progressbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Created by Anik Dey on 1/7/2020
 */

class ProgressiveCircle : View {

    private lateinit var foregroundRect: RectF
    private lateinit var backgroundRect: RectF
    private val startingPoint = 0f
    private var angle: Float = 0.toFloat()
    private var backgroundCircleColor: Int = Color.WHITE
    private var foregroundCircleColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK

    private var startAngel = -90f
    private var percentCompleted = 0f
    private var calculatedHeight = 0
    private var calculatedWidth = 0

    private val textBounds = Rect()
    private var percentageCompletedText = ""
    private var textSize = 50

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {attributes->
            var typedArray = context.obtainStyledAttributes(attributes, R.styleable.ProgressiveCircle)
            textSize = typedArray.getDimensionPixelSize(R.styleable.ProgressiveCircle_textSize, textSize)
            startAngel = typedArray.getFloat(R.styleable.ProgressiveCircle_startAngle, startAngel)
            percentCompleted = typedArray.getFloat(R.styleable.ProgressiveCircle_percentCompleted, percentCompleted)
            backgroundCircleColor = typedArray.getColor(R.styleable.ProgressiveCircle_backgroundCircleColor, backgroundCircleColor)
            textColor = typedArray.getColor(R.styleable.ProgressiveCircle_textColor, textColor)
            foregroundCircleColor = typedArray.getColor(R.styleable.ProgressiveCircle_foregroundCircleColor, foregroundCircleColor)
            typedArray.recycle()
        }
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        foregroundRect = RectF(startingPoint, startingPoint, calculatedWidth.toFloat(), calculatedHeight.toFloat())
        backgroundRect = RectF(startingPoint, startingPoint, calculatedWidth.toFloat(), calculatedHeight.toFloat())

        var paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        drawBackgroundCircle(canvas, paint)
        drawForegroundCircle(canvas, paint)

        if(!percentageCompletedText.isNullOrBlank()) {
            val centerX = (foregroundRect.left + foregroundRect.right) / 2
            val centerY = (foregroundRect.top + foregroundRect.bottom) / 2
            drawPercentageCompletedText(percentageCompletedText, paint, canvas, centerX, centerY)
        }
    }

    private fun drawBackgroundCircle(canvas: Canvas, paint: Paint) {
        paint.color = backgroundCircleColor
        canvas.drawArc(backgroundRect, startAngel, 360f, true, paint)
    }

    private fun drawForegroundCircle(canvas: Canvas, paint: Paint) {
        paint.color = foregroundCircleColor
        canvas.drawArc(foregroundRect, startAngel, angle, true, paint)
    }

    private inline fun drawPercentageCompletedText(str: String, paint: Paint, canvas: Canvas, centerX: Float, centerY: Float) {
        paint.textSize = textSize.toFloat()
        paint.color = textColor
        paint.getTextBounds(str, 0, str.length, textBounds)
        canvas.drawText(str, centerX - textBounds.width()/2, centerY + (textBounds.height()/2), paint)
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

    inner class CircleAngleAnimation(private val circle: ProgressiveCircle, percentCompleted: Float) : Animation() {
        private var oldAngle: Float = angle
        private var newAngle: Float = (360 * percentCompleted / 100)

        override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
            val newCalculatedAngle = oldAngle + (newAngle - oldAngle) * interpolatedTime
            angle = newCalculatedAngle
            circle.requestLayout()
        }
    }
}
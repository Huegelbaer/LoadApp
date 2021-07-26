package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var _backgroundColor: Int = 0
    private var _loadingBackgroundColor: Int = 0
    private var _loadingCircleColor: Int = 0
    private var _textColor: Int = 0
    private var _text: String? = null

    private var valueAnimator = ValueAnimator()
    private var progressValue : Float = 0.0f

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }

    private var _paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 60.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            _backgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            _loadingBackgroundColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            _textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            _loadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, _textColor)
            _text = getString(R.styleable.LoadingButton_text)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()

        valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener {
                progressValue = it.animatedValue as Float
                invalidate()
            }
            duration = 3000
            start()
        }


        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val myCanvas = canvas ?: return

        _paint.color = _backgroundColor
        myCanvas.drawRect(0.0F, 0.0F, width.toFloat(), height.toFloat(), _paint)

        val textStartX = width.toFloat() / 2
        val textStartY = height.toFloat() / 2 + _paint.descent()

        if (valueAnimator.isRunning) {

            drawProgressBar(myCanvas)

            var textWidth = 0.0f
            _text = resources.getString(R.string.button_loading)
            _text?.let {
                drawText(canvas, it, textStartX, textStartY)
                textWidth = _paint.measureText(it)
            }

            val arcStartX = textStartX + textWidth/2 + 20
            drawCircleProgress(myCanvas, arcStartX)
        } else {
            _text = resources.getString(R.string.button_name)
            _text?.let {
                drawText(canvas, it, textStartX, textStartY)
            }
        }
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float) {
        _paint.color = _textColor
        canvas.drawText(text, x, y, _paint)
    }

    private fun drawProgressBar(canvas: Canvas) {
        _paint.color = _loadingBackgroundColor
        canvas.drawRect(0.0F, 0.0F, progressValue * width, height.toFloat(), _paint)
    }

    private fun drawCircleProgress(canvas: Canvas, startX: Float) {
        val radius = height.toFloat()/4

        _paint.color = _textColor
        canvas.drawArc(
            startX,
            radius,
            startX + (radius*2),
            radius *3,
            0.0f,
            progressValue * 360f,
            true,
            _paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}
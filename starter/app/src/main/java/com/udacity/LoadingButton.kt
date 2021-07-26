package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var _backgroundColor: Int = 0
    private var _loadingBackgroundColor: Int = 0
    private var _textColor: Int = 0
    private var _text: String? = null

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }

    private var _paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 60.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    init {

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            _backgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            _loadingBackgroundColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            _textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            _text = getString(R.styleable.LoadingButton_text)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        _paint.color = _backgroundColor
        canvas?.drawRect(0.0F, 0.0F, width.toFloat(), height.toFloat(), _paint)

        _paint.color = _textColor
        val yPos = _paint.textSize
        _text?.let {
            canvas?.drawText(it, width.toFloat()/2, height.toFloat()/2 + yPos/2, _paint)
        }
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
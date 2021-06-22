package com.udacity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var textBound = Rect()
    private var displayText = ""

    private var backGroundColor = 0
    private var loadingBackgroundColor = 0
    private var textColor = 0
    private var archColor = 0
    private var buttonText = ""
    private var buttonLoadingText = ""
    private var animDuration = 0
    private var progress = 1F
    private val valueAnimator: ValueAnimator

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backGroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            loadingBackgroundColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            buttonText = getString(R.styleable.LoadingButton_buttonText) ?: ""
            buttonLoadingText = getString(R.styleable.LoadingButton_buttonLoadingText) ?: ""
            archColor = getColor(R.styleable.LoadingButton_archColor, 0)
            animDuration = getInt(R.styleable.LoadingButton_animDuration, 1000)
        }
        valueAnimator = ValueAnimator.ofFloat(0F, 1F)
        valueAnimator.duration = animDuration.toLong()
        valueAnimator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        invalidate()
        if (new == ButtonState.Loading) {
            valueAnimator.start()
            valueAnimator.doOnEnd { buttonState = ButtonState.Completed }
        }
    }

    public fun setState(state: ButtonState) {
        buttonState = state
    }



    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
    }




    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = backGroundColor
        canvas?.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)
        if (buttonState == ButtonState.Loading) {
            paint.color = loadingBackgroundColor
            displayText = buttonLoadingText
        } else {
            displayText = buttonText
        }
        canvas?.drawRect(0F, 0F, widthSize.toFloat()*progress, heightSize.toFloat(), paint)
        paint.color = textColor
        paint.getTextBounds(displayText, 0, displayText.length,textBound)
        canvas?.drawText(displayText, width.toFloat()/2, (height.toFloat()+paint.textSize/2)/2,paint)

        if (buttonState == ButtonState.Loading) {
            paint.color = archColor
            canvas?.drawArc(
                ((widthSize + textBound.width())/2).toFloat() + 10,
                (height.toFloat()-paint.textSize)/2,
                ((widthSize + textBound.width())/2).toFloat() + paint.textSize + 10,
                (height.toFloat()+paint.textSize)/2,
                0F, 360*progress, true, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
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
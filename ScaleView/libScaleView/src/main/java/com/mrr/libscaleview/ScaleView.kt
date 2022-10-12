package com.mrr.libscaleview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.mrr.libscaleview.attr.ScaleViewAttr
import com.mrr.libscaleview.enum.ScaleAttrEnum
import com.mrr.libscaleview.view.BaseScaleView
import com.mrr.libscaleview.view.CircleScaleView
import com.mrr.libscaleview.view.HorizontalScaleView
import com.mrr.libscaleview.view.VerticalScaleView
import java.util.function.Consumer
import com.mrr.libscaleview.R

class ScaleView : View {
    val TAG = "ScaleView"

    var mContext: Context? = null

    var mAttr = ScaleViewAttr();

    lateinit var mScaleView: BaseScaleView

    var mProgressChangeListener: ProgressChangeListener? = null
        set(value) {

            if (this::mScaleView.isInitialized) {
                mScaleView?.mProgressChangeListener = value
            }

        }

    /**
     * 是否已经初始化画笔
     */
    private var isInit = false

    var mWidth = 0f
    var mHeight = 0f

    var mTouchX = 0f;
    var mTouchY = 0f;


    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {


        this.mContext = context
        var typeArray = context?.obtainStyledAttributes(attrs, R.styleable.scaleView)
        mAttr.initAttr(typeArray, mContext!!)

        when {
            (mAttr.mScaleStyle == ScaleAttrEnum.LINE && mAttr.mScaleDirect == ScaleAttrEnum.VERTICAL) -> {
                mScaleView = VerticalScaleView(mAttr)
            }
            (mAttr.mScaleStyle == ScaleAttrEnum.LINE && mAttr.mScaleDirect == ScaleAttrEnum.HORIZONTAL) -> {
                mScaleView = HorizontalScaleView(mAttr)
            }
            (mAttr.mScaleStyle == ScaleAttrEnum.CIRCLE) -> {
                mScaleView = CircleScaleView(mAttr)
            }
        }

        typeArray?.recycle()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (isInit) {
            return
        }

        mWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        mHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        mAttr.mWidth = mWidth
        mAttr.mHeight = mHeight

        setMeasuredDimension(mWidth.toInt(), mHeight.toInt())

        mAttr.mPaddingLeft = paddingLeft
        mAttr.mPaddingRight = paddingRight
        mAttr.mPaddingTop = paddingTop
        mAttr.mPaddingBottom = paddingBottom



        mScaleView.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mScaleView.initTouchXY(
            Consumer { t -> mTouchX = t },
            Consumer { t -> mTouchY = t }
        )
        isInit = true

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mScaleView.onDraw(canvas, mTouchX, mTouchY)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mScaleView.touching = true
                touchView(event)
            }
            MotionEvent.ACTION_MOVE -> {
                touchView(event)
            }
            MotionEvent.ACTION_UP -> {
                touchView(event)
                mScaleView.touching = false
            }
        }
        var caninvalidate = if (mScaleView is HorizontalScaleView) {
            (mScaleView as HorizontalScaleView).canDraw(mTouchX)
        } else {
            true
        }
        if (caninvalidate) {
            invalidate()
        }
        return true
    }

    private fun touchView(event: MotionEvent) {
        mTouchX = event.x
        mTouchY = event.y

        if (mTouchX < paddingLeft) {
            mTouchX = paddingLeft.toFloat()
        }

        if (mTouchX > mWidth - paddingRight) {
            mTouchX = mWidth - paddingRight
        }

        if (mTouchY < paddingTop) {
            mTouchY = paddingTop.toFloat()
        }

        if (mTouchY > mHeight - paddingBottom) {
            mTouchY = mHeight - paddingBottom
        }
    }

    interface ProgressChangeListener {

        fun progressChange(progress: Float)
    }
}
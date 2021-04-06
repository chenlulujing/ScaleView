package com.mrr.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * 1. 画线性刻度
 * - 解决初始状态游标位置问题
 * - 设置一个背景颜色,看看是不是居中
 * - 绘制刻度节点长度不同效果
 *
 * 2. 画线性游标
 * - 游标按照节点最长的位置绘制
 */
class CalibrationView : View {
    val TAG = "CalibrationProgressView"

    var mContext: Context? = null
    var mParam = CalibrationParam();

    var mProgressListener: CalibrationParam.ProgressListener? = null

    var mWidth = 0
    var mHeight = 0

    var mPaddingLeft = 0
    var mPaddingTop = 0
    var mPaddingRight = 0
    var mPaddingBottom = 0

    /**
     * 刻度线一半的厚度,方便计算
     */
    var mHalfCalibration = 0f;

    /**
     * 刻度之间的缝隙大小
     */
    var mUnitInterval = 0

    /**
     * 每个一个刻度最长可绘制的空间
     */
    var mInterval = 0

    /**
     * 当前触摸到的位置
     */
    var mTouchY = 0f;

    /**
     * 默认刻度画笔
     */
    var mOriginColorPaint = Paint()

    /**
     * 走过的刻度画笔
     */
    var mChangeColorPaint = Paint()

    /**
     * 游标画笔
     */
    var mCursorPaint = Paint()

    /**
     * 是否已经初始化画笔
     */
    var isInit = false


    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mContext = context
        var typeArray = context?.obtainStyledAttributes(attrs, R.styleable.CalibrationView)

        mParam.mCalibrationWidth =
            typeArray!!.getFloat(R.styleable.CalibrationView_calibrationWidth, 0.5f)


        mParam.mCalibrationNodeWidth =
            typeArray!!.getFloat(R.styleable.CalibrationView_calibrationNodeWidth, 0.7f)

        mParam.mCalibrationThick =
            typeArray!!.getDimension(R.styleable.CalibrationView_calibrationThick, 5f)


        var style = typeArray!!.getInt(R.styleable.CalibrationView_calibrationStyle, -1)
        if (style > 0) {
            setCalibrationStyle(style)
        }

        var direct =
            typeArray!!.getInt(R.styleable.CalibrationView_calibrationDirect, -1)
        if (direct > 0) {
            setCalibrationDirect(direct)
        }

        var cursorLoc =
            typeArray!!.getInt(R.styleable.CalibrationView_cursorLoc, -1)
        if (cursorLoc > 0) {
            setCursorLoc(cursorLoc)
        }

        mParam.mCursorWidth =
            typeArray!!.getDimension(R.styleable.CalibrationView_cursorWidth, 20f)

        mParam.mCursorGap =
            typeArray!!.getDimension(R.styleable.CalibrationView_cursorGap, 5f)

        mParam.mTotalProgress =
            typeArray!!.getInt(R.styleable.CalibrationView_totalProgress, 50)

        mParam.mUnitCalibration =
            typeArray!!.getInt(R.styleable.CalibrationView_unitCalibration, 10)

        mParam.mDefaultColor =
            typeArray!!.getColor(R.styleable.CalibrationView_defaultColor, Color.DKGRAY)

        mParam.mProgressColor =
            typeArray!!.getColor(R.styleable.CalibrationView_progressColor, Color.DKGRAY)

        typeArray.recycle()
        setBackgroundColor(resources.getColor(R.color.yellow))
    }

    fun setCalibrationStyle(style: Int) {
        when (style) {
            CalibrationStyle.LINE.value -> {
                mParam.mCalibrationStyle = CalibrationStyle.LINE
            }
            CalibrationStyle.CIRCLE.value -> {
                mParam.mCalibrationStyle = CalibrationStyle.CIRCLE
            }
        }

    }

    fun setCalibrationDirect(direct: Int) {
        when (direct) {
            CalibrationStyle.HORIZONTAL.value -> {
                mParam.mCalibrationDirect = CalibrationStyle.HORIZONTAL
            }
            CalibrationStyle.VERTICAL.value -> {
                mParam.mCalibrationDirect = CalibrationStyle.VERTICAL
            }
        }

    }

    fun setCursorLoc(loc: Int) {
        when (loc) {
            CalibrationStyle.LEFT.value -> {
                mParam.mCursorLoc = CalibrationStyle.LEFT
            }
            CalibrationStyle.RIGHT.value -> {
                mParam.mCursorLoc = CalibrationStyle.RIGHT
            }
            CalibrationStyle.INSIDE.value -> {
                mParam.mCursorLoc = CalibrationStyle.INSIDE
            }
            CalibrationStyle.OUTSIDE.value -> {
                mParam.mCursorLoc = CalibrationStyle.OUTSIDE
            }
        }

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (isInit) {
            return
        }

        initData(widthMeasureSpec, heightMeasureSpec)

        isInit = true

        Log.d(TAG, "onMeasure : mWidth : $mWidth mHeight : $mHeight")
    }

    private fun initData(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(mWidth, mHeight)

        initPaint()

        mPaddingLeft = paddingLeft
        mPaddingTop = paddingTop
        mPaddingRight = paddingRight
        mPaddingBottom = paddingBottom

        if (mParam.mCalibrationStyle == CalibrationStyle.LINE) {

            //刻度线本身占用的空间
            var calibrationSpace = mParam.mCalibrationThick * (mParam.mTotalProgress + 1)

            if (mParam.mCalibrationDirect == CalibrationStyle.VERTICAL) {

                //总共的绘制空间
                var drawSpace = mHeight - mPaddingTop - mPaddingBottom
                //刻度之间的缝隙大小
                mUnitInterval = ((drawSpace - calibrationSpace) / mParam.mTotalProgress).toInt()
                //每个一个刻度最长可绘制的空间
                mInterval = mWidth - mPaddingLeft - mPaddingRight

            } else if (mParam.mCalibrationDirect == CalibrationStyle.HORIZONTAL) {

                var drawSpace = mWidth - mPaddingLeft - paddingRight
                mUnitInterval = ((drawSpace - calibrationSpace) / mParam.mTotalProgress).toInt()
                mInterval = mHeight - mPaddingTop - mPaddingBottom
            }

        } else if (mParam.mCalibrationStyle == CalibrationStyle.CIRCLE) {

        }


        mHalfCalibration = mParam.mCalibrationThick / 2

        mTouchY = (mPaddingTop + mUnitInterval).toFloat()

        mProgressListener = mParam.mProgressListener
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

//        drawCalibration(canvas, mChangeColorPaint, 0, mTouchY.toInt())
        drawCalibration(canvas, mOriginColorPaint, 0, mHeight)

//        drawCursor(canvas)
    }

    /**
     * 画刻度
     */
    private fun drawCalibration(canvas: Canvas?, paint: Paint, from: Int, to: Int) {


        canvas!!.save()

        //这里就是最重要的分割canvas,可以分成上下左右的任何部分
        val rect = Rect(0, from, mWidth, to)
        canvas.clipRect(rect)


        //第一个节点是一个刻度节点
        var nodeLength = mInterval * mParam.mCalibrationNodeWidth

        //普通刻度占组件减去padding之后的宽度/高度
        var linelength = mInterval * mParam.mCalibrationWidth

        var nodeStartX = 0f
        var nodeStopX = 0f

        var nodeStartY = 0f
        var nodeStopY = 0f

        var startX = 0f
        var stopX = 0f

        var startY = 0f
        var stopY = 0f


        if (mParam.mCalibrationDirect == CalibrationStyle.VERTICAL) {

            nodeStartX = (mWidth - nodeLength) / 2
            nodeStopX = nodeStartX + nodeLength

            startX = (mWidth - linelength) / 2
            stopX = startX + linelength

            startY = mPaddingTop + mHalfCalibration
            stopY = startY

            for (index in 0..mParam.mTotalProgress) {
                if (stopY > to) {
                    break
                }

                canvas?.drawLine(
                    if (index % mParam.mUnitCalibration == 0) nodeStartX.toFloat() else startX,
                    startY.toFloat(),
                    if (index % mParam.mUnitCalibration == 0) nodeStopX.toFloat() else stopX,
                    stopY.toFloat(),
                    paint
                )

                startY += (mUnitInterval + mParam.mCalibrationThick)
                stopY += (mUnitInterval + mParam.mCalibrationThick)

                Log.d(TAG, "index : $index ")
            }

        } else if (mParam.mCalibrationDirect == CalibrationStyle.HORIZONTAL) {

            nodeStartX = paddingLeft + mHalfCalibration
            nodeStopX = nodeStartX

            nodeStartY = (mHeight - nodeLength) / 2
            nodeStopY = nodeStartY + nodeLength

            startY = (mHeight - linelength) / 2
            stopY = startY + linelength

            for (index in 0..mParam.mTotalProgress) {

                canvas?.drawLine(
                    nodeStartX,
                    if (index % mParam.mUnitCalibration == 0) nodeStartY.toFloat() else startY,
                    nodeStopX,
                    if (index % mParam.mUnitCalibration == 0) nodeStopY.toFloat() else stopY,
                    paint
                )

                nodeStartX += (mUnitInterval + mParam.mCalibrationThick)
                nodeStopX += (mUnitInterval + mParam.mCalibrationThick)

            }
        }



        canvas.restore()

    }

    /**
     * 画游标
     */
    private fun drawCursor(canvas: Canvas?) {

        var startX = (mWidth / 4 * 3).toFloat()
        var startY = mTouchY
        var cursorpath = Path()
        cursorpath.moveTo(startX, mTouchY)

        var targetXLength =
            Math.sqrt((mParam.mCursorWidth * mParam.mCursorWidth - (mParam.mCursorWidth / 2) * (mParam.mCursorWidth / 2)).toDouble())

        cursorpath.lineTo(
            (startX + targetXLength).toFloat(),
            mTouchY - mParam.mCursorWidth / 2
        )
        cursorpath.lineTo(
            (startX + targetXLength).toFloat(),
            mTouchY + mParam.mCursorWidth / 2
        )
        cursorpath.close()
        canvas?.drawPath(cursorpath, mCursorPaint)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {

                mTouchY = event.y

                if (mTouchY < mPaddingTop + mUnitInterval) {
                    mTouchY = (mPaddingTop + mUnitInterval).toFloat()
                }

                if (mTouchY > mPaddingTop + (mUnitInterval * (mParam.mTotalProgress + 1))) {
                    mTouchY =
                        (mPaddingTop + (mUnitInterval * (mParam.mTotalProgress + 1))).toFloat()
                }
            }
        }

        Log.d(TAG, "mTouchY: $mTouchY")
        invalidate()

        mProgressListener?.progressChanged(mTouchY)
        return true
    }


    private fun initPaint() {
        mOriginColorPaint.isAntiAlias = true
        mOriginColorPaint.style = Paint.Style.STROKE
        mOriginColorPaint.color = mParam.mDefaultColor
        mOriginColorPaint.strokeWidth = mParam.mCalibrationThick

        mChangeColorPaint.isAntiAlias = true
        mChangeColorPaint.style = Paint.Style.STROKE
        mChangeColorPaint.color = mParam.mProgressColor
        mChangeColorPaint.strokeWidth = mParam.mCalibrationThick


        mCursorPaint.isAntiAlias = true
        mCursorPaint.style = Paint.Style.STROKE
        mCursorPaint.strokeWidth = 1f
    }
}
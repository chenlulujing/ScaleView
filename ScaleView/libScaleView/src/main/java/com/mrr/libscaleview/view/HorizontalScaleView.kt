package com.mrr.libscaleview.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import com.mrr.libscaleview.attr.ScaleViewAttr
import com.mrr.libscaleview.enum.ScaleAttrEnum
import com.mrr.libscaleview.rectf.ScaleTextRectF
import com.mrr.libscaleview.util.UnitConversion.Companion.px
import java.util.function.Consumer

class HorizontalScaleView : BaseScaleView {


    var letter: String = ""
    var scaleTextRectF: ScaleTextRectF? = null

    constructor(attr: ScaleViewAttr) : super(attr) {
        scaleTextRectF = ScaleTextRectF()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        //刻度线本身占用的空间
        var calibrationSpace = mAttr.mScaleLineWidth * (mAttr.mTotalProgress + 1)

        drawSpace = mAttr.mWidth - mAttr.mPaddingLeft - mAttr.mPaddingRight
        //刻度之间的缝隙大小
        perInterval = (drawSpace - calibrationSpace) / mAttr.mTotalProgress
        //每个一个刻度最长可绘制的空间
        interval = mAttr.mHeight - mAttr.mPaddingTop - mAttr.mPaddingBottom
        nodeLength = interval * mAttr.mScaleNodeWidth
        linelength = interval * mAttr.mScaleWidth
        keylength = interval * mAttr.mKeyNodeWidth
        clipRect = RectF()
    }

    fun calculateCurrentPosition(touchX: Float): Int {
        var position = mAttr.mCurrentPosition
        var nodeStartX_ = mAttr.mPaddingLeft.toFloat()
        for (index in 0..mAttr.mTotalProgress) {
            if (nodeStartX_ + mAttr.mScaleLineWidth + perInterval > touchX && nodeStartX_ < touchX) {
                position = index
                break
            }
            nodeStartX_ += (perInterval + mAttr.mScaleLineWidth)
        }
        return position
    }

    fun canDraw(touchX: Float): Boolean {
        var currentPosition: Int = calculateCurrentPosition(touchX)
        Log.i("llc_scale", "calculate currentPosition=$currentPosition")
        Log.i("llc_scale", "mAttr.mCurrentPosition=${mAttr.mCurrentPosition}")
        var result = currentPosition != mAttr.mCurrentPosition
        if (result) {
            mAttr.mCurrentPosition = currentPosition;
        }
        return result
    }

    override fun onDraw(canvas: Canvas?, touchX: Float, touchY: Float) {
        Log.i("llc_scale", "onDraw")
        clipProgress = drawSpace * (touchX - mAttr.mPaddingLeft) / drawSpace
//        clipRect?.set(0f, 0f, mAttr.mCurrentX, mAttr.mHeight)
        drawLineScale(canvas, changeColorPaint, clipRect)
//        clipRect?.set(mAttr.mCurrentX, 0f, mAttr.mWidth, mAttr.mHeight)
//        drawLineScale(canvas, originColorPaint, clipRect)
        progressChange(mAttr.mCurrentPosition.toFloat() / mAttr.mTotalProgress)
    }


    override fun initTouchXY(touchXCon: Consumer<Float>, touchYCon: Consumer<Float>) {
        touchXCon.accept(mAttr.mPaddingLeft.toFloat())
    }


    /**
     * 画线性的刻度
     */
    private fun drawLineScale(canvas: Canvas?, paint: Paint, clipRectF: RectF) {


        canvas!!.save()
//        canvas.clipRect(clipRectF)


        nodeStartX = mAttr.mPaddingLeft + halfCalibration
        nodeStopX = nodeStartX

        nodeStartY = mAttr.mPaddingTop + (interval - nodeLength) / 2
        nodeStopY = nodeStartY + nodeLength

        startY = mAttr.mPaddingTop + (interval - linelength) / 2
        stopY = startY + linelength

        keyStartY = mAttr.mPaddingTop + (interval - keylength) / 2
        keyStopY = keyStartY + keylength
        for (index in 0..mAttr.mTotalProgress) {

            if (index < mAttr.mCurrentPosition) {
                //已滑动部分
                canvas?.drawLine(
                    nodeStartX,
                    nodeStartY,
                    nodeStopX,
                    nodeStopY,
                    paint
                )
            } else {
                //未滑动部分
                canvas?.drawLine(
                    nodeStartX,
                    startY,
                    nodeStopX,
                    stopY,
                    paint
                )
            }
            if(index ==  mAttr.mCurrentPosition){
                //未滑动部分最后刻度
                canvas?.drawLine(
                    nodeStartX,
                    keyStartY,
                    nodeStopX,
                    keyStopY,
                    paint
                )
                mAttr.mCurrentX = nodeStartX + mAttr.mScaleLineWidth
            }
//            if (nodeStartX + mAttr.mScaleLineWidth + perInterval > touchX && nodeStartX < touchX) {
//                mAttr.mCurrentPosition = index
//                //未滑动部分最后刻度
//                canvas?.drawLine(
//                    nodeStartX,
//                    keyStartY,
//                    nodeStopX,
//                    keyStopY,
//                    paint
//                )
//                mAttr.mCurrentX = nodeStartX + mAttr.mScaleLineWidth
//            }
            nodeStartX += (perInterval + mAttr.mScaleLineWidth)
            nodeStopX += (perInterval + mAttr.mScaleLineWidth)
        }

        canvas.restore()
    }


    /**
     * 绘制游标
     *
     */
    private fun drawCursor(canvas: Canvas?, touchX: Float, touchY: Float) {

        if (null == mAttr.mCursorBitmap) {
            return
        }

        when (mAttr.mCursorSeat) {
            ScaleAttrEnum.TOP -> {
                if (mAttr.mScaleStyle != ScaleAttrEnum.LINE) {
                    return
                }

                cursorRectF.mTransX = touchX - mAttr.mCursorWidth.px / 2

                cursorRectF.mTransY =
                    mAttr.mPaddingTop + (interval - linelength) / 2 - mAttr.mCursorGap.px - mAttr.mCursorWidth.px
            }
            ScaleAttrEnum.BOTTOM -> {
                if (mAttr.mScaleStyle != ScaleAttrEnum.LINE) {
                    return
                }

                cursorRectF.mTransX = touchX - mAttr.mCursorWidth.px / 2

                cursorRectF.mTransY =
                    mAttr.mPaddingTop + (interval - linelength) / 2 + linelength + mAttr.mCursorGap.px

            }


        }


        cursorMatrix.reset()
        // 创建操作图片用的 Matrix 对象
        cursorMatrix.postScale(
            cursorRectF.mScaleX, cursorRectF.mScaleY
        );
        cursorMatrix.postTranslate(
            cursorRectF.mTransX,
            cursorRectF.mTransY
        );

        canvas?.drawBitmap(mAttr.mCursorBitmap!!, cursorMatrix, null)

    }


}
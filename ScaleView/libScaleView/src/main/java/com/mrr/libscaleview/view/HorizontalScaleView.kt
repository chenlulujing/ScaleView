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

    fun calculateCurrentPosition(touchX: Float) {
        for (index in 0..mAttr.mTotalProgress) {
            if (nodeStartX + mAttr.mScaleLineWidth > touchX && nodeStartX < touchX) {
                mAttr.mCurrentPosition = index
                break
            }
            nodeStartX += (perInterval + mAttr.mScaleLineWidth)
            nodeStopX += (perInterval + mAttr.mScaleLineWidth)
        }

    }


    override fun onDraw(canvas: Canvas?, touchX: Float, touchY: Float) {
//        if (touchX != 0f && touchY != 0f && mAttr.mCurrentPosition != 0) {
//            var currentPosition = mAttr.mCurrentPosition
//            calculateCurrentPosition(touchX)
//            if (currentPosition == mAttr.mCurrentPosition) {
//                return
//            }
//        }
        clipProgress = drawSpace * (touchX - mAttr.mPaddingLeft) / drawSpace

        clipRect?.set(0f, 0f, mAttr.mCurrentX, mAttr.mHeight)
        drawLineScale(canvas, changeColorPaint, clipRect, touchX)
        progressChange(mAttr.mTotalProgress * (touchX - mAttr.mPaddingLeft) / drawSpace)
        clipRect?.set(mAttr.mCurrentX, 0f, mAttr.mWidth, mAttr.mHeight)
        drawLineScale(canvas, originColorPaint, clipRect, touchX)

//        drawCursor(canvas, touchX, touchY)
    }


    override fun initTouchXY(touchXCon: Consumer<Float>, touchYCon: Consumer<Float>) {
        touchXCon.accept(mAttr.mPaddingLeft.toFloat())
    }


    /**
     * 画线性的刻度
     */
    private fun drawLineScale(canvas: Canvas?, paint: Paint, clipRectF: RectF, touchX: Float) {


        canvas!!.save()
        canvas.clipRect(clipRectF)


        nodeStartX = mAttr.mPaddingLeft + halfCalibration
        nodeStopX = nodeStartX

        nodeStartY = mAttr.mPaddingTop + (interval - nodeLength) / 2
        nodeStopY = nodeStartY + nodeLength

        startY = mAttr.mPaddingTop + (interval - linelength) / 2
        stopY = startY + linelength

        keyStartY = mAttr.mPaddingTop + (interval - keylength) / 2
        keyStopY = keyStartY + keylength

        Log.i("llc_scale", "keyStartY =${keyStartY}")
        Log.i("llc_scale", "keyStopY =${keyStopY}")
        Log.i("llc_scale", "keylength =${keylength}")

        for (index in 0..mAttr.mTotalProgress) {

            if (nodeStartX < touchX) {
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
//            Log.i("llc_scale","draw")
//            Log.i("llc_scale","mAttr.mScaleLineWidth=${mAttr.mScaleLineWidth}")
//            Log.i("llc_scale","nodeStartX + mAttr.mScaleLineWidth =${nodeStartX + mAttr.mScaleLineWidth }")
//            Log.i("llc_scale","touchX =${touchX}")
//            Log.i("llc_scale","nodeStartX =${nodeStartX}")
            if (nodeStartX + mAttr.mScaleLineWidth + perInterval > touchX && nodeStartX < touchX) {
                mAttr.mCurrentPosition = index
                //未滑动部分最后刻度
                Log.i("llc_scale", "未滑动部分最后刻度")
                canvas?.drawLine(
                    nodeStartX,
                    keyStartY,
                    nodeStopX,
                    keyStopY,
                    paint
                )
                mAttr.mCurrentX = nodeStartX + mAttr.mScaleLineWidth
            }
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
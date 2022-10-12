package com.mrr.libscaleview.view

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import com.mrr.libscaleview.attr.ScaleViewAttr
import com.mrr.libscaleview.rectf.ScaleTextRectF
import java.util.function.Consumer

class CircleScaleView : BaseScaleView {

    private val TAG = "CircleScaleView"

    private var letter = ""
    private var lengthDiff = 0f
    var angle = 0.0

    var scaleTextRectF: ScaleTextRectF? = null

    constructor(attr: ScaleViewAttr) : super(attr) {
        scaleTextRectF = ScaleTextRectF()

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        preDegrees = (Math.PI * 2 / mAttr.mTotalProgress).toFloat()
        circleRadius =
            Math.min(
                mAttr.mWidth - mAttr.mPaddingLeft - mAttr.mPaddingRight,
                mAttr.mHeight - mAttr.mPaddingTop - mAttr.mPaddingBottom
            ) / 2
        centerX = mAttr.mWidth / 2
        centerY = mAttr.mHeight / 2
    }

    override fun onDraw(canvas: Canvas?, touchX: Float, touchY: Float) {

        drawCircleScale(canvas, originColorPaint, Math.PI * 2)
        drawCursor(canvas, touchX, touchY)
        drawCircleScale(
            canvas,
            changeColorPaint,
            circleProgressAngel
        )
        progressChange((mAttr.mTotalProgress * circleProgressAngel / (Math.PI * 2)).toFloat())

    }


    override fun initTouchXY(touchXCon: Consumer<Float>, touchYCon: Consumer<Float>) {
        touchXCon.accept(centerX + circleRadius)
        touchYCon.accept(centerY)
    }


    private fun drawCircleScale(canvas: Canvas?, paint: Paint, maxDegress: Double) {

        if (maxDegress == 0.0) {
            return
        }

//        canvas?.drawCircle(mAttr.mWidth / 2, mAttr.mHeight / 2, circleRadius, cursorPaint)

        //节点刻度和普通刻度的长度差
        lengthDiff =
            ((circleRadius * mAttr.mScaleNodeWidth) - (circleRadius * mAttr.mScaleWidth)) / 2


        for (index in 0..mAttr.mTotalProgress - 1) {
            // 初始角度 + 当前旋转的角度
            angle = (index * preDegrees).toDouble()
            letter = index.toString()

            if (angle > maxDegress) {
                return
            }

            //从外往内绘制
            if (index % mAttr.mUnitScale == 0) {

                startX = (centerX + circleRadius * Math.cos(angle)).toFloat()
                startY = (centerY + circleRadius * Math.sin(angle)).toFloat()
                stopX = (centerX + (circleRadius - circleRadius * mAttr.mScaleNodeWidth)
                        * Math.cos(angle)).toFloat()
                stopY = (centerY + (circleRadius - circleRadius * mAttr.mScaleNodeWidth)
                        * Math.sin(angle)).toFloat()

                scaleTextRectF?.initCircleTextSeat(letter, angle, this)
                if (scaleTextRectF!!.init) {
                    canvas?.drawText(
                        letter,
                        scaleTextRectF!!.x,
                        scaleTextRectF!!.baseLine,
                        textPaint
                    )
                }

            } else {
                startX = (centerX + (circleRadius - lengthDiff)
                        * Math.cos(angle)).toFloat()
                startY = (centerY + (circleRadius - lengthDiff) * Math.sin(angle)).toFloat()
                stopX =
                    (centerX + (circleRadius - circleRadius * mAttr.mScaleWidth - lengthDiff)
                            * Math.cos(angle)).toFloat()
                stopY =
                    (centerY + (circleRadius - circleRadius * mAttr.mScaleWidth - lengthDiff)
                            * Math.sin(angle)).toFloat()
            }



            canvas!!.drawLine(
                startX, startY, stopX, stopY,
                paint
            )

        }
    }

    /**
     * 绘制游标
     *
     */
    private fun drawCursor(canvas: Canvas?, touchX: Float, touchY: Float) {

        if (null == mAttr.mCursorBitmap) {
            return
        }

        cursorRectF.calculateAttributes(
            touchX,
            touchY,
            centerX,
            centerY,
            circleRadius,
            mAttr
        )

        cursorMatrix.reset()
        // 创建操作图片用的 Matrix 对象
        cursorMatrix.postScale(
            cursorRectF.mScaleX, cursorRectF.mScaleY
        );
        cursorMatrix.postTranslate(
            cursorRectF.mTransX,
            cursorRectF.mTransY
        );

        cursorMatrix.postRotate(
            cursorRectF.mRoutateDegress,
            cursorRectF.mRoutateCenterX,
            cursorRectF.mRoutateCenterY
        )

        canvas?.drawBitmap(mAttr.mCursorBitmap!!, cursorMatrix, null)

    }

    override fun angelChange(curAngel: Double) {
        circleProgressAngel = curAngel
    }


}
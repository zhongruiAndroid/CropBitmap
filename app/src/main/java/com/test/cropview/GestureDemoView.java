package com.test.cropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 混合手势测试
 * Demo 中充分利用了 Matrix 的一些特性，需要对 Matrix 有较为充分的理解
 */
public class GestureDemoView extends View {

    GestureDetector mGestureDetector;
    ScaleGestureDetector mScaleGestureDetector;

    // 画布当前的 Matrix， 用于获取当前画布的一些状态信息，例如缩放大小，平移距离等
    private Matrix mCanvasMatrix = new Matrix();

    // 将用户触摸的坐标转换为画布上坐标所需的 Matrix， 以便找到正确的缩放中心位置
    private Matrix mInvertMatrix = new Matrix();

    // 所有用户触发的缩放、平移等操作都通过下面的 Matrix 直接作用于画布上，
    // 将系统计算的一些初始缩放平移信息与用户操作的信息进行隔离，让操作更加直观
    private Matrix mUserMatrix = new Matrix();

    private Bitmap mBitmap;

    // 基础的缩放和平移信息，该信息与用户的手势操作无关
    private float mBaseScale;
    private float mBaseTranslateX;
    private float mBaseTranslateY;

    private Paint mPaint;

    public GestureDemoView(Context context) {
        super(context);
    }

    public GestureDemoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        initGesture(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mBitmap.getWidth() * 1.0f / mBitmap.getHeight() > w * 1.0f / h) {
            mBaseScale = w * 1.0f / mBitmap.getWidth();
            mBaseTranslateX = 0;
            mBaseTranslateY = (h - mBitmap.getHeight() * mBaseScale) / 2;
        } else {
            mBaseScale = h * 1.0f / mBitmap.getHeight() * 1.0f;
            mBaseTranslateX = (w - mBitmap.getWidth() * mBaseScale) / 2;
            mBaseTranslateY = 0;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        canvas.translate(mBaseTranslateX, mBaseTranslateY);
        canvas.scale(mBaseScale, mBaseScale);

        canvas.save();
        canvas.concat(mUserMatrix);

        mCanvasMatrix = canvas.getMatrix();
        mCanvasMatrix.invert(mInvertMatrix);

        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.restore();
    }


    //--- 手势处理 ----------------------------------------------------------------------------------

    private void initGesture(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float scale = getMatrixValue(MSCALE_X, mCanvasMatrix);
                mUserMatrix.preTranslate(-distanceX / scale, -distanceY / scale);
                //fixTranslate();   // 在用户滚动时不进行修正，保证用户滚动时也有响应， 在用户抬起手指后进行修正
                invalidate();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!mUserMatrix.isIdentity()) {
                    mUserMatrix.reset();
                } else {
                    float[] points = mapPoint(e.getX(), e.getY(), mInvertMatrix);
                    mUserMatrix.postScale(MAX_SCALE, MAX_SCALE, points[0], points[1]);
                }
                fixTranslate();
                invalidate();
                return true;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float fx = detector.getFocusX();
                float fy = detector.getFocusY();
                float[] points = mapPoint(fx, fy, mInvertMatrix);
                scaleFactor = getRealScaleFactor(scaleFactor);
                mUserMatrix.preScale(scaleFactor, scaleFactor, points[0], points[1]);
//                mUserMatrix.preScale(scaleFactor, scaleFactor, fx,fy);

                Log.i("====",fx+"===="+fy+"==="+points[0]+"===="+points[1]);
                fixTranslate();
                invalidate();
                return true;
            }

        });
    }

    // 修正缩放
    private void fixTranslate() {
        // 对 Matrix 进行预计算，并根据计算结果进行修正
        Matrix viewMatrix = getMatrix();    // 获取当前控件的Matrix
        viewMatrix.preTranslate(mBaseTranslateX, mBaseTranslateY);
        viewMatrix.preScale(mBaseScale, mBaseScale);
        viewMatrix.preConcat(mUserMatrix);
        Matrix invert = new Matrix();
        viewMatrix.invert(invert);
        Rect rect = new Rect();
        getGlobalVisibleRect(rect);

        float userScale = getMatrixValue(MSCALE_X, mUserMatrix);
        float scale = getMatrixValue(MSCALE_X, viewMatrix);

        float[] center = mapPoint(mBitmap.getWidth() / 2.0f, mBitmap.getHeight() / 2.0f, viewMatrix);
        float distanceX = center[0] - getWidth() / 2.0f;
        float distanceY = center[1] - getHeight() / 2.0f;
        float[] wh = mapVectors(mBitmap.getWidth(), mBitmap.getHeight(), viewMatrix);

        if (userScale <= 1.0f) {
            mUserMatrix.preTranslate(-distanceX / scale, -distanceY / scale);
        } else {
            float[] lefttop = mapPoint(0, 0, viewMatrix);
            float[] rightbottom = mapPoint(mBitmap.getWidth(), mBitmap.getHeight(), viewMatrix);

            // 如果宽度小于总宽度，则水平居中
            if (wh[0] < getWidth()) {
                mUserMatrix.preTranslate(distanceX / scale, 0);
            } else {
                if (lefttop[0] > 0) {
                    mUserMatrix.preTranslate(-lefttop[0] / scale, 0);
                } else if (rightbottom[0] < getWidth()) {
                    mUserMatrix.preTranslate((getWidth() - rightbottom[0]) / scale, 0);
                }

            }
            // 如果高度小于总高度，则垂直居中
            if (wh[1] < getHeight()) {
                mUserMatrix.preTranslate(0, -distanceY / scale);
            } else {
                if (lefttop[1] > 0) {
                    mUserMatrix.preTranslate(0, -lefttop[1] / scale);
                } else if (rightbottom[1] < getHeight()) {
                    mUserMatrix.preTranslate(0, (getHeight() - rightbottom[1]) / scale);
                }
            }
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            fixTranslate();
        }
        return true;
    }


    //--- Tools ------------------------------------------------------------------------------------

    //--- 将坐标转换为画布坐标 ---
    private float[] mapPoint(float x, float y, Matrix matrix) {
        float[] temp = new float[2];
        temp[0] = x;
        temp[1] = y;
        matrix.mapPoints(temp);
        return temp;
    }

    private float[] mapVectors(float x, float y, Matrix matrix) {
        float[] temp = new float[2];
        temp[0] = x;
        temp[1] = y;
        matrix.mapVectors(temp);
        return temp;
    }


    //--- 获取 Matrix 中的属性 ---
    private float[] matrixValues = new float[9];
    private static final int MSCALE_X = 0, MSKEW_X = 1, MTRANS_X = 2;
    private static final int MSKEW_Y = 3, MSCALE_Y = 4, MTRANS_Y = 5;
    private static final int MPERSP_0 = 6, MPERSP_1 = 7, MPERSP_2 = 8;

    @IntDef({MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y, MPERSP_0, MPERSP_1, MPERSP_2})
    @Retention(RetentionPolicy.SOURCE)
    private @interface MatrixName {}

    private float getMatrixValue(@MatrixName int name, Matrix matrix) {
        matrix.getValues(matrixValues);
        return matrixValues[name];
    }

    //--- 限制缩放比例 ---
    private static final float MAX_SCALE = 4.0f;    //最大缩放比例
    private static final float MIN_SCALE = 0.5f;    // 最小缩放比例

    private float getRealScaleFactor(float currentScaleFactor) {
        float realScale = 1.0f;
        float userScale = getMatrixValue(MSCALE_X, mUserMatrix);    // 用户当前的缩放比例
        float theoryScale = userScale * currentScaleFactor;           // 理论缩放数值

        // 如果用户在执行放大操作并且理论缩放数据大于4.0
        if (currentScaleFactor > 1.0f && theoryScale > MAX_SCALE) {
            realScale = MAX_SCALE / userScale;
        } else if (currentScaleFactor < 1.0f && theoryScale < MIN_SCALE) {
            realScale = MIN_SCALE / userScale;
        } else {
            realScale = currentScaleFactor;
        }
        return realScale;
    }
}

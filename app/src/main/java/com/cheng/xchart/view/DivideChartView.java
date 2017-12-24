package com.cheng.xchart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2017/12/24.
 */

public class DivideChartView extends View implements GestureDetector.OnGestureListener {
    private float[] values = new float[]{268.54f, 100f, 180f, 233.98f, 257f, 98f, 169f, 150f, 50f, 100f, 166f, 79f, 268.54f, 100f, 180f, 233f, 257f, 98f, 169f, 150f, 50f, 100f, 166f, 79f};
    private String[] names = new String[]{"北京市", "天津市", "上海市", "山东省", "a", "b", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X"};
    private int width = 600;
    private int height = 300;
    private GestureDetectorCompat gestureDetectorCompat;
    private Paint paint;
    private Rect stringRect;

    /**
     * 刻度线宽度
     */
    private final int SCALE_LINE_WIDTH = 10;

    /**
     * 刻度值与刻度线之间的宽度
     */
    private final int VALUE_LINE_MARGIN = 10;

    /**
     * 绘制图形的左间距
     */
    private final int GRAPH_LEFT_MARGIN = 10;

    /**
     * Y轴刻度值行高
     */
    private final int LINE_HEIGHT = 50;

    /**
     * 最大值所在位置距离顶部距离
     */
    private final int TOP_MAX = 100;

    /**
     * 字体大小
     */
    private final float TEXT_SIZE = 20f;

    /**
     * 底部名称距离顶部距离
     */
    private final int NAME_TOP_MARGIN = 20;

    /**
     * 底部名称距离底部距离
     */
    private final int NAME_BOTTOM_MARGIN = 10;

    /**
     * 底部名称最大高度值
     */
    private int maxNameHeight;

    /**
     * 刻度值最大宽度
     */
    private int maxScaleValueWidth;

    /**
     * Y轴刻度值差
     */
    private int differenceY;

    /**
     * 最高柱状图高度
     */
    private float barMaxHeight;

    /**
     * 刻度值
     */
    private int[] scaleValues;

    /**
     * Y轴有效触摸滑动区域（也是图形边框下部距离）
     */
    private float bottom;

    /**
     * X轴有效触摸滑动区域（也是图形边框左边距离）
     */
    private float left;

    public DivideChartView(Context context) {
        this(context, null);
    }

    public DivideChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DivideChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        gestureDetectorCompat = new GestureDetectorCompat(context, this);
        paint = new Paint();
        paint.setAntiAlias(true);
        stringRect = new Rect();
        calculateMaxNameHeight();
    }

    /**
     * 计算底部所有名称中最大高度
     */
    private void calculateMaxNameHeight() {
        paint.setTextSize(TEXT_SIZE);
        paint.setColor(Color.BLACK);
        for (String name : names) {
            paint.getTextBounds(name, 0, name.length(), stringRect);
            int stringHeight = stringRect.height();
            if (maxNameHeight < stringHeight) {
                maxNameHeight = stringHeight;
            }
        }
    }

    /**
     * 计算Y轴刻度值差
     */
    private void calculateDifferenceY() {
        if (maxNameHeight != 0) {
            barMaxHeight = height - TOP_MAX - NAME_TOP_MARGIN - NAME_BOTTOM_MARGIN - maxNameHeight;
            differenceY = (int) (getMaxValue() / (barMaxHeight) * LINE_HEIGHT);
        }
    }

    /**
     * 计算数组中的最大值
     */
    private float getMaxValue() {
        float max = 0;
        for (float v : values) {
            if (max < v) {
                max = v;
            }
        }
        return max;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.AT_MOST) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //仅在第一次初始化时设置数据，之后的数据变换由触摸时完成
        if (differenceY == 0) {
            calculateDifferenceY();
            setScaleValues();
            calculateScaleValueMaxWidth();
            bottom = height - NAME_TOP_MARGIN - NAME_BOTTOM_MARGIN - maxNameHeight;
            left = maxScaleValueWidth + VALUE_LINE_MARGIN + SCALE_LINE_WIDTH;
        }
    }

    /**
     * 设置刻度值
     */
    private void setScaleValues() {
        //设置刻度值数量，并额外增加5个
        int scaleValueCount = (int) (barMaxHeight / 50 + 5);
        scaleValues = new int[scaleValueCount];
        for (int i = 0; i < scaleValueCount; i++) {
            scaleValues[i] = i * differenceY;
        }
    }

    /**
     * 计算所有刻度值中的最大宽度
     */
    private void calculateScaleValueMaxWidth() {
        paint.setTextSize(TEXT_SIZE);
        paint.setColor(Color.BLACK);
        for (int scaleValue : scaleValues) {
            String value = String.valueOf(scaleValue);
            paint.getTextBounds(value, 0, value.length(), stringRect);
            int stringWidth = stringRect.width();
            if (maxScaleValueWidth < stringWidth) {
                maxScaleValueWidth = stringWidth;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制刻度值和刻度线
        for (int i = 0; i < scaleValues.length; i++) {
            canvas.drawText(String.valueOf(scaleValues[i]), 0, bottom - LINE_HEIGHT * i, paint);
            //跳过值为0的刻度值
            if (i != 0) {
                canvas.drawLine(left - SCALE_LINE_WIDTH,
                        bottom - LINE_HEIGHT * i,
                        left,
                        bottom - LINE_HEIGHT * i, paint);
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(left, 0, width, bottom, paint);
        canvas.save();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getX() > left && event.getY() < bottom) {
            gestureDetectorCompat.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}

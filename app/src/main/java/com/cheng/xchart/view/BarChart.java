package com.cheng.xchart.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/22.
 */

public class BarChart extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private Paint paint;
    private float progress;
    private float[] values = new float[]{268.54f, 100, 180, 233.98f, 257, 98, 169, 150, 50, 100, 166, 79, 268.54f, 100, 180, 233, 257, 98, 169, 150, 50, 100, 166, 79};
    private String[] names = new String[]{"北京市", "天津市", "上海市", "山东省", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X"};
    private List<Integer> scaleValues;
    private Rect stringRect;
    private int width = 600;
    private int height = 300;
    private GestureDetectorCompat gestureDetectorCompat;

    /**
     * 标题
     */
    private String title = "地域分布图";
    /**
     * Y轴刻度值行高
     */
    private final int LINE_HEIGHT = 50;
    /**
     * 刻度值右边距或下边距
     */
    private final int SCALE_VALUE_MARGIN = 10;
    /**
     * 刻度线宽度
     */
    private final int SCALE_LINE_WIDTH = 10;
    /**
     * 字体大小
     */
    private final float TEXT_SIZE = 20f;
    /**
     * 柱状图宽度
     */
    private float barChartWidth = 50;
    /**
     * 柱状图间隔
     */
    private final int BAR_CHART_MARGIN = 20;

    /**
     * 柱状图名称顶部间隔
     */
    private final int NAME_TOP_MARGIN = 30;

    /**
     * 柱状图名称底部间隔
     */
    private final int NAME_BOTTOM_MARGIN = 10;

    /**
     * 横向滑动距离
     */
    private float scrollDistanceX;

    /**
     * 竖向滑动距离
     */
    private float scrollDistanceY;

    public BarChart(Context context) {
        this(context, null);
    }

    public BarChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", 0, 100);
        objectAnimator.setDuration(1000);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finishFirstAnimator = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        objectAnimator.start();
        stringRect = new Rect();
        scaleValues = new ArrayList<>(20);
        barHeights = new SparseArray<>();
        gestureDetectorCompat = new GestureDetectorCompat(context, this);
        gestureDetectorCompat.setOnDoubleTapListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.AT_MOST) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    private float borderWidth;
    private float maxScaleValueWidth;//最大刻度值宽度

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*
        绘制刻度值
         */
        int scaleCount = height % LINE_HEIGHT == 0 ? height / LINE_HEIGHT - 1 : height / LINE_HEIGHT;
        int startScaleValue = 1;//第一个不为0的刻度值
        paint.setTextSize(TEXT_SIZE);
        paint.setColor(Color.BLACK);
        if (scaleValues.isEmpty()) {
            for (int i = 0; i < scaleCount + 1; i++) {
                int value = (int) Math.ceil((getMaxValue(values) / (scaleCount - 1))) * i;
                String scaleValue = String.valueOf(value);
                scaleValues.add(value);
                paint.getTextBounds(scaleValue, 0, scaleValue.length(), stringRect);
                if (maxScaleValueWidth < stringRect.width()) {
                    maxScaleValueWidth = stringRect.width() + SCALE_VALUE_MARGIN;
                }
            }
        }
        for (int i = 0; i < scaleCount + 1; i++) {
            if (i == 1) {
                startScaleValue = scaleValues.get(i);
            }
            String scaleValue = String.valueOf(scaleValues.get(i));
            paint.getTextBounds(scaleValue, 0, scaleValue.length(), stringRect);
            float v = maxScaleValueWidth - stringRect.width() - SCALE_VALUE_MARGIN;
            canvas.drawText(scaleValue, v, height - LINE_HEIGHT * i - NAME_TOP_MARGIN - scrollDistanceY, paint);
        }
        canvas.save();
        //绘制Y轴刻度线
        int bottomOffset = stringRect.height() / 2;//底部偏移量
        float lastScaleYTop = 0;//最后一个刻度的top距离
        for (int i = 0; i < scaleCount; i++) {
            if (i < scaleCount) {
                canvas.drawLine(maxScaleValueWidth,
                        height - LINE_HEIGHT * (i + 1) - bottomOffset - NAME_TOP_MARGIN - scrollDistanceY,
                        SCALE_LINE_WIDTH + maxScaleValueWidth,
                        height - LINE_HEIGHT * (i + 1) - bottomOffset - NAME_TOP_MARGIN - scrollDistanceY, paint);
            } else {
                lastScaleYTop = height - LINE_HEIGHT * (i + 1) - bottomOffset;
            }
        }
        canvas.save();
        /*
        绘制边框
         */
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(maxScaleValueWidth + SCALE_LINE_WIDTH,
                0, width,
                height - bottomOffset - NAME_TOP_MARGIN, paint);
        borderWidth = width - (maxScaleValueWidth + SCALE_LINE_WIDTH);
        canvas.save();
        /*
        绘制名称
         */
        for (int i = 0; i < names.length; i++) {
            paint.getTextBounds(names[i], 0, names[i].length(), stringRect);
            paint.setAlpha(255);
            float left;
            if (stringRect.width() <= barChartWidth) {
                left = (barChartWidth + BAR_CHART_MARGIN) * i + maxScaleValueWidth + SCALE_LINE_WIDTH * 2 + scrollDistanceX + (barChartWidth - stringRect.width()) / 2;
            } else {
                left = (barChartWidth + BAR_CHART_MARGIN) * i + maxScaleValueWidth + SCALE_LINE_WIDTH * 2 + scrollDistanceX - (stringRect.width() - barChartWidth) / 2;
            }
            if (maxScaleValueWidth + SCALE_LINE_WIDTH - left >= stringRect.width() / 2) {
                paint.setAlpha(0);
            } else {
                paint.setAlpha(255);
            }
            canvas.drawText(names[i], left,
                    height - NAME_BOTTOM_MARGIN, paint);
        }
        canvas.save();
        /*
        绘制柱状图
         */
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < values.length; i++) {
            paint.setColor(getColor(i));
            float left = (barChartWidth + BAR_CHART_MARGIN) * i + maxScaleValueWidth + SCALE_LINE_WIDTH * 2 + scrollDistanceX;
            if (left <= maxScaleValueWidth + SCALE_LINE_WIDTH) {
                left = maxScaleValueWidth + SCALE_LINE_WIDTH;
            }
            float bottom = height - 1 - bottomOffset - NAME_TOP_MARGIN;
            float barHeight = bottom - (float) LINE_HEIGHT / startScaleValue * values[i];
            canvas.drawRect(left,
                    finishFirstAnimator ? barHeights.get(i) - ((barHeights.get(i) - barHeight) * progress / 100) : bottom - (bottom - barHeight) * progress / 100.0f,
                    (barChartWidth + BAR_CHART_MARGIN) * i + maxScaleValueWidth + SCALE_LINE_WIDTH * 2 + barChartWidth + scrollDistanceX,
                    bottom, paint
            );
            if (!finishFirstAnimator) {
                barHeights.put(i, barHeight);
            }
            Log.i("jfoiwjegwegw", barHeights.toString());
        }
    }

    /**
     * 第一次动画结束
     */
    private boolean finishFirstAnimator;

    /**
     * 每个柱状图距离顶部高度，只有在每次缩放的时候刷入新数据
     */
    private SparseArray<Float> barHeights;

    private float getMaxValue(float[] value) {
        float max = 0;
        for (float v : value) {
            if (max < v) {
                max = v;
            }
        }
        return max;
    }

    private int getColor(int index) {
        int[] colors = new int[]{Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.RED, Color.GREEN
                , Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};
        return colors[index % colors.length];
    }

    private float lastX, lastY;
    private boolean refresh;
    private VelocityTracker velocityTracker;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return true;
    }

    private ValueAnimator valueAnimator;

    /**
     * 惯性滑动
     */
    private void inertialSlide(float velocity) {
        valueAnimator = ValueAnimator.ofFloat(scrollDistanceX, scrollDistanceX + velocity * 0.1f);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollDistanceX = (float) animation.getAnimatedValue();
                if (scrollDistanceX >= 0) {
                    scrollDistanceX = 0;
                }
                float maxRightDistance = (values.length * barChartWidth + (values.length - 1) * BAR_CHART_MARGIN) + SCALE_LINE_WIDTH - borderWidth + SCALE_LINE_WIDTH;
                if (scrollDistanceX <= -maxRightDistance) {
                    scrollDistanceX = -maxRightDistance;
                }
                if (refresh) {
                    invalidate();
                } else {
                    valueAnimator.cancel();
                }
                refresh = !(scrollDistanceX == 0 || scrollDistanceX == -maxRightDistance);
            }
        });
        valueAnimator.start();
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        lastX = motionEvent.getX();
        lastY = motionEvent.getY();
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
        velocityTracker.addMovement(motionEvent1);
        float moveX = motionEvent1.getX();
        scrollDistanceX += moveX - lastX;
        if (scrollDistanceX >= 0) {
            scrollDistanceX = 0;
        }
        float maxRightDistance = (values.length * barChartWidth + (values.length - 1) * BAR_CHART_MARGIN) + SCALE_LINE_WIDTH * 2 - borderWidth;
        if (scrollDistanceX <= -maxRightDistance) {
            scrollDistanceX = -maxRightDistance;
        }
        if (refresh) {
            invalidate();
        }
        float moveY = motionEvent1.getX();
        scrollDistanceY += moveY - lastY;
        Log.i("jfowjegoweg", scrollDistanceY + "");
        lastY = moveY;
        refresh = !(scrollDistanceX == 0 || scrollDistanceX == -maxRightDistance);
        lastX = moveX;
        velocityTracker.computeCurrentVelocity(1000);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        float xVelocity = velocityTracker.getXVelocity();
        velocityTracker.clear();
        inertialSlide(xVelocity);
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        sortScaleValue();
        scaleBarChart(motionEvent.getX() - maxScaleValueWidth - SCALE_LINE_WIDTH + Math.abs(scrollDistanceX));
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    /**
     * 重新排序刻度值
     */
    private void sortScaleValue() {
        if (scaleValues != null && !scaleValues.isEmpty()) {
            int firstValue = scaleValues.get(1) * 9 / 10;
            int size = scaleValues.size();
            scaleValues.clear();
            for (int i = 0; i < size; i++) {
                scaleValues.add(firstValue * i);
            }
        }
    }

    /**
     * 缩放柱状图动画
     *
     * @param x
     */
    private void scaleBarChart(final float x) {
        final float startAllBarWidthSpace = (values.length * barChartWidth + (values.length - 1) * BAR_CHART_MARGIN) + SCALE_LINE_WIDTH * 2;
        final float ratio = x / startAllBarWidthSpace;
        final float startScrollDistance = scrollDistanceX;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(barChartWidth, barChartWidth + 20);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                barChartWidth = (float) animation.getAnimatedValue();
                float allBarWidthSpace = (values.length * barChartWidth + (values.length - 1) * BAR_CHART_MARGIN) + SCALE_LINE_WIDTH * 2;
                scrollDistanceX = startScrollDistance - (ratio * allBarWidthSpace - x);
            }
        });
        valueAnimator.start();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", 0, 100);
        objectAnimator.start();
    }
}

package com.example.android.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.customview.R;

import androidx.annotation.Nullable;

/**
 * 之前的一个机试题.要求实现的效果,数字动画,数字边框,触摸变色.
 */
public class CustomView extends View {
    private int mBorderWidth;
    private int mBorderHeight;
    private int mBorderColor;
    private int mBorderSize;
    private int mSpace;
    private Paint mTextPaint;
    private Paint mBorderPaint;
    private int mTextSize;
    private int mTopOffset;
    StringBuilder str = new StringBuilder();
    SparseArray<RectF> lists = new SparseArray<>();
    private int mNum;
    private ValueAnimator valueAnimator;

    private int select = -1;

    public CustomView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomView, defStyleAttr, R.style.custom_def_style);
        mBorderWidth = (int) typedArray.getDimension(R.styleable.CustomView_border_width, mBorderWidth);
        mBorderHeight = (int) typedArray.getDimension(R.styleable.CustomView_border_height, mBorderHeight);
        mBorderColor = typedArray.getColor(R.styleable.CustomView_border_color, mBorderColor);
        mBorderSize = (int) typedArray.getDimension(R.styleable.CustomView_border_size, mBorderSize);
        mSpace = (int) typedArray.getDimension(R.styleable.CustomView_space, mSpace);
        mTextSize = (int) typedArray.getDimension(R.styleable.CustomView_text_size, mTextSize);
        typedArray.recycle();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(mTextSize);
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderSize);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (str != null && str.length() > 0) {
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            for (int x = 0; x < str.length(); x++) {
                if (x == select) {
                    mBorderPaint.setColor(Color.RED);
                    mTextPaint.setColor(Color.RED);
                } else {
                    mBorderPaint.setColor(mBorderColor);
                    mTextPaint.setColor(Color.BLACK);
                }
                canvas.drawRoundRect(lists.get(x), 5, 5, mBorderPaint);
                int itemWidth = (int) mTextPaint.measureText(str.substring(x, x + 1));
                //算基线
                int bestLine = (int) (mTopOffset + (lists.get(x).bottom - lists.get(x).top) / 2 - (fontMetrics.descent + fontMetrics.ascent) / 2);
                int start = (int) (lists.get(x).left + (lists.get(x).right - lists.get(x).left - itemWidth) / 2);
                canvas.drawText(str.substring(x, x + 1), start, bestLine, mTextPaint);
            }
        }
    }


    private void resetRect() {
        str.replace(0, str.length(), String.valueOf(mNum));
            if (lists.size() - str.length() > 0) {
                lists.removeAtRange(0, lists.size() - str.length());
            }
        for (int x = 0; x < str.length(); x++) {
            RectF rectF;
            if(lists.size()<=x || lists.get(x)==null){
                 rectF=new RectF();
            }else {
                rectF=lists.get(x);
            }
            rectF.left = getPaddingLeft() + (mBorderWidth + mSpace) * x;
            rectF.right = rectF.left + mBorderWidth;
            rectF.top = getPaddingTop();
            rectF.bottom = getPaddingTop() + mBorderHeight;
            lists.put(x, rectF);
        }

    }


    public void setNum(int num) {
        this.mNum = num;
        resetRect();
        requestLayout();
        startAnimation(num);
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        return super.dispatchTouchEvent(event);
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (null != valueAnimator) {
            valueAnimator.pause();
        }
        mTopOffset = getPaddingTop();
        int height = getPaddingBottom() + getPaddingTop() + mBorderHeight;
        int width = str.length() * mBorderWidth + mSpace * (str.length() - 1) + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (null != valueAnimator) {
            valueAnimator.resume();
        }
    }

    private void startAnimation(int num) {
        valueAnimator = ValueAnimator.ofInt(0, num).setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                if (String.valueOf(animatedValue).length() != str.length()) {
                    str.replace(0, str.length(), String.valueOf(animatedValue));
                    requestLayout();
                } else {
                    str.replace(0, str.length(), String.valueOf(animatedValue));
                    invalidate();
                }

            }
        });
        valueAnimator.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //判断触摸的位置是否是在数字框内
                int downX = (int) event.getX();
                int downY = (int) event.getY();
                query(downX, downY);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getX();
                int moveY = (int) event.getY();
                query(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (select != -1) {
                    select = -1;
                    invalidate();
                }
                break;
        }
        return true;
    }


    public void query(int touchX, int touchY) {
        //先判断Y的大小
        if (lists.get(0).top < touchY && lists.get(0).bottom > touchY) {
            for (int x = 0; x < lists.size(); x++) {
                if (lists.get(x).right >= touchX && lists.get(x).left <= touchX) {
                    select = x;
                    invalidate();
                    break;
                }
            }
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }
}


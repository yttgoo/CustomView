package com.example.android.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.OverScroller;

import com.example.android.customview.R;

import java.util.ArrayList;
import java.util.List;

public class MyLineFeedView extends ViewGroup {
    private int mHorizontalSpace = 15;
    private int mVerticalSpace = 15;
    private List<MyFlexLine> flexLines = new ArrayList<>();
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private OverScroller mOverScroll;

    public MyLineFeedView(Context context) {
        super(context);
        initView(context, null, -1);
    }

    public MyLineFeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, -1);
    }

    public MyLineFeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);

    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mOverScroll = new OverScroller(getContext());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyLineFeedView, defStyleAttr, R.style.line_feed_def_style);
        mHorizontalSpace = (int) typedArray.getDimension(R.styleable.MyLineFeedView_horizontalSpace, mHorizontalSpace);
        mVerticalSpace = (int) typedArray.getDimension(R.styleable.MyLineFeedView_verticalSpace, mVerticalSpace);
        typedArray.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        flexLines.clear();
        if (getChildCount() > 0) {
            measureLines(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(resolveSize(getMaxWidth(), widthMeasureSpec), resolveSize(getCountHeight(), heightMeasureSpec));

        }

    }


    private void measureLines(int widthMeasureSpec, int heightMeasureSpec) {
        int largestSizeInCross = Integer.MIN_VALUE;
        MyFlexLine myFlexLine = new MyFlexLine();
        myFlexLine.mFirstIndex = 0;
        //一行已使用的width
        myFlexLine.mMainSize = getPaddingLeft() + getPaddingRight();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        for (int x = 0; x < getChildCount(); x++) {
            View child = getChildAt(x);
            if (child.getVisibility() == GONE) {
                continue;
            }
            MyFlexItem flexItem = (MyFlexItem) child.getLayoutParams();
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingStart() + getPaddingEnd() + flexItem.getMarginLeft() + flexItem.getMarginRight(), flexItem.getWidth());
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom() + flexItem.getMarginTop() + flexItem.getMarginBottom(), flexItem.getHeight());
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            int tempWidth = child.getMeasuredWidth() + myFlexLine.mMainSize + flexItem.getMarginRight() + flexItem.getMarginLeft();
            if(myFlexLine.mItemCount>0){
               tempWidth+=mVerticalSpace;
            }
            if (width <tempWidth) {
                //换行 重置
                if(myFlexLine.mItemCount>0){
                    flexLines.add(myFlexLine);
                }
                myFlexLine = new MyFlexLine();
                myFlexLine.mItemCount = 1;
                myFlexLine.mMainSize = getPaddingLeft() + getPaddingRight() + flexItem.getMarginLeft() + flexItem.getMarginRight();
                myFlexLine.mFirstIndex = x;
                largestSizeInCross = Integer.MIN_VALUE;
            } else {
                myFlexLine.mItemCount++;
            }
            myFlexLine.mMainSize += child.getMeasuredWidth() + flexItem.getMarginLeft() + flexItem.getMarginRight();
            if (myFlexLine.mItemCount > 1 && (myFlexLine.mMainSize+mVerticalSpace)<=width) {
                myFlexLine.mMainSize += mVerticalSpace;
            }
            largestSizeInCross = Math.max(largestSizeInCross, child.getMeasuredHeight() + flexItem.getMarginTop() + flexItem.getMarginBottom() + mHorizontalSpace);
            myFlexLine.mLineHeight = Math.max(myFlexLine.mLineHeight, largestSizeInCross);
            if (getChildCount() - 1 == x) {
                myFlexLine.mLineHeight -= mHorizontalSpace;
                flexLines.add(myFlexLine);
            }
        }


    }


    private int getMaxWidth() {
        int width = Integer.MIN_VALUE;
        for (MyFlexLine line : flexLines) {
            width = Math.max(line.mMainSize, width);
        }
        return width;
    }


    private int getCountHeight() {
        int heigth = getPaddingBottom() + getPaddingTop();
        for (int x = 0; x < flexLines.size(); x++) {
            heigth += flexLines.get(x).mLineHeight;
        }
        return heigth;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int childLeft;
        int childTop = getPaddingTop();
        for (int x = 0; x < flexLines.size(); x++) {
            childLeft = paddingLeft;
            MyFlexLine myFlexLine = flexLines.get(x);
            for (int i = 0; i < myFlexLine.mItemCount; i++) {
                final int index = i + myFlexLine.mFirstIndex;
                View child = getChildAt(index);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                MyFlexItem flexItem = (MyFlexItem) child.getLayoutParams();
                if (i != 0) {
                    childLeft += (flexItem.getMarginLeft() + mVerticalSpace);
                } else {
                    childLeft += flexItem.getMarginLeft();
                }
                child.layout(childLeft, childTop + flexItem.getMarginTop(), childLeft + child.getMeasuredWidth(), childTop + flexItem.getMarginTop() + child.getMeasuredHeight());
                childLeft += child.getMeasuredWidth() + flexItem.getMarginRight();
            }
            childTop += myFlexLine.mLineHeight;

        }


    }



    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean mIsBeingDragged = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) ev.getY();
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                mIsBeingDragged = !mOverScroll.isFinished();
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) ev.getY();
                int d = mLastY - y;
                if (Math.abs(d) > mTouchSlop) {
                    mLastY = y;
                    mIsBeingDragged = true;
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                recycleVelocityTracker();
                break;
        }
        return mIsBeingDragged;

    }

    int mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getChildCount() == 0) {
                    return false;
                }
                if (mIsBeingDragged = !mOverScroll.isFinished()) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (!mOverScroll.isFinished()) {
                    mOverScroll.abortAnimation();
                }
                //记录x y
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (canScroll()) {
                    int y = (int) event.getY();
                    int diff = y - mLastY;
                    if (Math.abs(diff) > mTouchSlop) {
                        mIsBeingDragged = true;
                        if (getParent() != null) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (mIsBeingDragged) {
                        mLastY = (int) event.getY();
                        int scrollY = getScrollY();
                        int verticalScrollRange = computeVerticalScrollRange();
                        if (scrollY - diff <= 0) {
                            diff = scrollY;
                        } else if (scrollY - diff >= verticalScrollRange) {
                            diff = scrollY - verticalScrollRange;
                        }
                        scrollBy(0, -diff);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (getChildCount() > 0 && canScroll()) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int yVelocity = (int) velocityTracker.getYVelocity();
                    if (Math.abs(yVelocity) >= mMinimumVelocity) {
                        int computeVerticalScrollRange = computeVerticalScrollRange();
                        int scrollX = getScrollX();
                        int scrollY = getScrollY();
                        mOverScroll.fling(scrollX, scrollY, 0, -yVelocity, 0, 0, 0, computeVerticalScrollRange);
                        postInvalidate();
                    }
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                endDrag();
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        return true;
    }

    private void endDrag() {
        mIsBeingDragged = false;
        recycleVelocityTracker();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (!mOverScroll.isFinished() && mOverScroll.computeScrollOffset()) {
            scrollTo(mOverScroll.getCurrX(), mOverScroll.getCurrY());
            postInvalidate();
        }


    }


    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }


    public static class LayoutParams extends ViewGroup.MarginLayoutParams implements MyFlexItem {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getMarginLeft() {
            return leftMargin;
        }

        @Override
        public int getMarginRight() {
            return rightMargin;
        }

        @Override
        public int getMarginBottom() {
            return bottomMargin;
        }

        @Override
        public int getMarginTop() {
            return topMargin;
        }

    }


    @Override
    protected int computeVerticalScrollRange() {
        final int count = getChildCount();
        final int parentSpace = getHeight() - getPaddingBottom() - getPaddingTop();
        if (count == 0) {
            return parentSpace;
        }
        View child = getChildAt(count - 1);
        MyLineFeedView.LayoutParams lp = (MyLineFeedView.LayoutParams) child.getLayoutParams();
        int scrollRange = child.getBottom() + lp.bottomMargin;
        return scrollRange - parentSpace;

    }


    private boolean canScroll() {
        View child = getChildAt(getChildCount() - 1);
        MyLineFeedView.LayoutParams lp = (MyLineFeedView.LayoutParams) child.getLayoutParams();
        int scrollRange = child.getBottom() + lp.bottomMargin;
        if (getHeight() >= scrollRange) {
            return false;
        }
        return true;
    }


}

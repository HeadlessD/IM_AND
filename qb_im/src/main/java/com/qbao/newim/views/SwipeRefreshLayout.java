package com.qbao.newim.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.view.animation.Animation.AnimationListener;

/**
 * Created by chenjian on 2017/8/8.
 */

public class SwipeRefreshLayout extends ViewGroup {
    private static final String LOG_TAG = SwipeRefreshLayout.class.getSimpleName();

    private static final long RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 300;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int INVALID_POINTER = -1;

    private View mTarget; //the content that gets pulled down
    private int mOriginalOffsetTop;
    private OnRefreshListener mListener;
    private OnPullListener mPullListener;
    private int mFrom;
    private boolean mRefreshing = false;
    private int mTouchSlop;
    private float mDistanceToTriggerSync = -1;
    private int mMediumAnimationDuration;
    private int mCurrentTargetOffsetTop;

    private float mInitialMotionY;
    private float mLastMotionY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;

    private boolean mReturningToStart;
    private boolean mCanStartRefresh;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[] {
            android.R.attr.enabled
    };

    private View mHeadView;
    private boolean mIsPulling;
    private boolean mCanRefreshing;
    private int mHeadHeight;

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            if (mFrom != mOriginalOffsetTop) {
                targetTop = (mFrom + (int)((mOriginalOffsetTop - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            if(mRefreshing){
                mTarget.setTop(mHeadHeight);
                mHeadView.setTop(0);
                mCurrentTargetOffsetTop = mTarget.getTop();
            }else{
                setTargetOffsetTopAndBottom(offset);
            }
        }
    };

    private final AnimationListener mReturnToStartPositionListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrentTargetOffsetTop = 0;
        }
    };

    private final Runnable mReturnToStartPosition = new Runnable() {

        @Override
        public void run() {
            mReturningToStart = true;
            animateOffsetToStartPosition(mCurrentTargetOffsetTop + getPaddingTop(),
                    mReturnToStartPositionListener);
        }

    };

    private final Runnable mCancel = new Runnable() {

        @Override
        public void run() {
            mReturningToStart = true;
            animateOffsetToStartPosition(mCurrentTargetOffsetTop + getPaddingTop(),
                    mReturnToStartPositionListener);
        }

    };

    public SwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    public void setHeadView(View headView){
        if(headView != null){
            mHeadView = headView;
            if(getChildCount() == 2){
                removeViewAt(0);
            }
            addView(mHeadView,0);
            requestLayout();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(mCancel);
        removeCallbacks(mReturnToStartPosition);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mReturnToStartPosition);
        removeCallbacks(mCancel);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(mMediumAnimationDuration);
        mAnimateToStartPosition.setAnimationListener(listener);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToStartPosition);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public void setOnPullListener(OnPullListener listener){
        mPullListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
            } else {
                setTargetOffsetTopAndBottom(-mHeadHeight);
            }
        }
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    private void ensureTarget() {
        if (mTarget == null) {
            if (getChildCount() > 2 && !isInEditMode()) {
                throw new IllegalStateException(
                        "SwipeRefreshLayout can host max two direct child");
            }
            mTarget = getChildAt(1);
            mOriginalOffsetTop = mTarget.getTop() + getPaddingTop();
        }

        if(mHeadView == null){
            mHeadView = getChildAt(0);
        }
        if (mDistanceToTriggerSync == -1) {
            if (getParent() != null && ((View)getParent()).getHeight() > 0) {
                mDistanceToTriggerSync = mHeadHeight;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width =  getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }

        final View child = getChildAt(1);
        final int childLeft = getPaddingLeft();
        final int childTop = mCurrentTargetOffsetTop + getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        mHeadView.layout(childLeft,childTop- mHeadView.getMeasuredHeight(),childLeft + childWidth,childTop);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() > 2 && !isInEditMode()) {
            throw new IllegalStateException("SwipeRefreshLayout can host max two direct child");
        }

        if (getChildCount() > 1) {
            if(mHeadView == null){
                mHeadView = getChildAt(0);
            }
            measureChild(mHeadView,widthMeasureSpec,heightMeasureSpec);
            mHeadHeight = mHeadView.getMeasuredHeight();
            getChildAt(1).measure(
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
                            MeasureSpec.EXACTLY));
        }
    }

    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                mIsPulling = false;
                mCanRefreshing = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop) {
                    mLastMotionY = y;
                    mIsBeingDragged = true;
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled()  || mReturningToStart || canChildScrollUp()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                mIsPulling = false;
                mCanRefreshing = false;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = y - mInitialMotionY;

                if (!mIsBeingDragged && yDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                }

                if (mIsBeingDragged ) {
                    if (yDiff/2 > mDistanceToTriggerSync ) {
                        mCanStartRefresh = true;
                    }else{
                        mCanStartRefresh = false;
                    }
                    updateContentOffsetTop((int) (yDiff/2));
                    if (mLastMotionY > y && mTarget.getTop() == getPaddingTop()) {
                        removeCallbacks(mCancel);
                    }
                    mLastMotionY = y;
                }
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionY = MotionEventCompat.getY(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mCanStartRefresh){
                    startRefresh();
                }else{
                    removeCallbacks(mCancel);
                    post(mCancel);
                }

                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                return false;
        }

        return true;
    }

    private void startRefresh() {
        removeCallbacks(mCancel);

        if(!mRefreshing && mListener != null){
            mListener.onRefresh();
        }

        if(!mRefreshing && mPullListener != null){
            mPullListener.onRefreshing(mHeadView);
        }

        setRefreshing(true);
        mReturnToStartPosition.run();
    }

    private void updateContentOffsetTop(int targetTop) {
        final int currentTop = mTarget.getTop();
        if (targetTop < 0) {
            targetTop = 0;
        }

        if(currentTop > mDistanceToTriggerSync){

            if(!mCanRefreshing && !mRefreshing ){
                mIsPulling = false;
                if(mPullListener != null){
                    mPullListener.onCanRefreshing(mHeadView);
                }

            }
            mCanRefreshing = true;

        }else{

            if(!mIsPulling && !mRefreshing ){
                mCanRefreshing = false;
                if(mPullListener != null){
                    mPullListener.onPulling(mHeadView);
                }
            }
            mIsPulling = true;
        }
        setTargetOffsetTopAndBottom(targetTop - currentTop);

    }

    private void setTargetOffsetTopAndBottom(int offset) {
        mTarget.offsetTopAndBottom(offset);
        mHeadView.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mTarget.getTop();
    }

    private void updatePositionTimeout() {
        removeCallbacks(mCancel);
        postDelayed(mCancel, RETURN_TO_ORIGINAL_POSITION_TIMEOUT);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = MotionEventCompat.getY(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    private class BaseAnimationListener implements AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}

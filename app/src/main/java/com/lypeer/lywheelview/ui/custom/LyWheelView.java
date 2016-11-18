package com.lypeer.lywheelview.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.lypeer.lywheelview.R;
import com.lypeer.lywheelview.bean.Coordinate;
import com.lypeer.lywheelview.dao.OnResultListener;
import com.lypeer.lywheelview.utils.DpiTask;
import com.lypeer.lywheelview.utils.RollTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by lypeer on 2016/11/18.
 */

public class LyWheelView extends View {
    private static final String TAG = "LyWheelView";
    private static final int ACTION_REFRESH_VIEW = 1024;
    private static final int ACTION_ROLL_TO_CENTER = 1025;

    private Timer mTimer;
    private List<String> mStrings;
    private List<Coordinate> mCoordinates;
    private Paint mPaintCenter;
    private Paint mPaintUp;
    private Paint mPaintDown;
    private VelocityTracker mVelocityTracker;

    private float mCircleCellHeight;
    private float mCircleRSquare;
    private float mTextSizeMax;
    private float mTextSizeMin;

    private int mPickerSize = 5;

    private int mCurrentMotion;
    private int mSelected;
    private int mCurrentCenter;

    private int mPartHalfHeight;

    private float mMotionDownY;
    private float mOffset = 0f;
    private float mMoveOffset = 0f;
    //private float mLastOffset = 0f;

    private MyHandler mHandler;

    public LyWheelView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LyWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LyWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mTextSizeMin = 48f;
        mTextSizeMax = 72f;

        mHandler = new MyHandler(this);
        this.mStrings = new ArrayList<>();

        this.mPaintCenter = new TextPaint(1);
        this.mPaintCenter.setStyle(Paint.Style.FILL);
        this.mPaintCenter.setTextSize(mTextSizeMax);
        this.mPaintCenter.setTextAlign(Paint.Align.CENTER);
        this.mPaintCenter.setColor(context.getResources().getColor(R.color.colorPrimary));
        this.mPaintCenter.setTypeface(Typeface.SANS_SERIF);

        this.mPaintUp = new Paint(1);
        this.mPaintUp.setStyle(Paint.Style.FILL);
        this.mPaintUp.setTextSize(mTextSizeMin);
        this.mPaintUp.setTypeface(Typeface.SANS_SERIF);
        this.mPaintUp.setTextAlign(Paint.Align.CENTER);
        this.mPaintUp.setColor(context.getResources().getColor(R.color.colorGray));

        this.mPaintDown = new Paint(1);
        this.mPaintDown.setStyle(Paint.Style.FILL);
        this.mPaintDown.setTextSize(mTextSizeMin);
        this.mPaintDown.setTypeface(Typeface.SANS_SERIF);
        this.mPaintDown.setTextAlign(Paint.Align.CENTER);
        this.mPaintDown.setColor(context.getResources().getColor(R.color.colorGray));

        initList();
    }

    private void initList() {
        mStrings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mStrings.add("测试数据".concat(String.valueOf(i)));
        }

        mSelected = mStrings.size() / 2;
        mCurrentCenter = mSelected;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCoordinates == null)
            initCoordinateList(canvas);

        drawText(canvas, mOffset);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mTimer != null){
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                mCurrentMotion = MotionEvent.ACTION_MOVE;
                mOffset = event.getY() - mMotionDownY;
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                mCurrentMotion = MotionEvent.ACTION_DOWN;
                mMotionDownY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mCurrentMotion = MotionEvent.ACTION_UP;
                mOffset = event.getY() - mMotionDownY;
                mVelocityTracker.computeCurrentVelocity(100);
                invalidate();
                break;
        }

        return true;
    }

    private void initCoordinateList(Canvas canvas) {
        int partHeight = canvas.getHeight() / mPickerSize;
        mCircleCellHeight = (mTextSizeMax - mTextSizeMin) / partHeight;
        mCircleRSquare = (float) Math.pow(partHeight, 2);

        mPartHalfHeight = partHeight / 2;
        mCoordinates = new ArrayList<>();

        int baseX = canvas.getWidth() / 2;
        for (int i = 0; i < mPickerSize; i++) {
            Coordinate coordinate = new Coordinate();
            int baseY = (int) ((mPartHalfHeight * 2 * (i + 0.5)) - ((mPaintCenter.descent() + mPaintCenter.ascent()) / 2));
            coordinate.setX(baseX);
            coordinate.setY(baseY);
            mCoordinates.add(coordinate);
        }
    }

    private void drawText(Canvas canvas, float offset) {
        int halfNum = (int) (offset / mPartHalfHeight);
        int remainder = (int) offset % mPartHalfHeight;

        if (halfNum >= 0) {
            mCurrentCenter = mSelected - (halfNum + 1) / 2;
            mMoveOffset = halfNum % 2 == 0 ? remainder : remainder - mPartHalfHeight;
        } else {
            mCurrentCenter = mSelected - (halfNum - 1) / 2;
            mMoveOffset = halfNum % 2 == 0 ? remainder : remainder + mPartHalfHeight;
        }

        if (mCurrentMotion == MotionEvent.ACTION_UP) {
            mCurrentMotion = -1;

            interpolate(mVelocityTracker.getYVelocity(), offset);
        }

        mCurrentCenter = getCyclePosition(mCurrentCenter);
        mPaintCenter.setTextSize(getBigTextSize(Math.abs(mMoveOffset)));
        canvas.drawText(mStrings.get(mCurrentCenter), mCoordinates.get(mPickerSize / 2).getX(), mCoordinates.get(mPickerSize / 2).getY() + mMoveOffset, mPaintCenter);

        for (int i = 0; i < mPickerSize / 2; i++) {
            if (i == mPickerSize / 2 - 1) {

                if (mMoveOffset > 0) {
                    mPaintUp.setTextSize(getSmallTextSize(Math.abs(mMoveOffset)));
                    mPaintDown.setTextSize(mTextSizeMin);
                } else {
                    mPaintDown.setTextSize(getSmallTextSize(Math.abs(mMoveOffset)));
                    mPaintUp.setTextSize(mTextSizeMin);
                }
            } else {
                mPaintUp.setTextSize(mTextSizeMin);
                mPaintDown.setTextSize(mTextSizeMin);
            }

            canvas.drawText(mStrings.get(getCyclePosition(mCurrentCenter - mPickerSize / 2 + i)), mCoordinates.get(i).getX(), mCoordinates.get(i).getY() + mMoveOffset, mPaintUp);
            canvas.drawText(mStrings.get(getCyclePosition(mCurrentCenter + mPickerSize / 2 - i)), mCoordinates.get(mPickerSize - i - 1).getX(), mCoordinates.get(mPickerSize - i - 1).getY() + mMoveOffset, mPaintDown);

        }

    }

    private void interpolate(float speed, float offset) {
        if (Math.abs(speed) < 10) {
            sendMessage(ACTION_ROLL_TO_CENTER);
            return;
        }

        if (mTimer == null) {
            mTimer = new Timer();
        }

        mTimer.schedule(new DpiTask(speed, offset, new OnResultListener<Float>() {
            @Override
            public void onFinish() {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;

                sendMessage(ACTION_ROLL_TO_CENTER);
            }

            @Override
            public void onResult(Float resultData) {
                mOffset = resultData;
                sendMessage(ACTION_REFRESH_VIEW);
            }
        }), 0, 10);
    }

    private void scrollToCenter() {
        mSelected = mCurrentCenter;
        mOffset = mMoveOffset;

        if (mTimer == null) {
            mTimer = new Timer();
        }

        mTimer.schedule(new RollTask(mOffset, new OnResultListener<Float>() {
            @Override
            public void onFinish() {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
            }

            @Override
            public void onResult(Float resultData) {
                mOffset = resultData;
                sendMessage(ACTION_REFRESH_VIEW);
            }
        }), 0, 10);
    }

    private int getCyclePosition(int position) {
        int cyclePosition = position;

        if (cyclePosition > 0) {
            for (int i = 0; cyclePosition >= mStrings.size(); i++)
                cyclePosition -= i * mStrings.size();
        } else if(cyclePosition < 0){
            for (int i = 0; ; i++) {
                cyclePosition += i * mStrings.size();
                if (cyclePosition > 0 && cyclePosition < mStrings.size()) {
                    break;
                }
            }
        }

        return cyclePosition;
    }

    private float getBigTextSize(float moveOffset) {
        float baseSquare = (float) Math.pow(moveOffset, 2);
        return (float) Math.sqrt(mCircleRSquare - baseSquare) * mCircleCellHeight + mTextSizeMin;
    }

    private float getSmallTextSize(float moveOffset) {
        float partHeight = 2 * mPartHalfHeight;
        float baseSquare = (float) Math.pow(partHeight - moveOffset, 2);

        return (float) Math.sqrt(mCircleRSquare - baseSquare) * mCircleCellHeight + mTextSizeMin;
    }

    private void sendMessage(int what) {
        Message message = new Message();
        message.what = what;
        mHandler.sendMessage(message);
    }

    private static class MyHandler extends Handler {

        private WeakReference<LyWheelView> mLyWheelViewReference;

        MyHandler(LyWheelView LyWheelView) {
            mLyWheelViewReference = new WeakReference<>(LyWheelView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LyWheelView LyWheelView = mLyWheelViewReference.get();
            switch (msg.what) {
                case ACTION_REFRESH_VIEW:
                    LyWheelView.invalidate();
                    break;
                case ACTION_ROLL_TO_CENTER:
                    LyWheelView.scrollToCenter();
                    break;

            }
        }
    }
}

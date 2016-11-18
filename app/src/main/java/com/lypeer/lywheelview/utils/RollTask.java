package com.lypeer.lywheelview.utils;

import com.lypeer.lywheelview.dao.OnResultListener;

import java.util.TimerTask;

/**
 * Created by lypeer on 2016/11/18.
 */

public class RollTask extends TimerTask {

    private final float mTimeTotal = 200;
    private float mTimePassed = 0f;
    private float mOffset;
    private OnResultListener<Float> mOnResultListener;

    public RollTask(float offset, OnResultListener<Float> onResultListener) {
        this.mOffset = offset;
        this.mOnResultListener = onResultListener;
    }

    @Override
    public void run() {
        mTimePassed += 10;

        if (mTimePassed >= mTimeTotal) {
            mOnResultListener.onFinish();
            return;
        }
        mOnResultListener.onResult((float) (Math.pow(1 - mTimePassed / mTimeTotal, 3) * mOffset));
    }
}

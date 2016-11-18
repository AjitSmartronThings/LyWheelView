package com.lypeer.lywheelview.utils;


import com.lypeer.lywheelview.dao.OnResultListener;

import java.util.TimerTask;

/**
 * Created by lypeer on 2016/11/17.
 */

public class DpiTask extends TimerTask {

    private float mSpeed;
    private float mOffsetOrigin;
    private float mOffsetFinal;
    private float mTimePassed;
    private float mTimeTotal;
    private OnResultListener<Float> mListener;

    public DpiTask(float speed, float offsetOrigin, OnResultListener<Float> onResultListener) {
        this.mSpeed = speed;
        this.mOffsetOrigin = offsetOrigin;
        this.mListener = onResultListener;

        mTimeTotal = Math.abs(speed / 2);
        mOffsetFinal = (speed * mTimeTotal - mTimeTotal * mTimeTotal) / 5000;
        mTimePassed = 0;
    }

    @Override
    public void run() {
        mTimePassed += 10;

        if (mTimePassed >= mTimeTotal) {
            mListener.onFinish();
            return;
        }

        float integralOffset = (float) (mOffsetFinal * (1 - Math.pow(1 - mTimePassed / mTimeTotal, 3)));
        if (mOffsetOrigin * mSpeed > 0) {
            mListener.onResult(mOffsetOrigin + integralOffset);
        } else {
            mListener.onResult(mOffsetOrigin - integralOffset);
        }
    }
}

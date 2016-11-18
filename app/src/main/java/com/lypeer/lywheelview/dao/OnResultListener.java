package com.lypeer.lywheelview.dao;

/**
 * Created by lypeer on 2016/11/17.
 */

public interface OnResultListener<V> {

    void onFinish();
    void onResult(V resultData);
}

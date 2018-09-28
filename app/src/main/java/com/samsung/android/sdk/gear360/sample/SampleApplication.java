package com.samsung.android.sdk.gear360.sample;

import android.app.Application;

import com.samsung.android.sdk.gear360.SGear360;
import com.samsung.android.sdk.gear360.device.Device;


public class SampleApplication extends Application {

    private SGear360 mGear360SDK;

    private Device mDevice;
    private static SampleApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create Gear 360 SDK
        mGear360SDK = new SGear360();

        sApplication = this;
    }

    public SGear360 getGear360SDK() {
        return mGear360SDK;
    }

    public static SampleApplication getApplicationInstance() {
        return sApplication;
    }

    public Device getDevice() {
        return mDevice;
    }

    public void setDevice(Device device) {
        mDevice = device;
    }

}

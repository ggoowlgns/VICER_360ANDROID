package com.samsung.android.sdk.gear360.sample.service;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.samsung.android.sdk.gear360.device.Device;
import com.samsung.android.sdk.gear360.sample.SampleApplication;

public class BaseServiceActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    protected SampleApplication mApplication;
    protected Device mDevice;
    private ProgressDialog mProgress;
    private Device.DeviceOverheatedEventListener mDeviceOverheatedEventListener = new Device.DeviceOverheatedEventListener() {
        @Override
        public void onWarningMessageRaised() {
            Log.d(TAG, "onWarningMessageRaised()");
        }

        @Override
        public void onDeviceTurnedOff() {
            Log.d(TAG, "onDeviceTurnedOff()");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = SampleApplication.getApplicationInstance();

        mDevice = mApplication.getDevice();

        if (mDevice == null) {
            Log.e(TAG, "Device is null");
            finish();
            return;
        }

        mDevice.addOverheatedEventListener(mDeviceOverheatedEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevice.removeOverheatedEventListener(mDeviceOverheatedEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showProgressDialog(String message) {
        if (mProgress != null) {
            hideProgressDialog();
        }

        mProgress = new ProgressDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setMessage(message);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    protected void hideProgressDialog() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
    }
}

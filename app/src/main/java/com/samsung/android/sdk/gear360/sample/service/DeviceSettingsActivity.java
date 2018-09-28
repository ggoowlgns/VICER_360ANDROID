package com.samsung.android.sdk.gear360.sample.service;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.samsung.android.sdk.gear360.ResponseListener;
import com.samsung.android.sdk.gear360.device.Device;
import com.samsung.android.sdk.gear360.device.Setting;
import com.samsung.android.sdk.gear360.device.data.AutoPowerOffTime;
import com.samsung.android.sdk.gear360.device.data.BatteryInformation;
import com.samsung.android.sdk.gear360.device.data.BeepVolume;
import com.samsung.android.sdk.gear360.device.data.Reset;
import com.samsung.android.sdk.gear360.device.data.StorageInformation;
import com.samsung.android.sdk.gear360.sample.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceSettingsActivity extends BaseServiceActivity {

    private final String TAG = getClass().getSimpleName();
    private Setting mSetting;
    private Spinner mBeepSpinner;
    private Switch mLedSwitch;
    private Spinner mPowerOffSpinner;
    private TextView mTotalStorage;
    private TextView mUsedStorage;
    private TextView mFreeStorage;
    private TextView mBatteryLevel;
    private TextView mChargingStatus;
    private TextView mFirmwareInfo;
    private List<BeepVolume> mBeepVolumeList = new ArrayList<>();
    private List<AutoPowerOffTime> mAutoPowerOffTimeList = new ArrayList<>();
    private List<Reset> mResetList = new ArrayList<>();

    private CompoundButton.OnCheckedChangeListener mLedSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            showProgressDialog(getString(R.string.wait_response));

            mSetting.setLedEnabled(new ResponseListener<Void>() {
                @Override
                public void onSucceed(Void result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            Log.d(TAG, "LED updated");
                        }
                    });
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            Log.d(TAG, "LED update failed");
                        }
                    });
                }
            }, isChecked);
        }
    };

    private Setting.AutoPowerOffTimeEventListener mAutoPowerOffTimeEventListener = new Setting.AutoPowerOffTimeEventListener() {
        @Override
        public void onAutoPowerOffTimeChanged(final AutoPowerOffTime time) {
            if (time == null) {
                Log.e(TAG, "AutoPowerOffTime is null");
                return;
            }

            if (!mAutoPowerOffTimeList.contains(time)) {
                Log.e(TAG, "Invalid time");
                return;
            }

            Log.d(TAG, "AutoPowerOffTime result: " + time.getName());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPowerOffSpinner.setOnItemSelectedListener(null);
                    mPowerOffSpinner.setSelection(mAutoPowerOffTimeList.indexOf(time), false);
                    mPowerOffSpinner.setOnItemSelectedListener(mSpinnerAutoPowerOffListener);
                }
            });
        }
    };

    private Setting.BeepVolumeEventListener mBeepVolumeEventListener = new Setting.BeepVolumeEventListener() {
        @Override
        public void onBeepVolumeChanged(final BeepVolume volume) {
            if (volume == null) {
                Log.e(TAG, "BeepVolume is null");
                return;
            }

            if (!mBeepVolumeList.contains(volume)) {
                Log.e(TAG, "Invalid volume");
                return;
            }

            Log.d(TAG, "BeepVolume result: " + volume.getValue());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBeepSpinner.setOnItemSelectedListener(null);
                    mBeepSpinner.setSelection(mBeepVolumeList.indexOf(volume), false);
                    mBeepSpinner.setOnItemSelectedListener(mSpinnerBeepVolumeListener);
                }
            });
        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerBeepVolumeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            BeepVolume beepVolume = BeepVolume.valueOf(mBeepSpinner.getSelectedItem().toString());
            setBeepVolume(beepVolume);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private AdapterView.OnItemSelectedListener mSpinnerAutoPowerOffListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            AutoPowerOffTime time = AutoPowerOffTime.valueOf(mPowerOffSpinner.getSelectedItem().toString());
            setAutoPowerOffTime(time);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.device_settings);
        }

        mDevice.addBatteryEventListener(new Device.BatteryEventListener() {
            @Override
            public void onChargingStatusChanged(final BatteryInformation.BatteryStatus status) {
                if (status == null) {
                    Log.e(TAG, "BatteryStatus is null");
                    return;
                }

                Log.d(TAG, "Charging result: " + status.getValue());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChargingStatus.setText(status.getValue());
                    }
                });
            }

            @Override
            public void onBatteryLevelChanged(final int batteryLevel) {
                Log.d(TAG, "Battery level: " + batteryLevel);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBatteryLevel.setText(String.valueOf(batteryLevel));
                    }
                });
            }
        });

        mSetting = mApplication.getDevice().getSetting();

        if (mSetting == null) {
            Log.e(TAG, "Setting service is null");
            finish();
            return;
        }

        mSetting.addAutoPowerOffTimeEventListener(mAutoPowerOffTimeEventListener);

        mSetting.addBeepVolumeEventListener(mBeepVolumeEventListener);

        mSetting.getSupportedBeepVolumeList(new ResponseListener<List<BeepVolume>>() {
            @Override
            public void onSucceed(List<BeepVolume> result) {
                if (result == null) {
                    Log.e(TAG, "Null beep volume result");
                    return;
                }
                mBeepVolumeList.addAll(result);
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "Failed to get beep volume list. Code: " + code + " Message: " + (message == null ? "" : message));
            }
        });

        mSetting.getSupportedAutoPowerOffTimeList(new ResponseListener<List<AutoPowerOffTime>>() {
            @Override
            public void onSucceed(List<AutoPowerOffTime> result) {
                if (result == null) {
                    Log.e(TAG, "Null auto power off result");
                    return;
                }
                mAutoPowerOffTimeList.addAll(result);
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "Failed to get auto power off list. Code: " + code + " Message: " + (message == null ? "" : message));
            }
        });

        mDevice.getSupportedResetList(new ResponseListener<List<Reset>>() {
            @Override
            public void onSucceed(List<Reset> result) {
                if (result == null) {
                    Log.e(TAG, "Null reset result");
                    return;
                }

                mResetList.addAll(result);
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "Failed to get reset list. Code: " + code + " Message: " + (message == null ? "" : message));
            }
        });

        initLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSetting.removeAutoPowerOffTimeEventListener(mAutoPowerOffTimeEventListener);
        mSetting.removeBeepVolumeEventListener(mBeepVolumeEventListener);
        mDevice.removeAllEventListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateViewStatus();
    }

    private void updateViewStatus() {
        if (mSetting == null || mDevice == null) {
            Log.e(TAG, "Invalid state to perform updateViewStatus()");
            return;
        }

        showProgressDialog(getString(R.string.wait_response));

        mSetting.getBeepVolume(new ResponseListener<BeepVolume>() {
            @Override
            public void onSucceed(final BeepVolume result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        if (result == null) {
                            Log.e(TAG, "Beep result is null");
                            return;
                        }
                        if (!mBeepVolumeList.contains(result)) {
                            Log.e(TAG, "Invalid volume");
                            return;
                        }

                        mBeepSpinner.setOnItemSelectedListener(null);
                        mBeepSpinner.setSelection(mBeepVolumeList.indexOf(result), false);
                        mBeepSpinner.setOnItemSelectedListener(mSpinnerBeepVolumeListener);
                        Log.d(TAG, "Beep Result: " + result.getValue());
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "Beep Error : " + code + ", message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        mBeepSpinner.setOnItemSelectedListener(mSpinnerBeepVolumeListener);
                    }
                });
            }
        });

        mSetting.isLedEnabled(new ResponseListener<Boolean>() {
            @Override
            public void onSucceed(final Boolean result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        if (result == null) {
                            Log.e(TAG, "LED result is null");
                            return;
                        }

                        mLedSwitch.setOnCheckedChangeListener(null);
                        mLedSwitch.setChecked(result);
                        mLedSwitch.setOnCheckedChangeListener(mLedSwitchListener);
                        Log.d(TAG, "LED Result: " + result);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "LED Error : " + code + ", message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        mLedSwitch.setOnCheckedChangeListener(mLedSwitchListener);
                    }
                });
            }
        });

        mSetting.getAutoPowerOffTime(new ResponseListener<AutoPowerOffTime>() {
            @Override
            public void onSucceed(final AutoPowerOffTime result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        if (result == null) {
                            Log.e(TAG, "AutoPowerOffTime result is null");
                            return;
                        }

                        if (!mAutoPowerOffTimeList.contains(result)) {
                            Log.e(TAG, "Invalid time");
                            return;
                        }

                        mPowerOffSpinner.setOnItemSelectedListener(null);
                        mPowerOffSpinner.setSelection(mAutoPowerOffTimeList.indexOf(result), false);
                        mPowerOffSpinner.setOnItemSelectedListener(mSpinnerAutoPowerOffListener);
                        Log.d(TAG, "AutoPowerOffTime Result: " + result.getName());

                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "AutoPowerOffTime Error : " + code + ", message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        mPowerOffSpinner.setOnItemSelectedListener(mSpinnerAutoPowerOffListener);
                    }
                });
            }
        });

        mDevice.getStorageInformation(new ResponseListener<StorageInformation>() {
            @Override
            public void onSucceed(final StorageInformation result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        if (result == null) {
                            Log.e(TAG, "StorageInformation result is null");
                            return;
                        }
                        Log.d(TAG, "StorageInformation Result: " + result.toString());

                        mTotalStorage.setText(String.valueOf(result.getTotalSize()));
                        mUsedStorage.setText(String.valueOf(result.getUsedSize()));
                        mFreeStorage.setText(String.valueOf(result.getAvailableSize()));
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "StorageInformation Error : " + code + ", message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                });
            }
        });

        mDevice.getBatteryInformation(new ResponseListener<BatteryInformation>() {
            @Override
            public void onSucceed(final BatteryInformation result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        if (result == null) {
                            Log.e(TAG, "BatteryInformation result is null");
                            return;
                        }
                        Log.d(TAG, "BatteryInformation Result: " + result.toString());

                        mBatteryLevel.setText(String.valueOf(result.getBatteryLevel()));
                        mChargingStatus.setText(result.getBatteryStatus().getValue());
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "BatteryInformation Error : " + code + ", message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                });
            }
        });

        mFirmwareInfo.setText(mDevice.getFirmwareVersion());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mSetting.refreshAutoPowerOffTime();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initLayout() {
        mBeepSpinner = (Spinner) findViewById(R.id.spinner_beep);
        mLedSwitch = (Switch) findViewById(R.id.switch_led);
        mPowerOffSpinner = (Spinner) findViewById(R.id.spinner_power_off);
        mTotalStorage = (TextView) findViewById(R.id.text_storage_total);
        mUsedStorage = (TextView) findViewById(R.id.text_storage_used);
        mFreeStorage = (TextView) findViewById(R.id.text_storage_free);
        mBatteryLevel = (TextView) findViewById(R.id.text_battery_level);
        mChargingStatus = (TextView) findViewById(R.id.text_charging_status);
        mFirmwareInfo = (TextView) findViewById(R.id.text_firmware_info);
        Button resetSettings = (Button) findViewById(R.id.btn_reset_settings);
        Button resetConnection = (Button) findViewById(R.id.btn_reset_connection);
        Button resetAll = (Button) findViewById(R.id.btn_reset_all);
        Button format = (Button) findViewById(R.id.btn_format);

        ArrayAdapter<BeepVolume> beepAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, mBeepVolumeList);
        beepAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBeepSpinner.setAdapter(beepAdapter);

        ArrayAdapter<AutoPowerOffTime> powerOffAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, mAutoPowerOffTimeList);
        powerOffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPowerOffSpinner.setAdapter(powerOffAdapter);

        Log.d(TAG, "reset list: " + mResetList);

        resetSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog(getString(R.string.wait_response));

                mDevice.resetSettings(new ResponseListener<Void>() {
                    @Override
                    public void onSucceed(Void result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Reset camera settings success");
                                updateViewStatus();
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                Log.d(TAG, "Reset camera settings failed");
                            }
                        });
                    }
                }, Reset.SETTINGS);
            }
        });

        resetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog(getString(R.string.wait_response));

                mDevice.resetSettings(new ResponseListener<Void>() {
                    @Override
                    public void onSucceed(Void result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                Log.d(TAG, "Reset connection success");
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                Log.d(TAG, "Reset connection failed");
                            }
                        });
                    }
                }, Reset.CONNECTION);
            }
        });

        resetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog(getString(R.string.wait_response));

                mDevice.resetSettings(new ResponseListener<Void>() {
                    @Override
                    public void onSucceed(Void result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                Log.d(TAG, "Reset all success");
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                Log.d(TAG, "Reset all failed");
                            }
                        });
                    }
                }, Reset.ALL);
            }
        });

        format.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog(getString(R.string.wait_response));

                mDevice.formatStorage(new ResponseListener<Void>() {
                    @Override
                    public void onSucceed(Void result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateViewStatus();
                                Log.d(TAG, "Format success");
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                Log.d(TAG, "Format failed");
                            }
                        });
                    }
                });
            }
        });
    }

    private void setBeepVolume(BeepVolume beepVolume) {
        showProgressDialog(getString(R.string.wait_response));

        mSetting.setBeepVolume(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Log.d(TAG, "Beep updated");
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Log.d(TAG, "Beep update failed");
                    }
                });
            }
        }, beepVolume);
    }

    private void setAutoPowerOffTime(AutoPowerOffTime time) {
        showProgressDialog(getString(R.string.wait_response));

        mSetting.setAutoPowerOffTime(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Log.d(TAG, "Power off time updated");
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Log.d(TAG, "Power off time update failed");
                    }
                });
            }
        }, time);
    }
}

package com.samsung.android.sdk.gear360.sample.service;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.gear360.ResponseListener;
import com.samsung.android.sdk.gear360.device.camera.Camera;
import com.samsung.android.sdk.gear360.device.camera.LensErrorEventListener;
import com.samsung.android.sdk.gear360.device.camera.LensModeEventListener;
import com.samsung.android.sdk.gear360.device.camera.ModeSettingEventListener;
import com.samsung.android.sdk.gear360.device.camera.TimerEventListener;
import com.samsung.android.sdk.gear360.device.data.ExposureValueRange;
import com.samsung.android.sdk.gear360.device.data.IsoSensitivity;
import com.samsung.android.sdk.gear360.device.data.LensCount;
import com.samsung.android.sdk.gear360.device.data.LensMode;
import com.samsung.android.sdk.gear360.device.data.MainLens;
import com.samsung.android.sdk.gear360.device.data.RecordingResolution;
import com.samsung.android.sdk.gear360.device.data.Sharpness;
import com.samsung.android.sdk.gear360.device.data.ShootingMode;
import com.samsung.android.sdk.gear360.device.data.TimeLapseInterval;
import com.samsung.android.sdk.gear360.device.data.Timer;
import com.samsung.android.sdk.gear360.device.data.VideoLoopingRecordingTime;
import com.samsung.android.sdk.gear360.device.data.WhiteBalance;
import com.samsung.android.sdk.gear360.sample.R;

import java.util.List;

public class CameraSettingActivity extends BaseServiceActivity {
    private final String TAG = getClass().getSimpleName();
    private Camera mCamera;
    private CompoundButton.OnCheckedChangeListener mSwitchWindCutListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setWindCut(isChecked);
        }
    };
    private Spinner mSpinnerLensMode;
    private Spinner mSpinnerWhiteBalance;
    private Switch mSwitchPhotoHdr;
    private Switch mSwitchRecordingHdr;
    private TextView mViewExposureValue;
    private TextView mViewMinExposureValue;
    private TextView mViewMaxExposureValue;
    private SeekBar mSeekBarExposureValue;
    private float mMinExposureValue;
    private float mMaxExposureValue;
    private float mExposureValueStep;
    private Spinner mSpinnerInterval;
    private Spinner mSpinnerRecordingTime;
    private Spinner mSpinnerMainLens;
    private Spinner mSpinnerIso;
    private Spinner mSpinnerTimer;
    private Spinner mSpinnerSharpness;
    private Switch mSwitchWindCut;
    private Spinner mSpinnerSingleVideoResolution;
    private Spinner mSpinnerDualVideoResolution;
    private Spinner mSpinnerSingleTimeLapseResolution;
    private Spinner mSpinnerDualTimeLapseResolution;
    private AdapterView.OnItemSelectedListener mSpinnerLensModeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LensMode lensMode = LensMode.valueOf(mSpinnerLensMode.getSelectedItem().toString());
            setLensMode(lensMode);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerWhiteBalanceListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            WhiteBalance whiteBalance = WhiteBalance.valueOf(mSpinnerWhiteBalance.getSelectedItem().toString());
            setWhiteBalance(whiteBalance);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private CompoundButton.OnCheckedChangeListener mSwitchPhotoHdrListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setPhotoHdrStatus(isChecked);
        }
    };

    private CompoundButton.OnCheckedChangeListener mSwitchRecordingHdrListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setRecordingHdrStatus(isChecked);
        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerIntervalListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            TimeLapseInterval timeLapseInterval = TimeLapseInterval.valueOf(mSpinnerInterval.getSelectedItem().toString());
            setTimeLapseInterval(timeLapseInterval);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerRecordingTimeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            VideoLoopingRecordingTime recordingTime = VideoLoopingRecordingTime.valueOf(mSpinnerRecordingTime.getSelectedItem().toString());
            setVideoLoopingRecordingTime(recordingTime);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerMainLensListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            MainLens selectedValue = MainLens.valueOf(mSpinnerMainLens.getSelectedItem().toString());
            setMainLens(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private AdapterView.OnItemSelectedListener mSpinnerTimerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Timer selectedValue = Timer.valueOf(mSpinnerTimer.getSelectedItem().toString());
            setTimer(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private AdapterView.OnItemSelectedListener mSpinnerIsoListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            IsoSensitivity selectedValue = IsoSensitivity.valueOf(mSpinnerIso.getSelectedItem().toString());
            setIsoSensitivity(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private AdapterView.OnItemSelectedListener mSpinnerSharpnessListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Sharpness selectedValue = Sharpness.valueOf(mSpinnerSharpness.getSelectedItem().toString());
            setSharpness(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private AdapterView.OnItemSelectedListener mSpinnerSingleVideoListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            RecordingResolution selectedValue = RecordingResolution.valueOf(mSpinnerSingleVideoResolution.getSelectedItem().toString());
            setSingleLensVideoResolution(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerDualVideoListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            RecordingResolution selectedValue = RecordingResolution.valueOf(mSpinnerDualVideoResolution.getSelectedItem().toString());
            setDualLensVideoResolution(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerSingleTimeLapseListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            RecordingResolution selectedValue = RecordingResolution.valueOf(mSpinnerSingleTimeLapseResolution.getSelectedItem().toString());
            setSingleLensTimeLapseResolution(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mSpinnerDualTimeLapseListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            RecordingResolution selectedValue = RecordingResolution.valueOf(mSpinnerDualTimeLapseResolution.getSelectedItem().toString());
            setDualLensTimeLapseResolution(selectedValue);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private SeekBar.OnSeekBarChangeListener mSeekBarExposureListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float result = (float) (mMinExposureValue + (mMaxExposureValue - mMinExposureValue) * (progress * 0.01));
            setExposureValue(result);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private LensModeEventListener mLensModeEventListener = new LensModeEventListener() {
        @Override
        public void onLensModeSelected(final LensMode lensMode) {
            Log.d(TAG, "onLensModeSelected - " + lensMode);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraSettingActivity.this, "Lens mode is selected - " + lensMode, Toast.LENGTH_SHORT).show();
                    if (mSpinnerLensMode != null) {
                        mSpinnerLensMode.setSelection(getSelectedPosition(mSpinnerLensMode, lensMode.toString()), false);
                    }
                }
            });
        }
    };

    private TimerEventListener mTimerEventListener = new TimerEventListener() {
        @Override
        public void onTimerChanged(final Timer timer) {
            Log.d(TAG, "onTimerChanged - " + timer);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraSettingActivity.this, "Timer is changed - " + timer, Toast.LENGTH_SHORT).show();
                    getTimer();
                }
            });
        }
    };

    private ModeSettingEventListener mModeSettingEventListener = new ModeSettingEventListener() {
        @Override
        public void onRecordingResolutionChanged(final ShootingMode shootingMode, final LensCount lensCount, final RecordingResolution recordingResolution) {
            Log.d(TAG, "onRecordingResolutionChanged - shootingMode : " + shootingMode + " lensCount : " + lensCount + " photoResolution : " + recordingResolution);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraSettingActivity.this, lensCount + " lens " + shootingMode + " resolution is changed - " + recordingResolution, Toast.LENGTH_SHORT).show();
                    if (shootingMode == ShootingMode.VIDEO) {
                        if (lensCount == LensCount.SINGLE) {
                            getSingleLensVideoResolution();
                        } else {
                            getDualLensVideoResolution();
                        }
                    } else if (shootingMode == ShootingMode.TIME_LAPSE) {
                        if (lensCount == LensCount.SINGLE) {
                            getSingleLensTimeLapseResolution();
                        } else {
                            getDualLensTimeLapseResolution();
                        }
                    }
                }
            });
        }

        @Override
        public void onTimeLapseIntervalChanged(final TimeLapseInterval timeLapseInterval) {
            Log.d(TAG, "onTimeLapseIntervalChanged - " + timeLapseInterval);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraSettingActivity.this, "Time lapse interval is changed - " + timeLapseInterval, Toast.LENGTH_SHORT).show();
                    getTimeLapseInterval();
                }
            });
        }

        @Override
        public void onVideoLoopingRecordingTimeChanged(final VideoLoopingRecordingTime videoLoopingRecordingTime) {
            Log.d(TAG, "onVideoLoopingRecordingTimeChanged - " + videoLoopingRecordingTime);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraSettingActivity.this, "Video looping recording time is changed - " + videoLoopingRecordingTime, Toast.LENGTH_SHORT).show();
                    getVideoLoopingRecordingTime();
                }
            });
        }
    };

    private LensErrorEventListener mLensErrorEventListener = new LensErrorEventListener() {
        @Override
        public void onLensErrorOccurred() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraSettingActivity.this, "Lens error occurred!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_setting);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.camera_settings);
        }

        mCamera = mApplication.getDevice().getCamera();
        if (mCamera == null) {
            Log.e(TAG, "Camera service is null");
            finish();
            return;
        }

        mCamera.addLensModeEventListener(mLensModeEventListener);
        mCamera.addTimerEventListener(mTimerEventListener);
        mCamera.addModeSettingEventListener(mModeSettingEventListener);
        mCamera.addLensErrorEventListener(mLensErrorEventListener);

        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDefaultSetting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.removeLensModeEventListener(mLensModeEventListener);
        mCamera.removeTimerEventListener(mTimerEventListener);
        mCamera.removeModeSettingEventListener(mModeSettingEventListener);
        mCamera.removeLensErrorEventListener(mLensErrorEventListener);
    }

    private void initLayout() {
        mSpinnerLensMode = (Spinner) findViewById(R.id.spinner_lens_mode);
        getSupportedLensModeList();

        mSpinnerWhiteBalance = (Spinner) findViewById(R.id.spinner_white_balance);
        getSupportedWhiteBalanceList();

        mSwitchPhotoHdr = (Switch) findViewById(R.id.switch_photo_hdr);
        mSwitchRecordingHdr = (Switch) findViewById(R.id.switch_recording_hdr);

        mSeekBarExposureValue = (SeekBar) findViewById(R.id.seekbar_exposure_value);
        mViewExposureValue = (TextView) findViewById(R.id.view_exposure_value);
        mViewMinExposureValue = (TextView) findViewById(R.id.view_min_exposure_value);
        mViewMaxExposureValue = (TextView) findViewById(R.id.view_max_exposure_value);
        getSupportedExposureValueRange();

        mSpinnerInterval = (Spinner) findViewById(R.id.spinner_interval);
        getSupportedTimeLapseIntervalList();

        mSpinnerRecordingTime = (Spinner) findViewById(R.id.spinner_recording_time);
        getSupportedVideoLoopingRecordingTimeList();

        mSpinnerMainLens = (Spinner) findViewById(R.id.spinner_main_lens);
        getSupportedMainLensList();

        mSpinnerIso = (Spinner) findViewById(R.id.spinner_iso);
        getSupportedIsoSensitivityList();

        mSpinnerTimer = (Spinner) findViewById(R.id.spinner_timer);
        getSupportedTimerList();

        mSpinnerSharpness = (Spinner) findViewById(R.id.spinner_sharpness);
        getSupportedSharpnessList();

        mSwitchWindCut = (Switch) findViewById(R.id.switch_wind_cut);

        mSpinnerSingleVideoResolution = (Spinner) findViewById(R.id.spinner_single_video_resolution);
        getSupportedSingleLensVideoResolutionList();

        mSpinnerDualVideoResolution = (Spinner) findViewById(R.id.spinner_dual_video_resolution);
        getSupportedDualLensVideoResolutionList();

        mSpinnerSingleTimeLapseResolution = (Spinner) findViewById(R.id.spinner_single_time_lapse_resolution);
        getSupportedSingleLensTimeLapseResolutionList();

        mSpinnerDualTimeLapseResolution = (Spinner) findViewById(R.id.spinner_dual_time_lapse_resolution);
        getSupportedDualLensTimeLapseResolutionList();
    }

    private void getDefaultSetting() {
        getLensMode();
        getWhiteBalance();
        getPhotoHdrStatus();
        getRecordingHdrStatus();
        getExposureValue();
        getTimeLapseInterval();
        getVideoLoopingRecordingTime();
        getMainLens();
        getTimer();
        getIsoSensitivity();
        getSharpness();
        getWindCut();
        getSingleLensVideoResolution();
        getDualLensVideoResolution();
        getSingleLensTimeLapseResolution();
        getDualLensTimeLapseResolution();
    }

    private void getMainLens() {
        mCamera.getMainLens(new ResponseListener<MainLens>() {
            @Override
            public void onSucceed(final MainLens result) {
                Log.d(TAG, "getMainLens onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerMainLens.setOnItemSelectedListener(null);
                        mSpinnerMainLens.setSelection(getSelectedPosition(mSpinnerMainLens, result.toString()), false);
                        mSpinnerMainLens.setOnItemSelectedListener(mSpinnerMainLensListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getMainLens onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerMainLens.setOnItemSelectedListener(mSpinnerMainLensListener);
                    }
                });

            }
        });
    }

    private void setMainLens(MainLens mainLens) {
        mCamera.setMainLens(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, mainLens);
    }

    private void getIsoSensitivity() {
        mCamera.getIsoSensitivity(new ResponseListener<IsoSensitivity>() {
            @Override
            public void onSucceed(final IsoSensitivity result) {
                Log.d(TAG, "getIsoSensitivity onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerIso.setOnItemSelectedListener(null);
                        mSpinnerIso.setSelection(getSelectedPosition(mSpinnerIso, result.toString()), false);
                        mSpinnerIso.setOnItemSelectedListener(mSpinnerIsoListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getIsoSensitivity onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerIso.setOnItemSelectedListener(mSpinnerIsoListener);
                    }
                });
            }
        });
    }

    private void setIsoSensitivity(IsoSensitivity isoSensitivity) {
        mCamera.setIsoSensitivity(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, isoSensitivity);
    }

    private void getTimer() {
        mCamera.getTimer(new ResponseListener<Timer>() {
            @Override
            public void onSucceed(final Timer result) {
                Log.d(TAG, "getTimer onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerTimer.setOnItemSelectedListener(null);
                        mSpinnerTimer.setSelection(getSelectedPosition(mSpinnerTimer, result.toString()), false);
                        mSpinnerTimer.setOnItemSelectedListener(mSpinnerTimerListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getTimer onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerTimer.setOnItemSelectedListener(mSpinnerTimerListener);
                    }
                });
            }
        });
    }

    private void setTimer(Timer timer) {
        mCamera.setTimer(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, timer);
    }

    private void getSharpness() {
        mCamera.getSharpness(new ResponseListener<Sharpness>() {
            @Override
            public void onSucceed(final Sharpness result) {
                Log.d(TAG, "getSharpness onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerSharpness.setOnItemSelectedListener(null);
                        mSpinnerSharpness.setSelection(getSelectedPosition(mSpinnerSharpness, result.toString()), false);
                        mSpinnerSharpness.setOnItemSelectedListener(mSpinnerSharpnessListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getSharpness onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerSharpness.setOnItemSelectedListener(mSpinnerSharpnessListener);
                    }
                });
            }
        });
    }

    private void setSharpness(Sharpness sharpness) {
        mCamera.setSharpness(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, sharpness);
    }

    private void getWindCut() {
        mCamera.isWindCutEnabled(new ResponseListener<Boolean>() {
            @Override
            public void onSucceed(final Boolean result) {
                Log.d(TAG, "getWindCut onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchWindCut.setChecked(result);
                        mSwitchWindCut.setOnCheckedChangeListener(mSwitchWindCutListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getWindCut onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchWindCut.setOnCheckedChangeListener(mSwitchWindCutListener);
                    }
                });
            }
        });
    }

    private void setWindCut(boolean isOn) {
        mCamera.setWindCutEnabled(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, isOn);
    }

    private void getSingleLensVideoResolution() {
        mCamera.getRecordingResolution(new ResponseListener<RecordingResolution>() {
            @Override
            public void onSucceed(final RecordingResolution result) {
                Log.d(TAG, "getSingleLensVideoResolution onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerSingleVideoResolution.setOnItemSelectedListener(null);
                        mSpinnerSingleVideoResolution.setSelection(getSelectedPosition(mSpinnerSingleVideoResolution, result.toString()), false);
                        mSpinnerSingleVideoResolution.setOnItemSelectedListener(mSpinnerSingleVideoListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getSingleLensVideoResolution onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerSingleVideoResolution.setOnItemSelectedListener(mSpinnerSingleVideoListener);
                    }
                });
            }
        }, ShootingMode.VIDEO, LensCount.SINGLE);
    }

    private void setSingleLensVideoResolution(RecordingResolution resolution) {
        mCamera.setRecordingResolution(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, ShootingMode.VIDEO, LensCount.SINGLE, resolution);
    }

    private void getDualLensVideoResolution() {
        mCamera.getRecordingResolution(new ResponseListener<RecordingResolution>() {
            @Override
            public void onSucceed(final RecordingResolution result) {
                Log.d(TAG, "getDualLensVideoResolution onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerDualVideoResolution.setOnItemSelectedListener(null);
                        mSpinnerDualVideoResolution.setSelection(getSelectedPosition(mSpinnerDualVideoResolution, result.toString()), false);
                        mSpinnerDualVideoResolution.setOnItemSelectedListener(mSpinnerDualVideoListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getDualLensVideoResolution onFailed code : " + code + " message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerDualVideoResolution.setOnItemSelectedListener(mSpinnerDualVideoListener);
                    }
                });
            }

        }, ShootingMode.VIDEO, LensCount.DUAL);
    }

    private void setDualLensVideoResolution(RecordingResolution resolution) {
        mCamera.setRecordingResolution(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, ShootingMode.VIDEO, LensCount.DUAL, resolution);
    }

    private void getSingleLensTimeLapseResolution() {
        mCamera.getRecordingResolution(new ResponseListener<RecordingResolution>() {
            @Override
            public void onSucceed(final RecordingResolution result) {
                Log.d(TAG, "getSingleLensTimeLapseResolution onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerSingleTimeLapseResolution.setOnItemSelectedListener(null);
                        mSpinnerSingleTimeLapseResolution.setSelection(getSelectedPosition(mSpinnerSingleTimeLapseResolution, result.toString()), false);
                        mSpinnerSingleTimeLapseResolution.setOnItemSelectedListener(mSpinnerSingleTimeLapseListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getSingleLensTimeLapseResolution onFailed code : " + code + " message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerSingleTimeLapseResolution.setOnItemSelectedListener(mSpinnerSingleTimeLapseListener);
                    }
                });
            }
        }, ShootingMode.TIME_LAPSE, LensCount.SINGLE);
    }

    private void setSingleLensTimeLapseResolution(RecordingResolution resolution) {
        mCamera.setRecordingResolution(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, ShootingMode.TIME_LAPSE, LensCount.SINGLE, resolution);
    }

    private void getDualLensTimeLapseResolution() {
        mCamera.getRecordingResolution(new ResponseListener<RecordingResolution>() {
            @Override
            public void onSucceed(final RecordingResolution result) {
                Log.d(TAG, "getDualLensTimeLapseResolution onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerDualTimeLapseResolution.setOnItemSelectedListener(null);
                        mSpinnerDualTimeLapseResolution.setSelection(getSelectedPosition(mSpinnerDualTimeLapseResolution, result.toString()), false);
                        mSpinnerDualTimeLapseResolution.setOnItemSelectedListener(mSpinnerDualTimeLapseListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getDualLensTimeLapseResolution onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerDualTimeLapseResolution.setOnItemSelectedListener(mSpinnerDualTimeLapseListener);
                    }
                });
            }
        }, ShootingMode.TIME_LAPSE, LensCount.DUAL);
    }

    private void setDualLensTimeLapseResolution(RecordingResolution resolution) {
        mCamera.setRecordingResolution(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, ShootingMode.TIME_LAPSE, LensCount.DUAL, resolution);
    }

    private void getLensMode() {
        mCamera.getLensMode(new ResponseListener<LensMode>() {
            @Override
            public void onSucceed(final LensMode result) {
                Log.d(TAG, "getLensMode onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerLensMode.setOnItemSelectedListener(null);
                        mSpinnerLensMode.setSelection(getSelectedPosition(mSpinnerLensMode, result.toString()), false);
                        mSpinnerLensMode.setOnItemSelectedListener(mSpinnerLensModeListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getLensMode onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerLensMode.setOnItemSelectedListener(mSpinnerMainLensListener);
                    }
                });
            }
        });
    }

    private void setLensMode(LensMode lensMode) {
        mCamera.setLensMode(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, lensMode);
    }

    private void getWhiteBalance() {
        mCamera.getWhiteBalance(new ResponseListener<WhiteBalance>() {
            @Override
            public void onSucceed(final WhiteBalance result) {
                Log.d(TAG, "getWhiteBalance onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerWhiteBalance.setOnItemSelectedListener(null);
                        mSpinnerWhiteBalance.setSelection(getSelectedPosition(mSpinnerWhiteBalance, result.toString()), false);
                        mSpinnerWhiteBalance.setOnItemSelectedListener(mSpinnerWhiteBalanceListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getWhiteBalance onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerWhiteBalance.setOnItemSelectedListener(mSpinnerWhiteBalanceListener);
                    }
                });
            }
        });
    }

    private void setWhiteBalance(WhiteBalance whiteBalance) {
        mCamera.setWhiteBalance(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, whiteBalance);
    }


    private void getExposureValue() {
        mCamera.getExposureValue(new ResponseListener<Float>() {
            @Override
            public void onSucceed(final Float result) {
                Log.d(TAG, "getExposureValue onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mViewExposureValue != null) {
                            mViewExposureValue.setText(String.valueOf(result));
                        }

                        int progress = (int) ((result - mMinExposureValue) / (mMaxExposureValue - mMinExposureValue) * 100);
                        if (mSeekBarExposureValue != null) {
                            mSeekBarExposureValue.setOnSeekBarChangeListener(null);
                            mSeekBarExposureValue.setProgress(progress);
                            mSeekBarExposureValue.setOnSeekBarChangeListener(mSeekBarExposureListener);
                        }
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getExposureValue onFailed code : " + code + "message : " + message);
            }
        });
    }

    private void setExposureValue(final float exposureValue) {
        mCamera.setExposureValue(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                mCamera.getExposureValue(new ResponseListener<Float>() {
                    @Override
                    public void onSucceed(final Float result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                if (mViewExposureValue != null) {
                                    mViewExposureValue.setText(String.valueOf(result));
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, exposureValue);
    }

    private void getPhotoHdrStatus() {
        mCamera.isPhotoHdrEnabled(new ResponseListener<Boolean>() {
            @Override
            public void onSucceed(final Boolean result) {
                Log.d(TAG, "isPhotoHdrEnabled onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchPhotoHdr.setOnCheckedChangeListener(null);
                        mSwitchPhotoHdr.setChecked(result);
                        mSwitchPhotoHdr.setOnCheckedChangeListener(mSwitchPhotoHdrListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "isPhotoHdrEnabled onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchPhotoHdr.setOnCheckedChangeListener(mSwitchPhotoHdrListener);
                    }
                });
            }
        });
    }

    private void setPhotoHdrStatus(boolean isOn) {
        mCamera.setPhotoHdrEnabled(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, isOn);
    }

    private void getRecordingHdrStatus() {
        mCamera.isRecordingHdrEnabled(new ResponseListener<Boolean>() {
            @Override
            public void onSucceed(final Boolean result) {
                Log.d(TAG, "isRecordingHdrEnabled onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchRecordingHdr.setOnCheckedChangeListener(null);
                        mSwitchRecordingHdr.setChecked(result);
                        mSwitchRecordingHdr.setOnCheckedChangeListener(mSwitchRecordingHdrListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "isRecordingHdrEnabled onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchRecordingHdr.setOnCheckedChangeListener(mSwitchRecordingHdrListener);
                    }
                });
            }
        });
    }

    private void setRecordingHdrStatus(boolean isOn) {
        mCamera.setRecordingHdrEnabled(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, isOn);
    }

    private void getTimeLapseInterval() {
        mCamera.getTimeLapseInterval(new ResponseListener<TimeLapseInterval>() {
            @Override
            public void onSucceed(final TimeLapseInterval result) {
                Log.d(TAG, "getTimeLapseInterval onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerInterval.setOnItemSelectedListener(null);
                        mSpinnerInterval.setSelection(getSelectedPosition(mSpinnerInterval, result.toString()), false);
                        mSpinnerInterval.setOnItemSelectedListener(mSpinnerIntervalListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getTimeLapseInterval onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerInterval.setOnItemSelectedListener(mSpinnerIntervalListener);
                    }
                });
            }
        });
    }

    private void setTimeLapseInterval(TimeLapseInterval timeLapseInterval) {
        mCamera.setTimeLapseInterval(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, timeLapseInterval);
    }

    private void getVideoLoopingRecordingTime() {
        mCamera.getVideoLoopingRecordingTime(new ResponseListener<VideoLoopingRecordingTime>() {
            @Override
            public void onSucceed(final VideoLoopingRecordingTime result) {
                Log.d(TAG, "getVideoLoopingRecordingTime onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerRecordingTime.setOnItemSelectedListener(null);
                        mSpinnerRecordingTime.setSelection(getSelectedPosition(mSpinnerRecordingTime, result.toString()), false);
                        mSpinnerRecordingTime.setOnItemSelectedListener(mSpinnerRecordingTimeListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getVideoLoopingRecordingTime onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpinnerRecordingTime.setOnItemSelectedListener(mSpinnerRecordingTimeListener);
                    }
                });
            }
        });
    }

    private void setVideoLoopingRecordingTime(VideoLoopingRecordingTime videoLoopingRecordingTime) {
        mCamera.setVideoLoopingRecordingTime(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraSettingActivity.this, "Fail..." + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, videoLoopingRecordingTime);
    }

    private void getSupportedLensModeList() {
        mCamera.getSupportedLensModeList(new ResponseListener<List<LensMode>>() {
            @Override
            public void onSucceed(final List<LensMode> result) {
                Log.d(TAG, "getSupportedLensModeList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<LensMode> adapter_lens_mode = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, result.toArray(new LensMode[result.size()]));
                        adapter_lens_mode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerLensMode.setAdapter(adapter_lens_mode);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedLensModeList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<LensMode> adapter_lens_mode = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, LensMode.values());
                        adapter_lens_mode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerLensMode.setAdapter(adapter_lens_mode);
                    }
                });
            }
        });
    }

    private void getSupportedExposureValueRange() {
        mCamera.getSupportedExposureValueRange(new ResponseListener<ExposureValueRange>() {
            @Override
            public void onSucceed(final ExposureValueRange result) {
                Log.d(TAG, "getSupportedExposureValueRange onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMinExposureValue = result.getMin();
                        mMaxExposureValue = result.getMax();
                        mExposureValueStep = result.getStep();
                        if (mViewMinExposureValue != null) {
                            mViewMinExposureValue.setText(String.valueOf(result.getMin()));
                        }
                        if (mViewMaxExposureValue != null) {
                            mViewMaxExposureValue.setText(String.valueOf(result.getMax()));
                        }
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedWhiteBalanceList onFailed code : " + code + "message : " + message);
            }
        });
    }

    private void getSupportedWhiteBalanceList() {
        mCamera.getSupportedWhiteBalanceList(new ResponseListener<List<WhiteBalance>>() {
            @Override
            public void onSucceed(final List<WhiteBalance> result) {
                Log.d(TAG, "getSupportedWhiteBalanceList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<WhiteBalance> adapter_white_balance = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new WhiteBalance[result.size()]));
                        adapter_white_balance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerWhiteBalance.setAdapter(adapter_white_balance);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedWhiteBalanceList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<WhiteBalance> adapter_white_balance = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, WhiteBalance.values());
                        adapter_white_balance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerWhiteBalance.setAdapter(adapter_white_balance);
                    }
                });
            }
        });
    }

    private void getSupportedTimeLapseIntervalList() {
        mCamera.getSupportedTimeLapseIntervalList(new ResponseListener<List<TimeLapseInterval>>() {
            @Override
            public void onSucceed(final List<TimeLapseInterval> result) {
                Log.d(TAG, "getSupportedTimeLapseIntervalList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<TimeLapseInterval> adapter_interval = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new TimeLapseInterval[result.size()]));
                        adapter_interval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerInterval.setAdapter(adapter_interval);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedTimeLapseIntervalList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<TimeLapseInterval> adapter_interval = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, TimeLapseInterval.values());
                        adapter_interval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerInterval.setAdapter(adapter_interval);
                    }
                });
            }
        });
    }

    private void getSupportedVideoLoopingRecordingTimeList() {
        mCamera.getSupportedVideoLoopingRecordingTimeList(new ResponseListener<List<VideoLoopingRecordingTime>>() {
            @Override
            public void onSucceed(final List<VideoLoopingRecordingTime> result) {
                Log.d(TAG, "getSupportedVideoLoopingRecordingTimeList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<VideoLoopingRecordingTime> adapter_recording_time = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new VideoLoopingRecordingTime[result.size()]));
                        adapter_recording_time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerRecordingTime.setAdapter(adapter_recording_time);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedVideoLoopingRecordingTimeList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<VideoLoopingRecordingTime> adapter_recording_time = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                VideoLoopingRecordingTime.values());
                        adapter_recording_time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerRecordingTime.setAdapter(adapter_recording_time);
                    }
                });
            }
        });
    }

    private void getSupportedMainLensList() {
        mCamera.getSupportedMainLensList(new ResponseListener<List<MainLens>>() {
            @Override
            public void onSucceed(final List<MainLens> result) {
                Log.d(TAG, "getSupportedMainLensList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<MainLens> adapter_main_lens = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, result.toArray(new MainLens[result.size()]));
                        adapter_main_lens.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerMainLens.setAdapter(adapter_main_lens);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedMainLensList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<MainLens> adapter_main_lens = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, MainLens.values());
                        adapter_main_lens.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerMainLens.setAdapter(adapter_main_lens);
                    }
                });
            }
        });
    }

    private void getSupportedIsoSensitivityList() {
        mCamera.getSupportedIsoSensitivityList(new ResponseListener<List<IsoSensitivity>>() {
            @Override
            public void onSucceed(final List<IsoSensitivity> result) {
                Log.d(TAG, "getSupportedIsoSensitivityList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<IsoSensitivity> adapter_spinner_iso = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new IsoSensitivity[result.size()]));
                        adapter_spinner_iso.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerIso.setAdapter(adapter_spinner_iso);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedIsoSensitivityList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<IsoSensitivity> adapter_spinner_iso = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, IsoSensitivity.values());
                        adapter_spinner_iso.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerIso.setAdapter(adapter_spinner_iso);
                    }
                });
            }
        });
    }

    private void getSupportedTimerList() {
        mCamera.getSupportedTimerList(new ResponseListener<List<Timer>>() {
            @Override
            public void onSucceed(final List<Timer> result) {
                Log.d(TAG, "getSupportedTimerList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<Timer> adapter_timer = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, result.toArray(new Timer[result.size()]));
                        adapter_timer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerTimer.setAdapter(adapter_timer);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedTimerList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<Timer> adapter_timer = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Timer.values());
                        adapter_timer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerTimer.setAdapter(adapter_timer);
                    }
                });
            }
        });
    }

    private void getSupportedSharpnessList() {
        mCamera.getSupportedSharpnessList(new ResponseListener<List<Sharpness>>() {
            @Override
            public void onSucceed(final List<Sharpness> result) {
                Log.d(TAG, "getSupportedSharpnessList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<Sharpness> adapter_sharpness = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, result.toArray(new Sharpness[result.size()]));
                        adapter_sharpness.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerSharpness.setAdapter(adapter_sharpness);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedSharpnessList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<Sharpness> adapter_sharpness = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Sharpness.values());
                        adapter_sharpness.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerSharpness.setAdapter(adapter_sharpness);
                    }
                });
            }
        });
    }

    private void getSupportedSingleLensVideoResolutionList() {
        mCamera.getSupportedRecordingResolutionList(new ResponseListener<List<RecordingResolution>>() {
            @Override
            public void onSucceed(final List<RecordingResolution> result) {
                Log.d(TAG, "getSupportedSingleLensVideoResolutionList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_single_video = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new RecordingResolution[result.size()]));
                        adapter_single_video.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerSingleVideoResolution.setAdapter(adapter_single_video);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedSingleLensVideoResolutionList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_single_video = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, RecordingResolution.values());
                        adapter_single_video.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerSingleVideoResolution.setAdapter(adapter_single_video);
                    }
                });
            }
        }, ShootingMode.VIDEO, LensCount.SINGLE);
    }

    private void getSupportedDualLensVideoResolutionList() {
        mCamera.getSupportedRecordingResolutionList(new ResponseListener<List<RecordingResolution>>() {
            @Override
            public void onSucceed(final List<RecordingResolution> result) {
                Log.d(TAG, "getSupportedDualLensVideoResolutionList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_dual_video = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new RecordingResolution[result.size()]));
                        adapter_dual_video.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerDualVideoResolution.setAdapter(adapter_dual_video);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedDualLensVideoResolutionList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_dual_video = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, RecordingResolution.values());
                        adapter_dual_video.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerDualVideoResolution.setAdapter(adapter_dual_video);
                    }
                });
            }
        }, ShootingMode.VIDEO, LensCount.DUAL);
    }

    private void getSupportedSingleLensTimeLapseResolutionList() {
        mCamera.getSupportedRecordingResolutionList(new ResponseListener<List<RecordingResolution>>() {
            @Override
            public void onSucceed(final List<RecordingResolution> result) {
                Log.d(TAG, "getSupportedSingleLensTimeLapseResolutionList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_single_time_lapse = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new RecordingResolution[result.size()]));
                        adapter_single_time_lapse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerSingleTimeLapseResolution.setAdapter(adapter_single_time_lapse);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedSingleLensTimeLapseResolutionList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_single_time_lapse = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                RecordingResolution.values());
                        adapter_single_time_lapse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerSingleTimeLapseResolution.setAdapter(adapter_single_time_lapse);
                    }
                });
            }
        }, ShootingMode.TIME_LAPSE, LensCount.SINGLE);
    }

    private void getSupportedDualLensTimeLapseResolutionList() {
        mCamera.getSupportedRecordingResolutionList(new ResponseListener<List<RecordingResolution>>() {
            @Override
            public void onSucceed(final List<RecordingResolution> result) {
                Log.d(TAG, "getSupportedDualLensTimeLapseResolutionList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_dual_time_lapse = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,
                                result.toArray(new RecordingResolution[result.size()]));
                        adapter_dual_time_lapse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerDualTimeLapseResolution.setAdapter(adapter_dual_time_lapse);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedDualLensTimeLapseResolutionList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<RecordingResolution> adapter_dual_time_lapse = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, RecordingResolution.values());
                        adapter_dual_time_lapse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerDualTimeLapseResolution.setAdapter(adapter_dual_time_lapse);
                    }
                });
            }
        }, ShootingMode.TIME_LAPSE, LensCount.DUAL);
    }

    private int getSelectedPosition(Spinner spinner, String selectedItem) {
        int selectedPosition = 0;
        for (int position = 0; position < spinner.getCount(); position++) {
            if (selectedItem.equals(spinner.getAdapter().getItem(position).toString())) {
                selectedPosition = position;
                break;
            }
        }
        return selectedPosition;
    }
}

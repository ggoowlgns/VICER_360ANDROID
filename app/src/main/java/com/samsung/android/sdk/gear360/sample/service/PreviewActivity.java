package com.samsung.android.sdk.gear360.sample.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.samsung.android.sdk.gear360.ResponseListener;
import com.samsung.android.sdk.gear360.device.ConnectionManager;
import com.samsung.android.sdk.gear360.device.Device;
import com.samsung.android.sdk.gear360.device.camera.Camera;
import com.samsung.android.sdk.gear360.device.camera.DeviceButtonPressEventListener;
import com.samsung.android.sdk.gear360.device.camera.LensModeEventListener;
import com.samsung.android.sdk.gear360.device.camera.ShootingModeEventListener;
import com.samsung.android.sdk.gear360.device.camera.StreamReceiver;
import com.samsung.android.sdk.gear360.device.camera.TimerResponseListener;
import com.samsung.android.sdk.gear360.device.data.DeviceButtonType;
import com.samsung.android.sdk.gear360.device.data.LensMode;
import com.samsung.android.sdk.gear360.device.data.Location;
import com.samsung.android.sdk.gear360.device.data.MotionInfo;
import com.samsung.android.sdk.gear360.device.data.OpticalCalibration;
import com.samsung.android.sdk.gear360.device.data.ShootingMode;
import com.samsung.android.sdk.gear360.sample.R;
import com.samsung.android.sdk.gear360.sample.SampleApplication;
import com.samsung.android.sdk.gear360.sample.helper.UIUpdater;

import net.ossrs.yasea.SrsAllocator;
import net.ossrs.yasea.SrsFlvMuxer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * Activity for render preview.
 */

public class PreviewActivity extends Activity {
    //Socket socket = null;
    public static final int ServerPort = 8010;
    public static final String ServerIP = "18.179.74.220";
//    public static final String ServerIP = "192.168.35.211";


    ///////////
    private boolean isConnected = false;
    Socket socket = null;
    /////

    private final String TAG = PreviewActivity.class.getSimpleName();
    private Camera mCamera;

    private View mProgressLayout;
    private MySurfaceView mSurfaceView;
    private Spinner mLensModeSpinner;
    private boolean mIsPreviewing;
    private SurfaceHolder mSurfaceHolder;
    private Button mRecordingVideoBeginButton;
    private Button mRecordingVideoPauseButton;
    private Button mPreviewButton;
    private Spinner mDialModeSpinner;

    private int surfaceIndex = 1;


    private DataOutputStream os;

    private Boolean sendReady = false;
    private Boolean drawReady = true;

    private byte[] data;
    private byte[] size;


    /****************************************
     **************스트리밍***********************
     ******************************************/

    private static final long INPUT_BUFF_TIMEOUT_US = 5000;// in us
    private static final long OUTPUT_BUFF_TIMEOUT_US = 10000;   // in us

    private static final int ENCODE_VIDEO_WIDTH = 960;
    private static final int ENCODE_VIDEO_HEIGHT = 480;
    private static final int ENCODE_VIDEO_BIT_RATE = 4 * 1000 * 1000;
    //2000 * 2000;

    private Context mContext;
    private UIUpdater mUpdateHelper;
    private SrsFlvMuxer mMuxer;


    private int mAudioTrack;
    private int mVideoTrack;

    private Surface mRenderSurface;

    /****************************************
     **************스트리밍***********************
     ******************************************/


    private final ShootingModeEventListener mShootingModeEventListener = new ShootingModeEventListener() {
        @Override
        public void onShootingModeChanged(final ShootingMode shootingMode) {
            Log.d(TAG, "ShootingMode is changed by device. ShootingMode = " + shootingMode.getValue());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDialModeSpinner.setOnItemSelectedListener(null);
                    mDialModeSpinner.setSelection(shootingMode.ordinal(), false);
                    mDialModeSpinner.setOnItemSelectedListener(mSpinnerShootingModeListener);
                    Toast.makeText(PreviewActivity.this, "Shooting mode is changed into " + shootingMode.getValue() + " on device", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private final DeviceButtonPressEventListener mDeviceButtonPressEventListener = new DeviceButtonPressEventListener() {
        @Override
        public void onDeviceButtonPressed(final DeviceButtonType type) {
            Log.d(TAG, "Button is pressed on device. type = " + type);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PreviewActivity.this, type + " button is pressed on device", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private final LensModeEventListener mLensModeEventListener = new LensModeEventListener() {
        @Override
        public void onLensModeSelected(final LensMode lensMode) {
            Log.d(TAG, "onLensModeSelected - " + lensMode);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PreviewActivity.this, "Lens mode is selected - " + lensMode, Toast.LENGTH_SHORT).show();
                    if (mLensModeSpinner != null) {
                        mLensModeSpinner.setSelection(lensMode.ordinal(), false);
                    }
                }
            });
        }
    };

    private final AdapterView.OnItemSelectedListener mSpinnerShootingModeListener = (new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final ShootingMode shootingMode = ShootingMode.valueOf(mDialModeSpinner.getSelectedItem().toString());
            Log.d(TAG, "mCamera.setShootingMode. mode = " + shootingMode);
            mCamera.setShootingMode(new ResponseListener<ShootingMode>() {
                @Override
                public void onSucceed(ShootingMode result) {
                    Log.d(TAG, "mCamera.takePhoto onSucceed()");
                    if (result != null) {
                        Log.d(TAG, "shootingMode = " + result.getValue());
                    }
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    Log.d(TAG, "mCamera.setShootingMode onFailed()");
                    Log.d(TAG, "code = " + code);
                    Log.d(TAG, "message = " + message);
                }
            }, shootingMode);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.d(TAG, "mDialModeSpinner.onNothingSelected.");
        }
    });

    private final AdapterView.OnItemSelectedListener mSpinnerLensModeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final LensMode lensMode = LensMode.valueOf(mLensModeSpinner.getSelectedItem().toString());
            if (mIsPreviewing) {
                mPreviewButton.setEnabled(false);
                mCamera.stopPreview(new ResponseListener<Void>() {
                    @Override
                    public void onSucceed(Void result) {
                        Log.d(TAG, "stopPreview success");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSurfaceView.setVisibility(View.INVISIBLE);
                                setLensMode(lensMode);
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, final String message) {
                        Log.e(TAG, "stopPreview failed - code : " + code + "message : " + message);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPreviewButton.setEnabled(true);
                                Toast.makeText(getApplicationContext(), "Fail - " + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                setLensMode(lensMode);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        mProgressLayout = findViewById(R.id.lay_preview_waiting);
        mSurfaceView = (MySurfaceView) findViewById(R.id.suf_videoPreview);

        //mImageView = (ImageView)findViewById(R.id.imageView_test);

        Device device = ((SampleApplication) getApplication()).getDevice();
        device.addConnectionStatusListener(new Device.ConnectionStatusListener() {
            @Override
            public void onClosed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressLayout.setVisibility(View.GONE);
                        Toast.makeText(PreviewActivity.this, "A device disconnected!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        });

        mCamera = device.getCamera();
        if (mCamera == null) {
            Log.e(TAG, "Camera service is null");
            finish();
            return;
        }

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceHolder.surfaceCreated");
                mRenderSurface = holder.getSurface();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceHolder.surfaceChanged");
                Toast.makeText(getApplicationContext(), "Surface" + surfaceIndex + " changed", Toast.LENGTH_LONG).show();
                surfaceIndex++;

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceHolder.surfaceDestroyed");
            }
        });


        // add device event listener
        if (mCamera != null) {
            mCamera.addShootingModeEventListener(mShootingModeEventListener);
            mCamera.addDeviceButtonPressEventListener(mDeviceButtonPressEventListener);
            mCamera.addLensModeEventListener(mLensModeEventListener);
        }

        initializeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLensMode();
        getShootingMode();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsPreviewing) {
            mCamera.stopPreview(new ResponseListener<Void>() {
                @Override
                public void onSucceed(Void result) {
                    mIsPreviewing = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPreviewButton.setText(R.string.start_preview);
                            mProgressLayout.setVisibility(View.GONE);
                            mSurfaceView.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                @Override
                public void onFailed(ErrorCode code, String message) {

                }
            });
        }
    }

    private void initializeView() {
        mDialModeSpinner = (Spinner) findViewById(R.id.spinner_camera_dial_mode);
        final Button takePictureShotButton = (Button) findViewById(R.id.button_picture);
        mPreviewButton = (Button) findViewById(R.id.button_preview);
        mRecordingVideoBeginButton = (Button) findViewById(R.id.button_start_record);
        mRecordingVideoPauseButton = (Button) findViewById(R.id.button_pause_record);
        mLensModeSpinner = (Spinner) findViewById(R.id.spinner_lens_mode);
        getSupportedLensModeList();
        getSupportedShootingModeList();

        mPreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mPreviewButton.setEnabled(false);
                    mProgressLayout.setVisibility(View.VISIBLE);

                    if (getResources().getString(R.string.start_preview).equals(mPreviewButton.getText())) {
                        Log.d(TAG, "start preview pressed");

                        mSurfaceView.setVisibility(View.VISIBLE);

                        if (mCamera != null) {


                            //startStreaming 만듬
                            mCamera.startStreaming(new StreamReceiver() {
                                private MediaCodec mDecoderRender;
                                private AudioThread mAudioPlayer;

                                private MediaCodec.BufferInfo mVideoDecodeOut = new MediaCodec.BufferInfo();
                                private MediaCodec.BufferInfo mAudioDecodeOut = new MediaCodec.BufferInfo();

                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onStarted(MediaFormat video, MediaFormat audio, OpticalCalibration opticalCalibration) {
                                    //Change UI state.

                                    //Start encoders & decoders.
                                    try {
                                        mDecoderRender = MediaCodec.createDecoderByType(video.getString(MediaFormat.KEY_MIME));
                                        //Surface 부분이 null 이어야지만 frame을 처리할수있음
                                        mDecoderRender.configure(video, null, null, 0);
                                        mDecoderRender.start();
                                        //mDecoderRender.setOutputSurface(mRenderSurface);

                                        /*mAudioPlayer = new AudioThread(audio.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                                        mAudioPlayer.start();*/

                                        /*mDecoderRTMPAudio = MediaCodec.createDecoderByType(audio.getString(MediaFormat.KEY_MIME));
                                        mDecoderRTMPAudio.configure(audio, null, null, 0);
                                        mDecoderRTMPAudio.start();*/
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onVideoReceived(long timestamp, boolean isConfigFrame, boolean isKeyFrame, byte[] bytes) {
                                    //Consume frames to surface
                                    if (mDecoderRender != null) {
                                        int inBufIndex = mDecoderRender.dequeueInputBuffer(INPUT_BUFF_TIMEOUT_US);
                                        if (inBufIndex >= 0) {
                                            ByteBuffer inBuffer = mDecoderRender.getInputBuffer(inBufIndex);
                                            if (inBuffer != null) {
                                                inBuffer.put(bytes);
                                                mDecoderRender.queueInputBuffer(inBufIndex, 0, bytes.length, timestamp
                                                        , isConfigFrame ? MediaCodec.BUFFER_FLAG_CODEC_CONFIG : (isKeyFrame ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0));

                                                int outBufIndex = mDecoderRender.dequeueOutputBuffer(mVideoDecodeOut, OUTPUT_BUFF_TIMEOUT_US);
                                                if (outBufIndex >= 0) {
                                                    /*******************************여기분명 맞는것 같음************************************
                                                     **************************************수정*********************************************
                                                     ******************************************************************************************/
                                                    ByteBuffer outputBuffer = mDecoderRender.getOutputBuffer(outBufIndex);
                                                    if (drawReady) {
                                                        //Image mDecoderRenderOutputImage = mDecoderRender.getOutputImage(outBufIndex);
                                                        //ByteBuffer buffer = mDecoderRenderOutputImage.getPlanes()[0].getBuffer();
                                                        byte[] bytesTemp = new byte[outputBuffer.remaining()];
                                                        outputBuffer.get(bytesTemp, 0, bytesTemp.length);
                                                        Log.e("bytesTemp:", bytesTemp.toString());
                                                        Bitmap bitTemp = yourFunction(bytesTemp, ENCODE_VIDEO_WIDTH, ENCODE_VIDEO_HEIGHT);
                                                        data = getImageByte(bitTemp);

                                                        Log.e("data length is", String.valueOf(data.length));
                                                        size = getByte(data.length);
                                                        drawReady = false;
                                                        sendReady = true;

                                                    }

                                                    /***********************************************************************
                                                     ****************************수정****************************************
                                                     ************************************************************************/
                                                    //에서 surface로 rendering
                                                    mDecoderRender.releaseOutputBuffer(outBufIndex, true);
                                                }
                                            }
                                        }
/*
                                        MediaCodec.BufferInfo bufInfo = new MediaCodec.BufferInfo();
                                        bufInfo.set(0, bytes.length, timestamp
                                                , isConfigFrame?MediaCodec.BUFFER_FLAG_CODEC_CONFIG:(isKeyFrame?MediaCodec.BUFFER_FLAG_KEY_FRAME:0));

//                                        mMuxer.writeSampleData(mVideoTrack, ByteBuffer.wrap(bytes), bufInfo);
                                        Surface surfaceTemp = mDecoderRender.createInputSurface();
                                        Bitmap b = Bitmap.createBitmap (mSurfaceView.getWidth(), mSurfaceView.getHeight (), Bitmap.Config.ARGB_8888);

                                        Canvas canvasTemp = surfaceTemp.lockCanvas(new Rect(0, 0, ENCODE_VIDEO_WIDTH, ENCODE_VIDEO_HEIGHT));*/
                                    }
                                }

                                @Override
                                public void onAudioReceived(long timestamp, byte[] bytes) {
                                    //Consume frames to audio
                                    /*if(mDecoderRTMPAudio != null){
                                        int bufferInIndex = mDecoderRTMPAudio.dequeueInputBuffer(INPUT_BUFF_TIMEOUT_US);
                                        if (bufferInIndex >= 0) {
                                            ByteBuffer buffer = mDecoderRTMPAudio.getInputBuffer(bufferInIndex);
                                            if (buffer != null) {
                                                buffer.put(bytes);
                                                mDecoderRTMPAudio.queueInputBuffer(bufferInIndex, 0, bytes.length, timestamp, 0);

                                                int outBufferIndex = mDecoderRTMPAudio.dequeueOutputBuffer(mAudioDecodeOut, OUTPUT_BUFF_TIMEOUT_US);
                                                if (outBufferIndex > 0) {
                                                    ByteBuffer outBuffer = mDecoderRTMPAudio.getOutputBuffer(outBufferIndex);
                                                    if (outBuffer != null) {
                                                        byte[] rawFrame = new byte[mAudioDecodeOut.size];
                                                        outBuffer.get(rawFrame);
                                                        if(mAudioPlayer != null) {
                                                            mAudioPlayer.pushSample(rawFrame);
                                                        }
                                                    }
                                                    mDecoderRTMPAudio.releaseOutputBuffer(outBufferIndex, true);
                                                }
                                            }
                                        }

                                        MediaCodec.BufferInfo bufInfo = new MediaCodec.BufferInfo();
                                        bufInfo.set(0, bytes.length, timestamp, 0);
//                                        mMuxer.writeSampleData(mAudioTrack, ByteBuffer.wrap(bytes), bufInfo);
                                    }*/
                                }

                                @Override
                                public void onMotionReceived(long l, MotionInfo motionInfo) {

                                }

                                @Override
                                public void onStopped() {
                                    mMuxer.stop();

                                    if (mDecoderRender != null) {
                                        mDecoderRender.stop();
                                        mDecoderRender.release();
                                        mDecoderRender = null;
                                    }

                                    if (mAudioPlayer != null) {
                                        mAudioPlayer.release();
                                        mAudioPlayer = null;
                                    }
                                }

                                @Override
                                public void onError(ResponseListener.ErrorCode errorCode, String s) {

                                }
                            }, ENCODE_VIDEO_WIDTH, ENCODE_VIDEO_HEIGHT, ENCODE_VIDEO_BIT_RATE, true);

                            /*****************서버로 보내주는곳*****************************/
                            connect();
                            /**********************STREAM END***********************************/


                        }


                    } else {
                        Log.d(TAG, "stop preview pressed");
                        if (mCamera != null) {
                            mCamera.stopPreview(new ResponseListener<Void>() {
                                @Override
                                public void onSucceed(Void result) {
                                    mIsPreviewing = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mPreviewButton.setText(R.string.start_preview);
                                            mPreviewButton.setEnabled(true);
                                            mProgressLayout.setVisibility(View.GONE);
                                            mSurfaceView.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }

                                @Override
                                public void onFailed(ErrorCode code, String message) {
                                    Log.d(TAG, "Failed to stopPreview : code : " + code + " , message : " + message);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mProgressLayout.setVisibility(View.GONE);
                                            mPreviewButton.setEnabled(true);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }
        });

        if (takePictureShotButton != null) {
            takePictureShotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "mCamera.takePhoto");
                    if (getResources().getString(R.string.camera_button_take_picture_shot).equals(takePictureShotButton.getText())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                takePictureShotButton.setText(getResources().getString(R.string.camera_button_take_picture_shot_cancel));
                            }
                        });

                        Location location = new Location();
                        location.setLatitude(37.25861);
                        location.setLongitude(127.05611);

                        mCamera.takePhoto(new TimerResponseListener<Integer>() {

                            @Override
                            public void onTimerCountChanged(final int timerCount) {
                                Log.d(TAG, "onTimerCountChanged. timerCount = " + timerCount);
                            }

                            @Override
                            public void onSucceed(Integer result) {
                                Log.d(TAG, "mCamera.takePhoto onSucceed()");
                                if (result != null) {
                                    Log.d(TAG, "availableTakePhotoCount = " + result);
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        takePictureShotButton.setText(getResources().getString(R.string.camera_button_take_picture_shot));
                                    }
                                });
                            }

                            @Override
                            public void onFailed(ErrorCode code, final String message) {
                                Log.d(TAG, "mCamera.takePhoto onFailed()");
                                Log.d(TAG, "code = " + code);
                                Log.d(TAG, "message = " + message);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PreviewActivity.this, "Fail - " + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                                        takePictureShotButton.setText(getResources().getString(R.string.camera_button_take_picture_shot));
                                    }
                                });
                            }
                        }, location);
                    } else {
                        mCamera.cancelTakingPhoto(new ResponseListener<Void>() {
                            @Override
                            public void onSucceed(Void result) {
                                Log.d(TAG, "mCamera.cancelTakingPhoto onSucceed()");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        takePictureShotButton.setText(getResources().getString(R.string.camera_button_take_picture_shot));
                                    }
                                });
                            }

                            @Override
                            public void onFailed(ErrorCode code, String message) {
                                Log.d(TAG, "mCamera.cancelTakingPhoto onFailed()");
                                Log.d(TAG, "code = " + code);
                                Log.d(TAG, "message = " + message);
                            }
                        });
                    }
                }
            });
        }

        if (mRecordingVideoBeginButton != null) {
            mRecordingVideoBeginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mIsPreviewing) {
                        mCamera.stopPreview(new ResponseListener<Void>() {
                            @Override
                            public void onSucceed(Void result) {
                                Log.d(TAG, "stopPreview success");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSurfaceView.setVisibility(View.INVISIBLE);
                                        mPreviewButton.setEnabled(false);
                                        startRecording();
                                    }
                                });
                            }

                            @Override
                            public void onFailed(ErrorCode code, final String message) {
                                Log.e(TAG, "stopPreview failed - code : " + code + "message : " + message);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Fail - " + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } else {
                        startRecording();
                    }
                }
            });
        }

        if (mRecordingVideoPauseButton != null) {
            mRecordingVideoPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "mCamera.pauseRecording");

                    if (getResources().getString(R.string.camera_button_record_video_pause).equals(mRecordingVideoPauseButton.getText())) {
                        mCamera.pauseRecording(new ResponseListener<Integer>() {
                            @Override
                            public void onSucceed(Integer result) {
                                Log.d(TAG, "mCamera.pauseRecording onSucceed()");
                                Log.d(TAG, "recordedVideoPeriod = " + result);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecordingVideoPauseButton.setText(getResources().getString(R.string.camera_button_record_video_resume));
                                    }
                                });
                            }

                            @Override
                            public void onFailed(ErrorCode code, String message) {
                                Log.d(TAG, "mCamera.pauseRecording onFailed()");
                                Log.d(TAG, "code = " + code);
                                Log.d(TAG, "message = " + message);
                            }
                        });
                    } else {
                        mCamera.resumeRecording(new ResponseListener<Void>() {
                            @Override
                            public void onSucceed(Void result) {
                                Log.d(TAG, "mCamera.resumeRecording onSucceed()");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecordingVideoPauseButton.setText(getResources().getString(R.string.camera_button_record_video_pause));
                                    }
                                });
                            }

                            @Override
                            public void onFailed(ErrorCode code, String message) {
                                Log.d(TAG, "mCamera.resumeRecording onFailed()");
                                Log.d(TAG, "code = " + code);
                                Log.d(TAG, "message = " + message);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        ConnectionManager connectionManager = SampleApplication.getApplicationInstance().getGear360SDK().getConnectionManager();
        connectionManager.disconnectDevice(new ConnectionManager.DisconnectDeviceListener() {
            @Override
            public void onDisconnected() {
                finish();
            }

            @Override
            public void onFailed(ResponseListener.ErrorCode code, String message) {
                finish();
            }
        }, ((SampleApplication) getApplication()).getDevice());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remove device event listener
        if (mCamera != null) {
            mCamera.removeShootingModeEventListener(mShootingModeEventListener);
            mCamera.removeDeviceButtonPressEventListener(mDeviceButtonPressEventListener);
            mCamera.removeLensModeEventListener(mLensModeEventListener);
        }
    }

    private void getSupportedLensModeList() {
        mCamera.getSupportedLensModeList(new ResponseListener<List<LensMode>>() {
            @Override
            public void onSucceed(final List<LensMode> result) {
                Log.d(TAG, "getSupportedLensModeList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<LensMode> lensModeAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.item_camera_spinner, result.toArray(new LensMode[result.size()]));
                        mLensModeSpinner.setAdapter(lensModeAdapter);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedLensModeList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<LensMode> lensModeAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.item_camera_spinner, LensMode.values());
                        mLensModeSpinner.setAdapter(lensModeAdapter);
                    }
                });
            }
        });
    }

    private void getSupportedShootingModeList() {
        mCamera.getSupportedShootingModeList(new ResponseListener<List<ShootingMode>>() {
            @Override
            public void onSucceed(final List<ShootingMode> result) {
                Log.d(TAG, "getSupportedShootingModeList onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<ShootingMode> dialModeAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.item_camera_spinner, result.toArray(new ShootingMode[result.size()]));
                        mDialModeSpinner.setAdapter(dialModeAdapter);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getSupportedShootingModeList onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<ShootingMode> shootingModeAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.item_camera_spinner, ShootingMode.values());
                        mDialModeSpinner.setAdapter(shootingModeAdapter);
                    }
                });
            }
        });
    }

    private void startPreview() {
        mSurfaceView.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.VISIBLE);
        mCamera.startPreview(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                Log.d(TAG, "startPreview success");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressLayout.setVisibility(View.GONE);
                        mPreviewButton.setText(R.string.stop_preview);
                        mPreviewButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                Log.e(TAG, "startPreview failed - code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIsPreviewing = false;
                        mSurfaceView.setVisibility(View.INVISIBLE);
                        mProgressLayout.setVisibility(View.GONE);
                        mPreviewButton.setText(R.string.start_preview);
                        mPreviewButton.setEnabled(true);
                    }
                });
            }
        }, mSurfaceHolder.getSurface(), true);
    }

    private void getLensMode() {
        mCamera.getLensMode(new ResponseListener<LensMode>() {
            @Override
            public void onSucceed(final LensMode result) {
                Log.d(TAG, "getLensMode onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLensModeSpinner.setOnItemSelectedListener(null);
                        mLensModeSpinner.setSelection(result.ordinal(), false);
                        mLensModeSpinner.setOnItemSelectedListener(mSpinnerLensModeListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.d(TAG, "getLensMode onFailed code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLensModeSpinner.setOnItemSelectedListener(mSpinnerLensModeListener);
                    }
                });
            }
        });
    }

    private void getShootingMode() {
        mCamera.getShootingMode(new ResponseListener<ShootingMode>() {
            @Override
            public void onSucceed(final ShootingMode result) {
                Log.d(TAG, "getShootingMode onSucceed result : " + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialModeSpinner.setOnItemSelectedListener(null);
                        mDialModeSpinner.setSelection(result.ordinal(), false);
                        mDialModeSpinner.setOnItemSelectedListener(mSpinnerShootingModeListener);
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "getShootingMode onFailed code : " + code + "message : " + message);
            }
        });
    }

    private void setLensMode(LensMode lensMode) {
        mCamera.setLensMode(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                Log.d(TAG, "setLensMode success");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsPreviewing) {
                            startPreview();
                        }

                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, final String message) {
                Log.e(TAG, "setLensMode failed - code : " + code + "message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreviewButton.setEnabled(true);
                        mPreviewButton.setText(R.string.start_preview);
                        Toast.makeText(getApplicationContext(), "Fail - " + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, lensMode);
    }

    private void startRecording() {
        Log.d(TAG, "mCamera.startRecording");
        if (getResources().getString(R.string.camera_button_record_start).equals(mRecordingVideoBeginButton.getText())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecordingVideoBeginButton.setText(getResources().getString(R.string.camera_button_record_start_cancel));
                }
            });

            Location location = new Location();
            location.setLatitude(37.25861);
            location.setLongitude(127.05611);

            mCamera.startRecording(new TimerResponseListener<Integer>() {
                @Override
                public void onTimerCountChanged(int timerCount) {
                    Log.d(TAG, "onTimerCountChanged. timerCount = " + timerCount);
                }

                @Override
                public void onSucceed(Integer result) {
                    Log.d(TAG, "mCamera.startRecording onSucceed()");
                    Log.d(TAG, "availableRecordingVideoPeriod = " + result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecordingVideoBeginButton.setText(getResources().getString(R.string.camera_button_record_stop));

                            if (mIsPreviewing) {
                                startPreview();
                            }
                        }
                    });
                }

                @Override
                public void onFailed(ErrorCode code, final String message) {
                    Log.d(TAG, "mCamera.startRecording onFailed()");
                    Log.d(TAG, "code = " + code);
                    Log.d(TAG, "message = " + message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PreviewActivity.this, "Fail - " + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                            mRecordingVideoBeginButton.setText(getResources().getString(R.string.camera_button_record_start));

                            if (mIsPreviewing) {
                                startPreview();
                            }
                        }
                    });
                }
            }, location);
        } else if (getResources().getString(R.string.camera_button_record_start_cancel).equals(mRecordingVideoBeginButton.getText())) {
            mCamera.cancelStartingRecording(new ResponseListener<Void>() {
                @Override
                public void onSucceed(Void result) {
                    Log.d(TAG, "mCamera.cancelStartingRecording onSucceed()");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecordingVideoBeginButton.setText(getResources().getString(R.string.camera_button_record_start));

                            if (mIsPreviewing) {
                                startPreview();
                            }
                        }
                    });
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    Log.d(TAG, "mCamera.cancelTakingPhoto onFailed()");
                    Log.d(TAG, "code = " + code);
                    Log.d(TAG, "message = " + message);
                }
            });
        } else {
            mCamera.stopRecording(new ResponseListener<Integer>() {
                @Override
                public void onSucceed(Integer result) {
                    Log.d(TAG, "mCamera.stopRecording onSucceed()");
                    if (result != null) {
                        Log.d(TAG, "availableRecordingPeriod = " + result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecordingVideoBeginButton.setText(getResources().getString(R.string.camera_button_record_start));
                                mRecordingVideoPauseButton.setText(getResources().getString(R.string.camera_button_record_video_pause));

                                if (mIsPreviewing) {
                                    Log.d(TAG, "Preview is restarted.");
                                    startPreview();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onFailed(ErrorCode code, final String message) {
                    Log.d(TAG, "mCamera.stopRecording onFailed()");
                    Log.d(TAG, "code = " + code);
                    Log.d(TAG, "message = " + message);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Fail - " + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    public void onFileManagerClicked(View view) {
        Log.d(TAG, "File service");
        startActivity(new Intent(this, FileManagerActivity.class));
    }

    public void onDeviceSettingClicked(View view) {
        Log.d(TAG, "Setting service");
        startActivity(new Intent(this, DeviceSettingsActivity.class));
    }

    public void onCameraSettingClicked(View view) {
        Log.d(TAG, "Camera Settings");
        startActivity(new Intent(this, CameraSettingActivity.class));
    }

    //비트맵의 byte배열을 얻는다
    public byte[] getImageByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        return out.toByteArray();
    }

    //숫자를 byte형태로 바꾼다
    private byte[] getByte(int num) {
        byte[] buf = new byte[4];
        buf[0] = (byte) ((num >>> 0) & 0xFF);
        buf[1] = (byte) ((num >>> 8) & 0xFF);
        buf[2] = (byte) ((num >>> 16) & 0xFF);
        buf[3] = (byte) ((num >>> 24) & 0xFF);

        return buf;
    }


    public Bitmap getBitmap() {
        mSurfaceView.setDrawingCacheEnabled(true);
        mSurfaceView.buildDrawingCache(true);
        final Bitmap bitmap = Bitmap.createBitmap(mSurfaceView.getDrawingCache());
        Log.e("bitmap : ", bitmap.toString());
        mSurfaceView.setDrawingCacheEnabled(false);
        mSurfaceView.destroyDrawingCache();
        return bitmap;
    }




    public void connect() {
        if (false == isConnected) {
            Thread sendTread = new Thread() {
                public void run() {
                    try {
                        //소켓 생성
                        socket = new Socket(ServerIP, ServerPort);
                        //출력 스트림을 생성
                        os = new DataOutputStream(socket.getOutputStream());
                        os.write(0x99); //Server 쪽에 frame 전송에 대한 대비를 시키기 위해 Header을 붙인다.
                        os.flush();

                        while (true) {
                            //이미지를 불려온다
                            if (sendReady) {
                                os.write(size, 0, size.length);
                                os.flush();

                                //실제 데이터를 보낸다
                                os.write(data, 0, data.length);
                                os.flush();
                                drawReady = true;
                                sendReady = false;

                            }
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        isConnected = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                        isConnected = false;
                    }
                }
            };
            sendTread.start();
            isConnected = true;
        } else {
            isConnected = false;
            onDestroy();
        }
    }

    private class AudioThread extends Thread {
        private AudioTrack mAudioTrack;

        private int mSamplingRate;

        private boolean mIsWork = false;

        private ConcurrentLinkedDeque<byte[]> mSamplings;

        AudioThread(int samplingRate) {
            mSamplings = new ConcurrentLinkedDeque<>();
            mSamplingRate = samplingRate;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    mSamplingRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioTrack.getMinBufferSize(mSamplingRate,
                            AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT),
                    AudioTrack.MODE_STREAM);
            mAudioTrack.play();

            mIsWork = true;
            while (mIsWork) {
                if (mSamplings.size() > 0) {
                    byte[] sample = mSamplings.pop();
                    if (sample != null) {
                        mAudioTrack.write(sample, 0, sample.length, AudioTrack.WRITE_NON_BLOCKING);
                    }
                }
            }
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }

        void pushSample(byte[] samplingData) {
            mSamplings.push(samplingData);
        }

        void release() {
            mIsWork = false;
        }
    }


    public Bitmap yourFunction(byte[] data, int mWidth, int mHeight) {

        int[] mIntArray = new int[mWidth * mHeight];

// Decode Yuv data to integer array
        decodeYUV(mIntArray, data, mWidth, mHeight);

//Initialize the bitmap, with the replaced color
        Bitmap bmp = Bitmap.createBitmap(mIntArray, mWidth, mHeight, Bitmap.Config.ARGB_8888);
        return bmp;

    }

    /************************
     * ******YUV ****************
     * ********************/
    public void decodeYUV(int[] out, byte[] fg, int width, int height)
            throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length
                    + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length
                    + " < minimum " + sz * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = fg[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = fg[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;

                out[pixPtr++] = 0xff000000 + (B ) + (G << 8) + (R<<16);
            }
        }

    }

}

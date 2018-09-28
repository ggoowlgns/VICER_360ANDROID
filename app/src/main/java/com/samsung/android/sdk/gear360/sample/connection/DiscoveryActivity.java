package com.samsung.android.sdk.gear360.sample.connection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.samsung.android.sdk.gear360.ResponseListener;
import com.samsung.android.sdk.gear360.SGear360;
import com.samsung.android.sdk.gear360.SGear360Exception;
import com.samsung.android.sdk.gear360.device.ConnectionManager;
import com.samsung.android.sdk.gear360.device.Device;
import com.samsung.android.sdk.gear360.device.DeviceInfo;
import com.samsung.android.sdk.gear360.device.Discovery;
import com.samsung.android.sdk.gear360.sample.R;
import com.samsung.android.sdk.gear360.sample.SampleApplication;
import com.samsung.android.sdk.gear360.sample.service.PreviewActivity;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryActivity extends AppCompatActivity {

    private static final String TAG = DiscoveryActivity.class.getSimpleName();
    private static SampleApplication application;

    private final int REQUEST_PERMISSIONS = 1000;
    private final String[] DANGEROUS_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private Context mContext;

    private Button mStartDiscoveryButton;
    private Button mStopDiscoveryButton;
    private View mProgressLayout;

    private DeviceListAdapter mDeviceListViewAdapter;
    private final Discovery.DiscoveryListener mDiscoveryListener = new Discovery.DiscoveryListener() {
        @Override
        public void onAvailableDeviceChanged(List<DeviceInfo> availableDeviceList) {
            Log.d(TAG, "Available device is changed!");

            if (mDeviceListViewAdapter != null) {
                mDeviceListViewAdapter.addItemList(availableDeviceList);
                mDeviceListViewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onDiscoveryStarted() {
            Log.d(TAG, "Device discovery is started!");
        }

        @Override
        public void onDiscoveryStopped() {
            Log.d(TAG, "Device discovery is finished!");

            mStartDiscoveryButton.setVisibility(View.VISIBLE);
            mStopDiscoveryButton.setVisibility(View.GONE);
        }
    };
    private Discovery mDiscovery;
    private ConnectionManager mConnectionManager;
    private DeviceInfo mDeviceInfo;
    private Device mDevice;

    private final ConnectionManager.ConnectDeviceListener mConnectionListener = new ConnectionManager.ConnectDeviceListener() {
        @Override
        public void onConnected(final Device connectedDevice) {
            Log.d(TAG, "A device is connected!");

            mDevice = connectedDevice;
            application.setDevice(mDevice);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressLayout.setVisibility(View.GONE);
                    Toast.makeText(mContext, "A device is connected!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(DiscoveryActivity.this, PreviewActivity.class));
                }
            });
        }

        @Override
        public void onFailed(final ResponseListener.ErrorCode code, final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressLayout.setVisibility(View.GONE);
                    Toast.makeText(mContext, "A device connect failed..." + (message != null ? message : ""), Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private final View.OnClickListener mBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.button_start_discovery:
                    if (mDiscovery != null) {
                        if (mDeviceListViewAdapter != null) {
                            mDeviceListViewAdapter.clearAllItemList();
                            mDeviceListViewAdapter.notifyDataSetChanged();
                        }

                        List<DeviceInfo> pairedDevice = mDiscovery.getPairedDeviceInfoList();
                        if (pairedDevice != null) {
                            if (mDeviceListViewAdapter != null) {
                                mDeviceListViewAdapter.addItemList(pairedDevice);
                                mDeviceListViewAdapter.notifyDataSetChanged();
                            }
                        }

                        mDiscovery.stopDiscovery();
                        mDiscovery.startDiscovery(mDiscoveryListener);

                        mStartDiscoveryButton.setVisibility(View.GONE);
                        mStopDiscoveryButton.setVisibility(View.VISIBLE);
                    }

                    break;

                case R.id.button_stop_discovery:
                    if (mDiscovery != null) {
                        mDiscovery.stopDiscovery();
                        mStartDiscoveryButton.setVisibility(View.VISIBLE);
                        mStopDiscoveryButton.setVisibility(View.GONE);
                    }
                    break;

                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        mContext = getApplicationContext();
        application = SampleApplication.getApplicationInstance();

        if (checkPermissions()) {
            initSDK();
            init();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDevice != null && mDevice.isConnected()) {
            if (mConnectionManager != null) {
                mConnectionManager.disconnectDevice(new ConnectionManager.DisconnectDeviceListener() {
                    @Override
                    public void onDisconnected() {
                    }

                    @Override
                    public void onFailed(ResponseListener.ErrorCode code, String message) {
                    }
                }, mDevice);
            }
        } else {
            releaseSDK();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private void init() {
        ListView mDeviceListView = (ListView) findViewById(R.id.view_device_list);

        mStartDiscoveryButton = (Button) findViewById(R.id.button_start_discovery);
        mStartDiscoveryButton.setOnClickListener(mBtnClickListener);

        mStopDiscoveryButton = (Button) findViewById(R.id.button_stop_discovery);
        mStopDiscoveryButton.setOnClickListener(mBtnClickListener);

        mDeviceListViewAdapter = new DeviceListAdapter();

        mDeviceListView.setAdapter(mDeviceListViewAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int pos, long l) {
                mProgressLayout.setVisibility(View.VISIBLE);

                mDeviceInfo = (DeviceInfo) adapterView.getItemAtPosition(pos);

                if (mDevice != null && mDevice.isConnected()) {
                    if (!mDevice.getName().equals(mDeviceInfo.getName()) && !mDevice.getPairedAddress().equals(mDeviceInfo.getAddress())) {
                        if (mConnectionManager != null) {
                            mConnectionManager.disconnectDevice(new ConnectionManager.DisconnectDeviceListener() {
                                @Override
                                public void onDisconnected() {
                                    if (mConnectionManager != null) {
                                        mConnectionManager.connectDevice(mConnectionListener, mDeviceInfo);
                                    }
                                    Log.d(TAG, "new device request to connect");
                                }

                                @Override
                                public void onFailed(ResponseListener.ErrorCode code, String message) {
                                    Log.d(TAG, "Old Device disconnection is failed!");
                                }
                            }, mDevice);
                        }
                    } else {
                        Log.d(TAG, "Device disconnection is selected!");

                        if (mConnectionManager != null) {
                            mConnectionManager.disconnectDevice(new ConnectionManager.DisconnectDeviceListener() {
                                @Override
                                public void onDisconnected() {
                                    Log.d(TAG, "Device disconnection is success");
                                }

                                @Override
                                public void onFailed(ResponseListener.ErrorCode code, String message) {
                                    mProgressLayout.setVisibility(View.GONE);
                                    Log.d(TAG, "Device disconnection is failed!");
                                }
                            }, mDevice);
                        }
                    }
                } else {
                    if (mConnectionManager != null) {
                        mConnectionManager.connectDevice(mConnectionListener, mDeviceInfo);
                    }

                    Log.d(TAG, "Device connection is selected!");

                    if (mDiscovery != null) {
                        mDiscovery.stopDiscovery();
                    }
                }

            }
        });

        mDiscovery = ((SampleApplication) getApplication()).getGear360SDK().getDiscovery();
        mConnectionManager = ((SampleApplication) getApplication()).getGear360SDK().getConnectionManager();
        mProgressLayout = findViewById(R.id.layout_connection_waiting);
    }

    private boolean checkPermissions() {
        List<String> requestList = new ArrayList<>();
        List<String> deniedList = new ArrayList<>();

        for (String permission : DANGEROUS_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    String permissionGroupName = "";
                    try {
                        PermissionInfo permissionInfo = mContext.getPackageManager().getPermissionInfo(permission, 0);
                        permissionGroupName = permissionInfo.group;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (!deniedList.contains(permissionGroupName)) {
                        deniedList.add(permissionGroupName);
                    }
                } else {
                    requestList.add(permission);
                }
            }
        }

        if (requestList.size() > 0 || deniedList.size() > 0) {
            ActivityCompat.requestPermissions(this, requestList.toArray(new String[requestList.size()]), REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (verifyPermissions(grantResults)) {
                    initSDK();
                    init();

                    return;
                }

                String message = "Cannot launch this application.\n"
                        + "Be sure you have required permissions in [Settings > Applications > Application manager > "
                        + getApplicationInfo().loadLabel(getPackageManager())
                        + " > Permissions] to launch this application.\n";

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(android.R.string.dialog_alert_title);
                builder.setMessage(message);
                builder.setCancelable(false);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                AlertDialog mAlertDialog = builder.create();
                mAlertDialog.show();

                break;

            default:
                break;
        }
    }

    private boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initSDK() {
        SGear360 instanceSDK = ((SampleApplication) getApplication()).getGear360SDK();
        if (instanceSDK == null) {
            return;
        }

        try {
            instanceSDK.initialize(getApplicationContext());
        } catch (final SGear360Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "SDK initialized fail - " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "SDK initialized success", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void releaseSDK() {
        SGear360 instanceSDK = ((SampleApplication) getApplication()).getGear360SDK();
        if (instanceSDK == null) {
            return;
        }
        instanceSDK.release();
    }
}
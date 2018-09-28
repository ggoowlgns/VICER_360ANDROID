package com.samsung.android.sdk.gear360.sample.connection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.samsung.android.sdk.gear360.device.DeviceInfo;
import com.samsung.android.sdk.gear360.sample.R;

import java.util.ArrayList;
import java.util.List;

class DeviceListAdapter extends BaseAdapter{

    private List<DeviceInfo> mDeviceList = new ArrayList<>() ;

    public DeviceListAdapter() {}

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final Context context = viewGroup.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_device_list, viewGroup, false);
        }

        TextView deviceNameView = (TextView) convertView.findViewById(R.id.view_device_name) ;
        TextView pairedAddressView = (TextView) convertView.findViewById(R.id.view_paired_address) ;

        DeviceInfo deviceInfo = mDeviceList.get(position);

        if(deviceInfo != null) {
            deviceNameView.setText(deviceInfo.getName());
            pairedAddressView.setText(deviceInfo.getAddress());
        }
        return convertView;
    }

    public void addItemList(List<DeviceInfo> deviceInfoList) {
        for (DeviceInfo deviceInfo : deviceInfoList) {
            boolean isNewDeviceInfo = true;

            for(DeviceInfo addedDeviceInfo : mDeviceList) {
                if(addedDeviceInfo.getAddress().equals(deviceInfo.getAddress()) && addedDeviceInfo.getName().equals(deviceInfo.getName())) {
                    isNewDeviceInfo = false;

                    break;
                }
            }

            if(isNewDeviceInfo) {
                mDeviceList.add(deviceInfo);
            }
        }
    }

    public void clearAllItemList() {
        mDeviceList.clear();
    }
}

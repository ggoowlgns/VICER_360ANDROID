package com.samsung.android.sdk.gear360.sample.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.gear360.ResponseListener;
import com.samsung.android.sdk.gear360.device.FileManager;
import com.samsung.android.sdk.gear360.device.data.FileDetailInformation;
import com.samsung.android.sdk.gear360.device.data.FileInformation;
import com.samsung.android.sdk.gear360.device.data.FileListInformation;
import com.samsung.android.sdk.gear360.device.data.FileType;
import com.samsung.android.sdk.gear360.sample.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileManagerActivity extends BaseServiceActivity {

    private final String TAG = FileManagerActivity.class.getSimpleName();

    private FileManager mFileManager;
    private EditText mEditStartIndex;
    private EditText mEditRequestCount;
    private ImageView mPreviewImage;
    private FileListAdapter mArrayAdapter;
    private List<FileInformation> fileList = new ArrayList<>();
    private Button mButtonCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.file_manager);
        }

        Log.d(TAG, "onCreate FileManagerActivity!");
        setContentView(R.layout.activity_file);

        mFileManager = mApplication.getDevice().getFileManager();
        if (mFileManager == null) {
            Log.e(TAG, "no File service loaded");
            return;
        }
        initLayout();
    }

    private void initLayout() {
        Button buttonLoadFiles = (Button) findViewById(R.id.button_load);
        Button buttonDeleteAll = (Button) findViewById(R.id.button_delete_all);
        Button buttonDelete = (Button) findViewById(R.id.button_delete);
        mButtonCopy = (Button) findViewById(R.id.button_copy);
        ListView mFileListView = (ListView) findViewById(R.id.view_file_list);

        mEditStartIndex = (EditText) findViewById(R.id.edit_start_index);
        mEditRequestCount = (EditText) findViewById(R.id.edit_request_count);

        mPreviewImage = (ImageView) findViewById(R.id.view_preview_image);
        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllFileList();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> idList = new ArrayList<>();
                String id;

                FileListItem fileListItem;
                for (int i = 0; i < mArrayAdapter.getCount(); i++) {
                    fileListItem = mArrayAdapter.getItem(i);
                    if (fileListItem != null && fileListItem.isChecked()) {
                        id = fileListItem.getId();
                        idList.add(id);
                    }
                }
                deleteFileList(idList);
            }
        });

        mButtonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyFileList();
            }
        });

        buttonLoadFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditStartIndex.getText().toString().isEmpty() || mEditRequestCount.getText().toString().isEmpty()) {
                    Toast.makeText(FileManagerActivity.this, "Start index or requested count is empty", Toast.LENGTH_LONG).show();
                    return;
                }

                getFileList(Integer.parseInt(mEditStartIndex.getText().toString()), Integer.parseInt(mEditRequestCount.getText().toString()));
                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                mInputMethodManager.hideSoftInputFromWindow(mEditStartIndex.getWindowToken(), 0);
                mInputMethodManager.hideSoftInputFromWindow(mEditRequestCount.getWindowToken(), 0);
            }
        });

        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getFileDetailInformation(getFileListItem(position).getId());
            }
        });
        mFileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int index = position;
                PopupMenu popupMenu = new PopupMenu(FileManagerActivity.this, view);
                getMenuInflater().inflate(R.menu.menu_filemanager_popup, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_copy_to_phone:
                                String folder = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Gear 360/";
                                File baseDir = new File(folder);
                                if (!baseDir.isDirectory()) {
                                    if (baseDir.mkdirs()) {
                                        Log.d(TAG, "Directory created");
                                    } else {
                                        Log.d(TAG, "Directory create failed");
                                    }
                                }
                                String fileName = getFileListItem(index).getFileTitle()
                                        + (getFileListItem(index).getFileType().equals(FileType.IMAGE) ? ".jpg" : ".mp4");
                                copyFile(getFileListItem(index).getId(), folder + fileName, false);
                                break;

                            case R.id.menu_preview_video:
                                final String id = getFileListItem(index).getId();
                                mFileManager.getFileDetailInformation(new ResponseListener<FileDetailInformation>() {
                                    @Override
                                    public void onSucceed(final FileDetailInformation result) {
                                        Intent intent = new Intent(FileManagerActivity.this, VideoPlayerActivity.class);
                                        intent.putExtra("ID", id);
                                        intent.putExtra("DURATION", result.getDuration());
                                        intent.putExtra("RESOLUTION", result.getResolution());
                                        intent.putExtra("NAME", result.getTitle());
                                        intent.putExtra("FRAME_RATE", result.getFrameRate());
                                        intent.putExtra("TIME", result.getTakenTimeUtc());
                                        FileManagerActivity.this.startActivity(intent);
                                    }

                                    @Override
                                    public void onFailed(ErrorCode code, String message) {
                                        Log.d(TAG, "file delete request failed : " + code.toString() + " ,message : " + message);
                                    }
                                }, id);
                                break;

                            case R.id.menu_preview_image:
                                getImage(getFileListItem(index).getId());
                                break;

                            default:
                                break;
                        }
                        return false;
                    }
                });
                if (getFileListItem(index).getFileType().equals(FileType.IMAGE)) {
                    popupMenu.getMenu().findItem(R.id.menu_preview_video).setEnabled(false);
                } else {
                    popupMenu.getMenu().findItem(R.id.menu_preview_image).setEnabled(false);
                }
                popupMenu.show();
                return false;
            }

        });

        mArrayAdapter = new FileListAdapter(this, R.layout.item_file_list);
        mFileListView.setAdapter(mArrayAdapter);
    }

    /******************************** Start SDK API Call ********************************/
    private void deleteFileList(final List<String> idList) {
        showProgressDialog(getString(R.string.delete));
        Log.d(TAG, "deleteFileList mId : " + idList.toString());
        mFileManager.deleteFileList(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileListItem fileListItem;
                        List<FileListItem> fileListItems = new ArrayList<>();
                        for (int i = 0; i < mArrayAdapter.getCount(); i++) {
                            fileListItem = mArrayAdapter.getItem(i);
                            if (fileListItem != null && !fileListItem.isChecked()) {
                                fileListItems.add(fileListItem);
                            }
                        }
                        mArrayAdapter.clear();
                        mArrayAdapter.addAll(fileListItems);
                        mArrayAdapter.notifyDataSetChanged();
                        hideProgressDialog();
                        Toast.makeText(FileManagerActivity.this, "Success to delete", Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onFailed(final ErrorCode code, final String message) {
                Log.e(TAG, "file delete request failed : " + code.toString() + " ,message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Fail to delete", Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                });
            }
        }, idList);
    }

    private void copyFileList() {
        String folder = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Gear 360/";
        File baseDir = new File(folder);
        if (!baseDir.isDirectory()) {
            if (baseDir.mkdirs()) {
                Log.d(TAG, "Directory created");
            } else {
                Log.e(TAG, "Directory create failed");
                return;
            }
        }

        String id;
        String destination;
        FileListItem fileListItem;
        for (int i = 0; i < mArrayAdapter.getCount(); i++) {
            fileListItem = mArrayAdapter.getItem(i);
            if (fileListItem != null && fileListItem.isChecked()) {
                id = fileListItem.getId();
                final String requestedId = id;

                destination = folder + fileListItem.getFileTitle()
                        + (fileListItem.getFileType().equals(FileType.IMAGE) ? ".jpg" : ".mp4");

                Log.d(TAG, "(copyFileList) id : " + id + " , destination : " + destination);
                CopyTask copyTask = new CopyTask(new FileManager.CopyResponseListener<Void>() {
                    @Override
                    public void onProgressUpdated(final int percent) {
                        Log.i(TAG, "CopyProgress  ( " + requestedId + " ) = " + percent + "(%)");
                    }

                    @Override
                    public void onSucceed(Void result) {
                        Log.i(TAG, "CopyProgress  onSucceed ( " + requestedId + " ) ");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FileManagerActivity.this, "CopyProgress  onSucceed ( " + requestedId + " ) ", Toast.LENGTH_LONG).show();
                                mButtonCopy.setText(getString(R.string.copy_file));
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, String message) {
                        Log.e(TAG, "file delete request failed : " + code.toString() + " ,message : " + message);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mButtonCopy.setText(getString(R.string.copy_file));
                                Toast.makeText(FileManagerActivity.this, "Fail to copy", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }, id, destination, false);
                copyTask.execute();
            }
        }
    }

    private void deleteAllFileList() {
        showProgressDialog(getString(R.string.delete));
        mFileManager.deleteAllFileList(new ResponseListener<Void>() {
            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Success to delete", Toast.LENGTH_LONG).show();
                        fileList.clear();
                        mArrayAdapter.clear();
                        mArrayAdapter.notifyDataSetChanged();
                        hideProgressDialog();
                    }
                });
            }

            @Override
            public void onFailed(final ErrorCode code, final String message) {
                Log.e(TAG, "file delete request failed : " + code.toString() + " ,message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Fail to delete", Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                });
            }
        });
    }

    private void getFileList(final int startIndex, final int requestCount) {
        showProgressDialog(getString(R.string.load_files));
        mArrayAdapter.clear();
        mFileManager.getFileList(new ResponseListener<FileListInformation>() {
            @Override
            public void onSucceed(FileListInformation result) {
                fileList = result.getFileInformationList();
                if (result.getReturnedCount() > 0) {
                    for (final FileInformation item : fileList) {
                        final FileListItem fileListItem = new FileListItem(item.getId(), item.getTitle(), item.getType());
                        ThumbnailTask thumbnailTask = new ThumbnailTask(new ResponseListener<byte[]>() {
                            @Override
                            public void onSucceed(final byte[] result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result != null) {
                                            Log.d(TAG, "getFileList mId : " + fileListItem.getId() + ", success to get Thumbnail ");
                                            fileListItem.setThumbnail(result);
                                        } else {
                                            Log.e(TAG, "mThumbnail returns null");
                                        }
                                        if (fileList.get((fileList.size() - 1)).getId().equals(item.getId())) {
                                            hideProgressDialog();
                                        }
                                        mArrayAdapter.add(fileListItem);
                                        mArrayAdapter.notifyDataSetChanged();
                                    }
                                });
                            }

                            @Override
                            public void onFailed(ErrorCode code, String message) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (fileList.get((fileList.size() - 1)).getId().equals(item.getId())) {
                                            hideProgressDialog();
                                        }
                                    }
                                });
                                Log.e(TAG, "Error : " + code + ", message : " + message);
                            }
                        }, item.getId());
                        thumbnailTask.execute();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FileManagerActivity.this, "No file exists.", Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        }
                    });
                }
            }

            @Override
            public void onFailed(final ErrorCode code, final String message) {
                Log.e(TAG, "Error : " + code + ", message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Fail to get files information", Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                });
            }
        }, startIndex, requestCount);
    }

    private void copyFile(String id, final String destinationPath, boolean horizontalCorrection) {
        showProgressDialog(getString(R.string.copy_file));
        mFileManager.copyFile(new FileManager.CopyResponseListener<Void>() {
            @Override
            public void onProgressUpdated(int percent) {
                Log.d(TAG, " Progress :  " + percent);
            }

            @Override
            public void onSucceed(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Success to copy", Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                        try {
                            Log.d(TAG, "need to trigger media scan " + destinationPath);
                            String[] paths = new String[]{destinationPath};
                            String extension = MimeTypeMap.getFileExtensionFromUrl(destinationPath.toLowerCase(Locale.getDefault()));
                            String[] mimeTypes = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)};
                            MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "file copy request failed : " + code.toString() + " ,message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Fail to copy", Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                });
            }
        }, id, destinationPath, horizontalCorrection);
    }

    private void getImage(final String id) {
        showProgressDialog(getString(R.string.preview_image));
        mFileManager.previewImage(new FileManager.LoadResponseListener<byte[]>() {
            @Override
            public void onProgressUpdated(int percent) {
                Log.i(TAG, percent + "");
            }

            @Override
            public void onSucceed(final byte[] result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InputStream inputStream = new ByteArrayInputStream(result);
                        mPreviewImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                        hideProgressDialog();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "Get image request failed Error : " + code + ", message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Fail to load mThumbnail", Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                });
            }
        }, id);
    }

    private void getFileDetailInformation(String id) {
        mFileManager.getFileDetailInformation(new ResponseListener<FileDetailInformation>() {
            @Override
            public void onSucceed(final FileDetailInformation result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, result.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailed(ErrorCode code, String message) {
                Log.e(TAG, "get file detail request failed : " + code.toString() + " ,message : " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileManagerActivity.this, "Fail to get FileInformation", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, id);
    }

    /******************************** End SDK API Call ********************************/
    private String getListItemName(FileListItem fileListItem) {
        String fileType = " ( " + fileListItem.getFileType().toString() + " )";
        return (fileListItem.getFileTitle() + fileType);
    }

    private FileListItem getFileListItem(int position) {
        return mArrayAdapter.getItem(position);
    }

    class FileListItem {
        private String mFileTitle;
        private String mId = "";
        private byte[] mThumbnail;
        private FileType mFileType;
        private boolean mIsChecked = false;

        FileListItem(String id, String fileTitle, FileType fileType) {
            this.mFileTitle = fileTitle;
            this.mId = id;
            this.mFileType = fileType;
        }

        String getFileTitle() {
            return mFileTitle;
        }

        byte[] getThumbnail() {
            return mThumbnail;
        }

        void setThumbnail(byte[] thumbnail) {
            this.mThumbnail = thumbnail;
        }

        String getId() {
            return mId;
        }

        FileType getFileType() {
            return mFileType;
        }

        boolean isChecked() {
            return mIsChecked;
        }

        void setChecked(boolean checked) {
            mIsChecked = checked;
        }
    }

    private class FileListAdapter extends ArrayAdapter<FileListItem> {
        FileListAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.item_file_list, parent, false);
            }

            final FileListItem fileListItem = getItem(position);
            if (fileListItem != null) {
                TextView title = (TextView) view.findViewById(R.id.view_title);
                ImageView thumbnail = (ImageView) view.findViewById(R.id.view_thumbnail);
                final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_file);

                if (fileListItem.getThumbnail() != null && fileListItem.getThumbnail().length > 0) {
                    InputStream inputStream = new ByteArrayInputStream(fileListItem.getThumbnail());
                    thumbnail.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                }

                Log.d(TAG, "getView  title : " + getListItemName(fileListItem));
                title.setText(getListItemName(fileListItem));
                if (fileListItem.isChecked()) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "checkBox.mIsChecked() : " + checkBox.isChecked());
                        if (checkBox.isChecked()) {
                            fileListItem.setChecked(true);
                        } else {
                            fileListItem.setChecked(false);
                        }
                    }
                });
            } else {
                Log.e(TAG, "selected file item is null");
            }
            return view;
        }
    }

    private class ThumbnailTask extends AsyncTask<Void, Void, Void> {
        final private Object mObject = new Object();
        private ResponseListener<byte[]> mListener;
        private String mId;

        ThumbnailTask(ResponseListener<byte[]> listener, String id) {
            this.mListener = listener;
            this.mId = id;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "ThumbnailTask  mId: " + mId);
            mFileManager.getThumbnail(new ResponseListener<byte[]>() {
                @Override
                public void onSucceed(byte[] result) {
                    Log.d(TAG, "onSuccess");
                    synchronized (mObject) {
                        if (mListener != null) {
                            mListener.onSucceed(result);
                        }
                        mObject.notify();
                    }
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    Log.e(TAG, "onFailed : " + "code : " + code.toString() + " , message : " + message);
                    synchronized (mObject) {
                        if (mListener != null) {
                            mListener.onFailed(code, message);
                        }
                        mObject.notify();
                    }
                }
            }, mId);

            try {
                synchronized (mObject) {
                    mObject.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class CopyTask extends AsyncTask<Void, Void, Void> {
        final private Object mObject = new Object();
        private FileManager.CopyResponseListener<Void> mListener;
        private String mId;
        private String mDestination;
        private boolean mTiltCorrection;

        CopyTask(FileManager.CopyResponseListener<Void> listener, String id, String destination, boolean tiltCorrection) {
            this.mListener = listener;
            this.mId = id;
            this.mDestination = destination;
            this.mTiltCorrection = tiltCorrection;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mFileManager.copyFile(new FileManager.CopyResponseListener<Void>() {
                @Override
                public void onProgressUpdated(int percent) {
                    if (mListener != null) {
                        mListener.onProgressUpdated(percent);
                    }
                }

                @Override
                public void onSucceed(Void result) {
                    Log.d(TAG, "onSuccess");
                    synchronized (mObject) {
                        if (mListener != null) {
                            mListener.onSucceed(null);
                        }
                        mObject.notify();
                    }
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    Log.e(TAG, "onFailed : " + "code : " + code.toString() + " , message : " + message);
                    synchronized (mObject) {
                        if (mListener != null) {
                            mListener.onFailed(code, message);
                        }
                        mObject.notify();
                    }
                }
            }, mId, mDestination, mTiltCorrection);

            try {
                synchronized (mObject) {
                    mObject.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
package com.samsung.android.sdk.gear360.sample.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * Created by JiHoon on 2018-08-01.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public MySurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        // TODO Auto-generated constructor stub
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        // TODO Auto-generated constructor stub
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    public Bitmap getBitmap() {
       // super.getBitmap();
        setDrawingCacheEnabled(true);
        buildDrawingCache(true);
        final Bitmap bitmap = Bitmap.createBitmap(getDrawingCache());
        setDrawingCacheEnabled(false);
        destroyDrawingCache();
        return bitmap;
    }

    public Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (view instanceof SurfaceView) {
            SurfaceView surfaceView = (SurfaceView) view;
            surfaceView.setZOrderOnTop(true);
            surfaceView.draw(canvas);
            surfaceView.setZOrderOnTop(false);
            return bitmap;
        } else {
            //For ViewGroup & View
            view.draw(canvas);
            return bitmap;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public Bitmap drawBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        surfaceDestroyed(null); //Thread 잠시 멈춤(pause)
        onDraw(canvas);
        surfaceCreated(null); //Thread 재개(resume)

        return bitmap;
    }




}

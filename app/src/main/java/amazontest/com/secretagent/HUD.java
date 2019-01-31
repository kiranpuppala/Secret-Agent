package amazontest.com.secretagent;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class HUD extends Service {
    View mView;
    private android.hardware.Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private CameraPreview mPreview;
    private boolean isRecording = false;
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());

        mView = layoutInflater.inflate(R.layout.activity_main, null);
        mCamera = android.hardware.Camera.open();
        mPreview = new CameraPreview(getBaseContext(), mCamera);
        FrameLayout preview = mView.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                0,
                PixelFormat.TRANSLUCENT);
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Load Average");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mView, params);


        Runnable startRecTask = new Runnable() {
            @Override
            public void run() {
                startRecording();
            }
        };
        mainThreadHandler.postDelayed(startRecTask, 3000);


        Runnable delayedTask = new Runnable() {
            @Override
            public void run() {
                stopRecording();
            }
        };
        mainThreadHandler.postDelayed(delayedTask, 10000);
    }

    void startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            isRecording = true;
        } else {
            releaseMediaRecorder();
        }
    }

    void stopRecording() {
        if (isRecording) {
            mMediaRecorder.stop();
            releaseMediaRecorder();
            mCamera.lock();
            isRecording = false;
            releaseMediaRecorder();
            releaseCamera();
            this.stopSelf();
        }
    }

    private boolean prepareVideoRecorder() {

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("ERR", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("ERR", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }





    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mView != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
            mView = null;
        }
    }
}



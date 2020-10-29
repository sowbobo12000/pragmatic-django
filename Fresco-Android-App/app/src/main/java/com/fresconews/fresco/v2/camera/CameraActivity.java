package com.fresconews.fresco.v2.camera;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.FrescoLocationManager;
import com.fresconews.fresco.framework.rx.RxMediaScannerConnection;
import com.fresconews.fresco.v2.mediabrowser.MediaBrowserActivity;
import com.fresconews.fresco.v2.utils.AnimationUtils;
import com.fresconews.fresco.v2.utils.DimensionUtils;
import com.fresconews.fresco.v2.utils.FileUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.MathUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.TranscodingUtils;
import com.google.android.gms.location.LocationRequest;
import com.ragnarok.rxcamera.RxCamera;
import com.ragnarok.rxcamera.RxCameraData;
import com.ragnarok.rxcamera.config.RxCameraConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.internal.Util;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = CameraActivity.class.getSimpleName();

    public static final String EXTRA_VIDEO_MODE = "EXTRA_VIDEO_MODE";
    private static final int NEXT_BUTTON_ANIM_DURATION = 300;
    private static final int REVEAL_ANIM_DURATION = 800;

    private static final int AUDIO_SAMPLING_RATE = 44100;
    private static final int AUDIO_ENCODING_BIT_RATE = 192 * 1024;
    private static final int AUDIO_CHANNELS = 2;
    private static final double ASPECT_RATIO_ERROR_MARGIN = 0.12;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    FrescoLocationManager locationManager;

    private TextureView textureView;
    private View rootLayout;
    private View bottomButtonsLayout;
    private View browseMediaLayout;
    private CircleImageView browseMediaButton;
    private ImageView browseMediaWhiteCircle;
    private ImageView galleryIconImageView;
    private TextView nextText;
    private FloatingActionButton fab;
    private ImageButton takeVideoButton;
    private ImageButton takePictureButton;
    private ImageButton flashTorchButton;
    private View videoCameraPortraitLayout;
    private View phoneImageView;

    private RxCamera camera;

    private MediaRecorder mediaRecorder;
    private Camera.Size cameraSize;
    private int videoBitRate;

    private Point displaySize;
    private boolean videoMode = false;
    private boolean recording = false;
    private String videoFilename = "";
    private int picturesTaken = 0;
    private int videoOrientation = 0;
    private int videoInPortraitAttempts = 0;
    private OrientationEventListener orientationListener;

    public static void start(Activity context, boolean videoMode) {
        Intent starter = new Intent(context, CameraActivity.class);
        starter.putExtra(EXTRA_VIDEO_MODE, videoMode);
        context.startActivityForResult(starter, 0);
    }

    public static void start(Activity context, boolean videoMode, View fab) {
        Intent starter = new Intent(context, CameraActivity.class);
        starter.putExtra(EXTRA_VIDEO_MODE, videoMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = context.getString(R.string.camera_fab_transition);
            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(context, fab, transitionName);
            context.startActivity(starter, transitionActivityOptions.toBundle());
        }
        else {
            context.startActivity(starter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        analyticsManager.trackScreen("Camera");

        orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            private static final int ORIENTATION_PORTRAIT = 1;
            private static final int ORIENTATION_LANDSCAPE_LEFT = 2;
            private static final int ORIENTATION_LANDSCAPE_RIGHT = 3;
            private static final int ORIENTATION_UPSIDEDOWN_PORTRAIT = 4;
            private static final int ORIENTATION_UNKNOWN = -1;
            int currentOrientation = ORIENTATION_UNKNOWN;

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation >= 315 || orientation < 45) {
                    if (currentOrientation != ORIENTATION_PORTRAIT) {
                        rotateButtons(-90.0f, 0.0f);
                        currentOrientation = ORIENTATION_PORTRAIT;
                        setCameraRotation(orientation); //fixes S7, S5, S3 and LG G2 rotating issues
                        if (videoMode && !recording) {
                            videoOrientation = 90;
                            AnimationUtils.fade(videoCameraPortraitLayout, true);
                            AnimationUtils.fade(fab, false);
                            fab.setClickable(false);
                            videoCameraPortraitLayout.setClickable(true);
                        }
                    }
                }
                else if (orientation >= 45 && orientation < 135) {
                    if (currentOrientation != ORIENTATION_LANDSCAPE_LEFT) {
                        rotateButtons(0.0f, -90.0f);
                        currentOrientation = ORIENTATION_LANDSCAPE_LEFT;
                        setCameraRotation(orientation);
                        if (videoMode) {
                            videoOrientation = 180;
                            if (!recording) {
                                AnimationUtils.fade(fab, true);
                                AnimationUtils.fade(videoCameraPortraitLayout, false);
                            }
                            fab.setClickable(true);
                            videoCameraPortraitLayout.setClickable(false);
                        }
                    }
                }
                else if (orientation >= 225 && orientation < 315) {
                    if (currentOrientation != ORIENTATION_LANDSCAPE_RIGHT) {
                        rotateButtons(0.0f, 90.0f);
                        currentOrientation = ORIENTATION_LANDSCAPE_RIGHT;
                        setCameraRotation(orientation);
                        if (videoMode) {
                            videoOrientation = 0;
                            if (!recording) {
                                AnimationUtils.fade(fab, true);
                                AnimationUtils.fade(videoCameraPortraitLayout, false);
                            }
                            fab.setClickable(true);
                            videoCameraPortraitLayout.setClickable(false);
                        }
                    }
                }
                else if (orientation >= 135 && orientation < 225) {
                    //upside down portrait
                    if (currentOrientation != ORIENTATION_UPSIDEDOWN_PORTRAIT) {
                        rotateButtons(0.0f, 180.0f);
                        currentOrientation = ORIENTATION_UPSIDEDOWN_PORTRAIT;
                        setCameraRotation(orientation);
                        if (videoMode) {
                            videoOrientation = 270;
                            if (!recording) {
                                AnimationUtils.fade(fab, true);
                                AnimationUtils.fade(videoCameraPortraitLayout, false);
                            }
                            fab.setClickable(true);
                            videoCameraPortraitLayout.setClickable(false);
                        }
                    }
                }
            }

            private void rotateButtons(float from, float to) {
                AnimationUtils.rotate(takePictureButton, from, to);
                AnimationUtils.rotate(takeVideoButton, from, to);
                AnimationUtils.rotate(flashTorchButton, from, to);
                AnimationUtils.rotate(browseMediaLayout, from, to);
            }
        };

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setFormat(PixelFormat.TRANSLUCENT);

        setContentView(R.layout.activity_camera);

        textureView = ((TextureView) findViewById(R.id.surface));
        textureView.setSurfaceTextureListener(this);
        if (textureView.isAvailable()) {
            LogUtils.i(TAG, "already avail");
            onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
        }
        else {
            LogUtils.i(TAG, "not avail");
        }

        View revealLayout = findViewById(R.id.reveal_layout);
        rootLayout = findViewById(R.id.root_layout);
        galleryIconImageView = (ImageView) findViewById(R.id.gallery_icon_image_view);
//        surfaceView = ((TextureView) findViewById(R.id.surface));
        bottomButtonsLayout = findViewById(R.id.bottom_buttons_layout);
        takePictureButton = (ImageButton) findViewById(R.id.take_picture_button);
        takeVideoButton = (ImageButton) findViewById(R.id.take_video_button);
        flashTorchButton = (ImageButton) findViewById(R.id.flash_torch_button);
        browseMediaLayout = findViewById(R.id.browse_media_layout);
        browseMediaButton = ((CircleImageView) findViewById(R.id.browse_media_button));
        browseMediaWhiteCircle = ((ImageView) findViewById(R.id.browse_media_white_circle));
        nextText = ((TextView) findViewById(R.id.next_text_view));
        fab = (FloatingActionButton) findViewById(R.id.capture_photo_fab);
        videoCameraPortraitLayout = findViewById(R.id.camera_video_portrait_layout);
        phoneImageView = findViewById(R.id.phone_image_view);

        fab.setOnClickListener(v -> capture());
        videoCameraPortraitLayout.setOnClickListener(v -> {
            phoneImageView.animate().rotationBy(-360f).setDuration(300).start();
            videoInPortraitAttempts++;
            if (videoInPortraitAttempts > 4) {
                videoInPortraitAttempts = 0;
                SnackbarUtil.dismissableSnackbar(v, R.string.error_rotate_phone_video);
            }
            analyticsManager.attemptedToUseCameraInPortrait();
        });

        displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        takePictureButton.setOnClickListener(view -> {
            if (videoMode && !recording) {
//                closeCameraAnimate();
//                camera.closeCamera();

                Intent result = new Intent();
                result.putExtra(EXTRA_VIDEO_MODE, false);
                setResult(RESULT_OK, result);
                finish();

            }
        });
        takeVideoButton.setOnClickListener(view -> {
            if (!videoMode) {
//                closeCameraAnimate();
                Intent result = new Intent();
                result.putExtra(EXTRA_VIDEO_MODE, true);
                setResult(RESULT_OK, result);
                finish();
            }
        });
        flashTorchButton.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            try{
                if (camera != null && camera.getNativeCamera() != null && camera.getNativeCamera().getParameters() != null) {
                    Camera.Parameters parameters = camera.getNativeCamera().getParameters();
                    String flashMode;
                    if (videoMode) {
                        flashMode = v.isSelected() ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF;
                    }
                    else {
                        flashMode = v.isSelected() ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF;
                    }
                    parameters.setFlashMode(flashMode);
                    camera.getNativeCamera().setParameters(parameters);
                }
            } catch(RuntimeException e){
                //from https://fabric.io/fresco-news/android/apps/com.fresconews.fresco/issues/58e936da0aeb16625b6c0020?time=last-seven-days
            }

        });

        browseMediaButton.setOnClickListener(view -> {
            if (!recording) {
                finish();
                MediaBrowserActivity.start(this);
            }
        });

        textureView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                int height = view.getHeight();
                int width = view.getWidth();

                int left = MathUtils.clamp(MathUtils.map(x - 50, 0, width, -1000, 1000), -1000, 1000);
                int top = MathUtils.clamp(MathUtils.map(y - 50, 0, height, -1000, 1000), -1000, 1000);
                int right = MathUtils.clamp(MathUtils.map(x + 50, 0, width, -1000, 1000), -1000, 1000);
                int bottom = MathUtils.clamp(MathUtils.map(y + 50, 0, height, -1000, 1000), -1000, 1000);

                Rect focusRect = new Rect(left, top, right, bottom);
                if (camera != null && camera.action() != null) {
                    camera.action().areaFocusAction(Collections.singletonList(new Camera.Area(focusRect, 1000)))
                          .onErrorReturn(throwable -> null)
                          .subscribe();
                }
            }
            return true;
        });

        setVideoMode(getIntent().getBooleanExtra(EXTRA_VIDEO_MODE, false));

        if (videoMode) {
            revealLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.fresco_red));
        }
        else {
            revealLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.fresco_yellow));
        }
        ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int cx = (fab.getLeft() + fab.getRight()) / 2;
                    int cy = (fab.getTop() + fab.getBottom()) / 2;
                    Point center = new Point(cx, cy);
                    float radius = (float) Math.hypot(Math.max(cx, rootLayout.getWidth()), Math.max(cy, rootLayout.getHeight()));
//                    AnimationUtils.circularReveal(rootLayout, center, radius, false, REVEAL_ANIM_DURATION);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    else {
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        if (locationManager.getReactiveLocationProvider() == null) {
            locationManager.setReactiveLocationProvider(new ReactiveLocationProvider(this));
            LocationRequest request = LocationRequest.create()
                                                     .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                                                     .setInterval(10000L);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.getReactiveLocationProvider()
                           .getUpdatedLocation(request)
                           .onErrorReturn(throwable -> null)
                           .subscribe();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        if (videoMode && recording) {
            stopRecording();
        }

        if (camera != null && camera.closeCamera()) {
            camera = null;
        }
        orientationListener.disable();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (camera != null && camera.closeCamera()) {
            camera = null;
        }
        analyticsManager.closedCamera();
        analyticsManager.photoCount(picturesTaken);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (camera != null && camera.closeCamera()) {
            camera = null;
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    //Sets up the rxCamera Preview size
    private void setupUI(List<Camera.Size> sizes, int widthPx, int heightPx) {
        Camera.Size maxSize = sizes.get(0);

        PercentFrameLayout.LayoutParams params = (PercentFrameLayout.LayoutParams) textureView.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
        if (videoMode) {
            info.heightPercent = 1.0f;
            info.aspectRatio = (float) heightPx / widthPx;
        }
        else {
            //max size/width refers to the camera's capture
            info.aspectRatio = (float) maxSize.height / maxSize.width; //this is the camera's AR
            //however the screen's AR is W/H. please keep that in mind.
            //we have camera width, height and ar, and we have screen w, h and ar, and display w,h,ar.
            //display width must be the same as the screen width (duh).
            //but the display height must be calculated to work with the camera's AR.
            float cameraHeight = maxSize.width; //these are reversed due to the nature of the AR differences.
            float cameraWidth = maxSize.height; //they're different because one is calculated in portrait, the other in landscape.
            float displayHeight = (cameraHeight / cameraWidth) * widthPx; //where widthPx is the width of the screen
            info.heightPercent = displayHeight / heightPx;

            //I will still have to fool around with this for 3.1.3 to fix some AR differences
//            float preferredARHeightPX = widthPx * info.aspectRatio;
//            info.heightPercent = preferredARHeightPX/heightPx;
//
            Log.i(TAG, "cam Height: " + cameraHeight +
                    "\ncam Width: " + cameraWidth +
                    "\nscreen Height: " + heightPx +
                    "\nscreen Width: " + widthPx +
//                    "\ncam aspect ratio: " + info.aspectRatio +
                    "\ndisplay Height: " + displayHeight +
                    "\nheight Percent: " + info.heightPercent);
        }
        info.widthPercent = 1.0f;
        textureView.requestLayout();

        CoordinatorLayout.LayoutParams bottomParams = (CoordinatorLayout.LayoutParams) bottomButtonsLayout.getLayoutParams();
        CoordinatorLayout.LayoutParams fabParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        if (!videoMode) {
            if (MathUtils.round(info.aspectRatio, 2) == MathUtils.round(((float) widthPx / (float) heightPx), 2)) {
                fabParams.setMargins(0, 0, 0, DimensionUtils.convertDpToPixel(10, this));
                bottomParams.setMargins(0, 0, 0, DimensionUtils.convertDpToPixel(10, this));
                takePictureButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_white));
                takeVideoButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_capture_video_white));
                flashTorchButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_flash_white));
            }
            else {
                float height = heightPx * (1 - info.aspectRatio);
                float fabHeightPx = DimensionUtils.convertDpToPixel(72, this);
                fabParams.setMargins(0, 0, 0, Math.round((height - fabHeightPx) / 2f));
                bottomParams.setMargins(0, 0, 0, Math.round((height - fabHeightPx) / 2f));
                takePictureButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera));
                takeVideoButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_capture_video));
                flashTorchButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_flash));
            }
        }
        bottomButtonsLayout.setLayoutParams(bottomParams);
        fab.setLayoutParams(fabParams);
    }

    //Sets up the actual camera's capture parameters
    private void setupCameraCaptureParameters(RxCamera rxCamera, int widthPx) {
        Camera.Parameters parameters = rxCamera.getNativeCamera().getParameters();

        PercentFrameLayout.LayoutParams params = (PercentFrameLayout.LayoutParams) textureView.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
        int width = Math.min(widthPx, Math.round(widthPx * info.aspectRatio));
        int height = Math.max(widthPx, Math.round(widthPx * info.aspectRatio));
        Point p = new Point(width, height);

        Camera.Size previewSize = getBestPreviewSize(parameters, p);
        if (previewSize != null) {
            cameraSize = previewSize;
            TranscodingUtils.setCameraHeight(cameraSize.height);
            parameters.setPreviewSize(cameraSize.width, cameraSize.height);

            if (!videoMode) {
                Camera.Size resultSize = getBestPictureSize(parameters, previewSize);
                parameters.setPictureSize(resultSize.width, resultSize.height);
            }

            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            if (supportedFlashModes != null &&
                    supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON) &&
                    supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                flashTorchButton.setVisibility(View.VISIBLE);
            }
            else {
                flashTorchButton.setVisibility(View.GONE);
            }
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (videoMode) {
                if (rxCamera.getConfig() != null) {
                    videoBitRate = TranscodingUtils.getPreferredBitrate(rxCamera.getConfig().currentCameraId, cameraSize.height, true);
                }
                else {
                    videoBitRate = TranscodingUtils.getPreferredBitrate(cameraSize.height, true);
                }
                if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
            }
            else {
                if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
            }

            rxCamera.getNativeCamera().setParameters(parameters);
        }
        else {
            // Camera seems to not work properly
            finish();
        }
    }

    private void setCameraRotation(int orientation) {
        try {
            if (camera != null && camera.getNativeCamera() != null && camera.getNativeCamera().getParameters() != null) {

                Camera.Parameters parameters = camera.getNativeCamera().getParameters();

                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, cameraInfo);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = (cameraInfo.orientation + orientation) % 360;
                parameters.setRotation(rotation);

                camera.getNativeCamera().setParameters(parameters);
            }
        }
        catch(RuntimeException e){
            //from https://fabric.io/fresco-news/android/apps/com.fresconews.fresco/issues/58e936da0aeb16625b6c0020?time=last-seven-days
        }
    }

    private void takePicture() {
        LogUtils.d(TAG, "Taking Picture");
        camera.request().takePictureRequest(true, null)
              .observeOn(Schedulers.newThread())
              .map(this::savePicture)
              .observeOn(AndroidSchedulers.mainThread())
              .map(filePath -> {
                  showNextButton(Uri.fromFile(new File(filePath)));
                  return filePath;
              })
              .observeOn(Schedulers.newThread())
              .flatMap(fileName -> RxMediaScannerConnection.scanFile(getApplicationContext(), fileName))
              .onErrorResumeNext(throwable -> {
                  LogUtils.e(TAG, "Error taking picture: ", throwable);
                  return Observable.empty();
              })
              .flatMap(this::saveLocation)
              .onErrorResumeNext(throwable -> {
                  LogUtils.e(TAG, "Could not get current location", throwable);
//                  SnackbarUtil.dismissableSnackbar(textureView, R.string.error_camera_picture_no_location);
                  return Observable.empty();
              })
              .subscribe(uri -> {
                  picturesTaken++;
              });
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);

        if (Build.MODEL.equalsIgnoreCase("Nexus 5X")) {
            mediaRecorder.setOrientationHint((videoOrientation + 180) % 360);
        }
        else {
            mediaRecorder.setOrientationHint(videoOrientation);
        }

        camera.getNativeCamera().unlock();
        mediaRecorder.setCamera(camera.getNativeCamera());

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(AUDIO_CHANNELS);
        mediaRecorder.setAudioEncodingBitRate(AUDIO_ENCODING_BIT_RATE);
        mediaRecorder.setAudioSamplingRate(AUDIO_SAMPLING_RATE);

        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoFrameRate(TranscodingUtils.FRAME_RATE);
        mediaRecorder.setVideoSize(cameraSize.width, cameraSize.height);
        mediaRecorder.setVideoEncodingBitRate(videoBitRate);

        File outFile = FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_VIDEO);
        if (outFile == null) {
            SnackbarUtil.dismissableSnackbar(textureView, getString(R.string.error_camera_video_save));
            return;
        }

        videoFilename = outFile.toString();
        mediaRecorder.setOutputFile(videoFilename);

        try {
            recording = true;
            mediaRecorder.prepare();
            mediaRecorder.start();
        }
        catch (IOException e) {
            e.printStackTrace();
            SnackbarUtil.dismissableSnackbar(textureView, R.string.error_camera_video);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            SnackbarUtil.dismissableSnackbar(textureView, R.string.error_camera_video);
        }

        Drawable progressArc = ContextCompat.getDrawable(this, R.drawable.video_progress_arc);
        AnimatorSet arcAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.arc_animator);
        arcAnimation.setTarget(progressArc);

        fab.setImageDrawable(progressArc);
        arcAnimation.start();
        arcAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (recording) {
                    stopRecording();
                }
            }
        });
    }

    private void stopRecording() {
        try {
            recording = false;

            Drawable cameraIris = ContextCompat.getDrawable(this, R.drawable.ic_camera_iris_72dp);
            fab.setImageDrawable(cameraIris);

            mediaRecorder.stop();
            mediaRecorder.reset();
            camera.getNativeCamera().lock();

            showNextButton(Uri.fromFile(new File(videoFilename)));

            RxMediaScannerConnection.scanFile(getApplicationContext(), videoFilename)
                                    .onErrorResumeNext(throwable -> {
                                        LogUtils.e(TAG, "Error taking video: ", throwable);
                                        return Observable.empty();
                                    })
                                    .flatMap(this::saveLocation)
                                    .onErrorResumeNext(throwable -> {
                                        LogUtils.e(TAG, "Could not get current location", throwable);
//                                        SnackbarUtil.dismissableSnackbar(textureView, getString(R.string.error_camera_video_no_location));
                                        return Observable.empty();
                                    })
                                    .subscribe(uri -> { //todo not sure if empty observable is handled
                                        MediaPlayer mp = MediaPlayer.create(this, Uri.parse(uri.toString()));
                                        long duration = mp.getDuration() / 1000L;
                                        mp.release();
                                        analyticsManager.videoDuration(duration);
                                    });
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void capture() {
        LogUtils.i(TAG, "Trying to take picture/video");
        if (camera != null) {
            if (videoMode) {
                if (!recording) {
                    startRecording();
                }
                else {
                    stopRecording();
                }
            }
            else {
                takePicture();
            }
        }
    }

    private void showNextButton(Uri uri) {
        Glide.with(this)
             .load(uri)
             .into(browseMediaButton);
        browseMediaWhiteCircle.setAlpha(0.65f);
        galleryIconImageView.setVisibility(View.GONE);
        nextText.setVisibility(View.VISIBLE);

        Animation in = new AlphaAnimation(0, 1);
        in.setDuration(NEXT_BUTTON_ANIM_DURATION);

        browseMediaWhiteCircle.setAnimation(in);
        nextText.setAnimation(in);
    }

    private Observable<Uri> saveLocation(Uri uri) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Observable.just(null);
        }
        return locationManager.getReactiveLocationProvider()
                              .getLastKnownLocation()
                              .defaultIfEmpty(null)
                              .map(location -> {
                                  if (location == null) {
                                      throw new RuntimeException("Error getting current location");
                                  }

                                  LogUtils.i(TAG, "Updating location: " + location.getLatitude() + " " + location.getLongitude());
                                  ContentValues geocode = new ContentValues(2);
                                  geocode.put(MediaStore.Video.VideoColumns.LATITUDE, location.getLatitude());
                                  geocode.put(MediaStore.Video.VideoColumns.LONGITUDE, location.getLongitude());

                                  getContentResolver().update(uri, geocode, null, null);
                                  return uri;
                              });
    }

    private String savePicture(RxCameraData cameraData) {
        FileOutputStream os = null;
        File originalFile = FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_IMAGE);
        if (originalFile == null) {
            LogUtils.e(TAG, "Error saving picture");
            return null;
        }

        try {
            LogUtils.i(TAG, "Saving Picture");
            os = new FileOutputStream(originalFile);
            os.write(cameraData.cameraData, 0, cameraData.cameraData.length);
            String originalPath = originalFile.getAbsolutePath();
            os.flush();

            //Commenting out at current moment in time to get hands on S3 device. This code was being triggered all the time.
            //Serious out of memory issues, but "corrected" the images rotating on select devices. Not a good call.
//            Bitmap rotated = BitmapUtils.rotateBitmapIfNecessary(originalPath, cameraData);
//            //android:largeHeap="true" //in Manifiest <application ... >
//            os.flush();
//
//            File rotatedFile = FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_IMAGE);
//            if (rotatedFile != null && rotated !=null) {
//                os = new FileOutputStream(rotatedFile);
//                rotated.compress(Bitmap.CompressFormat.JPEG, 100, os);
//                rotated.recycle(); //helps prevent crashes from trying to take multiple pictures at once
//                rotated = null;
//                return rotatedFile.getAbsolutePath();
//            }
            return originalPath;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Util.closeQuietly(os);
        }

        return null;
    }

    public void setVideoMode(boolean videoMode) {
        this.videoMode = videoMode;
        flashTorchButton.setSelected(false);
        if (camera != null && camera.getNativeCamera() != null && camera.getNativeCamera().getParameters() != null) {
            Camera.Parameters parameters = camera.getNativeCamera().getParameters();
            parameters.setRecordingHint(this.videoMode);
            camera.getNativeCamera().setParameters(parameters);
        }

        if (this.videoMode) {
            videoCameraPortraitLayout.setVisibility(View.VISIBLE);
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.fresco_red)));
            takeVideoButton.setAlpha(1.0f);
            takePictureButton.setAlpha(0.7f);
            flashTorchButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_flashlight));
        }
        else {
            videoCameraPortraitLayout.setVisibility(View.GONE);
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.fresco_yellow)));
            takeVideoButton.setAlpha(0.7f);
            takePictureButton.setAlpha(1.0f);
            flashTorchButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_flash));
        }
    }

    private Camera.Size getBestSize(List<Camera.Size> supportedSizes, Point currentSize, float optimalRatio, int limitWidth, double ratioError) {
        if (supportedSizes != null) {
            for (Camera.Size size : supportedSizes) {
                if ((size.height <= currentSize.x && size.height <= limitWidth) || size.width <= currentSize.y) {
                    double camRatio = (double) size.height / (double) size.width;
                    if (Math.abs(optimalRatio - camRatio) <= ratioError) {
                        return size;
                    }
                }
            }
            if (!supportedSizes.isEmpty()) {
                return supportedSizes.get(0);
            }
        }
        return null;
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters, Point currentSize) {
        float optimalRatio = (float) currentSize.x / currentSize.y;
        List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
        return getBestSize(supportedSizes, currentSize, optimalRatio, 1080, ASPECT_RATIO_ERROR_MARGIN);
    }

    private Camera.Size getBestVideoSize(Camera.Parameters parameters, Camera.Size cameraSize) {
        float optimalRatio = (float) cameraSize.width / cameraSize.height;
        List<Camera.Size> supportedSizes = parameters.getSupportedVideoSizes();
        Point currentSize = new Point(cameraSize.width, cameraSize.height);

        return getBestSize(supportedSizes, currentSize, optimalRatio, 2600, ASPECT_RATIO_ERROR_MARGIN);
    }

    private Camera.Size getBestPictureSize(Camera.Parameters parameters, Camera.Size cameraSize) {
        float optimalRatio = (float) cameraSize.width / cameraSize.height;
        List<Camera.Size> supportedSizes = parameters.getSupportedPictureSizes();
        Point currentSize = new Point(cameraSize.width, cameraSize.height);

        return getBestSize(supportedSizes, currentSize, optimalRatio, 4000, ASPECT_RATIO_ERROR_MARGIN);
    }

    private void closeCameraAnimate() {
        Animator.AnimatorListener closeCameraAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.overlay_view).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        };
        int cx = (fab.getLeft() + fab.getRight()) / 2;
        int cy = (fab.getTop() + fab.getBottom()) / 2;
        Point center = new Point(cx, cy);
        float radius = (float) Math.hypot(Math.max(cx, rootLayout.getWidth()), Math.max(cy, rootLayout.getHeight()));
        AnimationUtils.circularReveal(rootLayout, center, radius, true, REVEAL_ANIM_DURATION, closeCameraAnimatorListener);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogUtils.i(TAG, "attached to window");
        if (textureView != null && textureView.isAvailable()) {
            LogUtils.i(TAG, "already avail");
            onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
        }
        else {
            LogUtils.i(TAG, "not avail");
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        LogUtils.i(TAG, "surface avail");
//        onResume();
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        Point screenSize = new Point(outMetrics.widthPixels, outMetrics.heightPixels);

        RxCameraConfig config = new RxCameraConfig.Builder().useBackCamera()
                                                            .setAutoFocus(true)
                                                            .setPreferPreviewFrameRate(15, 30)
                                                            .setPreferPreviewSize(displaySize, true)
                                                            .setHandleSurfaceEvent(true)
                                                            .build();

        RxCamera.open(this, config)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> {
                    LogUtils.e(TAG, "1 - " + throwable.getMessage());
                    return null;
                })
                .flatMap(rxCamera -> {
                    if (rxCamera == null) {
                        return null;
                    }
                    List<Camera.Size> sizes = rxCamera.getNativeCamera().getParameters().getSupportedPictureSizes();
                    setupUI(sizes, screenSize.x, screenSize.y);
                    return rxCamera.bindTexture(textureView);
                })
                .onErrorReturn(throwable -> {
                    LogUtils.e(TAG, "2 - " + throwable.getMessage());
                    return null;
                })
                .flatMap(rxCamera -> {
                    if (rxCamera == null) {
                        return null;
                    }
                    orientationListener.enable();
                    setupCameraCaptureParameters(rxCamera, screenSize.x);
                    return rxCamera.startPreview();
                })
                .onErrorReturn(throwable -> null)
                .subscribe(rxCamera -> {
                    LogUtils.i(TAG, "subscribed to open RxCam");
                    if (rxCamera != null) {
                        camera = rxCamera;
                    }
                    else {
                        finish();
                    }
                });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}

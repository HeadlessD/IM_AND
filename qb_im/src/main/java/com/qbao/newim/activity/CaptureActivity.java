package com.qbao.newim.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.android.AmbientLightManager;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.DecodeFormatManager;
import com.google.zxing.client.android.DecodeHintManager;
import com.google.zxing.client.android.InactivityTimer;
import com.google.zxing.client.android.IntentSource;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.ViewfinderView;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.qbao.newim.business.ApiRequest;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.helper.Base64;
import com.qbao.newim.model.CouponsInfo;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.ShowUtils;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.dialog.Effectstype;
import com.qbao.newim.views.dialog.NiftyDialogBuilder;
import com.qbao.newim.views.imgpicker.NIM_PhotoPickerAct;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final int REQUEST_ALBUM = 999;
    private static final int DEFAULT_SCAN_WIDTH = 220;
    private static final int DEFAULT_SCAN_HEIGHT = 220;

    private ViewfinderView viewfinderView;

    private CameraManager cameraManager;
    private CaptureActivityHandler captureActivityHandler;

    private boolean hasSurface;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private DisplayMetrics mDisplayMetrics;
    private Uri qrCodeUri;
    private TextView tvTitle;
    private ImageView ivBack;
    private TextView tvRight;
    private boolean hasPermission;

    private final static int START_CHATTING = 300;
    public Dialog progressDialog;

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return captureActivityHandler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public static final void startScanQR(Context context) {
        startScan(context, Collections.singleton(BarcodeFormat.QR_CODE.name()));
    }

    public static final void startScan(Context context, Collection<String> desiredBarcodeFormats) {
        Intent intentScan = new Intent(Intents.Scan.ACTION);
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);

        if (desiredBarcodeFormats != null) {
            StringBuilder joinedByComma = new StringBuilder();
            for (String format : desiredBarcodeFormats) {
                if (joinedByComma.length() > 0) {
                    joinedByComma.append(',');
                }
                joinedByComma.append(format);
            }
            intentScan.putExtra(Intents.Scan.FORMATS, joinedByComma.toString());
        }

        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        context.startActivity(intentScan);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_scan_qrcode);
        initViews();
        initEvents();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AndPermission.hasPermission(this, (Manifest.permission.CAMERA))){
            initResume();
        }
    }

    @Override
    protected void onPause() {
        if (hasPermission) {
            if (captureActivityHandler != null) {
                captureActivityHandler.quitSynchronously();
                captureActivityHandler = null;
            }
            inactivityTimer.onPause();
            ambientLightManager.stop();
            cameraManager.closeDriver();
            if (!hasSurface) {
                SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.removeCallback(this);
            }
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ALBUM:
                    if (data != null) {
                        ArrayList<String> imgList = NIM_PhotoPickerAct.getSelectedImages(data);
                        getPhoto(imgList.get(0));
                    }
                    break;
                case START_CHATTING:
                    finish();
            }
        }
    }

    private void getPhoto(final String url) {
        showPreDialog("正在识别");
        Glide.with(CaptureActivity.this).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
               @Override
               public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                   handlerPhoto(resource);
               }
           }
        );
    }

    private void handlerPhoto(final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    QRCodeReader reader = new QRCodeReader();
                    Hashtable<DecodeHintType, String> hints = new Hashtable<>();
                    hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
                    final Result rawResult = reader.decode(binaryBitmap, hints);
                    final ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(CaptureActivity.this, rawResult);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleDecodeInternally(rawResult, resultHandler, bitmap);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShowUtils.showToast(CaptureActivity.this, "未发现二维码");
                        }
                    });
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hidePreDialog();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }

        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);

        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            beepManager.playBeepSoundAndVibrate();
        }

        handleDecodeInternally(rawResult, resultHandler, barcode);
    }

    private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        String content = resultHandler.getDisplayContents().toString();
        String aa[] = content.split("=");
        if (aa[0].contains("group")) {
            String bb[] = aa[1].split("_");
            long group_id = Long.parseLong(bb[0]);
            long user_id = Long.parseLong(bb[1]);
            NIMStartActivityUtil.startToScanGroupActivity(this, group_id, user_id);
            return;
        }

        if (aa[0].contains("user")) {
            long user_id = Long.parseLong(aa[1]);
            NIMStartActivityUtil.startToNIMUserActivity(this, user_id, FriendTypeDef.FRIEND_SOURCE_TYPE.QRCODE);
            return;
        }

        try {
            JSONObject json = new JSONObject();

            Uri uri = Uri.parse(content);
            if (content.startsWith(getString(R.string.coupons_match)) ||
                    content.startsWith(getString(R.string.coupons_match_https))) {
                showPreDialog("");
                String url = content.replace("http://", "https://");
                Call<ResponseBody> call = ApiRequest.getApiQbao().sendGetRequest(url);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        hidePreDialog();
                        if (response.isSuccessful()) {
                            try {
                                String result = response.body().string();
                                JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
                                if (obj.get("responseCode").toString().equals("1000")) {
                                    String data = obj.getAsJsonArray("data").toString();
                                    CouponsInfo couponsInfo = new Gson().fromJson(data, CouponsInfo.class);
                                    if (couponsInfo != null && !TextUtils.isEmpty(couponsInfo.getUrl())) {
//										Intent i = new Intent(CaptureActivity.this, HTMLViewerActivity.class);
//										i.putExtra("url", couponsInfo.getUrl());
//										startActivity(i);
                                        return;
                                    } else {
                                        restartPreviewAfterDelay(500);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                return;
            } else if ("store.qbao.com".equals(uri.getHost())) {
//				QBaoAppStoreActivity.startActivity(mContext, content);
                return;
            } else if ("o2o.qbao.com".equals(uri.getHost())) {
                return;
            } else if (content.matches("^https?://(im|imuat|impaiuat|imtest|mpuat).(qbao|qianbao666).com/api/r/.*")) {
                String lastPathSegment = uri.getLastPathSegment();
                if (lastPathSegment != null) {
                    lastPathSegment = new String(Base64.decode(lastPathSegment));
                } else {
                    lastPathSegment = "";
                }

                String[] data = lastPathSegment.split("/");
//				JSONObject json = new JSONObject();
                if (data != null && data.length == 2) {
                    if ("user".equals(data[0])) {
                        json.put("userId", data[1]);
                    } else if ("group".equals(data[0])) {
                        json.put("groupId", data[1]);
                    } else if ("ware".equals(data[0])) {
                        json.put("ware", data[1]);
                    } else if ("seller".equals(data[0])) {
                        json.put("seller", data[1]);
                    } else if ("pub".equals(data[0])) {
                        json.put("pub", data[1]);
                    } else if ("contactUser".equals(data[0])) {
                        json.put("contactUser", data[1]);
                    } else if ("contactPub".equals(data[0])) {
                        json.put("contactPub", data[1]);
                    }
                }
                if (data != null && data.length == 6) {
                    json = new JSONObject();
                    if ("ware".equals(data[0])) {
                        json.put("ware", data[1]);
                        if ("sourceType".equals(data[2])) {
                            json.put("sourceType", data[3]);
                        }
                        if ("promoter".equals(data[4])) {
                            json.put("promoter", data[5]);
                        }
                    }
                }
            }
//			if (QianbaoShare.isNewLink(content)) {
//				String user = uri.getQueryParameter("user");
//				if (!TextUtils.isEmpty(user)) {
//					json.put("userId", user);
//				}
//				String group = uri.getQueryParameter("group");
//				if (!TextUtils.isEmpty(group)) {
//					json.put("groupId", group);
//				}
//			}


            String accountId = json.optString("userId");
            if (!TextUtils.isEmpty(accountId)) {
                Intent intent = new Intent(CaptureActivity.this, NIMUserInfoActivity.class);
                intent.putExtra("user_id", Long.parseLong(accountId));
                intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.QRCODE);
                startActivityForResult(intent, START_CHATTING);
                return;
            }

            accountId = json.optString("seller");
            if (!TextUtils.isEmpty(accountId)) {
                Intent intent = new Intent(CaptureActivity.this, NIMUserInfoActivity.class);
                intent.putExtra("user_id", Long.parseLong(accountId));
                intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.QRCODE);
                startActivityForResult(intent, START_CHATTING);
                return;
            }

            accountId = json.optString("groupId");
            if (!TextUtils.isEmpty(accountId)) {
//				Intent intent = new Intent(mContext, JoinGroupChatgActivity.class);
//				intent.putExtra("GROUP_ID", accountId);
//				startActivityForResult(intent, FriendsActivity.START_CHATING);
//				finish();
                return;
            }
            String ware = json.optString("ware");
            if (!TextUtils.isEmpty(ware)) {
                if ("add".equalsIgnoreCase(ware)) {
                    return;
                } else if (ware.matches("\\d+")) {
                }
            }

            String pub = json.optString("pub");
            if (!TextUtils.isEmpty(pub)) {
                return;
            }

            String contactPub = json.optString("contactPub");
            if (!TextUtils.isEmpty(contactPub)) {
                return;
            }
            String contactUser = json.optString("contactUser");
            if (!TextUtils.isEmpty(contactUser)) {
                return;
            }

//			if (resultHandler.getType() == ParsedResultType.URI) {
            if (content.matches("^https?://.+")) {
                String host = uri.getHost();
                String path = uri.getPath();
                if (host != null && (host.endsWith("qbao.com") || host.endsWith("qianbao666.com") || host.endsWith("qbcdn.com"))) {
                } else {
                }
                return;
            } else {
                showResult("该二维码无法识别");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            restartPreviewAfterDelay(500);
        }

//		restartPreviewAfterDelay(2000L);
    }

    private void showResult(String msg) {
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
        dialogBuilder
                .withTitle("提示")                                  //.withTitle(null)  no title
                .withTitleColor("#333333")                                  //def
                .withDividerColor("#10000000")                              //def
                .withMessage(msg)                     //.withMessage(null)  no Msg
                .withMessageColor("#333333")                              //def  | withMessageColor(int resid)
                .withDialogColor("#FFFFFFFF")                               //def  | withDialogColor(int resid)                               //def
                .isCancelableOnTouchOutside(false)                           //def    | isCancelable(true)
                .withDuration(400)                                          //def
                .withEffect(Effectstype.SlideBottom)                                         //def Effectstype.Slidetop
                .withButton2Text("确定")
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                    }
                })
                .show();
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartPreviewAfterDelay(500);
            }
        });
    }


    private void handleDecodeInternally(String content) {
        try {
            JSONObject json = new JSONObject(content);

            String accountId = json.optString("userId");
            if (!TextUtils.isEmpty(accountId)) {
                Intent intent = new Intent(CaptureActivity.this, NIMUserInfoActivity.class);
                intent.putExtra("user_id", Long.parseLong(accountId));
                intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.QRCODE);
                startActivityForResult(intent, START_CHATTING);
                finish();
                return;
            }

            accountId = json.optString("groupId");
            if (!TextUtils.isEmpty(accountId)) {
//				Intent intent = new Intent(mContext, GroupScanResultActivity.class);
//				intent.putExtra("GROUP_ID", accountId);
//				startActivity(intent);
//				finish();
                return;
            }

            Toast.makeText(CaptureActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

//		restartPreviewAfterDelay(2000L);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG,
                    "initCamera() while already open -- late SurfaceView callback?");
            return;
        }

        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (captureActivityHandler == null) {
                captureActivityHandler = new CaptureActivityHandler(CaptureActivity.this, decodeFormats,
                        decodeHints, characterSet, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        ProgressDialog.showCustomDialog(this, getString(R.string.msg_camera_framework_bug), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (captureActivityHandler != null) {
            captureActivityHandler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        viewfinderView.setVisibility(View.VISIBLE);
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initResume() {
        cameraManager = new CameraManager(getApplication());
        viewfinderView.setCameraManager(cameraManager);

        captureActivityHandler = null;

        resetStatusView();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        inactivityTimer.onResume();

        Intent intent = getIntent();
        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;

        if (intent != null) {
            String action = intent.getAction();
            if (Intents.Scan.ACTION.equals(action)) {
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
                decodeHints = DecodeHintManager.parseDecodeHints(intent);
            }

            if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                if (width > 0 && height > 0) {
                    cameraManager.setManualFramingRect(width, height);
                }
            } else {
                int width = (int) (DEFAULT_SCAN_WIDTH * mDisplayMetrics.density);
                int height = (int) (DEFAULT_SCAN_HEIGHT * mDisplayMetrics.density);
                cameraManager.setManualFramingRect(width, height);
            }

            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
        }
    }

    private void handleIntent(Uri uri) {
        if (uri != null && "qbao".equals(uri.getScheme()) && "qr".equals(uri.getHost())) {
            List<String> path = uri.getPathSegments();
            if (path.size() < 2) {
                return;
            }
            String key = path.get(path.size() - 2);
            String value = path.get(path.size() - 1);
            if ("user".equals(key)) {
                handleUser(value);
            } else if ("group".equals(key)) {
                handleGroup(value);
            }
        }
    }

    private void handleUser(String userId) {
        try {
            JSONObject userObject = new JSONObject();
            userObject.put("userId", userId);
            handleDecodeInternally(userObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleGroup(String groupId) {
        try {
            JSONObject userObject = new JSONObject();
            userObject.put("groupId", groupId);
            handleDecodeInternally(userObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void initViews() {
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        ivBack = (ImageView) findViewById(R.id.scan_title_back);
        tvTitle = (TextView) findViewById(R.id.scan_title_txt);
        tvTitle.setText("扫一扫");
        tvRight = (TextView) findViewById(R.id.scan_title_right);
        tvRight.setText("相册");
    }

    protected void initEvents() {
        viewfinderView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(NIM_PhotoPickerAct.newIntent(CaptureActivity.this, null, 1, null, false),
                        REQUEST_ALBUM);
            }
        });
    }

    protected void initData() {
        mDisplayMetrics = getResources().getDisplayMetrics();
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);

        qrCodeUri = getIntent().getData();
        handleIntent(qrCodeUri);
    }

    private OnGlobalLayoutListener mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onGlobalLayout() {
            final int height = viewfinderView.getHeight();
            TextView hintView = (TextView) findViewById(R.id.txt_hint);
            if (height != 0 && viewfinderView.getBottomPosition() != 0 && hintView.getVisibility() == View.GONE) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) hintView.getLayoutParams();
                params.topMargin = viewfinderView.getBottomPosition() + (int) mDisplayMetrics.density * 22;
                hintView.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewfinderView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    viewfinderView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }

        }
    };
    public void showPreDialog(String str) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = ProgressDialog.createRequestDialog(this, str, false);
        progressDialog.show();
    }

    /**
     * 对话框消失
     */
    public void hidePreDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

}

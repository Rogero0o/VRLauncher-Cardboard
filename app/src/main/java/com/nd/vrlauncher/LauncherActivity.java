package com.nd.vrlauncher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.vr.cardboard.TransitionView;
import com.google.vr.cardboard.UiLayer;
import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetView;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaEventListener;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends AppCompatActivity {

  @Bind(R.id.btn_change_left) ImageButton imageButtonLeft;
  @Bind(R.id.btn_change_right) ImageButton imageButtonRight;
  @Bind(R.id.mode_text_left) TextView mTextViewLeft;
  @Bind(R.id.mode_text_right) TextView mTextViewRight;
  @Bind(R.id.btn_setting_left) ImageButton imageSettingButtonLeft;
  @Bind(R.id.btn_setting_right) ImageButton imageSettingButtonRight;
  @Bind(R.id.btn_usercenter_left) ImageButton imageUserCenterLeft;
  @Bind(R.id.btn_usercenter_right) ImageButton imageUserCenterRight;
  @Bind(R.id.btn_player_left) ImageButton imagePlayerLeft;
  @Bind(R.id.btn_player_right) ImageButton imagePlayerRight;
  @Bind(R.id.time_text_left) TextView mTimeTextViewLeft;
  @Bind(R.id.time_text_right) TextView mTimeTextViewRight;
  @Bind(R.id.chosed_icon_left) TextView mChosedIconLeft;
  @Bind(R.id.chosed_icon_right) TextView mChosedIconRight;
  @Bind(R.id.btn_travel_left) ImageButton mTravelLeft;
  @Bind(R.id.btn_travel_right) ImageButton mTravelRight;
  @Bind(R.id.btn_edu_left) ImageButton mEduLeft;
  @Bind(R.id.btn_edu_right) ImageButton mEduRight;
  @Bind(R.id.battary_image_left) ImageView battary_left;
  @Bind(R.id.battary_image_right) ImageView battary_right;
  @Bind(R.id.battary_text_left) TextView battary_text_left;
  @Bind(R.id.battary_text_right) TextView battary_text_right;
  @Bind(R.id.wifi_image_left) ImageView wifi_image_left;
  @Bind(R.id.wifi_image_right) ImageView wifi_image_right;
  @Bind(R.id.pano_view_left) VrPanoramaView mVrPanoramaViewLeft;
  private boolean isStudyMode = true;

  private BroadcastReceiver batteryLevelRcvr, wifiIntentReceiver;
  private IntentFilter batteryLevelFilter;
  private IntentFilter wifiIntentFilter;
  private Uri fileUri;
  private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
  private ImageLoaderTask backgroundImageLoaderTask;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    int mCurrentOrientation = getResources().getConfiguration().orientation;

    if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {

      setContentView(R.layout.activity_port);
    } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {

      setContentView(R.layout.activity_launcher);

      ButterKnife.bind(this);

      setOnFoucuseChange(imageButtonLeft, imageButtonRight, "切换模式");
      setOnFoucuseChange(imageSettingButtonLeft, imageSettingButtonRight, "设置");
      setOnFoucuseChange(imageUserCenterLeft, imageUserCenterRight, "个人中心");
      setOnFoucuseChange(imagePlayerLeft, imagePlayerRight, "VR播放器");
      setOnFoucuseChange(mTravelLeft, mTravelRight, "VR旅游");
      setOnFoucuseChange(mEduLeft, mEduRight, "VR教育");

      initCardBoardView();
      handleIntent(getIntent());
      monitorBatteryState();
      initPan();
    }
  }

  public void setOnFoucuseChange(final View mView, final View rightView, final String text) {
    mView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (!isStudyMode && text.contains("VR")) {
          if (text.equals("VR旅游")) {
            mChosedIconLeft.setText("VR游戏-Dive");
            mChosedIconRight.setText("VR游戏-Dive");
          } else if (text.equals("VR播放器")) {
            mChosedIconLeft.setText("VR游戏-Sniper");
            mChosedIconRight.setText("VR游戏-Sniper");
          } else if (text.equals("VR教育")) {
            mChosedIconLeft.setText("VR游戏-Neosun");
            mChosedIconRight.setText("VR游戏-Neosun");
          }
        } else {
          mChosedIconLeft.setText(text);
          mChosedIconRight.setText(text);
        }
        if (hasFocus) {
          mView.setBackgroundColor(Color.parseColor("#77ffffff"));
          rightView.setBackgroundColor(Color.parseColor("#77ffffff"));
        } else {
          mView.setBackgroundColor(Color.parseColor("#00000000"));
          rightView.setBackgroundColor(Color.parseColor("#00000000"));
        }
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      synTime();
      mVrPanoramaViewLeft.resumeRendering();
      closeVrUiLayer();
    }
  }

  @Override protected void onPause() {
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      mVrPanoramaViewLeft.pauseRendering();
      if (btp != null && !btp.isRecycled()) {
        btp.recycle(); //回收图片所占的内存
      }
      System.gc();//提醒系统及时回收
    }
    super.onPause();
  }

  private void synTime() {
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
    GregorianCalendar ca = new GregorianCalendar();
    String time;
    if (ca.get(GregorianCalendar.AM_PM) == 0) {
      time = "上午";
    } else {
      time = "下午";
    }
    time = sdf.format(new Date()) + " " + time;
    mTimeTextViewLeft.setText(time);
    mTimeTextViewRight.setText(time);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
      return;
    }
    ButterKnife.unbind(this);
    unregisterReceiver(batteryLevelRcvr);
    unregisterReceiver(wifiIntentReceiver);
    if (mVrPanoramaViewLeft != null) {
      mVrPanoramaViewLeft.shutdown();
    }
    //if (mVrPanoramaViewRight != null) {
    //  mVrPanoramaViewRight.shutdown();
    //}
    // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
    // after the activity is destroyed unless it is explicitly cancelled.
    if (backgroundImageLoaderTask != null) {
      backgroundImageLoaderTask.cancel(true);
    }
  }

  @OnClick(R.id.btn_change_left) public void clickChangeLeft(View mView) {
    changeMode();
  }

  @OnClick(R.id.btn_change_right) public void clickChangeRight(View mView) {
    changeMode();
  }

  private void changeMode() {

    if (isStudyMode) {
      //mViewLeft.setBackgroundResource(R.drawable.fun_bg);
      //mViewRight.setBackgroundResource(R.drawable.fun_bg);
      imageButtonLeft.setImageResource(R.drawable.ic_study);
      imageButtonRight.setImageResource(R.drawable.ic_study);
      mTextViewLeft.setText(R.string.entertainment_mode);
      mTextViewRight.setText(R.string.entertainment_mode);

      mTextViewLeft.setTextColor(getResources().getColor(R.color.colorFun));
      mTextViewRight.setTextColor(getResources().getColor(R.color.colorFun));

      imagePlayerLeft.setImageResource(R.drawable.ic_game);
      imagePlayerRight.setImageResource(R.drawable.ic_game);

      mTravelLeft.setImageResource(R.drawable.ic_game1);
      mTravelRight.setImageResource(R.drawable.ic_game1);

      mEduLeft.setImageResource(R.drawable.ic_game2);
      mEduRight.setImageResource(R.drawable.ic_game2);
    } else {
      //mViewLeft.setBackgroundResource(R.drawable.study_bg);
      //mViewRight.setBackgroundResource(R.drawable.study_bg);
      imageButtonLeft.setImageResource(R.drawable.ic_fun);
      imageButtonRight.setImageResource(R.drawable.ic_fun);
      mTextViewLeft.setText(R.string.study_mode);
      mTextViewRight.setText(R.string.study_mode);

      mTextViewLeft.setTextColor(getResources().getColor(R.color.colorStudy));
      mTextViewRight.setTextColor(getResources().getColor(R.color.colorStudy));

      imagePlayerLeft.setImageResource(R.drawable.ic_player);
      imagePlayerRight.setImageResource(R.drawable.ic_player);

      mTravelLeft.setImageResource(R.drawable.ic_travel);
      mTravelRight.setImageResource(R.drawable.ic_travel);

      mEduLeft.setImageResource(R.drawable.ic_edu);
      mEduRight.setImageResource(R.drawable.ic_edu);
    }
    handleIntent(getIntent());
    isStudyMode = !isStudyMode;
  }

  @OnClick(R.id.btn_setting_left) public void clickSettingLeft(View mView) {
    startSetting();
  }

  @OnClick(R.id.btn_setting_right) public void clickSettingRight(View mView) {
    startSetting();
  }

  private void startSetting() {
    try {
      startAppByPackageName("com.nd.vrsettings");
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(LauncherActivity.this, R.string.please_install_settings, Toast.LENGTH_SHORT)
          .show();
      e.printStackTrace();
    }
  }

  @OnClick(R.id.btn_usercenter_left) public void clickUserCenterLeft(View mView) {
    startUC();
  }

  @OnClick(R.id.btn_usercenter_right) public void clickUserCenterRight(View mView) {
    startUC();
  }

  private void startUC() {
    try {
      startAppByPackageName("com.nd.vr.usercenter");
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(LauncherActivity.this, R.string.please_install_UC_, Toast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
  }

  @OnClick(R.id.btn_player_left) public void clickPlayerLeft(View mView) {
    if (isStudyMode) {
      startPlayer();
    } else {
      startGames(2);
    }
  }

  @OnClick(R.id.btn_player_right) public void clickPlayerRight(View mView) {
    if (isStudyMode) {
      startPlayer();
    } else {
      startGames(2);
    }
  }

  private void startPlayer() {
    try {
      startAppByPackageName("com.nd.EDUVR_360Player_01");
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(LauncherActivity.this, R.string.please_install_player, Toast.LENGTH_SHORT)
          .show();
      e.printStackTrace();
    }
  }

  @OnClick(R.id.btn_travel_left) public void clickTravleLeft(View mView) {
    if (isStudyMode) {
      startTravle();
    } else {
      startGames(1);
    }
  }

  @OnClick(R.id.btn_travel_right) public void clickTravleRight(View mView) {
    if (isStudyMode) {
      startTravle();
    } else {
      startGames(1);
    }
  }

  private void startTravle() {
    try {
      startAppByPackageName("com.nd.EDUVR_360Img_01");
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(LauncherActivity.this, R.string.please_install_player, Toast.LENGTH_SHORT)
          .show();
      e.printStackTrace();
    }
  }

  @OnClick(R.id.btn_edu_left) public void clickEduLeft(View mView) {
    if (isStudyMode) {
      startEdu();
    } else {
      startGames(3);
    }
  }

  @OnClick(R.id.btn_edu_right) public void clickEduRight(View mView) {
    if (isStudyMode) {
      startEdu();
    } else {
      startGames(3);
    }
  }

  private void startEdu() {
    try {
      startAppByPackageName("com.nd.EDUVR_360Movie_01");
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(LauncherActivity.this, R.string.please_install_player, Toast.LENGTH_SHORT)
          .show();
      e.printStackTrace();
    }
  }

  public void startAppByPackageName(String packageName)
      throws PackageManager.NameNotFoundException {
    PackageInfo pi = null;
    pi = getPackageManager().getPackageInfo(packageName, 0);
    Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    resolveIntent.setPackage(pi.packageName);
    List<ResolveInfo> apps = getPackageManager().queryIntentActivities(resolveIntent, 0);
    ResolveInfo ri = apps.iterator().next();
    if (ri != null) {
      String packageName1 = ri.activityInfo.packageName;
      String className = ri.activityInfo.name;
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      ComponentName cn = new ComponentName(packageName1, className);
      intent.setComponent(cn);
      startActivity(intent);
    }
  }

  public void startGames(int num) {
    try {
      switch (num) {
        case 1:
          startAppByPackageName("com.funkytokyoclub.vrrideoceancity");
          break;
        case 2:
          startAppByPackageName("com.corp.Sniper");
          break;
        case 3:
          startAppByPackageName("com.neoun.dive");
          break;
      }
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(LauncherActivity.this, R.string.please_intall_game, Toast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
  }

  private void monitorBatteryState() {
    batteryLevelRcvr = new BroadcastReceiver() {

      public void onReceive(Context context, Intent intent) {
        StringBuilder sb = new StringBuilder();
        int rawlevel = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        int status = intent.getIntExtra("status", -1);
        int health = intent.getIntExtra("health", -1);
        int level = -1; // percentage, or -1 for unknown
        if (rawlevel >= 0 && scale > 0) {
          level = (rawlevel * 100) / scale;
        }
        battary_text_left.setText(level + "%");
        battary_text_right.setText(level + "%");

        if (level >= 95) {
          battary_left.setImageResource(R.drawable.nd_sys_stat_battery_level_6);
          battary_right.setImageResource(R.drawable.nd_sys_stat_battery_level_6);
        } else if (level >= 80) {
          battary_left.setImageResource(R.drawable.nd_sys_stat_battery_level_5);
          battary_right.setImageResource(R.drawable.nd_sys_stat_battery_level_5);
        } else if (level >= 60) {
          battary_left.setImageResource(R.drawable.nd_sys_stat_battery_level_4);
          battary_right.setImageResource(R.drawable.nd_sys_stat_battery_level_4);
        } else if (level >= 40) {
          battary_left.setImageResource(R.drawable.nd_sys_stat_battery_level_3);
          battary_right.setImageResource(R.drawable.nd_sys_stat_battery_level_3);
        } else if (level >= 20) {
          battary_left.setImageResource(R.drawable.nd_sys_stat_battery_level_2);
          battary_right.setImageResource(R.drawable.nd_sys_stat_battery_level_2);
        } else if (level >= 10) {
          battary_left.setImageResource(R.drawable.nd_sys_stat_battery_level_1);
          battary_right.setImageResource(R.drawable.nd_sys_stat_battery_level_1);
        } else if (level >= 0) {
          battary_left.setImageResource(R.drawable.nd_sys_stat_battery_level_0);
          battary_right.setImageResource(R.drawable.nd_sys_stat_battery_level_0);
        }
      }
    };
    batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    wifiIntentFilter = new IntentFilter();
    wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    wifiIntentReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        int level =
            Math.abs(((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getRssi());
        wifi_image_left.setImageResource(R.drawable.wifi);
        wifi_image_right.setImageResource(R.drawable.wifi);
        if (level <= 50) {
          wifi_image_left.setImageLevel(3);
          wifi_image_right.setImageLevel(3);
        } else if (level >= 50 && level <= 70) {
          wifi_image_left.setImageLevel(2);
          wifi_image_right.setImageLevel(2);
        } else if (level >= 70) {
          wifi_image_left.setImageLevel(1);
          wifi_image_right.setImageLevel(1);
        } else {
          wifi_image_left.setImageLevel(0);
          wifi_image_right.setImageLevel(0);
        }
      }
    };
    registerReceiver(batteryLevelRcvr, batteryLevelFilter);
    registerReceiver(wifiIntentReceiver, wifiIntentFilter);
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  Bitmap btp = null;

  class ImageLoaderTask extends AsyncTask<Pair<Uri, VrPanoramaView.Options>, Void, Boolean> {

    /**
     * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
     */
    @Override protected Boolean doInBackground(
        Pair<Uri, VrPanoramaView.Options>... fileInformation) {
      VrPanoramaView.Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
      InputStream istr = null;
      if (fileInformation == null
          || fileInformation.length < 1
          || fileInformation[0] == null
          || fileInformation[0].first == null) {
        AssetManager assetManager = getAssets();
        try {
          panoOptions = new VrPanoramaView.Options();
          if (isStudyMode) {
            istr = assetManager.open("andes.jpg");
            panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
          } else {
            istr = assetManager.open("vrtest.jpg");
            panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
          }
        } catch (IOException e) {
          return false;
        }
      } else {
        try {
          istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
          panoOptions = fileInformation[0].second;
        } catch (IOException e) {
          return false;
        }
      }

      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = false;
      options.inSampleSize = 1;   //width，hight设为原来的十分一
      btp = BitmapFactory.decodeStream(istr, null, options);

      mVrPanoramaViewLeft.loadImageFromBitmap(btp, panoOptions);
      try {
        istr.close();
      } catch (IOException e) {
      }

      return true;
    }
  }

  /**
   * Listen to the important events from widget.
   */
  private class ActivityEventListener extends VrPanoramaEventListener {
    /**
     * Called by pano widget on the UI thread when it's done loading the image.
     */
    @Override public void onLoadSuccess() {
      loadImageSuccessful = true;
    }

    /**
     * Called by pano widget on the UI thread on any asynchronous error.
     */
    @Override public void onLoadError(String errorMessage) {
      loadImageSuccessful = false;
      Toast.makeText(LauncherActivity.this, "Error loading pano: " + errorMessage,
          Toast.LENGTH_LONG).show();
    }
  }

  private void initCardBoardView() {
    mVrPanoramaViewLeft.setEventListener(new ActivityEventListener());
    //mVrPanoramaViewRight.setEventListener(new ActivityEventListener());
    mVrPanoramaViewLeft.setCardboardButtonEnabled(false);
    //mVrPanoramaViewRight.setCardboardButtonEnabled(false);

    mVrPanoramaViewLeft.setFullscreenButtonEnabled(false);
    //mVrPanoramaViewRight.setFullscreenButtonEnabled(false);

    mVrPanoramaViewLeft.setInfoButtonEnabled(false);
    //mVrPanoramaViewRight.setInfoButtonEnabled(false);
  }

  private void handleIntent(Intent intent) {
    if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      fileUri = intent.getData();
      panoOptions.inputType = intent.getIntExtra("inputType", VrPanoramaView.Options.TYPE_MONO);
    } else {
      fileUri = null;
      panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
    }
    if (backgroundImageLoaderTask != null) {
      backgroundImageLoaderTask.cancel(true);
    }
    backgroundImageLoaderTask = new ImageLoaderTask();
    backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
  }

  private void initPan() {
    Class<VrWidgetView> clazz = VrWidgetView.class;
    try {
      Field fieldvr = clazz.getDeclaredField("isVrMode");
      fieldvr.setAccessible(true);
      fieldvr.set(mVrPanoramaViewLeft, true);
      fieldvr.setAccessible(false);

      Method m1 = clazz.getDeclaredMethod("updateButtonVisibility");
      m1.setAccessible(true);
      m1.invoke(mVrPanoramaViewLeft);
      m1.setAccessible(false);

      Field fieldisVrUiLayer = clazz.getDeclaredField("vrUiLayer");
      fieldisVrUiLayer.setAccessible(true);
      UiLayer mUiLayer = (UiLayer) fieldisVrUiLayer.get(mVrPanoramaViewLeft);
      mUiLayer.setTransitionViewEnabled(false);
      fieldisVrUiLayer.setAccessible(false);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void closeVrUiLayer() {
    Class<VrWidgetView> clazz = VrWidgetView.class;
    try {
      Field fieldisVrUiLayer = clazz.getDeclaredField("vrUiLayer");
      fieldisVrUiLayer.setAccessible(true);
      UiLayer mUiLayer = (UiLayer) fieldisVrUiLayer.get(mVrPanoramaViewLeft);
      mUiLayer.setTransitionViewEnabled(false);
      fieldisVrUiLayer.setAccessible(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

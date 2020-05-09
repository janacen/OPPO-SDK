package demo;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import layaair.autoupdateversion.AutoUpdateAPK;
import layaair.game.IMarket.IPlugin;
import layaair.game.IMarket.IPluginRuntimeProxy;
import layaair.game.Market.GameEngine;
import layaair.game.browser.ConchJNI;
import layaair.game.config.config;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.nearme.game.sdk.GameCenterSDK;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.opos.mobad.api.InitParams;
import com.opos.mobad.api.MobAdManager;
import com.opos.mobad.api.ad.BannerAd;
import com.opos.mobad.api.ad.InterstitialAd;
import com.opos.mobad.api.ad.NativeAd;
import com.opos.mobad.api.listener.IBannerAdListener;
import com.opos.mobad.api.listener.IInterstitialAdListener;
import com.opos.mobad.api.listener.INativeAdListener;
import com.opos.mobad.api.params.INativeAdData;
import com.opos.mobad.api.params.INativeAdFile;
import com.opos.mobad.api.params.NativeAdError;
import com.totgames.wcjz6.cxx3.nearme.gamecenter.R;


public class MainActivity extends Activity{
    public static final int AR_CHECK_UPDATE = 1;
    private IPlugin mPlugin = null;
    private IPluginRuntimeProxy mProxy = null;
    boolean isLoad=false;
    boolean isExit=false;
    public static SplashDialog mSplashDialog;

    private static MainActivity mA;
    public static MainActivity getInstance() {
        if (mA == null) {
            mA = new MainActivity();

        }
        return mA;
    }

    @Override    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //挖空屏适配
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        this.getWindow().setAttributes(lp);

        View decorView = getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            systemUiVisibility |= flags;
            getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        }

        mA = MainActivity.this;
        JSBridge.mMainActivity = this;


        //Splash
          //JSBridge.show_landSplash();
        mSplashDialog = new SplashDialog(this);
        mSplashDialog.showSplash();
        /*
         * 如果不想使用更新流程，可以屏蔽checkApkUpdate函数，直接打开initEngine函数
         */
        checkApkUpdate(this);


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            /**
             * 如果你的targetSDKVersion >= 23，就要主动申请好权限。如果您的App没有适配到Android6.0（即targetSDKVersion < 23），那么只需要在这里直接调用fetchSplashAd方法。
             *
             */
            checkAndRequestPermissions();
        } else {
            /**
             * 如果是Android6.0以下的机器，默认在安装时获得了所有权限，可以直接调用SDK。
             */
            initGame();
        }


    }

    void initGame(){
        //初始化超休闲SDK
        String appSecert = "c1b765adc4c64ae488b63fdd9cde836d";
        String appId = "30245514";
        GameCenterSDK.init(appSecert,this);

        InitParams initParams = new InitParams.Builder().setDebug(false).build();
        MobAdManager.getInstance().init(this, appId , initParams);

        JSBridge.doLogin();
        this.create_banner();
        this.createInsertAd();
        this.createNativeAd();
    }

    /**
     * 申请SDK运行需要的权限
     * 注意：READ_PHONE_STATE权限是必须权限，没有这个权限SDK无法正常获得广告。
     * WRITE_EXTERNAL_STORAGE 、ACCESS_FINE_LOCATION 是可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
     */
    private List<String> mNeedRequestPMSList = new ArrayList<>();
    private static final int REQUEST_PERMISSIONS_CODE = 100;
    private void checkAndRequestPermissions() {
        /**
         * READ_PHONE_STATE 两个权限是必须权限，没有这两个权限SDK无法正常获得广告。
         */
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_PHONE_STATE);
        }
        /**
         *  两个权限是必须权限，没有这两个权限SDK无法正常获得广告。
         */
        /**
         * WRITE_EXTERNAL_STORAGE、ACCESS_FINE_LOCATION 是两个可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
         */
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mNeedRequestPMSList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mNeedRequestPMSList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        //
        if (0 == mNeedRequestPMSList.size()) {
            /**
             * 权限都已经有了，那么直接调用SDK请求广告。
             */
        } else {
            /**
             * 有权限需要申请，主动申请。
             */
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(this, temp, REQUEST_PERMISSIONS_CODE);
        }
    }

    /**
     * 处理权限申请的结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            /**
             *处理SDK申请权限的结果。
             */
            case REQUEST_PERMISSIONS_CODE:
                if (hasNecessaryPMSGranted()) {
                    /**
                     * 应用已经获得SDK运行必须的READ_PHONE_STATE权限，直接请求广告。
                     */
                    initGame();
                } else {
                    /**
                     * 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
                     */
                    Toast.makeText(this, "应用缺少SDK运行必须的READ_PHONE_STATE权限！请点击\"应用权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 判断应用是否已经获得SDK运行必须的READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE两个权限。
     *
     * @return
     */
    private boolean hasNecessaryPMSGranted() {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            return true;
        }
        return false;
    }


    static BannerAd mBannerAd;
    static View bannerView;
    public void create_banner(){
        AbsoluteLayout mAdContainer = (GameEngine.getInstance().mLayaGameEngine.getAbsLayout()) ;
        String bannerId = "179537";
        mBannerAd = new BannerAd(MainActivity.this, bannerId);

        mBannerAd.setAdListener(new IBannerAdListener() {
            @Override
            public void onAdReady() {
            }

            @Override
            public void onAdClose() {

            }

            @Override
            public void onAdShow() {
            }

            @Override
            public void onAdFailed(String s) {

            }

            @Override
            public void onAdFailed(int i, String s) {
                JSBridge.isLoaded = false;
                ConchJNI.RunJS("show_banner_again()");
            }

            @Override
            public void onAdClick() {
            }
        });

        bannerView = mBannerAd.getAdView();
        if (null != bannerView) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int height = bannerView.getHeight();
            int width = bannerView.getWidth();
            int height_display = displayMetrics.heightPixels;
            int width_display = displayMetrics.widthPixels;
            float density = displayMetrics.density;
            float densityDpi = displayMetrics.densityDpi;

            bannerView.setY(height_display - 115);
            bannerView.setX(width_display/2 - 350);
            mAdContainer.addView(bannerView);
        }
    }

    static InterstitialAd insertAd;
    public void createInsertAd(){
        String Id = "179538";
        insertAd = new InterstitialAd(this,Id);
        insertAd.setAdListener(new IInterstitialAdListener() {
            @Override
            public void onAdReady() {
                //ConchJNI.RunJS("alert('插屏加载完成')");
                insertAd.showAd();
            }

            @Override
            public void onAdClose() {
                ConchJNI.RunJS("insertAd_close()");
               // bannerView.setVisibility(bannerView.VISIBLE);
            }

            @Override
            public void onAdShow() {
              // mBannerAd.destroyAd();
                bannerView.setVisibility(bannerView.INVISIBLE);
            }

            @Override
            public void onAdFailed(String s) {

            }

            @Override
            public void onAdFailed(int i, String s) {

            }

            @Override
            public void onAdClick() {

            }
        });
    }

//    static InterstitialAd insertAd2;
//    public void createSecondInsertAd(){
//        String Id = "179546";
//        insertAd2 = new InterstitialAd(this,Id);
//        insertAd2.setAdListener(new IInterstitialAdListener() {
//            @Override
//            public void onAdReady() {
//                //ConchJNI.RunJS("alert('插屏加载完成')");
//                insertAd2.showAd();
//            }
//
//            @Override
//            public void onAdClose() {
//                ConchJNI.RunJS("insertAd_close()");
//            }
//
//            @Override
//            public void onAdShow() {
//                bannerView.setVisibility(bannerView.INVISIBLE);
//            }
//
//            @Override
//            public void onAdFailed(String s) {
//
//            }
//
//            @Override
//            public void onAdFailed(int i, String s) {
//
//            }
//
//            @Override
//            public void onAdClick() {
//
//            }
//        });
//    }

    public void initEngine()
    {
        mProxy = new RuntimeProxy(this);
        mPlugin = new GameEngine(this);
        mPlugin.game_plugin_set_runtime_proxy(mProxy);
        mPlugin.game_plugin_set_option("localize","true");
        mPlugin.game_plugin_set_option("gameUrl", "http://stand.alone.version/index.js");
        mPlugin.game_plugin_init(3);
        View gameView = mPlugin.game_plugin_get_view();
        this.setContentView(gameView);
        isLoad = true;
    }

    public boolean isOpenNetwork(Context context)
    {
        if (!config.GetInstance().m_bCheckNetwork)
            return true;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getActiveNetworkInfo() != null && (connManager.getActiveNetworkInfo().isAvailable() && connManager.getActiveNetworkInfo().isConnected());
    }

    public void settingNetwork(final Context context, final int p_nType)
    {
        AlertDialog.Builder pBuilder = new AlertDialog.Builder(context);
        pBuilder.setTitle("连接失败，请检查网络或与开发商联系").setMessage("是否对网络进行设置?");
        // 退出按钮
        pBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface p_pDialog, int arg1) {
                Intent intent;
                try {
                    String sdkVersion = android.os.Build.VERSION.SDK;
                    if (Integer.valueOf(sdkVersion) > 10) {
                        intent = new Intent(
                                android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                    } else {
                        intent = new Intent();
                        ComponentName comp = new ComponentName(
                                "com.android.settings",
                                "com.android.settings.WirelessSettings");
                        intent.setComponent(comp);
                        intent.setAction("android.intent.action.VIEW");
                    }
                    ((Activity)context).startActivityForResult(intent, p_nType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        pBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ((Activity)context).finish();
            }
        });
        AlertDialog alertdlg = pBuilder.create();
        alertdlg.setCanceledOnTouchOutside(false);
        alertdlg.show();
    }
    public  void checkApkUpdate( Context context,final ValueCallback<Integer> callback)
    {
        if (isOpenNetwork(context)) {
            // 自动版本更新
            if ( "0".equals(config.GetInstance().getProperty("IsHandleUpdateAPK","0")) == false ) {
                Log.e("0", "==============Java流程 checkApkUpdate");
                new AutoUpdateAPK(context, new ValueCallback<Integer>() {
                    @Override
                    public void onReceiveValue(Integer integer) {
                        Log.e("",">>>>>>>>>>>>>>>>>>");
                        callback.onReceiveValue(integer);
                    }
                });
            } else {
                Log.e("0", "==============Java流程 checkApkUpdate 不许要自己管理update");
                callback.onReceiveValue(1);
            }
        } else {
            settingNetwork(context,AR_CHECK_UPDATE);
        }
    }
    public void checkApkUpdate(Context context) {
        InputStream inputStream = getClass().getResourceAsStream("/assets/config.ini");
        config.GetInstance().init(inputStream);
        checkApkUpdate(context,new ValueCallback<Integer>() {
            @Override
            public void onReceiveValue(Integer integer) {
                if (integer.intValue() == 1) {
                    initEngine();
                } else {
                    finish();
                }
            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == AR_CHECK_UPDATE) {
            checkApkUpdate(this);
        }
    }
    protected void onPause()
    {
        super.onPause();
        if(isLoad)mPlugin.game_plugin_onPause();
    }
    //------------------------------------------------------------------------------
    protected void onResume()
    {
        super.onResume();
        if(isLoad)mPlugin.game_plugin_onResume();
        
    }
    
    protected void onDestroy()
    {
        super.onDestroy();
        if(isLoad)mPlugin.game_plugin_onDestory();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return super.onKeyDown(keyCode, event);
    }


    //-----------------------------------------------------------------

    public static NativeAd mNativeAd;
    private INativeAdData mINativeAdData;
    private String nativeId = "179546";
    private AQuery mAQuery;
    private View adView;
    private AbsoluteLayout gameContainer;

    public void createNativeAd(){
        initView();
        initData_native();
    }

    private void initView() {
        mAQuery = new AQuery(this);
    }

    boolean isAddAdView = false;
    private void initData_native() {
        gameContainer = (GameEngine.getInstance().mLayaGameEngine.getAbsLayout());
        LayoutInflater inflater= LayoutInflater.from(MainActivity.this);
        adView = inflater.inflate(R.layout.activity_native_text_img_640_320,null);

        mNativeAd = new NativeAd(this, nativeId, new INativeAdListener() {
            @Override
            public void onAdSuccess(List iNativeAdDataList) {
                if(isAddAdView==false){
                    gameContainer.addView(adView);
                    isAddAdView = true;
                }
                else {
                    adView.setVisibility(View.VISIBLE);
                }

                if (null != iNativeAdDataList && iNativeAdDataList.size() > 0) {
                  //  Toast.makeText(MainActivity.this, "加载原生广告成功", Toast.LENGTH_SHORT).show();

                    mINativeAdData = (INativeAdData) iNativeAdDataList.get(0);

                    if(null != mINativeAdData && mINativeAdData.isAdValid()){
                        updateNativeAd();
                        bannerView.setVisibility(bannerView.INVISIBLE);
                    }
                }
            }

            @Override
            public void onAdFailed(NativeAdError nativeAdError) {

            }

            @Override
            public void onAdError(NativeAdError nativeAdError, INativeAdData iNativeAdData) {

            }
        });
    }

    void updateNativeAd(){
        mAQuery.id(R.id.show_native_ad_bn).visibility(View.INVISIBLE);
        mAQuery.id(R.id.load_native_ad_bn).visibility(View.INVISIBLE);
        //title
        mAQuery.id(R.id.title_tv).text(mINativeAdData.getTitle());
        //desc
        mAQuery.id(R.id.desc_tv).text(mINativeAdData.getDesc());
        //img
        mAQuery.id(R.id.img_iv).image(mINativeAdData.getImgFiles().get(0).getUrl()).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 *原生广告被点击时必须调用onAdClick方法通知SDK进行点击统计；
                 * 注意：onAdClick方法必须在onAdShow方法之后再调用才有效，否则是无效点击。
                 */
                mINativeAdData.onAdClick(v);
            }
        });

        //close
        mAQuery.id(R.id.close_iv).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adView.setVisibility(View.GONE);
                bannerView.setVisibility(bannerView.VISIBLE);
            }
        });
        //click
        mAQuery.id(R.id.click_bn).text(null != mINativeAdData.getClickBnText() ? mINativeAdData.getClickBnText() : "").clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 *原生广告被点击时必须调用onAdClick方法通知SDK进行点击统计；
                 * 注意：onAdClick方法必须在onAdShow方法之后再调用才有效，否则是无效点击。
                 */
                mINativeAdData.onAdClick(v);
            }
        });

        //原生广告曝光时必须调用onAdShow方法通知SDK进行曝光统计，否则就没有曝光数据。
        mINativeAdData.onAdShow(adView);
    }
}

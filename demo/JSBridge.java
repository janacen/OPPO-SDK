package demo;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.ApiResult;
import com.nearme.game.sdk.common.util.AppUtil;
import com.nearme.plugin.framework.LogUtils;
import com.opos.mobad.api.MobAdManager;

import Activity.RewardVideoActivity;
import Activity.LandSplashActivity;
import Activity.Native640X320Activity;

public class JSBridge extends Application{
    public static Handler m_Handler = new Handler(Looper.getMainLooper());
    public static Activity mMainActivity = null;

    public static void hideSplash() {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.dismissSplash();
                    }
                });
    }

    public static void setFontColor(final String color) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setFontColor(Color.parseColor(color));
                    }
                });
    }

    public static void setTips(final JSONArray tips) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        try {
                            String[] tipsArray = new String[tips.length()];
                            for (int i = 0; i < tips.length(); i++) {
                                tipsArray[i] = tips.getString(i);
                            }
                            MainActivity.mSplashDialog.setTips(tipsArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void bgColor(final String color) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setBackgroundColor(Color.parseColor(color));
                    }
                });
    }

    public static void loading(final double percent) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setPercent((int)percent);
                    }
                });
    }

    public static void showTextInfo(final boolean show) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.showTextInfo(show);
                    }
                });
    }


    public static void doLogin(){
        GameCenterSDK.getInstance().doLogin(mMainActivity,new ApiCallback(){
            @Override
            public void onSuccess(String resultMsg){
            }
            @Override
            public void onFailure(String resultMsg,int resultCode){
            }
        });
    }
    public static void doGetTokenAndSsoid(){
        GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback(){
            @Override
            public void onSuccess(String resultMsg){
                try{
                    JSONObject json = new JSONObject(resultMsg);
                    String toke = json.getString("token");
                    String ssoid = json.getString("ssoid");
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(String resultMsg,int resultCode){
            }
        });
    }

    public static void onExit(){
        GameCenterSDK.getInstance().onExit(mMainActivity,new GameExitCallback(){
            @Override
            public void exitGame(){
                // CP 实现游戏退出操作，也可以直接调用
                // AppUtil工具类里面的实现直接强杀进程~
                AppUtil.exitGameProcess(mMainActivity);
            }
        });
        MobAdManager.getInstance().exit(mMainActivity);
    }

    public static void doGetVerifedInfo(){//实名认证
        GameCenterSDK.getInstance().doGetVerifiedInfo(new ApiCallback(){
            @Override
            public void onSuccess(String resultMsg){
                try{
                    int age = Integer.parseInt(resultMsg);
                    if(age<18){}
                    else {}
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String resultMsg,int resultCode) {
                if (resultCode == ApiResult.RESULT_CODE_VERIFIED_FAILED_AND_RESUME_GAME) {
                    //实名认证失败，但还可以继续玩游戏
                }
                else if (resultCode == ApiResult.RESULT_CODE_VERIFIED_FAILED_AND_STOP_GAME) {
                    //实名认证失败，不允许继续游戏，需自己处理退出游戏
                }
            }
        });
    }

    public static void jumpLeisureSubject(){//游戏跳转
        LogUtils.log("JSBridge", "call jumpleisureSubject");
        GameCenterSDK.getInstance().jumpLeisureSubject();
    }

    static boolean isLoaded =  false;
    public static void show_banner(){
        if(isLoaded){
            MainActivity.bannerView.setVisibility(MainActivity.bannerView.VISIBLE);
        }
        else {
            MainActivity.mBannerAd.loadAd();
            isLoaded = true;
        }
    }

    public static void hide_banner(){
        MainActivity.bannerView.setVisibility(MainActivity.bannerView.INVISIBLE);
    }

    public static void show_video(){
        Intent intent = new Intent(mMainActivity, RewardVideoActivity.class);
        mMainActivity.startActivity(intent);
    }

    public static void show_insertAd(){
         MainActivity.insertAd.loadAd();
    }

    public static void showSecondInsertAd(){
           MainActivity.mNativeAd.loadAd();
    }

    public static void show_landSplash(){
        Intent intent = new Intent(mMainActivity, LandSplashActivity.class);
        mMainActivity.startActivity(intent);
    }
}

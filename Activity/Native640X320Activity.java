package Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import com.opos.mobad.api.ad.NativeAd;
import com.opos.mobad.api.listener.INativeAdListener;
import com.opos.mobad.api.params.INativeAdData;
import com.opos.mobad.api.params.INativeAdFile;
import com.opos.mobad.api.params.NativeAdError;
import com.totgames.wcjz6.cxx3.nearme.gamecenter.R;

import java.util.List;

import layaair.game.browser.ConchJNI;



public class Native640X320Activity extends Activity implements INativeAdListener {//

    private static final String TAG = "NativeAdvance640X320Activity";

    private NativeAd mNativeAd;
    /**
     * 原生广告数据对象。
     */
    private INativeAdData mINativeAdData;
    //
    private AQuery mAQuery;

    private String nativeId = "179546";

    private View adView;
    private AbsoluteLayout gameContainer;

    private static Native640X320Activity mA;
    public static Native640X320Activity getInstance() {
        if (mA == null) {
            mA = new Native640X320Activity();
        }
        return mA;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_text_img_640_320);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        initView();
        initData();
    }



    private void initView() {
        mAQuery = new AQuery(this);
        findViewById(R.id.native_ad_container).setVisibility(View.GONE);
//        mAQuery.id(R.id.load_native_ad_bn).clicked(this, "loadAd");
//        mAQuery.id(R.id.show_native_ad_bn).clicked(this, "showAd").enabled(false);
    }

    public void loadAd() {
        if (null != mNativeAd) {
            /**
             *调用loadAd方法加载原生广告。
             */
            mNativeAd.loadAd();
        }
    }

    public void showAd() {
        /**
         *在展示原生广告前调用isAdValid判断当前广告是否有效，否则即使展示了广告，也是无效曝光、点击，不计费的
         *注意：每个INativeAdData对象只有一次有效曝光、一次有效点击；多次曝光，多次点击都只扣一次费。
         */
        if (null != mINativeAdData && mINativeAdData.isAdValid()) {
            findViewById(R.id.native_ad_container).setVisibility(View.VISIBLE);
            /**
             *展示主图、大小为640X320。
             */
            if (null != mINativeAdData.getImgFiles() && mINativeAdData.getImgFiles().size() > 0) {
                INativeAdFile iNativeAdFile = (INativeAdFile) mINativeAdData.getImgFiles().get(0);
                showImage(iNativeAdFile.getUrl(),(ImageView)findViewById(R.id.img_iv));
            }
            /**
             * 判断是否需要展示“广告”Logo标签
             */
            if (null != mINativeAdData.getLogoFile()) {
                showImage(mINativeAdData.getLogoFile().getUrl(), (ImageView)findViewById(R.id.logo_iv));
            }
            mAQuery.id(R.id.title_tv).text(null != mINativeAdData.getTitle() ? mINativeAdData.getTitle() : "");
            mAQuery.id(R.id.desc_tv).text(null != mINativeAdData.getDesc() ? mINativeAdData.getDesc() : "");
            /**
             * 处理“关闭”按钮交互行为
             */
            mAQuery.id(R.id.close_iv).clicked(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.native_ad_container).setVisibility(View.GONE);
                    //mAQuery.id(R.id.show_native_ad_bn).enabled(false);

                }
            });
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
            /**
             * 原生广告曝光时必须调用onAdShow方法通知SDK进行曝光统计，否则就没有曝光数据。
             */
            mINativeAdData.onAdShow(findViewById(R.id.native_ad_container));
        }
    }

    private void showImage(String url, ImageView imageView) {
        ImageLoader.getInstance().displayImage(url, imageView);
    }

    private void initData() {
        /**
         *构造NativeAd对象。
         */
        mNativeAd = new NativeAd(this, nativeId, this);
        this.loadAd();
    }

    @Override
    protected void onDestroy() {
        if (null != mNativeAd) {
            /**
             *銷毀NativeAd对象，释放资源。
             */
            mNativeAd.destroyAd();
        }
        super.onDestroy();
    }

    /**
     * 原生广告加载成功，在onAdSuccess回调广告数据
     *
     * @param iNativeAdDataList
     */
    @Override
    public void onAdSuccess(List iNativeAdDataList) {
        if (null != iNativeAdDataList && iNativeAdDataList.size() > 0) {
            mINativeAdData = (INativeAdData) iNativeAdDataList.get(0);
            //mAQuery.id(R.id.show_native_ad_bn).enabled(true);
            Toast.makeText(Native640X320Activity.this, "加载原生广告成功", Toast.LENGTH_SHORT).show();
        }

//        gameContainer = (GameEngine.getInstance().mLayaGameEngine.getAbsLayout());
//        LayoutInflater inflater= LayoutInflater.from(this);
//        adView = inflater.inflate(R.layout.activity_native_text_img_640_320,null);
//        gameContainer.addView(adView);

        this.showAd();
    }

    @Override
    public void onAdFailed(NativeAdError nativeAdError) {
        Toast.makeText(Native640X320Activity.this, "加载原生广告失败,错误码：" + nativeAdError.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAdError(NativeAdError nativeAdError, INativeAdData iNativeAdData) {
        Toast.makeText(Native640X320Activity.this, "调用原生广告统计方法出错,错误码：" + nativeAdError.toString(), Toast.LENGTH_LONG).show();
    }
}

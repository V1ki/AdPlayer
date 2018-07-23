package com.horzon.ad.video;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.TextView;

import com.horzon.ad.adapter.ViewPageAdapter;
import com.horzon.ad.api.Api;
import com.horzon.ad.model.CmdModel;
import com.horzon.ad.model.ResouceModel;
import com.horzon.ad.tool.ImgInfo;
import com.horzon.ad.videoview.FllScreenVideoView;
import com.horzon.utils.LogUtil;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";


    private ViewPager mViewPager;           // 滑动页面
    private TextView tvDescription;        // 图片描述
    private LinearLayout llPoints;          // 小圆点存放视图

    FllScreenVideoView mFllScreenVideoView;//自定义Video(可全屏播放)

    private List<ImgInfo> ImgList = new ArrayList<ImgInfo>();    // 图片和描述集合
    private ViewPageAdapter mViewPagerAdapter;                   // ViewPage展示adapter

    private Api mApi = new Api();


    private List<ResouceModel> videos = new ArrayList<>();
    private List<ResouceModel> pics_left_top = new ArrayList<>();
    private List<ResouceModel> pics_left_bottom = new ArrayList<>();
    private List<ResouceModel> pics_right_bottom = new ArrayList<>();

    private int currentVideoIndex = 0 ;

    /**
     * handler处理定时任务
     */
    private Handler mMyHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            // 页面进入动画
            Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.a1);
            mViewPager.startAnimation(animation);

            //  添加页面
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);

            // 3秒定时
            mMyHandler.sendEmptyMessageDelayed(0, 3000);

            // 页面出去动画
            Animation animation2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.a2);
            mViewPager.startAnimation(animation2);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE ;
            decorView.setSystemUiVisibility(uiOptions);
        }
        */

        Intent intent = new Intent();
        intent.setAction("remove_navigation");
        sendBroadcast(intent);

        mViewPager = (ViewPager) findViewById(R.id.ad_viewPage);
        tvDescription = (TextView) findViewById(R.id.tv_msg);
        llPoints = (LinearLayout) findViewById(R.id.ll_dian);

        initMQTT();

        initDatas();
        mApi.checkVersion();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unInitMQTT();

        Intent intent = new Intent();
        intent.setAction("add_navigation");
        sendBroadcast(intent);
    }

    private void initDatas(){

        Realm realm = Realm.getDefaultInstance();


        RealmResults<ResouceModel> models = realm.where(ResouceModel.class).equalTo("package_type",1).findAll();

        for (ResouceModel model : models) {
            pics_left_top.add(model.copy());
            if(model.getFile_path() != null ){
                ImgList.add(new ImgInfo(Uri.fromFile(new File(model.getFile_path())),model.getPackage_id()));
            }
        }


        initData();

        models = realm.where(ResouceModel.class).equalTo("package_type",2).findAll();

        for (ResouceModel model : models) {
            videos.add(model.copy());
            LogUtil.e(TAG,"model:"+model);
        }
        initView();



        models = realm.where(ResouceModel.class).equalTo("package_type",3).findAll();

        for (ResouceModel model : models) {
            pics_left_bottom.add(model.copy());
        }

        models = realm.where(ResouceModel.class).equalTo("package_type",4).findAll();

        for (ResouceModel model : models) {
            pics_right_bottom.add(model.copy());
        }




    }


    /**
     * Page初始化数据
     */
    private void initData() {
        //initDots();     // 初始化小圆点

        mViewPagerAdapter = new ViewPageAdapter(MainActivity.this, ImgList);

        mViewPager.setAdapter(mViewPagerAdapter);

        if(ImgList.size() != 0) {

            // 默认在1亿多
            mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 - ((Integer.MAX_VALUE / 2) % ImgList.size()));
        }
        // 3秒定时
        mMyHandler.sendEmptyMessageDelayed(0, 3000);
//        updateIntroAndDot();   //  图片指示器更新文本
    }

    private void initImgInfo() {
        //  添加数据到图文集合内
        ImgList.add(new ImgInfo(R.drawable.im_01, "秋天的光"));
        ImgList.add(new ImgInfo(R.drawable.im_02, "冬天的雪"));
        ImgList.add(new ImgInfo(R.drawable.im_03, "夏天的叶"));
        ImgList.add(new ImgInfo(Uri.fromFile(new File("/mnt/sdcard/ad/微信图片_20180606142036.jpg")),"安慕希"));
    }


    private Uri getCurrentVideoIndex(){

        if (videos.size() == 0){
            currentVideoIndex = 0 ;
            return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.liangliang);
        }

        if (currentVideoIndex > videos.size()){
            currentVideoIndex = 0;
        }
        else if (currentVideoIndex < 0){
            currentVideoIndex = videos.size() - 1  ;
        }

        return  Uri.fromFile(new File(videos.get(currentVideoIndex).getFile_path()));
    }

    /**
     * 初始化视频
     */
    private void initView() {
        Uri mUri = null ;// Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.liangliang);    //本地视频
        if (videos.size() > 0){
            currentVideoIndex = 0 ;
            mUri =  getCurrentVideoIndex();
        }

        mFllScreenVideoView = (FllScreenVideoView) findViewById(R.id.fvideoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setVisibility(View.GONE);

        mFllScreenVideoView.setMediaController(mediaController);
        if(mUri != null) {

            mFllScreenVideoView.setVideoURI(mUri);
            mFllScreenVideoView.start();
            mFllScreenVideoView.requestFocus();
        }

        mFllScreenVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                /*
                currentVideoIndex ++ ;

                Uri uri = getCurrentVideoIndex() ;

                mFllScreenVideoView.setVideoURI(uri);*/
                mFllScreenVideoView.start();

            }
        });

    }

    public static String MQTT_TOPIC_FILTER_ID = "ad/manager/id/";
    public static String MQTT_TOPIC_FILTER_MODEL = "ad/manager/model/";

    // 订阅 IMEI
    // 订阅 设备型号

    private MqttClient mMqttClient;

    private void initMQTT() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        MQTT_TOPIC_FILTER_ID = MQTT_TOPIC_FILTER_ID +  tm.getDeviceId() ;



        String mShNameID = SystemProperties.get("ro.horzon.product.trader_id", "NA");
        mShNameID = "10007" ;
        //mShNameID = "10005";
        String mCustomerID = SystemProperties.get("ro.horzon.product.customer_id", "NA");
        mCustomerID = "35";
        String mDevModelID = SystemProperties.get("ro.horzon.product.dev_model_id", "NA");
        mDevModelID = "32";
        MQTT_TOPIC_FILTER_MODEL = MQTT_TOPIC_FILTER_MODEL + mShNameID + "/" + mCustomerID +"/"+mDevModelID ;



                String broker = "tcp://47.92.2.131:1883";//"tcp://120.78.229.190:1883";
        String clientId = "adapp_" + System.getProperty("os.name") + System.getProperty("os.version") + tm.getDeviceId();
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            mMqttClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
//            connOpts.setUserName("chisj");
//            connOpts.setPassword("chisj".toCharArray());
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setKeepAliveInterval(3600);

            mMqttClient.connect(connOpts);

            mMqttClient.subscribe(MQTT_TOPIC_FILTER_ID, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage msg)
                        throws Exception {
                    // msg : 格式 为JSON

                    Log.d(TAG, "topic:" + topic + " -- msg:" + msg.toString());


                    String content = new String (msg.getPayload());

                    parseContent(content);


                }
            });
            mMqttClient.subscribe(MQTT_TOPIC_FILTER_MODEL, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage msg)
                        throws Exception {

                    // msg : 格式 为JSON
                    Log.d(TAG, "topic:" + topic + " -- msg:" + msg.toString());
                    String content = new String (msg.getPayload());

                    parseContent(content);
                }
            });

        } catch (MqttException me) {
            Log.e(TAG, "initMQTT: reason " + me.getReasonCode());
            Log.e(TAG, "initMQTT: msg " + me.getMessage());
            Log.e(TAG, "initMQTT: loc " + me.getLocalizedMessage());
            Log.e(TAG, "initMQTT: cause " + me.getCause());
            Log.e(TAG, "initMQTT: excep " + me);
            Log.e(TAG, "", me);
        }
    }

    private void unInitMQTT() {
        try {
            mMqttClient.disconnect();
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void parseAddResouceModel(ResouceModel model){
        LogUtil.e(TAG,"parseAddResouceModel:"+model);
        if(model.getPackage_type() == ResouceModel.AD_LOC_RIGHT_TOP){
            if(!videos.contains(model)) {
                videos.add(model);
                LogUtil.e(TAG,"current videos.size:"+videos.size());
            }
            else {
                LogUtil.e(TAG,"exist ,current videos.size:"+videos.size());
            }

        }
        else if (model.getPackage_type() == ResouceModel.AD_LOC_LEFT_TOP) {
            if (!pics_left_top.contains(model)){
                pics_left_top.add(model);
                if(model.getFile_path() != null ){
                    ImgInfo info =  new ImgInfo(Uri.fromFile(new File(model.getFile_path())),model.getPackage_id()) ;
                    ImgList.add(info);
                    mViewPagerAdapter.addImgInfo(info);
                }
            }

        }

    }


    private void parseDelResouceModel(ResouceModel model){
        LogUtil.e(TAG,"parseDelResouceModel:"+model);
        if(model.getPackage_type() == ResouceModel.AD_LOC_RIGHT_TOP){
            if(videos.contains(model)) {
                ResouceModel currentModel = videos.get(currentVideoIndex) ;
                LogUtil.e(TAG,"current play id:"+currentModel.getPackage_id() + " -- del model id:"+model.getPackage_id());


                videos.remove(model);
                LogUtil.e(TAG,"current videos.size:"+videos.size());
                if(currentModel.getPackage_id().equals(model.getPackage_id())){

                    final Uri mUri = getCurrentVideoIndex();
                    LogUtil.e(TAG,"remove current video ,next video :"+mUri);
                    mMyHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mFllScreenVideoView.setVideoURI(mUri);
                            mFllScreenVideoView.requestFocus();
                            mFllScreenVideoView.start();
                        }
                    });

                }

            }
            else {

                LogUtil.e(TAG,"videos not contain:"+model);
            }

        }
        else if (model.getPackage_type() == ResouceModel.AD_LOC_LEFT_TOP) {

            LogUtil.e(TAG,"del pic model:"+model);
            if (pics_left_top.contains(model)){
                pics_left_top.remove(model);

                    ImgInfo info =  new ImgInfo(null,model.getPackage_id()) ;
                    ImgList.remove(info);
                    mViewPagerAdapter.removeImgInfo(info);
            }

        }


    }


    private void parseContent(String content) {


        final CmdModel model = mApi.gson.fromJson(content,CmdModel.class);


        if (CmdModel.CMD_DELETE.equals(model.cmd)) {
            if(model.data != null ){

                parseDelResouceModel(model.data);


                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                ResouceModel resouceModel = realm.where(ResouceModel.class).equalTo("package_id",model.data.getPackage_id()).findFirst();
                resouceModel.deleteFromRealm();
                realm.commitTransaction();

            }
        }
        else if (CmdModel.CMD_ADD.equals(model.cmd)){
            if(model.data != null ){

                new Thread() {
                    @Override
                    public void run() {
                        ResouceModel rmodel = model.data.copy() ;
                        mApi.parseResource(rmodel);

                        parseAddResouceModel(model.data);

                    }
                }.start();
            }
        }



    }




    /* ----------------------------------------------------------------------------  */

    /**
     * 初始化Dots(小圆点)
     */
    private void initDots() {
        for (int i = 0; i < ImgList.size(); i++) {
            View view = new View(this);
            LayoutParams params = new LayoutParams(12, 12);   // 小圆点容器大小
            if (i != 0) {    //第一个点不需要左边距
                params.leftMargin = 20;     // 小圆点间距
            }
            view.setLayoutParams(params);
            view.setBackgroundResource(R.drawable.selector_dot);
            llPoints.addView(view);
        }
    }



    /**
     * 图片指示器更新文本
     */
    protected void updateIntroAndDot() {
        int currentPage = mViewPager.getCurrentItem() % ImgList.size();
//        tvDescription.setText(ImgList.get(currentPage).getIntro());

        for (int i = 0; i < llPoints.getChildCount(); i++) {

            //设置setEnabled为true的话 在选择器里面就会对应的使用白色颜色
            llPoints.getChildAt(i).setEnabled(i == currentPage);
        }
    }

}

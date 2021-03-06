package com.syw.weiyu.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import cn.bmob.v3.Bmob;
import com.syw.weiyu.bean.Account;
import com.syw.weiyu.core.*;
import com.syw.weiyu.R;
import com.syw.weiyu.ui.user.LoginActivity;


public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wy_activity_launcher);

        //初始化BmobSDK
//        Bmob.initialize(this, AppConstants.bmob_app_id);

//        有账户信息→MainTabs主页面
//        无账户信息→Login登入页
        try {
            Account account = WeiyuApi.get().getAccount();
//            Account account = WeiyuApi.get().getAccount();
//            WeiyuApi.get().login(account.getToken());
            if (App.isFirstLaunch()) {
                //未作初始化时，加载开屏广告
                showSplashAdThenGotoMainPage();
                App.setIsFirstLaunch(false);
            } else {
                //有账户信息
                gotoMainPage();
            }
        } catch (AppException e) {
            //没有账户信息，进入登录页
            showSplashGotoLoginPage();
        }
    }

    private void showSplashGotoLoginPage() {
        new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //进入注册页
                Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
                startActivity(intent);
                //销毁
                finish();
            }
        }.sendEmptyMessageDelayed(0, 2000);
    }

    private void gotoMainPage() {
        //进入主页面
        Intent intent = new Intent(LauncherActivity.this, MainTabsActivity.class);
        startActivity(intent);
        //销毁
        finish();
    }

    private void showSplashAdThenGotoMainPage() {
        //[2s]后加载开屏广告页
        new Handler(){
            @Override
            public void handleMessage(Message msg) {
                WeiyuApi.get().showSplashAd(LauncherActivity.this, new AdListener() {
                    @Override
                    public void onFailedReceiveAd() {
                        //do nothing
                    }

                    @Override
                    public void onCloseAd() {
                        gotoMainPage();
                    }
                });
            }
        }.sendEmptyMessageDelayed(0, 2000);
    }


    //禁用返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

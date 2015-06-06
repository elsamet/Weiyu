package com.syw.weiyu.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.syw.weiyu.core.AppContext;
import com.syw.weiyu.core.AppException;
import com.syw.weiyu.R;
import com.syw.weiyu.core.AdListener;
import com.syw.weiyu.core.WeiyuApi;
import com.syw.weiyu.bean.Account;
import com.syw.weiyu.ui.user.LoginActivity;


public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wy_activity_launcher);

//        有账户信息→MainTabs主页面
//        无账户信息→Login登入页
        try {
            WeiyuApi.get().getAccount();
//            Account account = WeiyuApi.get().getAccount();
//            WeiyuApi.get().login(account.getToken());
            if (AppContext.isFirstLaunch()) {
                //未作初始化时，加载开屏广告
                showSplashAdThenGotoMainPage();
                AppContext.setIsFirstLaunch(false);
            } else {
                //有账户信息
                gotoMainPage();
            }
        } catch (AppException e) {
            //没有账户信息，进入登录页
            gotoLoginPage();
        }
    }

    private void gotoLoginPage() {
        //进入注册页
        Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
        startActivity(intent);
        //销毁
        finish();
    }

    private void gotoMainPage() {
        //进入主页面
        Intent intent = new Intent(LauncherActivity.this, MainTabsActivity.class);
        startActivity(intent);
        //销毁
        finish();
    }

    private void showSplashAdThenGotoMainPage() {
        //[1s]后加载开屏广告页
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
        }.sendEmptyMessageDelayed(0, 1000);
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
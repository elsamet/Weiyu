package com.syw.weiyu.activity.maintabs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

//import com.qq.e.ads.AdListener;
//import com.qq.e.ads.AdRequest;
//import com.qq.e.ads.AdSize;
//import com.qq.e.ads.AdView;
import com.paging.listview.PagingListView;
import com.syw.weiyu.AppContext;
import com.syw.weiyu.LBS.LBSCloud;
import com.syw.weiyu.R;
import com.syw.weiyu.ad.MoGo;
import com.syw.weiyu.adapter.NearByUserAdapter;
import com.syw.weiyu.adapter.ShuoshuoAdapter;
import com.syw.weiyu.adp.WeiyuBannerCustomEventPlatformAdapter;
import com.syw.weiyu.adp.WeiyuCustomEventPlatformEnum;
import com.syw.weiyu.controller.listener.WeiyuListener;

import net.tsz.afinal.http.AjaxCallBack;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import io.rong.imkit.RongIM;

/**
 * Created by songyouwei on 2015/2/9.
 */
public class ShuoshuoActivity extends FragmentActivity {

    private long currentTime=0;
    private long oldTime=0;
    /**
     * 两次返回键退出，间隔2秒
     */
    @Override
    public void onBackPressed() {
        currentTime = System.currentTimeMillis();
        if ((currentTime - oldTime) > 2000 || oldTime == 0) {
            Toast.makeText(this, "再按一次退出", 2000).show();
            oldTime = currentTime;
        } else {
            finish();
        }
    }

    //用于视图适配器的mapList
    List<HashMap<String, String>> shuoshuomapList = new ArrayList<>();

    int pageIndex = 0;
    int totalPage = 0;

    ShuoshuoAdapter.LOADTYPE loadType;
    /**
     * 设置加载类型
     * @param loadType
     */
    public void setLoadType(ShuoshuoAdapter.LOADTYPE loadType) {
        this.loadType = loadType;
    }
    PagingListView listView;
    ShuoshuoAdapter adapter;

    PtrClassicFrameLayout mPtrFrame;

    //AdMoGo
    MoGo moGo;

    //LBS callback
    AjaxCallBack<String> lbsCloudSearchCallback = new LBSCloudSearchCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wy_activity_shuoshuo);
        initViews();

//        initHeader();
        initUltraPullToRefresh();
        initListView();

        //init adsmogo
        moGo = new MoGo(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Weiyu","ShoshuoActivity onResume");

        //读取缓存的lists
        shuoshuomapList = AppContext.getInstance().getShuoshuomapList();
        //若空，则刷新数据（延迟一秒），否者直接设置适配器
        if(shuoshuomapList.isEmpty()) {
            new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    mPtrFrame.autoRefresh();
                }
            }.sendEmptyMessageDelayed(1,500);
        } else {
            adapter.setData(shuoshuomapList);
        }

        //add banner ad
        if (listView.getHeaderViewsCount() == 0) {
            listView.addHeaderView(moGo.getBannerAd());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        moGo.onBannerDestory();
        super.onDestroy();
    }

    private void initViews() {
        ImageView btnAdd = (ImageView) findViewById(R.id.header_iv_right);
        btnAdd.setImageResource(R.drawable.wy_icon_add);
        btnAdd.setVisibility(View.VISIBLE);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = getAddShuoshuoAlertDialog();
                dialog.show();
            }
        });

        TextView tvTitle = (TextView) findViewById(R.id.header_tv_title);
        tvTitle.setText("首页");
    }


    /**
     * 初始化下拉刷新框架
     */
    private void initUltraPullToRefresh() {
        mPtrFrame = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_list_view_frame);
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                //显示feeds广告
//                showFeedsAd();

                //刷新列表数据
//                updateListData(dataType);
                pageIndex = 0;
                setLoadType(ShuoshuoAdapter.LOADTYPE.TYPE_REFRESH);
                LBSCloud.getInstance().nearbyShuoshuoSearch(0,lbsCloudSearchCallback);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
    }

    /**
     * 初始化列表视图
     */
    private void initListView() {
        listView = (PagingListView) findViewById(R.id.lv_shuoshuo);
        adapter = new ShuoshuoAdapter(this);
        listView.setAdapter(adapter);
        listView.setHasMoreItems(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RongIM.getInstance().startPrivateChat(
                        ShuoshuoActivity.this,
                        adapter.getData().get(position-1).get("userId"),
                        adapter.getData().get(position-1).get("shuoshuo_tv_name"));
            }
        });

        listView.setPagingableListener(new PagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                if (pageIndex + 1 < totalPage) {
                    setLoadType(ShuoshuoAdapter.LOADTYPE.TYPE_MORE);
                    LBSCloud.getInstance().nearbyUserSearch(++pageIndex, lbsCloudSearchCallback);
                } else {
                    listView.onFinishLoading(false, null);
                }
            }
        });
    }

    /**
     * LBS云检索回调
     */
    class LBSCloudSearchCallback extends AjaxCallBack<String> {
        private List<HashMap<String,String>> newDataList = new ArrayList<>();
        @Override
        public void onStart() {
        }

        @Override
        public void onSuccess(String s) {
            Log.d("Weiyu", " LBSCloud poi search nearby shuoshuo return:" + s);
            //解析数据并存入
            JSONObject result = JSON.parseObject(s);
            if (result.getString("status").equals("0")) {
                JSONArray poiArray = result.getJSONArray("contents");
                newDataList.clear();
                for (int i=0; i<poiArray.size(); i++) {
                    JSONObject poi = poiArray.getJSONObject(i);

                    //map for adapter
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("userId", poi.getString("userId"));
                    map.put("shuoshuo_tv_name", poi.getString("userName"));
                    map.put("shuoshuo_tv_address", poi.getString("province")+poi.getString("district"));
                    map.put("shuoshuo_tv_time", new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.ENGLISH).format(new Timestamp(Long.parseLong(poi.getString("create_time")) * 1000)));
                    map.put("shuoshuo_tv_content", poi.getString("content"));
                    newDataList.add(map);
                }

                //set totalPage
                if (totalPage == 0) {
                    totalPage = (int)Math.ceil(Double.parseDouble(result.getString("total"))/Double.parseDouble(result.getString("size")));
                }
                //设置适配器
                if (loadType == ShuoshuoAdapter.LOADTYPE.TYPE_REFRESH) {
                    //结束下拉刷新
                    mPtrFrame.refreshComplete();
                    adapter.setData(newDataList);
                    //set has more page
                    listView.setHasMoreItems(pageIndex + 1 < totalPage);
                    //save result lists into ram
                    AppContext.getInstance().setShuoshuomapList(newDataList);
                } else if (loadType == ShuoshuoAdapter.LOADTYPE.TYPE_MORE){
                    adapter.addData(newDataList);
                    listView.onFinishLoading(pageIndex + 1 < totalPage, null);
                    //save result lists into ram
                    AppContext.getInstance().setUsermapList(adapter.getData());
                }
            }
        }

        @Override
        public void onFailure(Throwable t, int errorNo, String strMsg) {
            Log.d("Weiyu", " LBSCloud poi search nearby shuoshuo failure:" + strMsg);
            //结束下拉刷新
            mPtrFrame.refreshComplete();
        }
    }

    private AlertDialog getAddShuoshuoAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ShuoshuoActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        // Get the layout inflater
        final LayoutInflater inflater = ShuoshuoActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.wy_dialog_addshuoshuo, null);
        final EditText contentET = (EditText)view.findViewById(R.id.et_shuoshuo_content);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        LBSCloud.getInstance().publishShuoshuo(contentET.getText().toString(), new AjaxCallBack<String>() {
                            @Override
                            public void onSuccess(String s) {
                                if (JSON.parseObject(s).getString("status").equals("0")) {
                                    Toast.makeText(ShuoshuoActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ShuoshuoActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(Throwable t, int errorNo, String strMsg) {
                                Toast.makeText(ShuoshuoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });

                        new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                mPtrFrame.autoRefresh();
                            }
                        }.sendEmptyMessageDelayed(1, 1000);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        builder.create().dismiss();
                    }
                });
        return builder.create();
    }
}
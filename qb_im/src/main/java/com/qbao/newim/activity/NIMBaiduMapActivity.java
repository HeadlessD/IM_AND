package com.qbao.newim.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.qbao.newim.adapter.MapAddressAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.model.NIMAddressInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/7/17.
 */

public class NIMBaiduMapActivity extends NIM_ToolbarAct implements BaiduMap.OnMapStatusChangeListener
                    , BDLocationListener, OnGetGeoCoderResultListener {
    private BaiduMap baiduMap;
    private MapView mapView;

    private RecyclerView recyclerView;
    private MapAddressAdapter mAdapter;
    private ImageView iv_location;

    private LocationClient mLocClient;
    private boolean isFirstLoc = true;
    private LatLng locationLatLng;
    private LatLng mapChangeLatLng;
    private GeoCoder geoCoder;
    private int nPosition = 0;
    private ArrayList<NIMAddressInfo> mList;
    private LinearLayoutManager layout_manager;
    private ImageView iv_current;
    private boolean is_current;
    private ProgressBar progressBar;
    private ImageView iv_default_bg;
    private Handler mHandler;
    private TextView tvRight;
    private boolean is_click;
    private boolean is_send_location;
    private double lat;
    private double lon;
    private String address;
    private View show_info_layout;
    private TextView tv_info;
    private View white_bg;

    private int lastX;
    private int lastY;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_baidu_map);
        mapView = (MapView) findViewById(R.id.map_view);
        recyclerView = (RecyclerView) findViewById(R.id.map_address_list);
        layout_manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layout_manager);
        iv_location = (ImageView) findViewById(R.id.iv_map_center);
        iv_current = (ImageView) findViewById(R.id.map_address_item_current);
        iv_current.setImageResource(R.mipmap.nim_icon_current_normal);
        iv_default_bg = (ImageView) findViewById(R.id.map_default_bg);
        iv_default_bg.setVisibility(View.VISIBLE);
        show_info_layout = findViewById(R.id.map_address_item_info);
        tv_info = (TextView) findViewById(R.id.map_address_item_txt);
        white_bg = findViewById(R.id.rl_map);
        baiduMap = mapView.getMap();

        MapStatus mMapStatus = new MapStatus.Builder().zoom(18).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        baiduMap.setMapStatus(mMapStatusUpdate);

        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat", 0);
            lon = getIntent().getDoubleExtra("lon", 0);
            address = getIntent().getStringExtra("address");
            if (lat > 0 || lon > 0 || !TextUtils.isEmpty(address)) {
                is_send_location = false;
                show_info_layout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                iv_default_bg.setVisibility(View.GONE);
            } else {
                is_send_location = true;
                recyclerView.setVisibility(View.VISIBLE);
                show_info_layout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void setListener() {

        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = mHandler.obtainMessage(0);
                        mHandler.sendMessage(msg);
                    }
                }, 2000);

            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                progressBar.setVisibility(View.GONE);
                iv_default_bg.setVisibility(View.GONE);
                if (!is_send_location) {
                    white_bg.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    tvRight.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        // 先默认天安门,防止黑屏
        LatLng center = new LatLng(39.915071, 116.403907);
        MapStatusUpdate default_msu = MapStatusUpdateFactory.newLatLng(center);
        baiduMap.setMapStatus(default_msu);

        if (!is_send_location) {
            white_bg.setBackgroundColor(Color.WHITE);
            LatLng ll = new LatLng(lat, lon);
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(msu);
            tv_info.setText(address);
            return;
        }

        //地图状态改变相关监听
        baiduMap.setOnMapStatusChangeListener(this);

        //开启定位图层
        baiduMap.setMyLocationEnabled(true);

        //初始化定位
        mLocClient = new LocationClient(this);
        //注册定位监听
        mLocClient.registerLocationListener(this);

        //定位选项
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.disableCache(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //设置是否打开gps进行定位
        option.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        option.setScanSpan(1000);

        //设置 LocationClientOption
        mLocClient.setLocOption(option);

        //开始定位
        mLocClient.start();

        View child = mapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

        mapView.showScaleControl(false);
        // 隐藏缩放控件
        mapView.showZoomControls(false);

        baiduMap.setOnMapStatusChangeListener(this);

        mList = new ArrayList<>();
        mAdapter = new MapAddressAdapter(mList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position == nPosition) {
                    return;
                }
                is_click = true;
                mAdapter.getItem(position).is_select = true;
                mAdapter.getItem(nPosition).is_select = false;
                view.findViewById(R.id.map_address_item_radio).setVisibility(View.VISIBLE);
                View last_view = layout_manager.findViewByPosition(nPosition);
                if (last_view != null) {
                    last_view.findViewById(R.id.map_address_item_radio).setVisibility(View.GONE);
                }
                nPosition = position;

                LatLng latLng = mAdapter.getItem(position).location;
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
                baiduMap.animateMapStatus(update, 800);

                baiduMap.clear();
                if (position != 0) {
                    BitmapDescriptor mSelectIco = BitmapDescriptorFactory
                            .fromResource(R.mipmap.nim_icon_new_location);
                    OverlayOptions ooA = new MarkerOptions().position(latLng).icon(mSelectIco).anchor(0.5f, 0.5f);
                    baiduMap.addOverlay(ooA);
                }

                iv_current.setImageResource(R.mipmap.nim_icon_current_normal);
            }
        });

        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);

        iv_current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_current = true;
                baiduMap.clear();
                iv_current.setImageResource(R.mipmap.nim_icon_current_pressed);
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(locationLatLng);
                baiduMap.animateMapStatus(update, 800);
            }
        });

        baiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //计算移动的距离
                        int offX = x - lastX;
                        int offY = y - lastY;
                        if (offX > 10 || offY > 10) {
                            is_click = false;
                        }
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setText("发送");

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        if (is_send_location) {
            tvTitle.setText("位置");
        } else {
            tvTitle.setText("位置信息");
        }

        progressBar = (ProgressBar) actionView.findViewById(R.id.progressBar);
        progressBar.setVisibility(is_send_location ? View.VISIBLE : View.GONE);
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter == null || mAdapter.getItemCount() == 0) {
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("lat", mAdapter.getItem(nPosition).location.latitude);
                intent.putExtra("lon", mAdapter.getItem(nPosition).location.longitude);
                String address = nPosition == 0 ? mAdapter.getItem(nPosition).name : mAdapter.getItem(nPosition).address;
                intent.putExtra("address", address);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        return true;
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

    }

    @Override
    protected void onDestroy() {
        // 释放资源
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mLocClient != null) {
            mLocClient.unRegisterLocationListener(this);
            mLocClient.stop();
        }
        finish();
        overridePendingTransition(R.anim.nim_not_change, R.anim.nim_pop_out);
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    private void setAnimLocation(final LatLng latlng) {
        iv_location.animate().cancel();
        final int[] location = new int[2];
        TranslateAnimation ta = new TranslateAnimation(location[0], location[0], location[1], location[1] - 100);
        ta.setDuration(400);
        ta.setInterpolator(new DecelerateInterpolator());
        iv_location.startAnimation(ta);
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TranslateAnimation ta = new TranslateAnimation(location[0], location[0], location[1] - 100, location[1]);
                ta.setDuration(400);
                ta.setInterpolator(new AccelerateInterpolator());
                iv_location.startAnimation(ta);

                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latlng));
                baiduMap.clear();
                if (!is_current)
                    iv_current.setImageResource(R.mipmap.nim_icon_current_normal);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        if (mapChangeLatLng == null || is_click) {
            return;
        }

        Projection project = baiduMap.getProjection();
        Point startPoint = project.toScreenLocation(mapChangeLatLng);
        Point finishPoint = project.toScreenLocation(mapStatus.target);

        double x = Math.abs(finishPoint.x - startPoint.x);
        double y = Math.abs(finishPoint.y - startPoint.y);

        int distance = 10;
        if (x > distance || y > distance) {
            is_click = false;
            setAnimLocation(mapStatus.target);
        }

        mapChangeLatLng = mapStatus.target;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null || baiduMap == null) {
            return;
        }

        //定位数据
        MyLocationData data = new MyLocationData.Builder()
                //定位精度bdLocation.getRadius()
                .accuracy(bdLocation.getRadius())
                //此处设置开发者获取到的方向信息，顺时针0-360
                .direction(bdLocation.getDirection())
                //经度
                .latitude(bdLocation.getLatitude())
                .accuracy(0)
                //纬度
                .longitude(bdLocation.getLongitude())
                //构建
                .build();

        //设置定位数据
        baiduMap.setMyLocationData(data);

        //是否是第一次定位
        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            locationLatLng = ll;
            mapChangeLatLng = ll;
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
            baiduMap.animateMapStatus(msu);

            geoCoder.reverseGeoCode((new ReverseGeoCodeOption())
                    .location(locationLatLng));
            baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, null));
            iv_current.setImageResource(R.mipmap.nim_icon_current_pressed);
        }

    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }

        mList.clear();
        NIMAddressInfo first_info = new NIMAddressInfo();
        first_info.name = reverseGeoCodeResult.getAddress();
        first_info.is_select = true;
        first_info.location = reverseGeoCodeResult.getLocation();
        mList.add(first_info);

        List<PoiInfo> poi_info_list = reverseGeoCodeResult.getPoiList();
        for (PoiInfo poi_info : poi_info_list) {
            NIMAddressInfo info = new NIMAddressInfo();
            info.address = poi_info.address;
            info.name = poi_info.name;
            info.location = poi_info.location;
            mList.add(info);
        }
        nPosition = 0;
        mAdapter.setNewData(mList);
    }
}

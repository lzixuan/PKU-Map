package com.example.macpro.pku_map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.InfoWindow.*;
import com.baidu.mapapi.model.LatLngBounds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MyFragment1 extends Fragment implements View.OnClickListener {

    MapView map = null;
    BaiduMap bdmap;
    private Button locbtn = null;
    private Context mContext;
    private View view_custom;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private Marker marker;
    private Event[] eventList = new Event[1000];
    private int count = 0;
    private int type;
    private BitmapDescriptor bitmap = BitmapDescriptorFactory
            .fromResource(R.mipmap.icon_gcoding);
    private static final int msgKey1 = 1;
    private MyFragment1.TimeThread update_thread;
    private InfoWindow addWindow;
    private InfoWindow eventWindow;
    private RelativeLayout topbar = null;

    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(5000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(true);
        }
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    getEventByTypeAsyncHttpClientPost(type);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_content1, container, false);
        mContext = getActivity();
        map = (MapView) view.findViewById(R.id.bdmap);
        locbtn = (Button) view.findViewById(R.id.locbtn);
        topbar = (RelativeLayout) view.findViewById(R.id.topbar);
        bdmap = map.getMap();
        bdmap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bdmap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                bdmap.hideInfoWindow();
                Toast.makeText(mContext, "请到详情页选择地点！", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        //final LinearLayout selectplace = (LinearLayout) view.findViewById(R.id.selectplace);
        bdmap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                bdmap.hideInfoWindow();
                LayoutInflater inflater = LayoutInflater.from(mContext);
                View view = inflater.inflate(R.layout.addwindow, null);
                Button add = view.findViewById(R.id.addButton);

                add.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //在这个地方转到添加新事件
                        Toast.makeText(mContext, "转到添加事件", Toast.LENGTH_LONG).show();
                        //selectplace.setVisibility(View.GONE);
                        Bundle bd = new Bundle();
                        bd.putDouble("locationX", 1);
                        bd.putDouble("locationY", 2);
                        Intent it = new Intent(getActivity(), NewEvent.class);
                        it.putExtras(bd);
                        startActivity(it);
                    }
                });
                addWindow = new InfoWindow(view, latLng, 0);
                bdmap.showInfoWindow(addWindow);
            }
        });
        bdmap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Toast.makeText(mContext, marker.getExtraInfo().get("index").toString(), Toast.LENGTH_LONG).show();
                int eventIndex = (int)marker.getExtraInfo().get("index");
                bdmap.hideInfoWindow();
                LayoutInflater inflater = LayoutInflater.from(mContext);
                View view = inflater.inflate(R.layout.infowindow, null);
                TextView title = (TextView)view.findViewById(R.id.popTitle);
                Button next = (Button)view.findViewById(R.id.nextButton);
                Button pre = (Button)view.findViewById(R.id.preButton);
                Button getContent = (Button)view.findViewById(R.id.getContent);
                title.setText("标题: "+eventList[eventIndex].title);
                getContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "转到事件详情", Toast.LENGTH_SHORT).show();
                        final FragmentManager fManager = getFragmentManager();
                        FragmentTransaction fTransaction = fManager.beginTransaction();
                        EventFragment ncFragment = new EventFragment();
                        Bundle bd = new Bundle();
                        bd.putString("title", "title");
                        bd.putString("content", "content");
                        bd.putInt("which", 2);
                        ncFragment.setArguments(bd);
                        fTransaction.replace(R.id.bdmap, ncFragment);
                        fTransaction.addToBackStack(null);
                        fTransaction.commit();
                        topbar.setVisibility(View.VISIBLE);
                    }
                });
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "该地的下一个事件", Toast.LENGTH_SHORT).show();
                    }
                });
                pre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "该地的上一个事件", Toast.LENGTH_SHORT).show();
                    }
                });
                LatLng point = marker.getPosition();
                eventWindow = new InfoWindow(view, point, -47);
                bdmap.showInfoWindow(eventWindow);
                return false;
            }
        });
        builder = new AlertDialog.Builder(mContext);

        view_custom = inflater.inflate(R.layout.dialog, null, false);
        builder.setView(view_custom);
        builder.setCancelable(true);
        alert = builder.create();
        locbtn.setOnClickListener(this);
        view_custom.findViewById(R.id.cat1btn).setOnClickListener(this);
        view_custom.findViewById(R.id.cat2btn).setOnClickListener(this);
        view_custom.findViewById(R.id.cat3btn).setOnClickListener(this);
        return view;
    }
    public void onStart(){
        super.onStart();
        type = PreferenceUtil.maptype;
        //getEventByTypeAsyncHttpClientPost(type);
        //update_thread = new MyFragment1.TimeThread();
        //update_thread.start();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locbtn:
                alert.show();
                Window dialogWindow = alert.getWindow();
                WindowManager m = getActivity().getWindowManager();
                Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
                WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
                Point size = new Point();
                d.getSize(size);
                p.width = (int) (size.x * 0.8); // 宽度设置为屏幕的0.65，根据实际情况调整
                dialogWindow.setAttributes(p);
                break;
            case R.id.cat1btn:
                Toast.makeText(mContext, "切换为实时", Toast.LENGTH_SHORT).show();
                alert.dismiss();
                PreferenceUtil.maptype = 0;
                break;
            case R.id.cat2btn:
                Toast.makeText(mContext, "切换为活动", Toast.LENGTH_SHORT).show();
                alert.dismiss();
                PreferenceUtil.maptype = 1;
            case R.id.cat3btn:
                Toast.makeText(mContext, "切换为帮忙", Toast.LENGTH_SHORT).show();
                alert.dismiss();
                PreferenceUtil.maptype = 2;
            default:
                alert.dismiss();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        map.onDestroy();
    }
    public void onResume(){
        super.onResume();
        map.onResume();
        //设定地图显示范围
        bdmap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                LatLng center = new LatLng(39.99907, 116.316289);
                MapStatus mMapStatus = new MapStatus.Builder().target(center).zoom(17).build();
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                bdmap.setMapStatus(mMapStatusUpdate);
            }
        });
    }
    public void onPause(){
        super.onPause();
        map.onPause();
    }
    private void getEventByTypeAsyncHttpClientPost(int type) {
        //创建异步请求对象
        AsyncHttpClient client = new AsyncHttpClient();
        //输入要请求的url
        String url = "http://120.25.232.47:8002/getEventByType/";
        //String url = "http://www.baidu.com";
        //请求的参数对象
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //将参数加入到参数对象中
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //进行post请求
        client.post(mContext, url, entity, "application/json", new JsonHttpResponseHandler() {
            //如果成功
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    int status = response.getInt("getStatus");
                    if (status == 1) {
                        Toast.makeText(mContext, "status code is:"+ statusCode+ "\n更新失败!\n", Toast.LENGTH_LONG).show();
                    }
                    else if(status == 0) {
                        //Toast.makeText(mContext, response.toString(), Toast.LENGTH_LONG).show();
                        int count = response.getInt("eventNum");
                        JSONArray events = response.getJSONArray("events");
                        //Toast.makeText(mContext, events.toString(), Toast.LENGTH_LONG).show();
                        for (int i = 0; i < count; i++)
                        {
                            JSONObject temp = events.getJSONObject(i);
                            eventList[i] = new Event();
                            eventList[i].setEventID(temp.getInt("eventID"));
                            //eventList[i].setBeginTime(temp.getString("beginTime"));
                            eventList[i].setDescription(temp.getString("description"));
                            //eventList[i].setEndTime(temp.getString("endTime"));
                            eventList[i].setLocation(temp.getInt("locationID"));
                            eventList[i].setOutdate(temp.getInt("outdate"));
                            eventList[i].type = (temp.getInt("type"));
                            eventList[i].setPublisherID(temp.getInt("publisherID"));
                            eventList[i].setTitle(temp.getString("title"));
                            Bundle bundle = new Bundle();
                            bundle.putInt("index", i);
                            LatLng point = new LatLng(eventList[i].locationY, eventList[i].locationX);
                            OverlayOptions option = new MarkerOptions()
                                    .position(point)
                                    .icon(bitmap)
                                    .extraInfo(bundle);
                            //在地图上添加Marker，并显示
                            marker = (Marker) bdmap.addOverlay(option);
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(mContext, "connection error!Error number is:" + statusCode,  Toast.LENGTH_LONG).show();
            }
        });
        return;

    }
}
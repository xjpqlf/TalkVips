package dao.cn.com.talkvip.view.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import dao.cn.com.talkvip.Constants;
import dao.cn.com.talkvip.R;
import dao.cn.com.talkvip.adapter.IndosAdapter;
import dao.cn.com.talkvip.bean.Custom;
import dao.cn.com.talkvip.bean.CustomFrist;
import dao.cn.com.talkvip.bean.Data;
import dao.cn.com.talkvip.bean.Infos;
import dao.cn.com.talkvip.bean.Message;
import dao.cn.com.talkvip.utils.DebugFlags;
import dao.cn.com.talkvip.utils.Rsa;
import dao.cn.com.talkvip.utils.SPUtils;
import dao.cn.com.talkvip.utils.ToastUtil;
import dao.cn.com.talkvip.view.activity.RemarkActivity;
import dao.cn.com.talkvip.widget.MyAnimationDrawable;
import dao.cn.com.talkvip.widget.MyPtrRefresher;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import okhttp3.Call;

/**
 * Description:
 * User: xjp
 * Date: 2015/6/15
 * Time: 15:03
 */

public class TabFragments extends Fragment {

    @Bind(R.id.ptr_layout)
    PtrClassicFrameLayout ptrLayout;

    @Bind(R.id.tv_pager)
    TextView tvPager;
    @Bind(R.id.progressBar04_id)
    ProgressBar progressBar04Id;
    @Bind(R.id.iv_loading)
    ImageView image;
    @Bind(R.id.rl_bar)
    RelativeLayout rlBar;
    @Bind(R.id.iv_nodata)
    ImageView ivNodata;
    @Bind(R.id.id_main_lv_lv)
    RecyclerView lv;

    private String str;
    private String strs;
    private View mView;
    private List<Custom> mList;
    private IndosAdapter mAdapter;
    private List<CustomFrist> mC;
    private int mA;
    private  PhoneBroadcastReceiver mycodereceiver;
    private int pager = 1;
    final public static int REQUEST_CODE_ASK_CALL_PHONE = 123;
    private static final int DATA_LOAD_FAILED = 0X00123311;
    private static final int DATA_LOAD_SUCCESS = 0X43254564;
    private String mId;
    private int mP;
    private Infos mInfo;
    private Custom mCustom;
    private Handler handlers = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DATA_LOAD_FAILED:
                    Toast.makeText(getActivity(), "服务器连接失败...", Toast.LENGTH_SHORT).show();
                    break;
                case DATA_LOAD_SUCCESS:
                    Message ms = (Message) msg.obj;
                    Data data = ms.getData();
                    if ("0".equals(data.getTotal())) {
                        rlBar.setVisibility(View.GONE);
                        ptrLayout.setMode(PtrFrameLayout.Mode.NONE);
                        ivNodata.setVisibility(View.VISIBLE);
                        return;
                    }
                    mList = data.getList();
                    DebugFlags.logD("刷到最后了" + mList.size() + "---" + mList);
                    if (mList.get(0) == null) {
                        ToastUtil.showInCenter("没有更多数据了");
                        DebugFlags.logD(mList.size() + "---" + mList);
                        return;
                    }

                    //  tvPager.setText(1 + "/" + mList.size());
                    mC = new ArrayList<CustomFrist>();
                    for (int i = 0; i < mList.size(); i++) {
                        mC.add(new CustomFrist(mList.get(i), false,"未拨打"));

                    }

                    mC.get(0).setFirst(true);

                    mAdapter = new IndosAdapter(getActivity(), mC);

                    if (lv != null) {
                        lv.setAdapter(mAdapter);

                    }

                    mAdapter.setOnItemClickListener(new IndosAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            mP = position;
                            mCustom = mList.get(mP);

                            mInfo = new Infos(mList);

                            if (Build.VERSION.SDK_INT >= 23) {
                                int checkCallPhonePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE);
                                if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                                    //   ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.PROCESS_OUTGOING_CALLS},REQUEST_CODE_ASK_CALL_PHONE);
                                    //   return;

                                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());  //先得到构造器
                                    builder.setTitle("申请授权"); //设置标题
                                    builder.setMessage("拨打电话与读取电话状态需要授权,是否需要前往权限管理页面授权"); //设置内容
                                    builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
                                    builder.setPositiveButton("前往授权", new DialogInterface.OnClickListener() { //设置确定按钮
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            getAppDetailSettingIntent(getActivity());

                                            dialog.dismiss(); //关闭dialog

                                        }
                                    });
                                    builder.setNegativeButton("暂不处理", new DialogInterface.OnClickListener() { //设置取消按钮
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                        }
                                    });


                                    //参数都设置完成了，创建并显示出来
                                    builder.create().show();





                                }else{
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmsss");
                                    Date curDate = new Date(System.currentTimeMillis());
                                    String strs = formatter.format(curDate);
                                    Random rand = new Random();
                                    int i = rand.nextInt(100000);
                                    String order=strs+"1"+i;


                                    //产生日志
                                    creatLog(mList.get(position).getId(),order);
                                    getPhoneNum(mList.get(position).getMobile(),order);
//拨打电话
                                    CallPhone(mList.get(position).getMobile());
                                }
                            } else {
                                //上面已经写好的拨号方法
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmsss");
                                Date curDate = new Date(System.currentTimeMillis());
                                String strs = formatter.format(curDate);
                                Random rand = new Random();
                                int i = rand.nextInt(100000);
                                String order=strs+"1"+i;
                                creatLog(mList.get(position).getId(),order);
                                getPhoneNum(mList.get(position).getMobile(),order);

                                CallPhone(mList.get(position).getMobile());
                            }


                        }
                    });
            }
        }
    };

    private void creatLog(String id, final String order) {

        String token= SPUtils.getString(getActivity(),"token","");

        DebugFlags.logD("日志id"+id);
        OkHttpUtils.post()
                .url(Constants.BASE_URL + "/Comment/insertLog")
                // .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Bearer" + " " + token)
                .addParams("id", id)
                .addParams("order",order)

                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                DebugFlags.logD("日志"+response+"++"+order);

            }
        });




    }




    @PermissionGrant(4)
    public void requestContactSuccess()
    {
        CallPhone("13971410254");
    }

    @PermissionDenied(4)
    public void requestContactFailed()
    {
        ToastUtil.showInCenter("暂无权限，请授权");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }






    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.item_exo, container, false);


        ButterKnife.bind(this, mView);

        MyAnimationDrawable.animateRawManuallyFromXML(R.drawable.loadings, image, new Runnable() {
            @Override
            public void run() {
                // TODO onStart
                // 动画开始时回调
                Log.d("", "start");
            }
        }, new Runnable() {
            @Override
            public void run() {
                // TODO onComplete // 动画结束时回调
                Log.d("", "end");
            }
        });
        str = getArguments().getString("content");

        initData();
        initView();

        return mView;


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   str = getArguments().getString("content");
        if ("未拨打".equals(str)) {
        strs="/serviceNotCall";


        } else if ("待跟进".equals(str)) {

            strs="serviceFollowUp";

        } else if ("未接通".equals(str)) {
            strs="serviceNotThrough";


        } else if ("无意愿".equals(str)) {
            strs="serviceNoDesire";


        } else if ("已提取".equals(str)) {
            strs="serviceExtracted";

        }else if ("重点".equals(str)){

            strs="serviceFocusOn";
        }


        initData();
        initView();
    }






    private void showLoadingView() {
        if (image != null) {
            image.setVisibility(View.VISIBLE);

        }
    }

    private void hiddenLoadingView() {
        if (image != null) {
            image.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        DebugFlags.logD("界面重现");

    }



    private void initView() {

        mycodereceiver = new  PhoneBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        getActivity(). registerReceiver(mycodereceiver, filter);
        MyPtrRefresher myPtrRefresher = new MyPtrRefresher(getActivity());
        ptrLayout.setHeaderView(myPtrRefresher);
        // ptrLayout.setFooterView(myPtrRefresher);
        ptrLayout.addPtrUIHandler(myPtrRefresher);
        ptrLayout.setPtrHandler(new PtrDefaultHandler2() {
            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {

                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ptrLayout != null) {
                            ptrLayout.refreshComplete();
                        }
                    }
                }, 2000);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                pager++;
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //ToastUtil.showInCenter(pager+"");
                        if (ptrLayout != null) {
                            getData(pager);
                            ptrLayout.refreshComplete();
                        }
                    }
                }, 2000);


            }
        });


    }


    @TargetApi(Build.VERSION_CODES.M)
    private void initData() {
        showLoadingView();

        LinearLayoutManager  mLayoutManager = new LinearLayoutManager(getActivity());
        lv.setLayoutManager(mLayoutManager);

        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        lv.setHasFixedSize(true);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getData(pager);
            }
        }, 200);



        lv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    rlBar.setVisibility(View.GONE);

                } else {
                    rlBar.setVisibility(View.VISIBLE);

                }




            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                //判断是当前layoutManager是否为LinearLayoutManager
                // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    //  获取最后一个可见view的位置
                    int lastItemPosition = linearManager.findLastVisibleItemPosition();
                    // 获取第一个可见view的位置
                    int firstVisibleItem = linearManager.findFirstVisibleItemPosition();
                    int totalItemCount = linearManager.getItemCount();
                    progressBar04Id.setMax(totalItemCount);
                    tvPager.setText((firstVisibleItem + 1 + "/" + totalItemCount));
                    progressBar04Id.setProgress(firstVisibleItem);
                    if (mC != null) {
                        for (int i = 0; i < mC.size(); i++) {
                            if (firstVisibleItem == i) {


                                mC.get(i).setFirst(true);
                                mAdapter.notifyDataSetChanged();
                            } else {


                                mC.get(i).setFirst(false);
                                mAdapter.notifyDataSetChanged();


                            }

                        }


                    }

                }

            }
        });


       /* lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    rlBar.setVisibility(View.GONE);

                } else {
                    rlBar.setVisibility(View.VISIBLE);

                }


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                progressBar04Id.setMax(totalItemCount);
                tvPager.setText((firstVisibleItem + 1 + "/" + totalItemCount));
                progressBar04Id.setProgress(firstVisibleItem);
                if (mC != null) {
                    for (int i = 0; i < mC.size(); i++) {
                        if (firstVisibleItem == i) {


                            mC.get(i).setFirst(true);
                            mAdapter.notifyDataSetChanged();
                        } else {


                            mC.get(i).setFirst(false);
                            mAdapter.notifyDataSetChanged();


                        }

                    }


                }


            }
        });*/




    }

    private void getPhoneNum(String phone,String order) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        Random rand = new Random();
        int i = rand.nextInt(100);

        mA = rand.nextInt(10000000);

        String accountId = "1";
        String timeStamp = i + str;

        String sign = accountId + timeStamp + order;


        String mSigns = Rsa.encryptByPublic(sign);
        Log.d("时间戳", mSigns);
        OkHttpUtils.post()
                .url("http://c1.dev.talkvip.cn/Authorization")
                .addParams("accuntID", accountId)
                .addParams("callingPhone", "15102720175")
                .addParams("calledPhone", "13659827958")
                .addParams("dataID", 1 + "")
                .addParams("order", order)
                .addParams("timeStamp", timeStamp)
                .addParams("resultURL", "www.baidu.com")
                .addParams("notifyURL", "www.baidu.com")
                .addParams("remark", "")
                .addParams("type", "2")
                .addParams("line", "2")
                .addParams("signInfo", mSigns).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("电话", "onResponse: " + response);
            }
        });


    }


    private void getData(int page) {

        String token= SPUtils.getString(getActivity(),"token","");



        DebugFlags.logD("测试"+token);
        OkHttpUtils.post()
                .url(Constants.BASE_URL + "/Callphone/"+strs)
                // .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Bearer" + " " + token)
                .addParams("size", "20")
                .addParams("page", page + "")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                hiddenLoadingView();

            }

            @Override
            public void onResponse(String response, int id) {
                DebugFlags.logD("数据" + response);
                hiddenLoadingView();
                Message ms = JSON.parseObject(response, Message.class);


                DebugFlags.logD("数据" + ms.getData());
                handlers.obtainMessage(DATA_LOAD_SUCCESS, ms).sendToTarget();
               /* Data data = ms.getData();
                mList = data.getList();
                //tvPager.setText(1 + "/" + mList.size());
                mC = new ArrayList<CustomFrist>();
                for (int i = 0; i < mList.size(); i++) {
                    mC.add(new CustomFrist(mList.get(i), false));

                }

                mC.get(0).setFirst(true);

                mAdapter = new InfoAdapter(getActivity(), mC);

                lv.setAdapter(mAdapter);
*/
            }

        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.unbind(this);
    }



    public class PhoneBroadcastReceiver extends BroadcastReceiver {


        String TAG = "打电话";
        TelephonyManager telMgr;

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                Log.e("hg", "呼出……OUTING");
            }
            if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Service.TELEPHONY_SERVICE);
                switch (tm.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.e("响铃", "电话状态……RINGING");
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.e("空闲", "电话状态……OFFHOOK");
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.e("挂断", "电话状态……IDLE");

                        Intent inten1=new Intent(getActivity(),RemarkActivity.class);

                        inten1.putExtra("p",mCustom);
                        inten1.putExtra("list",mInfo);
                        inten1.putExtra("postion",mP);

                        //  getActivity(). unregisterReceiver(mycodereceiver);
                        getActivity().startActivity(inten1);

                        break;
                }
            }
        }

    }
    private void CallPhone(String phone) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        //url:统一资源定位符
        //uri:统一资源标示符（更广）
        intent.setData(Uri.parse("tel:" + phone));
        //开启系统拨号器
        getActivity(). startActivity(intent);


    }

    @Override
    public void onPause() {
        super.onPause();
        DebugFlags.logD("界面F失去焦点");

    }

    // 以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面(6.0系统测试可用)
    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package",getActivity(). getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName",getActivity(). getPackageName());
        }
        startActivity(localIntent);
    }



}
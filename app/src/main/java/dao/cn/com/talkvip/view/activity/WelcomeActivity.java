package dao.cn.com.talkvip.view.activity;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import dao.cn.com.talkvip.Constants;
import dao.cn.com.talkvip.R;
import dao.cn.com.talkvip.utils.DebugFlags;
import dao.cn.com.talkvip.utils.RsaU;
import dao.cn.com.talkvip.utils.SPUtils;
import okhttp3.Call;

/**
 * Created by uway on 2017/4/5.
 */
public class WelcomeActivity extends BaseActivity {
    static Handler handler = new Handler();
    private ImageView iv;


    @Override
    protected void initHead() {

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void initView() {
        iv = (ImageView) findViewById(R.id.welcome_iv);




        //    String url = "http://a3.qpic.cn/psb?/V130mBVT10Mxsi/rmpNG7SBqfj8UY.G*exBQz8kCr21PGxnp8WuHw3N0AE!/b/dB8BAAAAAAAA&bo=gAJyBAAAAAADB9Y!&rf=viewer_4";
       /* OkHttpUtils.get().url(Constants.BASE_URL + "/Agreement/getImgUrl").build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {


                Display display = JSON.parseObject(response, Display.class);
                List<Aurl> r = display.getData();
                String a = r.get(0).getUrl();


           new ImageHelper(WelcomeActivity.this).display(iv, Constants.BASE_URL + "/" + a);
              //  Glide.with(WelcomeActivity.this).load( Constants.BASE_URL + "/"+a).into(iv);


            }
        });*/

        //

    }


    @Override
    protected void onStart() {

        super.onStart();
        playAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 取消将要执行的任务
        handler.removeCallbacks(goNextUiRunnable);

    }

    private void playAnimation() {
        // 创建动画集合
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setAnimationListener(animationListener);
        // 构造缩放动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        scaleAnimation.setDuration(1000);

        // 构造透明度动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);

        alphaAnimation.setDuration(2000);

        // 把两个动画都添加到集合中
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        // 一起播放
        iv.startAnimation(animationSet);

    }


    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        // 动画播放完成后，会调用此方法
        @Override
        public void onAnimationEnd(Animation animation) {
            // 过一会跳转
            handler.postDelayed(goNextUiRunnable, 1000);
        }
    };

    Runnable goNextUiRunnable = new Runnable() {

        @Override
        public void run() {
            goNextUi();
        }

    };

    private void goNextUi() {
        String ac = SPUtils.getString(WelcomeActivity.this, "ac", "");
        String pwd = SPUtils.getString(WelcomeActivity.this, "pwd", "");
        DebugFlags.logD("记住密码"+ac+pwd);
        if (!TextUtils.isEmpty(ac) && !TextUtils.isEmpty(pwd)) {

            Wlogin(ac, pwd);


        } else {


            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);

            startActivity(intent);
            finish();


        }


    }

    private void Wlogin(String ac, String pwd) {


        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        String sign = ac + "|" + pwd + "|" + str;
        String strs = RsaU.encryptByPublic(sign);


        OkHttpUtils.post()
                .url(Constants.BASE_URL + "/login/loginOn")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")

                .addParams("sign", strs)

                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        DebugFlags.logD("welcom失败" + e.toString());


                        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);

                        startActivity(intent);
                        finish();


                    }

                    @Override
                    public void onResponse(String response, int id) {
                        DebugFlags.logD("欢迎" + response);

                        try {
                            JSONObject json = new JSONObject(response);

                            String result = json.getString("result");
                            String code = json.getString("code");

                            if ("8888".equals(code)) {

                                String data = json.getString("data");
                                JSONObject json1 = new JSONObject(data);
                                String extract_state = json1.getString("extract_state");
                                String continuous_cal = json1.getString("continuous_call");
                                SPUtils.putString(WelcomeActivity.this,"cc", continuous_cal);
                                String name = json1.getString("realname");
                                String headurl = json1.getString("headurl");
                                SPUtils.putString(WelcomeActivity.this,"username",name);

                                SPUtils.putString(WelcomeActivity.this,"hurl", headurl);
                                SPUtils.putString(WelcomeActivity.this,"extract_state",extract_state);
                                String token = json1.getString("token");
                                SPUtils.putString(WelcomeActivity.this,"token",token);

                                Constants.TOKEN = token;



                              Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                                intent.putExtra("name", name);

                                startActivity(intent);
                                finish();

                            } else {

                                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);

                                startActivity(intent);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });


    }


}

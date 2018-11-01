package com.example.degangel.paydemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private IWXAPI api;


    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;

    private Button bt_alipay, bt_wechat;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            Result payResult = new Result((Map<String, String>) msg.obj);
            String resultInfo = payResult.getResult();// 同步返回需要验证的信息
            String resultStatus = payResult.getResultStatus();
            // 判断resultStatus 为9000则代表支付成功
            if (TextUtils.equals(resultStatus, "9000")) {
                // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
            } else {
                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_alipay = findViewById(R.id.bt_alipay);
        bt_wechat = findViewById(R.id.bt_wechat);

        bt_alipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initAlipay();
            }
        });

        api = WXAPIFactory.createWXAPI(MainActivity.this, null);
        // 将该app注册到微信
        api.registerApp("appid");

        bt_wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initWechat();
            }
        });
    }

    private void initWechat() {
//        微信支付时WXPayEntryActivity这个类必须要放在工程包下面新建一个wxapi的包，将这个类放到wxapi这个包下，并且在清单
//        文件中注册。微信支付的时候一定要打正式包进行测试，无比保证appid的正确性。
        //这些值都是后台返回的
        PayReq request = new PayReq();
        request.appId = "wxd930ea5d5a258f4f";
        request.partnerId = "1900000109";
        request.prepayId = "1101000000140415649af9fc314aa427";
        request.packageValue = "Sign=WXPay";
        request.nonceStr = "1101000000140429eb40476f8896f4c9";
        request.timeStamp = "1398746574";
        request.sign = "7FFECB600D7157C5AA49810D2D8F28BC2811827B";
        api.sendReq(request);
    }

    private void initAlipay() {
        //orderInfo是服务器返回的签过名的数据---格式是唯一的
        final String orderInfo = "";

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
}

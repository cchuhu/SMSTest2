package huhu.com.smstest2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class MainActivity extends Activity implements OnClickListener {


    private EditText inputPhoneEt;
    private EditText inputCodeEt;
    private Button requestCodeBtn;
    private Button commitBtn;

    int i = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }


    private void init() {
        inputPhoneEt = (EditText) findViewById(R.id.login_input_phone_et);
        inputCodeEt = (EditText) findViewById(R.id.login_input_code_et);
        requestCodeBtn = (Button) findViewById(R.id.login_request_code_btn);
        commitBtn = (Button) findViewById(R.id.login_commit_btn);
        requestCodeBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);


        SMSSDK.initSDK(this, "977e03f905c0", "e67972344a8451954ba12d0b5969937f");
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
                Log.e("msg", "" + msg.arg1);
            }
        };

        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View v) {
        String phoneNums = inputPhoneEt.getText().toString();
        switch (v.getId()) {
            case R.id.login_request_code_btn:

                if (!judgePhoneNums(phoneNums)) {
                    return;
                }
                Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG).show();
                SMSSDK.getVerificationCode("86", phoneNums);

                requestCodeBtn.setClickable(false);
                requestCodeBtn.setText("倒计时(" + i + ")");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handler.sendEmptyMessage(-9);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(-8);
                    }
                }).start();
                break;

            case R.id.login_commit_btn:
                SMSSDK.submitVerificationCode("86", phoneNums, inputCodeEt.getText().toString());
                createProgressBar();
                break;
        }
    }

    /**
     *
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -9) {
                requestCodeBtn.setText("倒计时(" + i + ")");
            } else if (msg.what == -8) {
                requestCodeBtn.setText("重新发送");
                requestCodeBtn.setClickable(true);
                i = 30;
            } else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {

                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "�ύ��֤��ɹ�", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "��֤���Ѿ�����", Toast.LENGTH_SHORT).show();
                    } else {
                        ((Throwable) data).printStackTrace();
                    }
                }
            }
        }
    };


    /**
     * 检测手机号是否正确
     *
     * @param phoneNums
     */
    private boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11) && isMobileNO(phoneNums)) {
            return true;
        }
        Toast.makeText(this, "�ֻ�������������", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 检测手机号长度
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str.length() == length ? true : false;
        }
    }


    public static boolean isMobileNO(String mobileNums) {

        String telRegex = "[1][358]\\d{9}";
        if (TextUtils.isEmpty(mobileNums)) return false;
        else return mobileNums.matches(telRegex);
    }

    /**
     * progressbar
     */
    private void createProgressBar() {
        FrameLayout layout = (FrameLayout) findViewById(android.R.id.content);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        ProgressBar mProBar = new ProgressBar(this);
        mProBar.setLayoutParams(layoutParams);
        mProBar.setVisibility(View.VISIBLE);
        layout.addView(mProBar);
    }

    @Override
    protected void onDestroy() {
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }
}

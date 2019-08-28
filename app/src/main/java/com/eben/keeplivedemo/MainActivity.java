package com.eben.keeplivedemo;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * 只需要新建RemoteService、LocalService即可：
 * 1、其中RemoteService是远程进程，LocalService和主进程绑定
 * 2、既然是跨进程了，故通过aidl来实现进程间的通讯【需要进一步分析作用】
 * 3、参考demo：https://github.com/zywudev/AndroidKeepAlive，本demo是在上述demo的基础上优化实现的。不需要
 * 生成两个apk了。
 * 4、保活总结：https://www.jianshu.com/p/b5371df6d7cb
 * 5、需要监听开机广播让主进程开机自启动
 * 6、可以通过adb shell pkill keeplivedemo，来杀死主进程验证
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startService(new Intent(MainActivity.this,LocalService.class));
                startLocalService();
            }
        });
    }

    private void startLocalService() {
        Intent intent = new Intent();
        ComponentName pageCop = new ComponentName("com.eben.keeplivedemo","com.eben.keeplivedemo.LocalService");
        intent.setComponent(pageCop);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

    }
}

package com.eben.keeplivedemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * 只需要新建RemoteService、LocalService即可：
 * 1、其中RemoteService是远程进程，LocalService和主进程绑定
 * 2、既然是跨进程了，故通过aidl来实现进程间的通讯【需要进一步分析作用】
 * 3、参考demo：https://github.com/zywudev/AndroidKeepAlive，本demo是在上述demo的基础上优化实现的。不需要
 * 生成两个apk了。
 * 4、保活总结：https://www.jianshu.com/p/b5371df6d7cb
 * 5、需要监听开机广播让主进程开机自启动
 * 6、可以通过adb shell pkill keeplivedemo或者用adb shell kill -9 +进程号，来杀死主进程验证，用第二个命令
 * 比较准确
 * 7、如果某个进程没有界面（Activity），则需要在Application中执行“主进程需要绑定LocalService”，这时需要区分
 * 两个进程的Application，eg：https://blog.csdn.net/LVXIANGAN/article/details/85323004中的方案二：
 * 7.1、
 *     public static String getProcessName() {
 *         try {
 *             File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
 *             BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
 *             String processName = mBufferedReader.readLine().trim();
 *             mBufferedReader.close();
 *             return processName;
 *         } catch (Exception e) {
 *             e.printStackTrace();
 *             return null;
 *         }
 *     }
 * 7.2、
 *     然后在Application中的onCreate()中判断主进程才执行相关操作：
 *     if(!TextUtils.isEmpty(processName) && processName.equals(this.getPackageName())){
 *          //判断进程名，保证只有主进程运行，此处为主进程初始化逻辑
 *     }
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startLocalService();
        bindLocalService();
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startService(new Intent(MainActivity.this,LocalService.class));
                throw new IllegalStateException();//模拟主进程crash
            }
        });
    }

    /**
     * 主进程需要绑定LocalService
     */
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

    /**
     * 主进程需要绑定LocalService
     */
    private void bindLocalService(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.eben.keeplivedemo","com.eben.keeplivedemo.LocalService"));
        if (!getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
            Log.d(TAG, "bindRemoteService: 绑定远程服务失败");
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: 连接成功");
        }

        /**
         *在该场景中当RemoteService绑定的LocalService被Kill时，会调用该方法。即杀死主进程时，会调用该方法。
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: 断开连接，需要重新启动");
        }
    };
}

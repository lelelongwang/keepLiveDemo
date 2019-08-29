package com.eben.keeplivedemo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class LocalService extends Service {

    private static final String TAG = "LocalService ljh";
    private IMyAidlInterface iMyAidlInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        //MainActivity中用bindService的话，就不需要用通知了
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    createNotificationChannel();
        //}
        bindRemoteService();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "channel_01";
        CharSequence name ="keep live demo";
        String description ="keep live demo description";
        int importance = NotificationManager.IMPORTANCE_MIN;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mNotificationManager.createNotificationChannel(mChannel);

        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(this, id)
                .setContentTitle(name).setContentText(description)
                .setSmallIcon(null)
                .setChannelId(id)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
       return stub;
    }

    private IMyAidlInterface.Stub stub = new IMyAidlInterface.Stub() {
        @Override
        public void bindSuccess() throws RemoteException {
            Log.d(TAG, "bindSuccess: ");
        }

        @Override
        public void unbind() throws RemoteException {
            Log.d(TAG, "unbind: ");
            getApplicationContext().unbindService(conn);
        }
    };

    /**
     * LocalService需要绑定RemoteService
     */
    private void bindRemoteService(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.eben.keeplivedemo","com.eben.keeplivedemo.RemoteService"));
        if (!getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
            Log.d(TAG, "bindRemoteService: 绑定远程服务失败");
            stopSelf();
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: 连接成功");
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                iMyAidlInterface.bindSuccess();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * 在该场景中当LocalService绑定的RemoteService被Kill时，会调用该方法。即杀远程进程时，会调用该方法。
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: 断开连接，需要重新启动");
            bindRemoteService();
        }
    };
}

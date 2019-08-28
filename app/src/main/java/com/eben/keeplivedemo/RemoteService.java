package com.eben.keeplivedemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class RemoteService extends Service {

    private static final String TAG = "RemoteService ljh";

    private IMyAidlInterface iMyAidlInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        bindLocalService();
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
     * RemoteService需要绑定LocalService
     */
    private void bindLocalService(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.eben.keeplivedemo","com.eben.keeplivedemo.LocalService"));
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
         *在该场景中当RemoteService绑定的LocalService被Kill时，会调用该方法。即杀死主进程时，会调用该方法。
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: 断开连接，需要重新启动");
            bindLocalService();
        }
    };
}

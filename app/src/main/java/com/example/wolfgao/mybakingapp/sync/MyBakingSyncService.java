package com.example.wolfgao.mybakingapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by gaochuang on 2017/12/26.
 * Try to make MyBakingSyncAdaptor run in background as a service
 */

public class MyBakingSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static MyBakingSyncAdaptor sMyBakingSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("MyBakingSyncService", "onCreate - MyBakingSyncService");
        synchronized (sSyncAdapterLock) {
            if (sMyBakingSyncAdapter == null) {
                sMyBakingSyncAdapter = new MyBakingSyncAdaptor(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sMyBakingSyncAdapter.getSyncAdapterBinder();
    }
}

package com.example.wolfgao.mybakingapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by gaochuang on 2017/12/29.
 * If you didn't add the 2 class MyBakingAuthenticatorService and MyBakingAuthenticator, when running, the app
 * will raise security exception:
 * java.lang.SecurityException: uid 10287 cannot get secrets for accounts of type: wolfgao.example.com
 */

public class MyBakingAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private MyBakingAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new MyBakingAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

package br.com.simplepass.cadevan.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by leandro on 3/7/16.
 */
public class AccountService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }

}

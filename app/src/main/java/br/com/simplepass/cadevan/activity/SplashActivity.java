package br.com.simplepass.cadevan.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.simplepass.cadevan.R;
import br.com.simplepass.cadevan.utils.Constants;
import me.pushy.sdk.Pushy;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Pushy.listen(this);

        waitAndMove();
    }

    private void waitAndMove(){
        int SPLASH_TIME_OUT = 1500;

        final SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {

            /* Vamos mostrar nossa splash screen, depois passa para o app */
            @Override
            public void run() {
                AccountManager accountManager = AccountManager.get(SplashActivity.this);
                Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

                if (accounts.length > 0) {
                    long chosenDriverId =
                            sharedPreferences.getLong(Constants.SharedPrefs.CHOSEN_DRIVER_ID, -1);

                    Intent intent;
                    if(chosenDriverId == -1) {
                        intent = new Intent(SplashActivity.this, ChooseDriverActivity.class);
                    } else{
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra(MainActivity.EXTRA_CHOOSEN_VAN_ID, chosenDriverId);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                                SplashActivity.this);

                        startActivity(intent, activityOptions.toBundle());
                    } else {
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                                SplashActivity.this);

                        startActivity(intent, activityOptions.toBundle());
                    } else {
                        startActivity(intent);
                    }
                }
            }
        }, SPLASH_TIME_OUT);
    }
}

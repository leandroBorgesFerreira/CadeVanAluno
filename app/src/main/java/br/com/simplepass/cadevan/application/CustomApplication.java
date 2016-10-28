package br.com.simplepass.cadevan.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Application costumizado. Feito para ser utilizado o Realm
 */
public class CustomApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("realm-br.com.cadevan-db.realm")
                .schemaVersion(14L)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(realmConfiguration);
    }
}

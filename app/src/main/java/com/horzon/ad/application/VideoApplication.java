package com.horzon.ad.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class VideoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build() ;
        Realm.setDefaultConfiguration(config);
    }
}

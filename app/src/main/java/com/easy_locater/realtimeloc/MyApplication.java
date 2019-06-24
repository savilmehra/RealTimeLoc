package com.easy_locater.realtimeloc;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {


    @Override
    public void onCreate() {
        super.onCreate();


        try {
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Fabric.with(this, new Crashlytics());
        logUser();
      //  startService(new Intent(MyApplication.this, CommonAppService.class));

    }


    private void logUser() {

        Crashlytics.setUserIdentifier("12345");
        Crashlytics.setUserEmail("user@fabric.io");
        Crashlytics.setUserName("Test User");
    }



    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private void addLocationToServer(String lat, String lon) {
        DatabaseReference databaseArtists;
        databaseArtists = FirebaseDatabase.getInstance().getReference("location");

        DeBug.showLog("added", "yes");
        //checking if the value is provided
        if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {

            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Artist

            String id = new TinyDB(getApplicationContext()).getString("loginNumber");
            //creating an Artist Object
            UserNew newUser = new UserNew(lat, lon);

            //Saving the Artist
            databaseArtists.child(id).setValue(newUser);
            //setting edittext to blank again
            //displaying a success toast

        } else {
            //if the value is not given displaying a toast

        }
    }
}

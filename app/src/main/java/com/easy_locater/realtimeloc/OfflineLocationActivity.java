package com.easy_locater.realtimeloc;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.MyJobIntentService;
import android.widget.Toast;

public class OfflineLocationActivity extends MyJobIntentService {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, " MyService Created ", Toast.LENGTH_LONG).show();
    }



    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Servics Stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Toast.makeText(this, " MyService Started", Toast.LENGTH_LONG).show();
    }

}
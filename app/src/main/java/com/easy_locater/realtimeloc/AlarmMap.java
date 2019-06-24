package com.easy_locater.realtimeloc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.greenrobot.eventbus.EventBus;


public class AlarmMap extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        EventBus.getDefault().post(new EventGetMapData(context));


    }


}
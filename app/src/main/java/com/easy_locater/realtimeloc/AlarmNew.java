package com.easy_locater.realtimeloc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmNew extends BroadcastReceiver
{
    AlarmOffline alarm = new AlarmOffline();
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
            alarm.setAlarm(context);
        }
    }


}
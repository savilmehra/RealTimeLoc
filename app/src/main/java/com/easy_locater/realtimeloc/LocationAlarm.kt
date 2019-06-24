package com.easy_locater.realtimeloc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.greenrobot.eventbus.EventBus

class LocationAlarm : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        EventBus.getDefault().post(EventLocationAlarm(context))


    }


}
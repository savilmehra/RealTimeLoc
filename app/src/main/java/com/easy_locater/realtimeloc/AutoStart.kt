package com.easy_locater.realtimeloc


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class AutoStart : BroadcastReceiver() {
    internal var alarm = Alarm()
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

        }


    }
}

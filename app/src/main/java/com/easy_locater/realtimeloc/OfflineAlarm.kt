package com.easy_locater.realtimeloc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit


class OfflineAlarm : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val wifiWorkBuilder = PeriodicWorkRequest.Builder(
            WorkerForLocation::class.java, 15, TimeUnit.MINUTES
        )
            .addTag("location")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.METERED).build())
        val wifiWork = wifiWorkBuilder.build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("location", ExistingPeriodicWorkPolicy.REPLACE, wifiWork)
        // Toast.makeText(context, "offline alarm here i come", Toast.LENGTH_SHORT).show()
        /* val periodicWorkRequest = PeriodicWorkRequest.Builder(WorkerForLocation::class.java, 15, TimeUnit.MINUTES)
             .build()
         WorkManager.getInstance().enqueue(periodicWorkRequest)*/


    }


}
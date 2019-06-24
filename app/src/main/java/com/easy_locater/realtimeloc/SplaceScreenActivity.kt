package com.easy_locater.realtimeloc

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import androidx.work.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*
import java.util.concurrent.TimeUnit


class SplaceScreenActivity : Activity() {
    internal var databaseArtists: DatabaseReference? = null
    var id: String? = null
    var PERMISSION_ALL = 1
    internal var alarm = AlarmOffline()
    val GCM_REPEAT_TAG = "repeat|[7200,1800]"
    var PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION

    )


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splace_activity)
        TinyDB(this@SplaceScreenActivity).putBoolean("isActiveTrip", true)
        /*startService(Intent(this, OfflineLocationActivity::class.java))
        val cal = Calendar.getInstance()
        val intent = Intent(this, OfflineLocationActivity::class.java)
        val pintent = PendingIntent
            .getService(this, 0, intent, 0)*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        } else {
            startMainActivty()
        }


    }

    fun startMainActivty() {

        val periodicLocation = PeriodicWorkRequest.Builder(
            WorkerForLocation::class.java, 15, TimeUnit.MINUTES
        )
            .addTag("location")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.METERED).build())
        val wifiWork = periodicLocation.build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("location", ExistingPeriodicWorkPolicy.REPLACE, wifiWork)
        databaseArtists = FirebaseDatabase.getInstance().getReference("User")
        if (TinyDB(this).getString("loginNumber") != null && !TinyDB(this).getString("loginNumber").equals(""))
            databaseArtists!!.child(TinyDB(this).getString("loginNumber")).setValue(FirebaseInstanceId.getInstance().getToken());
        triggerAlarmManagerOffline()
        alarm.setAlarm(this)
        NewService.enqueueWork(applicationContext, Intent())
        Handler().postDelayed({
            val i = Intent(this@SplaceScreenActivity, MainHomePageActivity::class.java)
            startActivity(i)
            finish()
        }, SPLASH_TIME_OUT.toLong())


        /*  val constraints = Constraints.Builder()
         .setRequiredNetworkType(NetworkType.CONNECTED)
         .build()
     this.startService(Intent(this@SplaceScreenActivity, LocationService::class.java))
     triggerAlarmManager()
     Toast.makeText(this, "entering location service", Toast.LENGTH_SHORT).show()
     DeBug.showLog("Service Started", "enter")*/
    }

    fun hasPermissions(context: Context?, vararg permissions: Array<String>): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission.toString()
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    companion object {

        // Splash screen timer
        private val SPLASH_TIME_OUT = 3000
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startMainActivty()
                    turnGPSOn()
                } else {
                    startMainActivty()
                }
                return
            }


        }// other 'case' lines to check for other
        // permissions this app might request
    }

    override fun onResume() {
        super.onResume()

    }

    private fun turnGPSOn() {
        val provider = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)

        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider")
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            sendBroadcast(poke)
        }
    }

    fun triggerAlarmManager() {
        val cal = Calendar.getInstance()
        // add alarmTriggerTime seconds to the calendar object
        cal.add(Calendar.SECOND, 15)
        val ALARM_REQUEST_CODE = 344
        val alarmIntent: Intent = Intent(this, LocationAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, alarmIntent, 0)
        val manager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.set(
            AlarmManager.RTC_WAKEUP,
            cal.getTimeInMillis(),

            pendingIntent
        )
    }


    fun triggerAlarmManagerOffline() {

        val ALARM_REQUEST_CODE = 13993
        val alarmIntent: Intent = Intent(this, OfflineAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, alarmIntent, 0)
        val manager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 100, pendingIntent);

    }


}

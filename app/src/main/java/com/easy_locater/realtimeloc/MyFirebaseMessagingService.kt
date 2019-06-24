package com.easy_locater.realtimeloc


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.ContactsContract
import android.support.v4.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.TimeUnit

class MyFirebaseMessagingService : FirebaseMessagingService() {
    /*
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        DatabaseReference databaseArtists = FirebaseDatabase.getInstance().getReference("User");
        LoggedUsers newUser = new LoggedUsers(s);

        //Saving the Artist
        if (new TinyDB(this).getString("loginNumber") != null && !new TinyDB(this).getString("loginNumber").equalsIgnoreCase(""))
            databaseArtists.child(new TinyDB(this).getString("loginNumber")).setValue(s);
    }*/

    override fun onMessageReceived(message: RemoteMessage?) {


        var notificationTitle: String? = null
        var notificationBody: String? = null

        // Check if message contains a notification payload.
        if (message!!.notification != null) {

            notificationTitle = message.notification!!.title
            notificationBody = message.notification!!.body
            getContactName(notificationBody, applicationContext)
        }


    }

    fun getContactName(phoneNumber: String?, context: Context) {

        var contactName: String? = null
        try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))

            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            contactName = ""
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(0)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val intent = Intent(this, SplaceScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.route)
            .setContentTitle(contactName!! + " Wants to Know Your Location")
            .setContentText("")
            .setAutoCancel(false)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
        val periodicLocation = PeriodicWorkRequest.Builder(
            WorkerForLocation::class.java, 15, TimeUnit.MINUTES
        )
            .addTag("location")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.METERED).build())
        val wifiWork = periodicLocation.build()
    }

}
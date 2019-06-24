package com.easy_locater.realtimeloc

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.database.*

class GetLocationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    var phoneNumber:String = ""
    internal var databaseArtists: DatabaseReference? = null


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        DeBug.showLog("Service Started  new", "getting location" +
                "")
        phoneNumber=TinyDB(this).getString("user_number")
        databaseArtists = FirebaseDatabase.getInstance().getReference("location")
        databaseArtists!!.child(phoneNumber).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user = dataSnapshot.getValue(UserNew::class.java)
                if (user != null ) {

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return Service.START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()

        DeBug.showLog("Service Started new", "end")

    }

}
package com.easy_locater.realtimeloc

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.provider.ContactsContract
import android.support.v4.app.JobIntentService
import android.support.v4.app.MyJobIntentService
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import java.util.*

class NewService : MyJobIntentService() {

    private var fromDatabase: MutableList<String>? = null
    private var contactsList: MutableList<String>? = null

    companion object {

        fun enqueueWork(context: Context, work: Intent) {
            try {
                JobIntentService.enqueueWork(context, NewService::class.java!!, 49, work)
            } catch (e: Exception) {

            }

        }
    }

    override fun onHandleWork(intent: Intent) {
        val FetchUrl = FetchUrl()
        FetchUrl.execute()

    }

    override fun onDestroy() {
        super.onDestroy()

        DeBug.showLog("Service Started new", "end")

    }

    // Fetches data from url passed
    private inner class FetchUrl : AsyncTask<String, Void, List<String>>() {

        override fun doInBackground(vararg pp: String): List<String> {

            var gg: List<String> = ArrayList<String>()
            try {

                gg = getContactList()
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }

            return gg
        }

        override fun onPostExecute(result: List<String>?) {
            super.onPostExecute(result)

            if (result != null) {

                contactsList = result as MutableList<String>?

                val ref = FirebaseDatabase.getInstance().getReference("User")
                ref.addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            // Result will be holded Here
                            val td = dataSnapshot.value as HashMap<String, String>?
                            if (td != null) {
                                fromDatabase = ArrayList(td.keys)
                                val tt = HashMap<String, String>()
                                for ((key, value) in td) {
                                    tt[key] = value
                                }


                                if (contactsList != null && fromDatabase != null)
                                    fromDatabase!!.retainAll(contactsList!!)

                                TinyDB(applicationContext).putListString(
                                    "contactlist",
                                    fromDatabase as ArrayList<String>
                                )
                                TinyDB(applicationContext).saveHashMap(
                                    "hashmap",
                                    tt
                                )

                                EventBus.getDefault().post(EventContacts(this@NewService))
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            //handle databaseError
                        }
                    })
            }


        }
    }

    private fun getContactList(): List<String> {

        val hm = ArrayList<String>()
        val cr = contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )


                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )
                    while (pCur!!.moveToNext()) {
                        var phoneNo = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        if (phoneNo.length > 10) {
                            phoneNo = phoneNo.substring(phoneNo.length - 10)
                        }
                        hm.add(phoneNo)

                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
        return hm
    }
}
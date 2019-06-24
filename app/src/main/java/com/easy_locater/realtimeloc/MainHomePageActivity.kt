package com.easy_locater.realtimeloc

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.SearchView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.main_home_page.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


class MainHomePageActivity : AppCompatActivity(), ContactsAdapter.ContactsAdapterListener {

    private var interstitialAd: InterstitialAd? = null
    private val JSON: MediaType = MediaType.parse("application/json; charset=utf-8")
    private val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
    internal var mClient = OkHttpClient()
    private var locationManager: LocationManager? = null
    private var googleMap: GoogleMap? = null
    internal var databaseArtists: DatabaseReference? = null
    private var id: String? = null
    private var fab: SwitchCompat? = null;
    val MAP_TYPE_NORMAL = 1
    val MAP_TYPE_SATELLITE = 2
    private var phoneNumber: String? = null
    private val auth: FirebaseAuth? = null
    private var contactHas: HashMap<String, String>? = null
    private val databaseHas: Map<String, Any>? = null
    private var fromDatabase: MutableList<String>? = null
    private var contactsList: MutableList<String>? = null
    private val userWIthAppInstalled: List<String>? = null
    private val mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private val verificationCode: String? = null
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    private var recyclerView: RecyclerView? = null
    private val searchView: SearchView? = null
    private var mAdapter: ContactsAdapter? = null
    private var progressBar: ProgressBar? = null
    private var marker: Marker? = null
    private val options = MarkerOptions()
    var checked: Boolean = false
    private var share: ImageView? = null
    private var lytContactPermission: LinearLayout? = null
    private var isSetting: Boolean = false
    var mAdView: AdView? = null
    @Subscribe
    fun onEvent(eventHomeResumed: EventGetDataFromDataBase) {
        getDtaFromDatabse()
    }

    @Subscribe
    fun onEvent(contact: EventContacts) {
        getContact()
    }

    fun triggerAlarmManagerLocation() {
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

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
            && isSetting
            && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            NewService.enqueueWork(applicationContext, Intent())
            //  this.startService(Intent(this@MainHomePageActivity, LocationService::class.java))
            isSetting = false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
            && isSetting
            && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            //   this.startService(Intent(this@MainHomePageActivity, LocationService::class.java))
            isSetting = false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
            && isSetting
            && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            NewService.enqueueWork(applicationContext, Intent())
            isSetting = false
        }

    }

    fun initToolbarNew(title: String) {

        val collapsingToolbarLayout = findViewById(R.id.collapse_toolbar) as CollapsingToolbarLayout
        var appBarLayout = findViewById(R.id.app_bar) as AppBarLayout
        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            internal var isShow = true
            internal var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.title = "Location Tracker"

                    isShow = true
                } else if (isShow) {
                    collapsingToolbarLayout.title =
                        " "//carefull there should a space between double quote otherwise it wont work
                    isShow = false
                }
            }
        })
        val mToolbar2 = findViewById<Toolbar>(R.id.toolbar)


        setSupportActionBar(mToolbar2)

        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        //  supportActionBar!!.title = title
        mToolbar2.setNavigationOnClickListener { onBackPressed() }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_home_page)
        EventBus.getDefault().register(this)
        findViews()
        initToolbarNew("Location Tracker")
        databaseArtists = FirebaseDatabase.getInstance().getReference("location")
        phoneNumber = TinyDB(this).getString("loginNumber")
        TinyDB(this).putString("user_number", phoneNumber)
        if (TinyDB(this).getDouble("lat", 0.0) != 0.0) {
            initializeMap(TinyDB(this).getDouble("lat", 0.0).toString(), TinyDB(this).getDouble("lon", 0.0).toString())
            triggerAlarmManager()
        } else {
            databaseArtists!!.child(phoneNumber!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val user = dataSnapshot.getValue(UserNew::class.java)
                    if (user != null && !isFinishing) {
                        initializeMap(user!!.lat, user.long)
                        triggerAlarmManager()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainHomePageActivity, "failed", Toast.LENGTH_SHORT).show()
                }
            })
        }


        lytContactPermission = findViewById(R.id.lytContactPermission) as LinearLayout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            lytContactPermission!!.visibility = View.VISIBLE
        } else {
            lytContactPermission!!.visibility = View.GONE
        }
        val sw = findViewById(R.id.switch1) as Switch
        sw.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                if (isChecked) {
                    checked = true
                    googleMap!!.setMapType(MAP_TYPE_NORMAL)
                } else {
                    googleMap!!.setMapType(MAP_TYPE_SATELLITE)
                }
            }
        })

        val sw2 = findViewById(R.id.switch2) as Switch
        sw2.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                if (isChecked) {

                    isSetting = true
                    val intent = Intent()
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    var uri = Uri.fromParts("package", this@MainHomePageActivity.getPackageName(), null)
                    intent.setData(uri);
                    this@MainHomePageActivity.startActivity(intent)

                } else {

                }
            }
        })


        val Userlist = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getContact()
        }


        //////////////=========================ads

        val adRequest = AdRequest.Builder()
            .build()

        mAdView!!.adListener = object : AdListener() {
            override fun onAdLoaded() {}

            override fun onAdClosed() {

            }

            override fun onAdFailedToLoad(errorCode: Int) {

            }

            override fun onAdLeftApplication() {

            }

            override fun onAdOpened() {
                super.onAdOpened()
            }
        }

        mAdView!!.loadAd(adRequest)

        interstitialAd = InterstitialAd(this)


        // set the ad unit ID
        interstitialAd!!.adUnitId = "ca-app-pub-4164184164875270/7526353616"

        val adRequest2 = AdRequest.Builder().build()

        // Load ads into Interstitial Ads
        interstitialAd!!.loadAd(adRequest2)

        interstitialAd!!.adListener = object : AdListener() {

            override fun onAdLoaded() {
                super.onAdLoaded()
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }
        }

    }


    private fun initializeMap(lat: String, long: String) {

        val position = LatLng(lat.toDouble(), long.toDouble())

        // Instantiating MarkerOptions class
        val options = MarkerOptions()

        // Setting position for the MarkerOptions
        options.position(position)


        val fm = supportFragmentManager.findFragmentById(R.id.map1) as SupportMapFragment
        fm.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(mGoogleMap: GoogleMap) {
                googleMap = mGoogleMap


                marker = googleMap!!.addMarker(options)

                val camPos = CameraPosition.Builder()
                    .target(LatLng(lat.toDouble(), long.toDouble()))
                    .zoom(15F)
                    .build()

                val camUpd3 = CameraUpdateFactory.newCameraPosition(camPos)

                googleMap!!.animateCamera(camUpd3)
                setMap(MAP_TYPE_SATELLITE)
            }
        })


    }

    fun setMap(type: Int) {
        try {

            // Changing map type
            if (!checked)
                googleMap!!.setMapType(type)


            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Showing / hiding your current location
                googleMap!!.setMyLocationEnabled(false)
            } else {
                //Toast.makeText(this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Showing / hiding your current location
                    googleMap!!.setMyLocationEnabled(false)
                }
            }


            // Enable / Disable zooming controls
            googleMap!!.getUiSettings().setZoomControlsEnabled(true)

            // Enable / Disable my location button
            googleMap!!.getUiSettings().setMyLocationButtonEnabled(false)

            // Enable / Disable Compass icon
            googleMap!!.getUiSettings().setCompassEnabled(true)

            // Enable / Disable Rotate gesture
            googleMap!!.getUiSettings().setRotateGesturesEnabled(true)

            // Enable / Disable zooming functionality
            googleMap!!.getUiSettings().setZoomGesturesEnabled(true)


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun getDtaFromDatabse() {

        marker!!.position = LatLng(TinyDB(this).getDouble("lat", 0.0), TinyDB(this).getDouble("lon", 0.0))

    }


    companion object {

        private val TAG = "MainActivity"
    }


    fun triggerAlarmManager() {
        val cal = Calendar.getInstance()
        // add alarmTriggerTime seconds to the calendar object
        cal.add(Calendar.SECOND, 10)
        val ALARM_REQUEST_CODE = 133
        val alarmIntent: Intent = Intent(this, Alarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, alarmIntent, 0)
        val manager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.set(
            AlarmManager.RTC_WAKEUP,
            cal.getTimeInMillis(),

            pendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, Alarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 133, intent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        TinyDB(this@MainHomePageActivity).putBoolean("isForeground", false)
        TinyDB(this@MainHomePageActivity).putBoolean("isActiveTrip", false)
    }


    private fun findViews() {
        progressBar = findViewById(R.id.circularProgressBar) as ProgressBar
        mAdView = findViewById<AdView>(R.id.adView)
        share = findViewById(R.id.share) as ImageView
        share!!.setOnClickListener {
            shareIt()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                lytContactPermission!!.visibility = View.INVISIBLE
                // Permission is granted
                contactsList = ArrayList()

                contactsList = TinyDB(this).getListString("contactlist")
                contactHas = TinyDB(this).getHashMap("hashmap")

                if (contactsList != null && contactHas != null
                    && contactsList!!.size > 0 && !contactHas!!.isEmpty()
                ) {
                    recyclerView = findViewById(R.id.recycler_view)

                    mAdapter =
                        ContactsAdapter(
                            this@MainHomePageActivity,
                            contactsList,
                            this@MainHomePageActivity,
                            contactHas
                        )
                    val mLayoutManager = LinearLayoutManager(applicationContext)
                    recyclerView!!.setLayoutManager(mLayoutManager)
                    recyclerView!!.setItemAnimator(DefaultItemAnimator())
                    recyclerView!!.setAdapter(mAdapter)

                } else {
                    progressBar!!.setVisibility(View.VISIBLE)
                    val FetchUrl = FetchUrl()
                    FetchUrl.execute()
                }
            } else {
                lytContactPermission!!.visibility = View.VISIBLE
                /* Toast.makeText(this, "Until you grant the permission, we cannot display Contacts", Toast.LENGTH_SHORT)
                     .show()*/
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


    // Fetches data from url passed
    private inner class FetchUrl : AsyncTask<String, Void, List<String>>() {

        override fun doInBackground(vararg pp: String): List<String> {

            var gg: List<String> = ArrayList()
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

                contactsList = (result as MutableList<String>?)

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


                                if (contactsList != null && fromDatabase != null) {
                                    fromDatabase!!.retainAll(contactsList!!)



                                    recyclerView = findViewById(R.id.recycler_view)
                                    mAdapter = ContactsAdapter(
                                        this@MainHomePageActivity,
                                        fromDatabase,
                                        this@MainHomePageActivity,
                                        tt
                                    )
                                    val mLayoutManager = LinearLayoutManager(applicationContext)
                                    recyclerView!!.setLayoutManager(mLayoutManager)
                                    recyclerView!!.setItemAnimator(DefaultItemAnimator())
                                    recyclerView!!.setAdapter(mAdapter)
                                    progressBar!!.setVisibility(View.GONE)
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            //handle databaseError
                        }
                    })
            }


        }
    }

    override fun onContactSelected(contact: String) {
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
    private fun whiteNotificationBar(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
            window.statusBarColor = Color.WHITE
        }
    }

    /* @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main, menu);

         // Associate searchable configuration with the SearchView
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchView = (SearchView) menu.findItem(R.id.action_search)
                 .getActionView();
         searchView.setSearchableInfo(searchManager
                 .getSearchableInfo(getComponentName()));
         searchView.setMaxWidth(Integer.MAX_VALUE);

         // listening to search query text change
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String query) {
                 // filter recycler view when query submitted
                 mAdapter.getFilter().filter(query);
                 return false;
             }

             @Override
             public boolean onQueryTextChange(String query) {
                 // filter recycler view when text is changed
                 mAdapter.getFilter().filter(query);
                 return false;
             }
         });
         return true;
     }*/

    private fun shareIt() {
        try {

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Location")
            var shareMessage = "http://maps.google.com/?q=" + TinyDB(this).getDouble(
                "lat",
                0.0
            ).toString() + "," + TinyDB(this).getDouble("lon", 0.0).toString()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            if (!isFinishing)
                startActivity(Intent.createChooser(shareIntent, "Share Location"))

        } catch (e: Exception) {
        }

    }

    fun getNumber(cr: ContentResolver): List<String> {
        try {
            val phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
            while (phones!!.moveToNext()) {
                val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                contactsList!!.add(phoneNumber!!)
            }
            phones.close()// close cursor
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return contactsList!!
    }

    fun getContact() {
        contactsList = ArrayList<String>()

        contactsList = TinyDB(this).getListString("contactlist")
        contactHas = TinyDB(this).getHashMap("hashmap")

        if (contactsList != null && contactHas != null
            && contactsList!!.size > 0 && !contactHas!!.isEmpty()
        ) {
            recyclerView = findViewById(R.id.recycler_view)

            mAdapter =
                ContactsAdapter(this@MainHomePageActivity, contactsList, this@MainHomePageActivity, contactHas)
            val mLayoutManager = LinearLayoutManager(applicationContext)
            recyclerView!!.setLayoutManager(mLayoutManager)
            recyclerView!!.setItemAnimator(DefaultItemAnimator())
            recyclerView!!.setAdapter(mAdapter)

        } else {
            progressBar!!.setVisibility(View.VISIBLE)
            val FetchUrl = FetchUrl()
            FetchUrl.execute()
        }
    }


    private fun showInterstitial() {
        if (interstitialAd!!.isLoaded()) {
            interstitialAd!!.show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()


        if (interstitialAd != null && interstitialAd!!.isLoaded()) {
            val intent = Intent(this, ExitScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            this@MainHomePageActivity.finish()
            showInterstitial()

        }
    }
}
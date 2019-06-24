package com.easy_locater.realtimeloc

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {


    private var MarkerPoints: java.util.ArrayList<LatLng>? = null
    private val JSON: MediaType = MediaType.parse("application/json; charset=utf-8")
    private val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
    internal var mClient = OkHttpClient()
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    private var locationManager: LocationManager? = null
    private var mMap: GoogleMap? = null
    internal var databaseArtists: DatabaseReference? = null
    private var id: String? = null
    val MAP_TYPE_NORMAL = 1
    val MAP_TYPE_SATELLITE = 2
    private var marker: Marker? = null
    var checked: Boolean = false
    private var fab: FloatingActionButton? = null;
    private var phoneNumber: String = ""
    private var startingLatlong: LatLng? = null
    private var newlatlong: LatLng? = null
    private var marker2: Marker? = null
    private var isFirstTime: Boolean = false
    var mAdView: AdView? = null
    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

    @Subscribe
    fun onEvent(eventHomeResumed: EventGetDataFromDataBase) {
        getDtaFromDatabse()
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
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        MarkerPoints = java.util.ArrayList()
        EventBus.getDefault().register(this)

        databaseArtists = FirebaseDatabase.getInstance().getReference("location")
        phoneNumber = intent.getStringExtra("phoneNumber")
        id = intent.getStringExtra("id")
        TinyDB(this).putString("user_number", phoneNumber)

        databaseArtists!!.child(phoneNumber).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user = dataSnapshot.getValue(UserNew::class.java)
                if (user != null && !isFinishing) {
                    if (!isFirstTime) {
                        isFirstTime = true
                        startingLatlong = LatLng(user!!.lat.toDouble(), user.long.toDouble())
                    }
                    newlatlong = LatLng(user!!.lat.toDouble(), user.long.toDouble())


                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map1) as SupportMapFragment?
                    mapFragment!!.getMapAsync(this@HomeActivity)
                    // initializeMap(user!!.lat, user.long)
                    triggerAlarmManager()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "failed", Toast.LENGTH_SHORT).show()
            }
        })


        fab = findViewById(R.id.fab) as FloatingActionButton
        fab!!.setOnClickListener {

            val i = Intent(this@HomeActivity, MapsActivity::class.java)
            i.putExtra("phoneNumber", phoneNumber)
            startActivity(i)
        }
        val sw = findViewById<Switch>(R.id.switch1)
        sw.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {

                if (isChecked) {
                    checked = true
                    mMap!!.setMapType(GoogleMap.MAP_TYPE_NORMAL)
                } else {
                    mMap!!.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
                }
            }
        })
        sendMessage("Lets Share Location", "", "", "Love")

        mAdView = findViewById<AdView>(R.id.adView)
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
    }


    /*   private fun initializeMap(lat: String, long: String) {

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


       }*/

    /*fun setMap(type: Int) {
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

    }*/

    fun sendMessage(title: String, body: String, icon: String, message: String) {

        var registrationIds: MutableList<String> = ArrayList<String>()
        registrationIds!!.add(id!!)
        var notifi = com.easy_locater.realtimeloc.Notification()
        var bodyN = NotificationBody()
        notifi.text = TinyDB(this@HomeActivity).getString("loginNumber")
        notifi.title = "Share You location with "
        bodyN.notification = notifi
        bodyN.registrationIds = registrationIds
        bodyN.priority = "high"
        var gson = GsonBuilder().create()
        var json = gson.toJson(bodyN)
        var jsonObj = JSONObject(json)
        var ja: JSONArray = JSONArray()
        ja.put(FirebaseInstanceId.getInstance().getToken())
        //ja.put(TinyDB(this).getString("refreshedToken"))

        object : AsyncTask<String, String, String>() {
            override fun doInBackground(vararg params: String): String? {
                try {
                    val root = JSONObject()
                    val notification = JSONObject()
                    notification.put("body", TinyDB(this@HomeActivity).getString("refreshedToken"))
                    notification.put("title", title)
                    notification.put("icon", icon)

                    val data = JSONObject()
                    data.put("message", TinyDB(this@HomeActivity).getString("refreshedToken"))
                    root.put("notification", notification)
                    root.put("data", data)
                    root.put("registration_ids", ja)

                    val result = postToFCM(jsonObj.toString())

                    return result
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(result: String) {
                try {
                    val resultJson = JSONObject(result)
                    val success: Int
                    val failure: Int
                    success = resultJson.getInt("success")
                    failure = resultJson.getInt("failure")
                    Toast.makeText(
                        this@HomeActivity,
                        success.toString()+failure.toString()+    "We Have Sent the  Notification,We will Show You Their Location As soon As They Accept Your Request,Till Then We Will Show You Their Last Known Location",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this@HomeActivity, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG)
                        .show()
                }

            }
        }.execute()
    }

    @Throws(IOException::class)
    internal fun postToFCM(bodyString: String): String {
        val body = RequestBody.create(JSON, bodyString)
        val request = Request.Builder()
            .url(FCM_MESSAGE_URL)
            .post(body)
            .addHeader(
                "Authorization",
                "key=AAAAywW_yMA:APA91bFaRNNA3t5oxfCsoerLipPxPtL65jdkBEwYcz8kXi8KqLxOtmxPcDFG6-JYZlHRPRuaodsqHOXES--prCyFLHi_ec_xagkB_DqjaBqhhkcsmysbCUUxxga8UVuaPM-i2ZbP2HYE"
            )
            .build()
        val response = mClient.newCall(request).execute()
        return response.body().string()
    }

    private fun getDtaFromDatabse() {

        databaseArtists!!.child(phoneNumber).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user = dataSnapshot.getValue(UserNew::class.java)
                if (user != null && !isFinishing) {


                    newlatlong = LatLng(user!!.lat.toDouble(), user.long.toDouble())

                    if (startingLatlong != null)
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(startingLatlong))


                    // Already two locations
                    if (MarkerPoints!!.size > 1) {
                        MarkerPoints!!.clear()
                        mMap!!.clear()
                    }

                    // Adding new item to the ArrayList
                    MarkerPoints!!.add(startingLatlong!!)
                    MarkerPoints!!.add(newlatlong!!)
                    // Creating MarkerOptions
                    val options = MarkerOptions()
                    options.position(startingLatlong!!)
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    marker = mMap!!.addMarker(options)
                    /// destination marker
                    val optionsDest = MarkerOptions()
                    optionsDest.position(newlatlong!!)
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    marker2 = mMap!!.addMarker(optionsDest)
                    // Getting URL to the Google Directions API
                    val url = getUrl(startingLatlong!!, newlatlong!!)
                    Log.d("onMapClick", url)
                    val FetchUrl = FetchUrl()

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url)
                    //move map camera
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(startingLatlong))
                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
                    triggerAlarmManager()
                }


                /* val user = dataSnapshot.getValue(UserNew::class.java)
                 if (user != null && !isFinishing && user.lat != null && user.long != null) {
                     marker!!.setPosition(LatLng(user!!.lat.toDouble(), user.long.toDouble()))
                     triggerAlarmManager()
                 }*/
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "failed", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addLocationToServer(lat: String, long: String) {


        databaseArtists = FirebaseDatabase.getInstance().getReference("location")
        //getting the values to save
        val lat = lat
        val long = long

        //checking if the value is provided
        if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(long)) {

            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Artist
            var id = TinyDB(this@HomeActivity).getString("loginNumber")

            //creating an Artist Object
            val newUser = UserNew(lat, long)

            //Saving the Artist
            if (id != null && !id.equals(""))
                databaseArtists!!.child(id!!).setValue(newUser)
            //setting edittext to blank again
            //displaying a success toast
            DeBug.showLog("Service Started", "Sucesss adding")
        } else {
            //if the value is not given displaying a toast

        }
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
    }

    private fun getUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude


        // Sensor enabled
        val sensor = "sensor=false"

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor"

        // Output format
        val output = "json"

        // Building the url to the web service


        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyDWv75mZjT0FoUZE-NxxYqOJ4c4xLZLUhY"
    }


    // Fetches data from url passed
    private inner class FetchUrl : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg url: String): String {

            // For storing data from web service
            var data = ""

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0])
                Log.d("Background Task data", data)
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }

            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            val parserTask = ParserTask()

            // Invokes the thread for parsing the JSON data
            if (result != null)
                parserTask.execute(result)

        }
    }


    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            // Creating an http connection to communicate with url
            urlConnection = url.openConnection() as HttpURLConnection

            // Connecting to url
            urlConnection.connect()

            // Reading data from url
            iStream = urlConnection.inputStream

            val br = BufferedReader(InputStreamReader(iStream))

            val sb = StringBuffer()

            var line: String?

            do {
                line = br.readLine()

                if (line == null)
                    break
                sb.append(line)

            } while (true)


            data = sb.toString()
            Log.d("downloadUrl", data)
            br.close()

        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!checked)
            mMap!!.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
        // Add a marker in Sydney and move the camera
        val zoomLevel = 16.0f
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(startingLatlong))


        // Already two locations
        if (MarkerPoints!!.size > 1) {
            MarkerPoints!!.clear()
            mMap!!.clear()
        }

        // Adding new item to the ArrayList
        MarkerPoints!!.add(startingLatlong!!)
        MarkerPoints!!.add(newlatlong!!)
        // Creating MarkerOptions
        val options = MarkerOptions()
        options.position(startingLatlong!!)
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mMap!!.addMarker(options)
        /// destination marker
        val optionsDest = MarkerOptions()
        optionsDest.position(newlatlong!!)
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mMap!!.addMarker(optionsDest)
        // Getting URL to the Google Directions API
        val url = getUrl(startingLatlong!!, newlatlong!!)
        Log.d("onMapClick", url)
        val FetchUrl = FetchUrl()

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url)
        //move map camera
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(startingLatlong))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f))


    }

    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

        // Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {

            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null

            try {
                jObject = JSONObject(jsonData[0])
                Log.d("ParserTask", jsonData[0])
                val parser = DataParser()
                Log.d("ParserTask", parser.toString())

                // Starts parsing data
                routes = parser.parse(jObject)
                Log.d("ParserTask", "Executing routes")
                Log.d("ParserTask", routes.toString())

            } catch (e: Exception) {
                Log.d("ParserTask", e.toString())
                e.printStackTrace()
            }

            return routes
        }

        // Executes in UI thread, after the parsing process
        override fun onPostExecute(result: List<List<HashMap<String, String>>>) {
            var points: java.util.ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null

            // Traversing through all the routes
            for (i in result.indices) {
                points = java.util.ArrayList()
                lineOptions = PolylineOptions()

                // Fetching i-th route
                val path = result[i]

                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]

                    val lat = java.lang.Double.parseDouble(point["lat"]!!)
                    val lng = java.lang.Double.parseDouble(point["lng"]!!)
                    val position = LatLng(lat, lng)

                    points.add(position)
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(14f)
                lineOptions.pattern(PATTERN_POLYGON_ALPHA)
                lineOptions.color(Color.YELLOW)

                Log.d("onPostExecute", "onPostExecute lineoptions decoded")

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap!!.addPolyline(lineOptions)
                triggerAlarmManager()
            } else {
                Log.d("onPostExecute", "without Polylines drawn")
            }
        }
    }

    inner class DataParser {

        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        fun parse(jObject: JSONObject): List<List<HashMap<String, String>>> {

            val routes = java.util.ArrayList<List<HashMap<String, String>>>()
            val jRoutes: JSONArray
            var jLegs: JSONArray
            var jSteps: JSONArray

            try {

                jRoutes = jObject.getJSONArray("routes")

                /** Traversing all routes  */
                for (i in 0 until jRoutes.length()) {
                    jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")
                    val path = java.util.ArrayList<HashMap<String, String>>()

                    /** Traversing all legs  */
                    for (j in 0 until jLegs.length()) {
                        jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")

                        /** Traversing all steps  */
                        for (k in 0 until jSteps.length()) {
                            var polyline = ""
                            polyline =
                                ((jSteps.get(k) as JSONObject).get("polyline") as JSONObject).get("points") as String
                            val list = decodePoly(polyline)

                            /** Traversing all points  */
                            for (l in list.indices) {
                                val hm = HashMap<String, String>()
                                hm["lat"] = java.lang.Double.toString(list[l].latitude)
                                hm["lng"] = java.lang.Double.toString(list[l].longitude)
                                path.add(hm)
                            }
                        }
                        routes.add(path)
                    }
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: Exception) {
            }


            return routes
        }


        private fun decodePoly(encoded: String): List<LatLng> {

            val poly = java.util.ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val p = LatLng(
                    lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5
                )
                poly.add(p)
            }

            return poly
        }
    }


    companion object {
        val PATTERN_DASH_LENGTH_PX = 30
        val PATTERN_GAP_LENGTH_PX = 10
        val DOT: PatternItem = Dot()
        val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX.toFloat())
        val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())
        val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH)
    }


}
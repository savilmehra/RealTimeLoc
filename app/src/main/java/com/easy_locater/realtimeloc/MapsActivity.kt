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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.widget.CompoundButton
import android.widget.ImageView
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private val toolbar: Toolbar? = null
    private var mMap: GoogleMap? = null
    private var MarkerPoints: ArrayList<LatLng>? = null
    private val transparentImageView: ImageView? = null
    private var context: Context? = null
    private var line = ""
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    private var locationManager: LocationManager? = null
    private var googleMap: GoogleMap? = null
    internal var databaseArtists: DatabaseReference? = null
    private var id: String? = null
    private var lajpat: LatLng? = null
    private var agra: LatLng? = null
    var checked: Boolean = false
    val MAP_TYPE_NORMAL = 1
    val MAP_TYPE_SATELLITE = 2
    private var marker: Marker? = null
    private var marker2: Marker? = null
    private var phoneNumber: String? = null

    var mAdView: AdView? = null
    @Subscribe
    fun onEvent(eventHomeResumed: EventGetMapData) {
        getMapData()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_activity)
        context = this

        EventBus.getDefault().register(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        agra = LatLng(TinyDB(this).getDouble("lat", 0.0), TinyDB(this).getDouble("lon", 0.0))
        MarkerPoints = ArrayList()
        phoneNumber = intent.getStringExtra("phoneNumber")

        databaseArtists = FirebaseDatabase.getInstance().getReference("location")
        databaseArtists!!.child(phoneNumber!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user = dataSnapshot.getValue(UserNew::class.java)
                if (user != null && !isFinishing) {


                    lajpat = LatLng(user!!.lat.toDouble(), user.long.toDouble())

                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map1) as SupportMapFragment?
                    mapFragment!!.getMapAsync(this@MapsActivity)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapsActivity, "failed", Toast.LENGTH_SHORT).show()
            }
        })
        val sw = findViewById(R.id.switch1) as Switch
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
        //////////////=========================ads
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
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(lajpat))


        // Already two locations
        if (MarkerPoints!!.size > 1) {
            MarkerPoints!!.clear()
            mMap!!.clear()
        }

        // Adding new item to the ArrayList
        MarkerPoints!!.add(lajpat!!)
        MarkerPoints!!.add(agra!!)
        // Creating MarkerOptions
        val options = MarkerOptions()
        options.position(lajpat!!)
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        marker = mMap!!.addMarker(options)
        /// destination marker
        val optionsDest = MarkerOptions()
        optionsDest.position(agra!!)
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        marker2 = mMap!!.addMarker(optionsDest)
        // Getting URL to the Google Directions API
        val url = getUrl(lajpat!!, agra!!)
        Log.d("onMapClick", url)
        val FetchUrl = FetchUrl()

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url)
        //move map camera
        val markers = ArrayList<Marker>();
        markers.add(marker!!)
        markers.add(marker2!!)
        val builder = LatLngBounds.Builder()
        for (marker in markers) {
            builder.include(marker.getPosition())
        }
        val bounds = builder.build()
        val padding = 0 // offset from edges of the map in pixels
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap!!.moveCamera(cu);
        mMap!!.animateCamera(cu);
        /*  mMap!!.moveCamera(CameraUpdateFactory.newLatLng(lajpat))

          mMap!!.animateCamera(CameraUpdateFactory.zoomTo(13f))*/


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
            var points: ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null

            // Traversing through all the routes
            for (i in result.indices) {
                points = ArrayList()
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
                lineOptions.width(12f)
                lineOptions.pattern(PATTERN_POLYGON_ALPHA)
                lineOptions.color(Color.BLUE)

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

            val routes = ArrayList<List<HashMap<String, String>>>()
            val jRoutes: JSONArray
            var jLegs: JSONArray
            var jSteps: JSONArray

            try {

                jRoutes = jObject.getJSONArray("routes")

                /** Traversing all routes  */
                for (i in 0 until jRoutes.length()) {
                    jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")
                    val path = ArrayList<HashMap<String, String>>()

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

            val poly = ArrayList<LatLng>()
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
        val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DOT)
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
            var id = TinyDB(this@MapsActivity).getString("loginNumber")

            //creating an Artist Object
            val newUser = UserNew(lat, long)

            //Saving the Artist
            databaseArtists!!.child(id!!).setValue(newUser)
            //setting edittext to blank again
            //displaying a success toast

        } else {
            //if the value is not given displaying a toast

        }
    }


    fun triggerAlarmManager() {
        val cal = Calendar.getInstance()
        // add alarmTriggerTime seconds to the calendar object
        cal.add(Calendar.SECOND, 15)
        val ALARM_REQUEST_CODE = 145
        val alarmIntent: Intent = Intent(this, AlarmMap::class.java)
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
        val intent = Intent(this, AlarmMap::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 145, intent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun getMapData() {
        databaseArtists!!.child(phoneNumber!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(UserNew::class.java)
                if (user != null && !isFinishing) {
                    lajpat = LatLng(user!!.lat.toDouble(), user.long.toDouble())
                    val markers = ArrayList<Marker>();
                    agra = LatLng(
                        TinyDB(this@MapsActivity).getDouble("lat", 0.0),
                        TinyDB(this@MapsActivity).getDouble("lon", 0.0)
                    )

                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(lajpat))


                    // Already two locations
                    if (MarkerPoints!!.size > 1) {
                        MarkerPoints!!.clear()
                        mMap!!.clear()
                    }

                    // Adding new item to the ArrayList
                    MarkerPoints!!.add(lajpat!!)
                    MarkerPoints!!.add(agra!!)
                    // Creating MarkerOptions
                    val options = MarkerOptions()
                    options.position(lajpat!!)
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    marker = mMap!!.addMarker(options)
                    /// destination marker
                    val optionsDest = MarkerOptions()
                    optionsDest.position(agra!!)
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    marker2 = mMap!!.addMarker(optionsDest)
                    // Getting URL to the Google Directions API
                    val url = getUrl(lajpat!!, agra!!)
                    Log.d("onMapClick", url)
                    val FetchUrl = FetchUrl()

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url)
                    //move map camera
                    markers.add(marker!!)
                    markers.add(marker2!!)
                    val builder = LatLngBounds.Builder()
                    for (marker in markers) {
                        builder.include(marker.getPosition())
                    }
                    val bounds = builder.build()
                    val padding = 0 // offset from edges of the map in pixels
                    val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    mMap!!.moveCamera(cu);
                    mMap!!.animateCamera(cu);
                    /*mMap!!.moveCamera(CameraUpdateFactory.newLatLng(lajpat))
                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(13f))*/
                    triggerAlarmManager()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapsActivity, "failed", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta)))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60.0 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
}
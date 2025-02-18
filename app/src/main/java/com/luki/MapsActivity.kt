package com.luki

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.DecimalFormat

// GLOBAL VARIABLES
private const val LOCATION_PERMISSION_REQUEST_CODE = 1

private var mCurrentLocation: Location? = null

private val BARRANQUILLA_CENTER = LatLng(10.9878, -74.8089)

private lateinit var fusedLocationClient: FusedLocationProviderClient

private lateinit var locationCallback: LocationCallback

private val locationRequest = LocationRequest.create().apply {
    // interval in milliseconds
    interval = 1000
    fastestInterval = 500
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
}

lateinit var queue: RequestQueue

val gson = Gson()

lateinit var Thiscontext: Context

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    /**
     * askPermissionFineLocation - checks if the FINE_LOCATION permission is granted
     *
     * if not request the permission
     */
    private fun askPermissionFineLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        checkFinePermission()
    }

    /**
     * onRequestPermissionsResult - is called when the user accept or deny the permission
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when(requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkFinePermission()
            } else {
                // move the camera to Barranquilla center
                setCameraToUserLocation(null)
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    /**
     * checkFinePermission - Checks if the FINE_LOCATION permission is granted
     * and does everything that depends on the permission
     */
    private fun checkFinePermission() {
        // check if the permissions are granted
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // enable the user location and button myLocation in the map
            map.isMyLocationEnabled = true

            /**
             * async function - search the last known location
             */
            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                mCurrentLocation = location
                setCameraToUserLocation(mCurrentLocation)
            }
        }
    }

    /**
     * createLocationRequest - Create the location request and define the parameters
     *
     * checks if the GPS is active
     *
     * https://developer.android.com/training/location/change-location-settings
     */
    private fun createLocationRequest() {
        // add the location request that was created in the previous step
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // when the task is complete can check the locate configuration
        // checks if the location is active
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        // this run when the GPS is active
        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
        }

        // this run when the GPS is not active
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MapsActivity, 1)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    /**
     * startLocationUpdates - starts the location requests
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
        }
    }

    /**
     * stopLocationUpdates - stop the location updates
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // To create the ActionBarDrawerToggle instance
    private lateinit var toggle: ActionBarDrawerToggle

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        queue = Volley.newRequestQueue(this)

        Thiscontext = this

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // google play location service initialization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /**
         * locationCallback - requests the location each second
         */
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                val location = locationResult.locations[0]
                moveCameraToLocation(location, true)
            }
        }

        /**
         * switchBtn: is our switch in the layout
         *this method allow us to obtain a check status in the button switch
         * the checks status is a Boolean, true or false
         */
        val switchBtn = findViewById<Switch>(R.id.switchMode)
        switchBtn.setOnCheckedChangeListener { _, isChecked ->
            // if the Check status is true
            if (isChecked) {
                // if the actionbar is not null, hide the bar
                supportActionBar?.hide()
                stopLocationUpdates()
                Toast.makeText(this,
                        "Ahora puedes desplazarte en el mapa con libertad.",
                        Toast.LENGTH_SHORT).show()
            } else {
                // if the actionbar is not null, show the bar
                //supportActionBar?.show()
                startLocationUpdates()
                Toast.makeText(this,
                        "Regresaste al modo de localización en tiempo real constante.",
                        Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * publishBtn: is the ID of the publish button in the map layout
         * allow the publish button to acces to the login screen
         */
        val publishBtn = findViewById<Button>(R.id.publishbtn)
        publishBtn.setOnClickListener {
            val intent = Intent(this, LoginLandLord::class.java)
            this.startActivity(intent)
        }

        /**
         * Prepations for the navigation drawer
         */
        // create the instance of drawerLayout and navView
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_Layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        //Pass the ActionBarToggle action into the drawerListener
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        // the toggle its ready to use
        toggle.syncState()

        // Display the hamburger icon to launch the drawer
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        //hide the action bar
        supportActionBar?.hide()

        /**
         * Call setNavigationItemSelectedListener on the NavigationView
         * to detect when items are clicked
         */
        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.itemHistorial -> Toast.makeText(this,
                    "Historial del usuario", Toast.LENGTH_SHORT).show()

                R.id.itemConfiguration -> Toast.makeText(this,
                    "Configuracion de aplicacion", Toast.LENGTH_SHORT).show()

                R.id.itemHelp -> Toast.makeText(this,
                    "Ayuda de la App", Toast.LENGTH_SHORT).show()

                R.id.itemAbout -> {
                    // navigate to the about activity
                    val intent = Intent(this, About::class.java)
                    this.startActivity(intent)
                }

                R.id.itemExit -> finish()
            }
            true
        }

        /**
         * FloatingActionButton is gonna handle the navigation bar
         */
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        askPermissionFineLocation()
        createLocationRequest()
        getRents()

        val btnCloseRentInfo = findViewById<ImageButton>(R.id.btn_closeRentInfo)

        btnCloseRentInfo.setOnClickListener { showRentInfo(false) }

        map.setOnMarkerClickListener { marker ->
            markTouched(marker)
            chargeRentInfo(marker)
            showRentInfo(true)

            true
        }

        // addAllMarkers()
        addMark(10.931461, -74.824141, "My House")

        // activates the zoom buttons
        // map.uiSettings.isZoomControlsEnabled = true

        // Compass
        map.uiSettings.isCompassEnabled = true

        // tool bar default in true show when touch a marker
        map.uiSettings.isMapToolbarEnabled = false

        // enable perspective
        map.uiSettings.isTiltGesturesEnabled = true

        // to disable zoom gestures
        // map.uiSettings.isZoomGesturesEnabled = false

        // to disable the move with fingers gestures
        // map.uiSettings.isScrollGesturesEnabled = false

        // zoom to the map
        // map.animateCamera(CameraUpdateFactory.zoomIn())
    }

    /**
     * markTouched - move the camera to the touched marker
     */
    private fun markTouched(marker: Marker) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 18.0f))
    }

    /**
     * chargeRentInfo - Charges all rent info when the marker is touched
     */
    private fun chargeRentInfo(marker: Marker) {
        val rentTitle = findViewById<TextView>(R.id.rent_title)
        val rentPrice = findViewById<TextView>(R.id.rent_price)
        val rentAddress = findViewById<TextView>(R.id.rent_address)
        val rentBedrooms = findViewById<TextView>(R.id.rent_bedrooms)
        val rentArea = findViewById<TextView>(R.id.rent_area)
        val rentBathrooms = findViewById<TextView>(R.id.rent_bathrooms)
        val rentFloor = findViewById<TextView>(R.id.rent_floor)
        val rentParking = findViewById<TextView>(R.id.rent_parking)
        val rentStratum = findViewById<TextView>(R.id.rent_stratum)

        val markerDict = marker.tag as Map<*, *>

        // Number formats
        val priceF = DecimalFormat("#,###,###")
        val singleF = DecimalFormat("#")

        // value assignments
        val strTitle = "Apartamento en ${markerDict["city"].toString()}"
        rentTitle.text = strTitle
        rentPrice.text = priceF.format(markerDict["price"])

        rentAddress.text = markerDict["address"].toString()

        val strBed = "Cuartos ${singleF.format(markerDict["bedroom"])}"
        rentBedrooms.text = strBed

        val strArea = "Area ${singleF.format(markerDict["mt2"])}m²"
        rentArea.text = strArea

        val strBath = "Baños ${singleF.format(markerDict["bathroom"])}"
        rentBathrooms.text = strBath

        val strFloor = "Piso ${singleF.format(markerDict["floor_number"])}"
        rentFloor.text = strFloor

        val strParking = "Parqueaderos ${singleF.format(markerDict["parkinglot"])}"
        rentParking.text = strParking

        val strStratum = "Estrato ${singleF.format(markerDict["socialstratum"])}"
        rentStratum.text = strStratum

    }

    /**
     * showRentInfo - changes the map and rentInfo form size
     *
     *  [show]: define if show or hide rentInfo form
     *  true to show
     *  false to hide
     */
    private fun showRentInfo(show: Boolean) {
        val mapView = findViewById<ConstraintLayout>(R.id.mapConstraint)
        val rentConstraint = findViewById<ConstraintLayout>(R.id.rentConstraint)

        var mapValueStart = 1.0f
        var mapValueEnd = 0.35f
        var rentInfoValueStart = 0f
        var rentInfoValueEnd = 0.65f

        if (!show) {
            mapValueStart = 0.35f
            mapValueEnd = 1.0f
            rentInfoValueStart = 0.65f
            rentInfoValueEnd = 0f
        }

        // Initialize the animation values.
        val mapValueAnimator = ValueAnimator.ofFloat(mapValueStart, mapValueEnd)
        val rentInfoValueAnimator = ValueAnimator.ofFloat(rentInfoValueStart, rentInfoValueEnd)
        mapValueAnimator.duration = 300
        rentInfoValueAnimator.duration = 300

        // change the size each time that the value change
        mapValueAnimator.addUpdateListener {
            val animatedValue = mapValueAnimator.animatedValue as Float
            val layoutParams = mapView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.matchConstraintPercentHeight = animatedValue
            mapView.layoutParams = layoutParams
        }

        rentInfoValueAnimator.addUpdateListener {
            val animatedValue = rentInfoValueAnimator.animatedValue as Float
            val layoutParams = rentConstraint.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.matchConstraintPercentHeight = animatedValue
            rentConstraint.layoutParams = layoutParams
        }

        mapValueAnimator.start()
        rentInfoValueAnimator.start()

    }

    /**
     * setCameraToUserLocation - move camera when the app start
     *
     * if the last known location is found the camera move to the last location know
     * else
     * move camera to Barranquilla center
     *
     * [LAST_LOCATION]: the location to move the camera
     */
    private fun setCameraToUserLocation(LAST_LOCATION: Location?) {
        if (LAST_LOCATION == null) {
            // puts the camera in Barranquilla
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(BARRANQUILLA_CENTER, 12.5f))
            val stringAddress = getAddress(BARRANQUILLA_CENTER)
            Toast.makeText(applicationContext, "Current position\n$stringAddress", Toast.LENGTH_SHORT).show()
            return
        }

        moveCameraToLocation(LAST_LOCATION, false)
    }

    /**
     * moveCameraToLocation - move the camera to specific location
     * if param [animate] is true animates the camera
     * or if is false not animates the camera
     *
     * [LAST_LOCATION]: the location to move the camera
     * [animate]: true to animate the camera or false otherwise
     */
    private fun moveCameraToLocation(LAST_LOCATION: Location, animate: Boolean) {
        val poss = LatLng(LAST_LOCATION.latitude, LAST_LOCATION.longitude)

        if (animate) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(poss, 18.0f))
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poss, 18.0f))
        }
    }

    /**
     * addMark - adds a marker to the map
     *
     * [lat]: latitude
     * [lon]: longitude
     * [tit]: Title to the new marker
     *
     * Return: the created marker
     */
    private fun addMark(lat: Double, lon: Double, tit: String = ""): Marker {
        val newPlace = LatLng(lat, lon)

        return map.addMarker(MarkerOptions().position(newPlace).title(tit).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_purple)))
    }

    /**
     * getAddress - gets the string address from latitude and longitude
     *
     * [latLng]: latitude and longitude
     *
     * Return: the string address
     */
    private fun getAddress(latLng: LatLng): String {
        val geoCoder = Geocoder(this)
        val addressText: String

        // val locat = geocoder.getFromLocationName("Carrera 7 Sur #89-30, Barranquilla, Colombia", 1)

        val geoArray = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

        addressText = geoArray[0].getAddressLine(0)

        return addressText
    }

    // ====================== MENU ===================== //

    /**
     * onOptionsItemSelected - this functions allow os to atc on menu items
     * the toggle object is in sync with the menu options of the nav bar
     *
     * [item]: the item selected
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /* ================ REQUESTS =================== */
    /**
     * getRents - Makes the requests to get the data in JSON format
     */
    private fun getRents() {

        // val url = "http://luki-env-1.eba-2zc72njp.us-east-2.elasticbeanstalk.com/api/v1.0/rents"
        val url = "https://raw.githubusercontent.com/I7RANK/Luki/master/rents.json"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
                { response ->
                    removeFile("rents.json")

                    val newFile = createFile("rents.json")

                    newFile.appendText(response.toString())
                    getJSONRents(response.toString())
                },
                {
                    val rentsData = readFile("rents.json")

                    if (rentsData == null) {
                        val msg = "No es posible cargar los datos en este momento"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    } else {
                        val msg = "datos cargados del local"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        getJSONRents(rentsData.toString())
                    }
                }
        )

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    /**
     * getJSONRents - transforms the plain text to JSONObject and puts the markers in the map
     *
     * [res]: the plaint text to transform
     */
    private fun getJSONRents(res: String) {
        val mainDict = JSONObject(res)

        val mainList = gson.fromJson(mainDict["apartment"].toString(), Array<Any>::class.java)

        for (i in mainList) {
            val dict = i as Map<*, *>

            val marker = addMark(dict["latitude"] as Double, dict["longitude"] as Double, dict["price"].toString())

            marker.tag = dict
        }
    }

    /**
     * readFile - Reads a file in the "files" directory of the luki directory
     *
     * [filename]: the file name to read
     *
     * Return: the plain text in the file
     */
    private fun readFile(filename: String): String? {
        val path =  this.getExternalFilesDir(null)?.absolutePath.toString()
        return try {
            val file = File("$path/$filename")
            FileInputStream(file).bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) {
            null
        }
    }

    /**
     * createFile - creates an file
     *
     * [filename]: file name of the file to create
     *
     * Return: the file created
     */
    private fun createFile(filename: String): File {
        val path =  this.getExternalFilesDir(null)?.absolutePath.toString()
        val file = File("$path/$filename")

        file.createNewFile() // Creates file

        return file
    }

    /**
     * removeFile - removes an file
     *
     * [filename]: the file to delete
     */
    private fun removeFile(filename: String) {
        val path =  this.getExternalFilesDir(null)?.absolutePath.toString()
        val file = File("$path/$filename")

        file.delete()
    }
}

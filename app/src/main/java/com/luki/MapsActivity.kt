package com.luki

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

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
        task.addOnSuccessListener { locationSettingsResponse ->
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

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
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
        switchBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            // if the Check status is true
            if (isChecked) {
                // if the actionbar is not null, hide the bar
                supportActionBar?.hide()
                stopLocationUpdates()
            } else {
                // if the actionbar is not null, show the bar
                supportActionBar?.show()
                startLocationUpdates()
            }
        }

        /**
         * publishBtn: is the ID of the publish button in the map layout
         * allow the publish button to acces to the login screen
         */
        val publishBtn = findViewById<Button>(R.id.publishbtn)
        publishBtn.setOnClickListener() {
            val intent = Intent(this, LoginLandLord::class.java)
            this.startActivity(intent)
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

        map.setOnMarkerClickListener { marker ->
            markTouched(marker)
            true
        }

        addAllMarkers()

        // activates the zoom buttons
        map.uiSettings.isZoomControlsEnabled = true

        // brujula
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
        Toast.makeText(applicationContext,"Clicked ${marker.position.latitude} ${marker.position.longitude}",Toast.LENGTH_SHORT).show()

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 18.0f))
    }

    /**
     * addAllMarkers - adds all markers in the map
     */
    private fun addAllMarkers() {
        addMark(10.9878, -74.8089, "Barranquilla City")
        addMark(10.931461, -74.824141, "My House")
        addMark(10.931456, -74.824302, "Casa del Tata")

        addMark(11.004, -74.806, "Properati: 1")
        addMark(11.012, -74.834, "Properati: 2")
        addMark(10.994405, -74.8132697, "Properati: 3")
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

        return map.addMarker(MarkerOptions().position(newPlace).title(tit))
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
     * onCreateOptionsMenu - Initialize the contents of the Activity's standard options menu
     * this function doesn't mutate the option selected
     *
     * [menu]: the menu object
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * onOptionsItemSelected - this functions allow os to atc on menu items
     * exit - finish the app
     *
     * [item]: the item selected
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // when touched an item gets his id
        when (item.itemId) {
            R.id.exit -> {
                finish()
            }
            R.id.about -> {
                // navigate to the about activity
                val intent = Intent(this, About::class.java)
                this.startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

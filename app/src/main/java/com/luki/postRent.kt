package com.luki

import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.IOException

class PostRent : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_rent)

        // setup the SingUp button
        val btnSingUp = findViewById<Button>(R.id.buttonSignIn)
        btnSingUp.setOnClickListener {
            saveRent()
        }
    }

    private fun saveRent() {
        val url = "http://luki-env-1.eba-2zc72njp.us-east-2.elasticbeanstalk.com/api/v1.0/rents"

        val dataJSON = takeRent()

        val json = JSONObject(dataJSON)

        // Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, json,
                {
                    Toast.makeText(this, "Su apartamento ha sido publicado exitosamente", Toast.LENGTH_SHORT).show()
                },
                {
                    Toast.makeText(this, "Algo ha fallado: error 1245\nPor favor inténtalo más tarde", Toast.LENGTH_SHORT).show()
                }
        )

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest)
    }

    /**
     * takeRent - obtain all data of new apartment post
     *
     * Return: json of all data
     */
    private fun takeRent() : String {
        val txtState = findViewById<EditText>(R.id.txt_state)
        val txtCity = findViewById<EditText>(R.id.txt_city)
        val txtAddress = findViewById<EditText>(R.id.txt_address)
        val txtBuildingName = findViewById<EditText>(R.id.txt_buildingName)
        val txtSocialStratum = findViewById<EditText>(R.id.txt_stratum)
        val txtPrice = findViewById<EditText>(R.id.txt_price)
        val txtFloorNumber = findViewById<EditText>(R.id.txt_floor)
        val txtBedroom = findViewById<EditText>(R.id.txt_bedRooms)
        val txtBathroom = findViewById<EditText>(R.id.txt_bathRooms)
        val txtParkingLot = findViewById<EditText>(R.id.txt_parking)
        val txtMt2 = findViewById<EditText>(R.id.txt_areaM2)
        val txtDescription = findViewById<EditText>(R.id.txt_desc)

        val state = txtState.text.toString()
        val city = txtCity.text.toString()
        val address = txtAddress.text.toString()
        val buildingName = txtBuildingName.text.toString()

        val socialStratum = validateEmpty(txtSocialStratum.text.toString())
        val price = validateEmpty(txtPrice.text.toString())
        val floorNumber = validateEmpty(txtFloorNumber.text.toString())
        val bedroom = validateEmpty(txtBedroom.text.toString())
        val bathroom = validateEmpty(txtBathroom.text.toString())
        val parkingLot = validateEmpty(txtParkingLot.text.toString())
        val mt2 = validateEmpty(txtMt2.text.toString())

        //save the description text in the variable
        val desc = txtDescription.text.toString()

        //get the latitude and longitude of the full address using getGeoLocation
        val location = getGeoLocation("$address, $city, $state")

        // setup the data that contain the user information
        val dict: MutableMap<String, Any> = mutableMapOf(
                "startdate" to "2021-03-10",
                "enddate" to "2021-03-10",
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "country" to "Colombia",
                "state" to state,
                "city" to city,
                "address" to address,
                "building_name" to buildingName,
                "socialstratum" to socialStratum.toInt(),
                "price" to price.toBigInteger(),
                "floor_number" to floorNumber.toInt(),
                "bedroom" to bedroom.toInt(),
                "bathroom" to bathroom.toInt(),
                "parkinglot" to parkingLot.toInt(),
                "mt2" to mt2.toInt(),
                "description" to desc,
                "property_of" to 1,
                "sector_of" to 370,
                "category_of" to 1
        )

        //convert the data to Json
        return gson.toJson(dict)
    }

    /**
     * getGeoLocation - obtain the address in latLng
     * location: address un string
     *
     * [fullAddress]: the address to convert to latLng
     * Return: latLng representing the address
     */
    private fun getGeoLocation(fullAddress: String): LatLng {
        val geoCoder = Geocoder(this)
        var locat: List<Address>? = null
        try {
            locat = geoCoder.getFromLocationName(fullAddress, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val address = locat!![0]

        return LatLng(address.latitude, address.longitude)
    }

    /**
     * validateEmpty - Change the value to 0 if the string is empty
     *
     * [str]: The String to validate
     *
     * Return: the validated string
     */
    private fun validateEmpty(str: String): String {
        if (str == "") {
            return "0"
        }

        return str
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
        inflater.inflate(R.menu.menu_sing_up, menu)
        return true
    }
}
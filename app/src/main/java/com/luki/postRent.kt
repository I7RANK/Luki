package com.luki

import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.time.LocalDateTime

class PostRent : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_rent)

        val btnSingUp = findViewById<Button>(R.id.buttonSignIn)

        btnSingUp.setOnClickListener {
            takeRent()
        }
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
        val txtBuilding_name = findViewById<EditText>(R.id.txt_buildingName)
        val txtSocialstratum = findViewById<EditText>(R.id.txt_stratum)
        val txtPrice = findViewById<EditText>(R.id.txt_price)
        val txtFloor_number = findViewById<EditText>(R.id.txt_floor)
        val txtBedroom = findViewById<EditText>(R.id.txt_bedRooms)
        val txtBathroom = findViewById<EditText>(R.id.txt_bathRooms)
        val txtParkinglot = findViewById<EditText>(R.id.txt_parking)
        val txtMt2 = findViewById<EditText>(R.id.txt_areaM2)
        val txtDescription = findViewById<EditText>(R.id.txt_desc)

        val state = txtState.text.toString()
        val city = txtCity.text.toString()
        val address = txtAddress.text.toString()
        val buildingName = txtBuilding_name.text.toString()
        val socialstratum = txtSocialstratum.text.toString()
        val price = txtPrice.text.toString()
        val floorNumber = txtFloor_number.text.toString()
        val bedroom = txtBedroom.text.toString()
        val bathroom = txtBathroom.text.toString()
        val parkinglot = txtParkinglot.text.toString()
        val mt2 = txtMt2.text.toString()
        val desc = txtDescription.text.toString()

        var location = getGeoLocation("$address, $city, $state")

        val dict: MutableMap<String, Any> = mutableMapOf(
                "stardate" to "2021-03-10",
                "enddate" to "2021-03-10",
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "country" to "Colombia",
                "state" to state,
                "city" to city,
                "address" to address,
                "building_name" to buildingName,
                "socialstratum" to socialstratum,
                "price" to price,
                "floor_number" to floorNumber,
                "bedroom" to bedroom,
                "bathroom" to bathroom,
                "parkinglot" to parkinglot,
                "mt2" to mt2,
                "description" to desc,
                "property_of" to 1,
                "sector_of" to 370,
                "category_of" to 1
        )

        val strJSON = gson.toJson(dict)

        Toast.makeText(this, strJSON, Toast.LENGTH_SHORT).show()

        return strJSON
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
        val adressLatLng = LatLng(address.latitude, address.longitude)

        return adressLatLng
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
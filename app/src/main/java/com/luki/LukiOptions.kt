package com.luki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luki.databinding.LukiOptionsBinding

class LukiOptions: AppCompatActivity() {

    private lateinit var binding: LukiOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LukiOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * button selection if the user click on "Luki busqueda" or "Quiero publicar"
         * @button "Luki busqueda" - send the user to the Map
         * @button "Quiero publicar" - send the user to the login landlod screen
         */
        binding.button.setOnClickListener {
            val intent = Intent(this, LoginLandLord::class.java)
            startActivity(intent)
        }

        binding.button2.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}
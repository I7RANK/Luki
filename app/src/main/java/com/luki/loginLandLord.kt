package com.luki

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.Button
import android.widget.TextView

class LoginLandLord : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.landlord_sign_in)

        // añadir el acceso a registrarse en luki
        val registerBtn = findViewById<TextView>(R.id.registre)
        registerBtn.setOnClickListener() {
            val intent = Intent(this, SingUp::class.java)
            this.startActivity(intent)
        }
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
        inflater.inflate(R.menu.menu_registre, menu)
        return true
    }
}
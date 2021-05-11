package com.luki

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginLandLord : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.landlord_sign_in)

        val btnSingIn = findViewById<Button>(R.id.buttonSignIn)
        // Registration on luki
        val registerBtn = findViewById<TextView>(R.id.registre)
        registerBtn.setOnClickListener() {
            val intent = Intent(this, SingUp::class.java)
            this.startActivity(intent)
        }

        btnSingIn.setOnClickListener { checkLogin(it) }
    }

    /**
     * checkLogin - save all the input data of the Login form in a array
     * the array data is used to login in Luki
     */
    private fun checkLogin(btn: View) {
        val txtEmail = findViewById<EditText>(R.id.editTextEmail_signIn)
        val txtPass = findViewById<EditText>(R.id.editTextPassword_signIn)

        val email = txtEmail.text.toString()
        val pass = txtPass.text.toString()

        if (email == "admin" && pass == "admin") {
            val intent = Intent(this, SingUp::class.java)
            this.startActivity(intent)
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show()
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
package com.luki

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
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
        inflater.inflate(R.menu.menu_about, menu)
        return true
    }
}
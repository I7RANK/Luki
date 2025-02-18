package com.luki

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.luki.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    /**
     * This activity only works to display the intro screen of the app
     */
    // This is the loading time of the splash screen
    private val splashTimeOut:Long = 1000 // 1 sec

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //handle the display time of the screen to move to the next one
        Handler(Looper.getMainLooper()).postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            startActivity(Intent(this, LukiOptions::class.java))
            // close this activity
            finish()
        }, splashTimeOut)
    }
}
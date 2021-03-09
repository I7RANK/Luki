package com.luki

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.luki.databinding.RentInfoBinding

class ShowInfo : AppCompatActivity() {
    private lateinit var binding: RentInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RentInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    // ====================== MENU ===================== //
    /**
     * onCreateOptionsMenu - Initialize the contents of the Activity's standard options menu
     * this function doesn't mutate the option selected
     *
     * [menu]: the menu object
     */

}
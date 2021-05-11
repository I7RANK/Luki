package com.luki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.luki.databinding.LukiOptionsBinding
import java.io.File

lateinit var queueInOptions: RequestQueue

class LukiOptions: AppCompatActivity() {

    private lateinit var binding: LukiOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LukiOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        queueInOptions = Volley.newRequestQueue(this)

        getFileRents()

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

    /* ==================== REQUESTS ================== */
    /* this part will download the rents file and saved it in the local storage */
    /**
     * getFileRents - Makes the requests to get the file from luki repository in JSON format
     * and save the file in local storage
     */
    private fun getFileRents() {
        val url = "https://raw.githubusercontent.com/I7RANK/Luki/master/rents.json"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    removeFile("rents.json")

                    val file = createFile("rents.json")

                    file.appendText(response.toString())
                }, {}
        )

        // Add the request to the RequestQueue.
        queueInOptions.add(stringRequest)
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
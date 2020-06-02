package com.kislaya.allrounder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit var btnSpeechToText: Button
    lateinit var btnOcrImage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOcrImage = findViewById(R.id.btnOcrImage)
        btnSpeechToText = findViewById(R.id.btnSpeechToText)
        supportActionBar?.title = "Dashboard"

        btnSpeechToText.setOnClickListener {
            startActivity(Intent(this@MainActivity, SpeechToTextActivity::class.java))
        }

        btnOcrImage.setOnClickListener {
            startActivity(Intent(this@MainActivity, OcrImageActivity::class.java))
        }


    }


}

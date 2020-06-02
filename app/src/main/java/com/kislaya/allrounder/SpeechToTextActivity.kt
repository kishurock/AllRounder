package com.kislaya.allrounder

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.*


class SpeechToTextActivity : AppCompatActivity() {

    lateinit var imgMic: ImageView
    lateinit var txtResult: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_to_text_avtivity)

        imgMic = findViewById(R.id.imgMic)
        txtResult = findViewById(R.id.txtResult)
        supportActionBar?.title = "Speech To Text"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        imgMic.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault())
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 10)
            } else {
                Toast.makeText(
                    this@SpeechToTextActivity,
                    "Your Device don't Support this Functionality",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            10 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    var result = arrayListOf<String>()
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                    txtResult.text = result[0]
                }

            }
            else -> {

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}

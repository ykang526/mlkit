package com.google.mlkit.samples.vision.digitalink.kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.samples.vision.digitalink.R

class LandingPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.landing_page)
        val startBtn = findViewById<Button>(R.id.btn_start)
        startBtn.setOnClickListener(){
            val intent = Intent(this@LandingPageActivity, DigitalInkMainActivity::class.java)
            startActivity(intent)
        }
    }
}
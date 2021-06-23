package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val extras = intent.extras
        file_name_value_text.text = extras?.getString("FILE_NAME")  ?: ""
        status_value_text.apply {
            text = extras?.getString("DOWNLOAD_STATUS") ?: ""
            when (text) {
                "SUCCESSFUL" -> setTextColor(Color.GREEN)
                "FAILED" -> setTextColor(Color.RED)
                else -> setTextColor(Color.GRAY)
            }
        }
        val notificationId = extras?.getInt("NOTIFICATION_ID")
        notificationId?.apply {
            val notificationManager = application.getSystemService(NotificationManager::class.java)
            notificationManager.cancel(this)
        }

        ok_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}

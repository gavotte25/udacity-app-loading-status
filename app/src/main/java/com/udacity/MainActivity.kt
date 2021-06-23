package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val URL_LOAD_APP = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"

        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/refs/heads/master.zip"

        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/refs/heads/master.zip"

        private const val CHANNEL_ID = "LoadApp"
    }

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        notificationManager = applicationContext.getSystemService(NotificationManager::class.java)

        createChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        setListeners()
    }

    private fun setListeners() {
        custom_button.setOnClickListener {
            if (url != null) {
                download()
                custom_button.setState(ButtonState.Loading)
            } else {
                Toast.makeText(this, "Please select one!", Toast.LENGTH_SHORT).show()
            }
        }

        radioGroup.setOnCheckedChangeListener{ group, checkedId ->
            url = when (checkedId) {
                R.id.radioButton1 -> URL_GLIDE
                R.id.radioButton2 -> URL_LOAD_APP
                R.id.radioButton3 -> URL_RETROFIT
                else -> null
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val extras = getDownloadExtras(id)
            notificationManager.sendNotification(extras)
        }
    }

    private fun getDownloadExtras(downloadId: Long): Extras {
        val downloadManager = application.getSystemService(DownloadManager::class.java)
        val downloadQuery = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(downloadQuery)
        cursor.moveToFirst()
        var columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = statusParser(cursor.getInt(columnIndex))
        columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)
        val fileName = cursor.getString(columnIndex)
        return Extras(downloadId.toInt(), fileName, status)
    }

    private fun statusParser(statusInt:Int): String {
        return when (statusInt) {
            DownloadManager.STATUS_FAILED -> "FAILED"
            DownloadManager.STATUS_SUCCESSFUL -> "SUCCESSFUL"
            DownloadManager.STATUS_PAUSED -> "PAUSED"
            DownloadManager.STATUS_PENDING -> "PENDING"
            DownloadManager.STATUS_RUNNING -> "RUNNING"
            else -> "UNKNOWN"
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Download", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply {
                        setShowBadge(false)
                        enableLights(true)
                        lightColor = Color.BLUE
                        enableVibration(true)
                        description = "LoadApp Notifcation"
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun download() {
        val fileName = getFileName(url)
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(fileName)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "/udacity/${fileName}.zip")

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun getFileName(url: String?): String {
        return when(url) {
            URL_GLIDE -> "Glide - Image Loading Library"
            URL_LOAD_APP -> "LoadApp - Curent repository"
            URL_RETROFIT -> "Retrofit - Type-safe HTTP client"
            else -> "UnknownFile"
        }
    }

    private fun getPendingIntent(extras: Extras): PendingIntent {
        val intent = Intent(application,DetailActivity::class.java)
        intent.putExtra("FILE_NAME",extras.fileName)
        intent.putExtra("NOTIFICATION_ID",extras.downloadId)
        intent.putExtra("DOWNLOAD_STATUS",extras.status)
        return PendingIntent.getActivity(application, extras.downloadId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun NotificationManager.sendNotification(intendExtras: Extras) {
        val messageBody = intendExtras.fileName + " is downloaded"
        val pendingIntent = getPendingIntent(intendExtras)
        val largeIcon = BitmapFactory.decodeResource(application.resources, R.drawable.ic_assistant_black_24dp)
        val builder = NotificationCompat.Builder(application,CHANNEL_ID)
                .setContentText(messageBody)
                .setContentTitle("Download completed")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                .setLargeIcon(largeIcon)
                .addAction(R.drawable.ic_assistant_black_24dp, "Details", pendingIntent)

        notify(intendExtras.downloadId, builder.build())
    }

    private data class Extras(val downloadId: Int, val fileName: String, val status: String)

}

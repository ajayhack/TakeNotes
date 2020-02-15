package com.example.takenotes.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.takenotes.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


const val NOTIFICATION_CHANNEL_ID = "10001"
class NotificationMessage : FirebaseMessagingService(){

    private var messageId: String? = null
    private var messageFrom: String? = null
    private var messageDateTimeStamp: String? = null
    private var messageTitle: String? = null
    private var messageBody: String? = null
    private var messageImagePath: String? = null
    private var mNotificationManager: NotificationManager? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss" , Locale.ENGLISH)

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try { // Check if message contains a notification payload.
            if (remoteMessage.notification != null) {
                val mData = remoteMessage.data
                messageId = remoteMessage.messageId
                messageFrom = remoteMessage.from
                messageDateTimeStamp = dateFormat.format(Calendar.getInstance().time)
                messageTitle = remoteMessage.notification!!.title
                messageBody = remoteMessage.notification!!.body
                messageImagePath = remoteMessage.notification!!.icon

                handleNotification()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleNotification() {
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val intent = Intent(this , MainActivity :: class.java)
        val resultPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        showSmallNotification(mBuilder , messageTitle , messageBody , messageDateTimeStamp , resultPendingIntent)
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    fun getBitmapFromURL(strURL: String?): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun showSmallNotification(mBuilder: NotificationCompat.Builder, title: String?, message: String?,
                                      timeStamp: String? , resultPendingIntent : PendingIntent) {
        val inboxStyle: NotificationCompat.InboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine(message)
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder.setSound(alarmSound)
        //Check for OREO above version:-
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            val notification: Notification = mBuilder
                .setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(inboxStyle)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setOnlyAlertOnce(true)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher) //.setLargeIcon(bitmap)
                .setContentText(message)
                .build()
            mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager!!.createNotificationChannel(notificationChannel)
            mNotificationManager!!.notify(1, notification)
        } else {
            val notification: Notification = mBuilder
                .setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                //.setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(inboxStyle)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setOnlyAlertOnce(true)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(message)
                .build()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(100, notification)
        }
    }

    /*private fun showBigNotification(
        bitmap: Bitmap?, mBuilder: NotificationCompat.Builder,
        title: String?, message: String?,
        timeStamp: String?, resultPendingIntent: PendingIntent
    ) {
        val bigPictureStyle: NotificationCompat.BigPictureStyle = BigPictureStyle()
        bigPictureStyle.setBigContentTitle(title)
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString())
        //bigPictureStyle.bigPicture(bitmap);
//Check For OREO version and above:-
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME",
                importance
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(
                100,
                200,
                300,
                400,
                500,
                400,
                300,
                200,
                400
            )
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            val notification: Notification
            notification = mBuilder
                .setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(bigPictureStyle)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.drawable.ndmc_logo) //.setLargeIcon(bitmap)
                .setContentText(message)
                .build()
            mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager!!.createNotificationChannel(notificationChannel)
            mNotificationManager!!.notify(1, notification)
        } else {
            val notification: Notification
            notification = mBuilder
                .setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(bigPictureStyle)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.drawable.ndmc_logo) //.setLargeIcon(bitmap)
                .setContentText(message)
                .build()
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.notify(Constants.NOTIFICATION_ID_BIG_IMAGE, notification)
        }
    }*/


    private fun getTimeMilliSec(timeStamp: String?): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss" , Locale.ENGLISH)
        try {
            val date = format.parse(timeStamp!!)
            return date!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }
}
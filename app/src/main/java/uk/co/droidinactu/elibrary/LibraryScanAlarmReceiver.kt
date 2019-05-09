package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import androidx.core.app.NotificationCompat
import uk.co.droidinactu.elibrary.BookLibrary.Companion.mNotificationIdDbCheck
import uk.co.droidinactu.elibrary.BookLibrary.Companion.mNotificationIdScanning
import uk.co.droidinactu.elibrary.library.LibraryManager
import java.util.*

class LibraryScanAlarmReceiver : BroadcastReceiver() {

    private var mNotificationManager: NotificationManager? = null

    //#region Library Scanning Notification
    private val mHandlerScanningNotification = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val handlerCmd = (msg.obj as String).split(":".toRegex())
            if (mNotificationManager == null) {
                mNotificationManager =
                    BookLibApplication.instance.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (handlerCmd[0].equals("startscanning")) {
                displayScanningNotification(handlerCmd[1])
            } else if (handlerCmd[0].equals("stopscanning")) {
                removeScanningNotification()
            } else if (handlerCmd[0].equals("startdbCheck")) {
                displayDbCheckNotification()
            } else if (handlerCmd[0].equals("stopdbcheck")) {
                removeDbCheckNotification()
            }
        }
    }

    private fun displayScanningNotification(libname: String) {
        val resultIntent = Intent(BookLibApplication.instance.applicationContext, BookLibrary::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            BookLibApplication.instance.applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var notificationBuilder =
            NotificationCompat.Builder(BookLibApplication.instance.applicationContext, LibraryManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Scanning Library")
                .setContentText("Scan library $libname for ebooks")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent)
                .setProgress(0, 0, true)
        val notification = notificationBuilder?.build()

        mNotificationManager?.notify(mNotificationIdScanning, notification)
    }

    private fun removeScanningNotification() {
        mNotificationManager?.cancel(mNotificationIdScanning)
    }

    private fun displayDbCheckNotification() {
        var notificationBuilder =
            NotificationCompat.Builder(BookLibApplication.instance.applicationContext, LibraryManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Checking Library for consistency")
                .setContentText("Checking Library for consistency")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(0, 0, true)
        val notification = notificationBuilder?.build()

        mNotificationManager?.notify(mNotificationIdDbCheck, notification)
    }

    private fun removeDbCheckNotification() {
        mNotificationManager?.cancel(mNotificationIdDbCheck)
    }

    //#endregion

    override fun onReceive(context: Context, intent: Intent) {
        MyDebug.LOG.debug("BookLibrary::refreshLibraries()")
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //   throw new UnsupportedOperationException("Not yet implemented");
        BookLibApplication.instance.getLibManager().refreshLibraries(null, null, mHandlerScanningNotification)
        scheduleNextLibraryScan(context)
    }

    private fun scheduleNextLibraryScan(context: Context) {
        // time at which alarm will be scheduled here alarm is scheduled at 1 day from current time,
        // we fetch  the current time in milliseconds and added 1 day time
        // i.e. 24*60*60*1000= 86,400,000   milliseconds in a day
        val hours = 6
        val time = GregorianCalendar().timeInMillis + hours * (60 * 60 * 1000)
        val alarmManager =
            (context.applicationContext as BookLibApplication).applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // create an Intent and set the class which will execute when Alarm triggers, here we have
        // given LibraryScanAlarmReciever in the Intent, the onRecieve() method of this class will
        // execute when alarm triggers and we will write the code to send SMS inside onRecieve()
        // method pf LibraryScanAlarmReciever class
        val libraryScanAlarmIntent = Intent(
            (context.applicationContext as BookLibApplication).applicationContext,
            LibraryScanAlarmReceiver::class.java
        )

        //set the alarm for particular time
        val libraryScanPndingIntnt = PendingIntent.getBroadcast(
            (context.applicationContext as BookLibApplication).applicationContext,
            1,
            libraryScanAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, libraryScanPndingIntnt)
    }

}

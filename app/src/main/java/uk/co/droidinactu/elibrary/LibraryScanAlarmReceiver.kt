package uk.co.droidinactu.elibrary

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class LibraryScanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //   throw new UnsupportedOperationException("Not yet implemented");
        (context.applicationContext as BookLibApplication).getLibManager()!!.refreshLibraries(null, null)
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

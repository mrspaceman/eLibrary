package uk.co.droidinactu.ebooklibrary

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Environment
import android.os.PowerManager
import android.util.Patterns
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import uk.co.droidinactu.ebooklibrary.library.FileObserverService
import uk.co.droidinactu.ebooklibrary.library.LibraryManager
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.text.DecimalFormat

val Context.myApp: BookLibApplication
    get() = applicationContext as BookLibApplication

class BookLibApplication : Application() {

    companion object {
        lateinit var instance: BookLibApplication
            private set

        const val IS_DEBUGGING = true

        val LINE_SEPARATOR = System.getProperty("line.separator")
        val sdf = DateTimeFormat.forPattern("yyyy-MM-dd")
        val logDataFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
        const val simpleDateFmtStrView = "dd-MMM-yyyy"
        const val simpleDateFmtStrDb = "yyyyMMdd"

        val minsFmt = DecimalFormat("#0")
        val kmFmt = DecimalFormat("#0.0")
        val decFmt = DecimalFormat("#0.00")
        val gbp = DecimalFormat("£#0.00")

        const val ONE_SECOND = 1000
        const val ONE_MINUTE = 60000
        const val BLE_DEVICE_SCAN_PERIOD: Long = 10000
    }

    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var libMgr: LibraryManager

    private lateinit var fileObserverService: FileObserverService

    /**
     * (non-Javadoc)
     *
     * @see android.app.Application#onCreate()
     */
    override fun onCreate() {
        super.onCreate()
        instance = this
        MyDebug.LOG.errordebug("onCreate(); application being created.")

        //     copyDbFileToSd(LibraryManager.DB_NAME)

        libMgr = LibraryManager()
        try {
            libMgr.open(applicationContext)
        } catch (pE: SQLException) {
            pE.printStackTrace()
        }
    }

    fun getLibManager(): LibraryManager {
        return libMgr
    }


    /**
     * (non-Javadoc)
     *
     * @see android.app.Application#onTerminate()
     */
    override fun onTerminate() {
        super.onTerminate()
        libMgr.close()
    }

    @SuppressLint("InvalidWakeLockTag")
    fun getWakeLock(): PowerManager.WakeLock? {
        if (wakeLock == null) {
            // lazy loading: first call, create wakeLock via PowerManager.
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, ":wakeup")
        }
        return wakeLock
    }

    fun copyDbFileToSd(dbName: String) {
        try {
            var sd = Environment.getExternalStorageDirectory()
            //   val data = Environment.getDataDirectory()

            val dbPath = getDatabasePath(dbName)

            sd = File("/storage/9C33-6BBD/")
            sd = File("/sdcard")

            if (isExternalStorageWritable()) {
                val someDate = DateTime()
                val sdfFile = DateTimeFormat.forPattern("yyyy-MM-dd")
                val backupDBPath =
                    sd.path + File.separator + dbName + "-" + someDate.toString(sdfFile)

                MyDebug.LOG.errordebug("copying db file from [$dbPath] to [$backupDBPath]")
                if (dbPath.exists()) {
                    dbPath.copyTo(File(backupDBPath), true)
                    MyDebug.LOG.errordebug("Database file backed up to sdcard")
                } else {
                    MyDebug.LOG.errorerror("Can't find Database file [$dbPath]")
                }
            } else {
                MyDebug.LOG.errorerror("External Storage not writable")
            }
        } catch (e: Exception) {
            MyDebug.LOG.errorerror("Exception backing up database", e)
        }
    }

    @Throws(IOException::class)
    private fun copyFileUsingApacheCommonsIO(source: String, dest: String) {
        copyFileUsingApacheCommonsIO(File(source), File(dest))
    }

    @Throws(IOException::class)
    private fun copyFileUsingApacheCommonsIO(source: File, dest: File) {
        FileUtils.copyFile(source, dest)
    }

    /* Checks if external storage is available for read and write */
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state)
    }

    fun getAppImage(packageName: String): Drawable? {
        try {
            return packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun getApplicationImage(): Bitmap {
        return BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
    }

    fun getAppSdCardPathDir(): String {
        val extDir = Environment.getExternalStorageDirectory()
        return extDir.path + File.separator + getApplicationName() + File.separator
    }

    private fun getApplicationName(): String {
        return this.getString(R.string.app_name)
    }

    fun isActivityRunning(): Boolean {
//        ActivityManager activityManager = (ActivityManager) Monitor.this.getSystemService(Context.ACTIVITY_SERVICE);
        //        List<ActivityManager.RunningTaskInfo> activitys = activityManager.getRunningTasks(Integer.MAX_VALUE);
        //        for (int i = 0; i < activitys.size(); i++) {
        //            if (activitys.get(i).topActivity.toString().equalsIgnoreCase("ComponentInfo{com.example.testapp/com.example.testapp.Your_Activity_Name}")) {
        //                isActivityFound = true;
        //            }
        //        }
        return false
    }

    @SuppressLint("NewApi")
    fun getAppVersionNbr(packageName: String): Long {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            return pInfo.getLongVersionCode()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return -1
    }

    fun getAppVersionName(packageName: String): String {
        try {
            val pInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_META_DATA)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getEmailAddr(): String? {
        val emailPattern = Patterns.EMAIL_ADDRESS // API level 8+
        val accounts = AccountManager.get(applicationContext).accounts
        for (account in accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                val possibleEmail = account.name
                if (possibleEmail.endsWith("googlemail.com") || possibleEmail.endsWith("gmail.com")) {
                    return possibleEmail
                }
            }
        }
        return null
    }

    fun getFileObserverService(): FileObserverService {
        fileObserverService = FileObserverService("booklib file observer")
        return fileObserverService
    }


}

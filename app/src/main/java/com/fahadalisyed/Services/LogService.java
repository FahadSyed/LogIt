package com.fahadalisyed.Services;

import android.app.NotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.PendingIntent;
import android.app.Service;
import android.util.Log;

import com.fahadalisyed.Home.Home;
import com.fahadalisyed.Utilities.TimeFormat;
import com.fahadalisyed.logit.R;

/**
 * Created by fahadsyed on 15-07-25.
 */

public class LogService extends Service {
    /**
     *  This class is the primary service used to be able to track elapsed time, it services
     *  as the medium between Home.java and Tracker.java, and handles updating the notification as well
     */
    private static final String TAG = LogService.class.getSimpleName(); // DEBUG purposes only
    private static final int SERVICE_ID = 1;

    private Tracker m_tracker;
    private LocalBinder m_trackerBinder = new LocalBinder();
    private NotificationManager m_notificationManager;
    private Notification m_logNotification;
    private final long m_logFrequency = 1000;
    private final int TICK = 2;

    /**
     * This method creates a new tracker and a notification manager for our icon, time and name
     * to display on the settings panel
     */
    @Override
    public void onCreate() {
        super.onCreate();
        m_tracker = new Tracker(); //Create a new Tracker Object
        m_notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        createNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    //region Binders
    @Override
    public IBinder onBind(Intent intent) {
        return m_trackerBinder;
    }

    public class LocalBinder extends Binder {
        public LogService getService() {
            return LogService.this;
        }
    }
    //endregion
    /*
     * Create a Handler to that updated the notification at the appropriate frequency
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (isTrackerRunning()) {
                updateSettingNotification();
                sendMessageDelayed(Message.obtain(this, TICK), m_logFrequency);
            }
        }
    };

    //region Notifications

    /**
     * This method is responsible for displaying the Tempura and time on the settings
     * panel for Android Phones
     */
    private void createNotification() {
        int icon = R.drawable.icon;
        CharSequence settingsText = "LogIt";
        long when = System.currentTimeMillis();
        m_logNotification = new Notification(icon, settingsText, when);
        m_logNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        m_logNotification.flags |= Notification.FLAG_NO_CLEAR;
    }

    /**
     * This method updates our notification with the Log time
     */
    private void updateSettingNotification() {
        Context context = getApplicationContext();
        CharSequence contentTitle = "LogIt";
        Log.d(TAG, "yo we are inside updateSettingNotifications");
        CharSequence contentText = "Current Log: " + getFormattedTime();
        Intent notificationIntent = new Intent(this, Home.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        m_logNotification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        m_notificationManager.notify(SERVICE_ID, m_logNotification);

    }

    private void displayNotifications() {
        updateSettingNotification();
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK), m_logFrequency);
    }

    private void cancelNotifications() {
        m_notificationManager.cancelAll();
    }
    //endregion


    //region Tracker Start/Stop

    /**
     * The bottom two methods are mediums for the Home.java to communicate with the
     * Tracker
     */
    public void start() {
        m_tracker.start();
        displayNotifications();
    }

    public void stop() {
        m_tracker.stop();
        cancelNotifications();
    }
    //endregion

    public String getFormattedTime() {
        long elapsedTimeMillis = m_tracker.getElapsedTime();
        return TimeFormat.formatElapsedTime( elapsedTimeMillis );
    }

    public boolean isTrackerRunning() {
        return m_tracker.isRunning();
    }
}
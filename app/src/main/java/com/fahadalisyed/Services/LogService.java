package com.fahadalisyed.Services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.NotificationCompat;
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
    private NotificationCompat.Builder m_notificationBuilder;
    private Notification m_logNotification;
    private final long m_logFrequency = 1000;
    private final int TICK = 2;
    private BroadcastReceiver receiver;

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
        Intent notificationIntent = new Intent(this, Home.class);

        // Stop button in the notification
        Intent stop = new Intent();
        stop.setAction("Stop");
        PendingIntent pendingIntentStop = PendingIntent.getBroadcast(this, SERVICE_ID, stop, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        CharSequence settingsText = "LogIt";

        m_notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(settingsText)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.stop_icon, "Stop", pendingIntentStop);

        m_logNotification = m_notificationBuilder.build();
        m_notificationManager.notify(SERVICE_ID, m_logNotification);

        setupNotificationReceiver();
    }

    private void setupNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("Stop");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stop();
            }
        };

        registerReceiver(receiver, filter);

    }
    /**
     * This method updates our notification with the Log time
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateSettingNotification() {
        CharSequence contentText = "Current Log: " + getFormattedTime();
        m_notificationBuilder.setContentText(contentText);
        m_notificationManager.notify(SERVICE_ID, m_notificationBuilder.build());
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
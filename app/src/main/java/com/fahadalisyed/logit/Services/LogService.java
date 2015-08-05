package com.fahadalisyed.logit.Services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fahadalisyed.logit.Home.Home;
import com.fahadalisyed.logit.Utilities.TimeFormat;
import com.fahadalisyed.logit.R;

import java.util.Date;

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

    /**
     * This method creates a new tracker and a notification manager for our icon, time and name
     * to display on the settings panel
     */
    @Override
    public void onCreate() {
        super.onCreate();
        m_tracker = new Tracker(); //Create a new Tracker Object
        m_notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
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
     * This method updates our notification with the Log time and start/stop buttons
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void updateSettingNotification() {
        Intent notificationIntent = new Intent(this, Home.class);

        //TODO: Refactor RE5610
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        CharSequence settingsText = "LogIt";

        m_notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(settingsText)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(contentIntent)
                .setOngoing(true);

        CharSequence contentText;

        if (isTrackerRunning()) {
            // Stop button in the notification
            Intent stopLog = new Intent();
            stopLog.setAction("Stop");
            PendingIntent pendingIntentStop = PendingIntent.getBroadcast(this, SERVICE_ID, stopLog, PendingIntent.FLAG_UPDATE_CURRENT);

            m_notificationBuilder.addAction(R.drawable.stop_icon, "Stop", pendingIntentStop);
            contentText = "Current Log: " + getFormattedElapsedTime();
        } else {
            // Start button in notification
            Intent startLog = new Intent();
            startLog.setAction("Start");
            PendingIntent pendingIntentStart = PendingIntent.getBroadcast(this, SERVICE_ID, startLog, PendingIntent.FLAG_UPDATE_CURRENT);

            m_notificationBuilder.addAction(R.drawable.stop_icon, "Start", pendingIntentStart);
            contentText = "Log Finished: " + TimeFormat.formatElapsedTime( m_tracker.getDuration() );
        }

        m_notificationBuilder.setContentText(contentText);
        m_logNotification = m_notificationBuilder.build();
        m_notificationManager.notify(SERVICE_ID, m_logNotification);
    }

    //endregion

    //region Tracker Start/Stop

    /**
     * The bottom two methods are mediums for the Home.java to communicate with the
     * Tracker
     */
    public void start() {
        m_tracker.start();
        updateSettingNotification();
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK), m_logFrequency);
    }

    public void stop() {
        m_tracker.stop();
        updateSettingNotification();
        Log.d(TAG, "Start time: " + TimeFormat.formatDate( m_tracker.getStartDate()) );
        Log.d(TAG, "End time: " + TimeFormat.formatDateTime( m_tracker.getEndDate()) );
        Log.d(TAG, "Duration time: " + TimeFormat.formatElapsedTime( m_tracker.getDuration() ));
    }
    //endregion

    public String getFormattedElapsedTime() {
        long elapsedTimeMillis = m_tracker.getElapsedTime();
        return TimeFormat.formatElapsedTime( elapsedTimeMillis );
    }

    //TODO: put this in an interface
    public Date getStartDate() {
        return m_tracker.getStartDate();
    }

    public Date getEndDate() {
        return m_tracker.getEndDate();
    }

    public long getDuration() {
        return m_tracker.getDuration();
    }

    public boolean isTrackerRunning() {
        return m_tracker.isRunning();
    }
}
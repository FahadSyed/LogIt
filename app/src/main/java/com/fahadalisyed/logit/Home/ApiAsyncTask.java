package com.fahadalisyed.logit.Home;


import android.os.AsyncTask;
import android.util.Log;

import com.fahadalisyed.logit.Log.LogItem;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class ApiAsyncTask extends AsyncTask<Void, Void, Boolean> {
    static final int REQUEST_AUTHORIZATION = 1001;

    private MainActivity mActivity;
        private Confirm mConfirmActivity;
        private LogItem m_logItem;
    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(MainActivity activity) {
        this.mActivity = activity;
    }

    ApiAsyncTask(Confirm activity, LogItem item) {
        this.mConfirmActivity = activity;
        this.m_logItem = item;
    }


    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Log.d("ApiAsyncTask", "We inside doInBackground");
            createEvent();
            return true;
        } catch (Exception e) {
            //mConfirmActivity.updateStatus("The following error occurred:\n" +
                    //e.getMessage());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (aBoolean) {
            mConfirmActivity.updateStatus("Saved to Google Calendar");
        } else {
            mConfirmActivity.updateStatus("Failed to save to calendar");
        }
    }

    public void createEvent() {
        Event event = new Event()
                .setSummary(this.m_logItem.getName())
                .setLocation("Mississauga, Toronto") // Temporary
                .setDescription(this.m_logItem.getDescription());

        DateTime startDateTime = new DateTime("2015-05-26T09:00:00-02:00");

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


        TimeZone zone = TimeZone.getTimeZone("America/Toronto");

        startDateTime = new DateTime(this.m_logItem.getStartTime(), zone);


        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Toronto");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2015-05-26T17:00:00-03:00");

        endDateTime = new DateTime(this.m_logItem.getEndTime(), zone);


        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Toronto");

        event.setEnd(end);

        String calendarId = "primary";
        try {
            event = mConfirmActivity.mService.events().insert(calendarId, event).execute();
            Log.d("ApiAsyncTask", "Sucessfull event");
        } catch (UserRecoverableAuthIOException e) {
            mConfirmActivity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (Exception e) {
            Log.d("ApiAsyncTack", "Failed event: " + e.getLocalizedMessage());
        }
    }
}
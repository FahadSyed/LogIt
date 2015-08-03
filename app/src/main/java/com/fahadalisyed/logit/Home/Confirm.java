package com.fahadalisyed.logit.Home;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadalisyed.logit.Log.LogItem;
import com.fahadalisyed.logit.Log.LogItemManager;
import com.fahadalisyed.logit.R;
import com.fahadalisyed.logit.Utilities.TimeFormat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;
import java.util.Date;

public class Confirm extends ActionBarActivity {
    /**
     * This class is the confirm screen where the user inputs information
     * and is shown information before submitting the event to google calendar
     */
    private static final String START_DATE = "StartDate";
    private static final String END_DATE = "EndDate";
    private static final String ELAPSED_TIME = "ElapsedTime";

    private Date m_startDate;
    private Date m_endDate;
    private long m_duration;

    private EditText m_activityNameET;
    private EditText m_activityDescriptionET;

    private TextView m_durationTV;
    private TextView m_startDateTV;
    private TextView m_endDateTV;
    private TextView m_startTimeTV;
    private TextView m_endTimeTV;

    private LogItemManager m_logItemManager;
    private LogItem m_logItem;
    // Google Calendar

    com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        Intent intent = getIntent();
        extractIntent(intent);
        setContentView(R.layout.activity_confirm);

        setupGivenInformationViews();
        setViews();

        m_logItemManager = LogItemManager.getInstance();

        // Calendar

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();


    }

    private void extractIntent( Intent intent ) {
        m_startDate = new Date();
        m_endDate = new Date();

        m_startDate.setTime(intent.getLongExtra(START_DATE, -1));
        m_endDate.setTime(intent.getLongExtra(END_DATE, -1));
        m_duration = intent.getLongExtra(ELAPSED_TIME, -1);
    }

    private void setupGivenInformationViews() {
        m_activityNameET = (EditText)findViewById(R.id.logItemName);
        m_activityDescriptionET = (EditText)findViewById(R.id.logItemDescription);
        m_durationTV = (TextView)findViewById(R.id.duration);
        m_startDateTV = (TextView)findViewById(R.id.startDate);
        m_endDateTV = (TextView)findViewById(R.id.endDate);

        m_startTimeTV = (TextView)findViewById(R.id.startTime);
        m_endTimeTV = (TextView)findViewById(R.id.endTime);
    }

    private void setViews() {
        m_startDateTV.setText( TimeFormat.formatDate(m_startDate) );
        m_endDateTV.setText( TimeFormat.formatDate(m_endDate) );

        m_startTimeTV.setText(TimeFormat.formatDateTime(m_startDate));
        m_endTimeTV.setText(TimeFormat.formatDateTime(m_endDate));
        m_durationTV.setText( TimeFormat.formatElapsedTime(m_duration) );
    }

    public void saveToCalendar( View v ) {
        String logItemName = m_activityNameET.getText().toString();
        String logItemDescription = m_activityDescriptionET.getText().toString();

        m_logItem = m_logItemManager.createLogItem(
                logItemName,
                logItemDescription,
                m_startDate,
                m_endDate,
                m_duration
        );

        saveLogItem();
        m_logItemManager.printLogItem(m_logItem);
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        saveUsingAsyncTask();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    updateStatus("Account unspecified");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                saveUsingAsyncTask();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to save to Google Calendar, if the
     * email address isn't known yet, then call chooseAccount()
     * method so the user can pick an account.
     */
    private void saveLogItem() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            saveUsingAsyncTask();
        }
    }

    private void saveUsingAsyncTask() {
        if (isDeviceOnline()) {
            new ApiAsyncTask(this, m_logItem).execute();
        } else {
            updateStatus("No network connection available");
        }
    }
    /**
     * Show a status message in the list header TextView; called from background
     * threads and async tasks that need to update the UI (in the UI thread).
     * @param message a String to display in the UI header TextView.
     */
    public void updateStatus(final String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Starts the activity so the user can choose an account.
     */
    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        Confirm.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if( id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            return true;
        }
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        return super.onOptionsItemSelected(item);
    }
}

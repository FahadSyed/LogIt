package com.fahadalisyed.logit.Home;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fahadalisyed.logit.Log.LogItem;
import com.fahadalisyed.logit.Services.LogService;
import com.fahadalisyed.logit.R;

import android.os.Handler;

import java.util.Calendar;
import java.util.Date;


public class Home extends ActionBarActivity {
/*
    This class is the first activity the user encounters, contains
    the start/stop button along with the log
 */
    private static final String TAG = Home.class.getSimpleName();
    static final String ELAPSED_TIME = "elapsedTime";

    private final long TRACKER_MILLIS = 1000;

    private Button m_startButton;
    private Button m_stopButton;
    private TextView m_logTimeDisplay;
    private LogService m_logService;
    private ServiceConnection m_logServiceConnection;
    private Handler m_logHandler;
    private BroadcastReceiver m_receiver;
    private String m_elapsedTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Start the LogService
        startService(new Intent(this, LogService.class));

        // setup the buttons/handers/display/connection
        setupStopButton();
        setupLogHandler();
        setupStartButton();
        setupLogTimeDisplay();
        setupLogServiceConnection();
        setupNotificationReceiver();
        bindLogService();

    }

    private void setupLogHandler() {
        m_logHandler = new Handler() {
            public void handleMessage(Message m) {
                m_elapsedTime = m_logService.getFormattedElapsedTime();
                updateElapsedTime(m_elapsedTime);
                sendMessageDelayed(Message.obtain(this, 2), TRACKER_MILLIS);
            }
        };
    }

    private void bindLogService() {
        bindService(new Intent(this, LogService.class),
                m_logServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupLogServiceConnection(){
        m_logServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Home.this.m_logService = ((LogService.LocalBinder)service).getService();
                m_logHandler.sendMessageDelayed(Message.obtain(m_logHandler, 2), TRACKER_MILLIS);
                displayStartStopButtons();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                m_logService = null;
            }
        };

    }

    private void setupNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("Stop");
        filter.addAction("Start");

        m_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Stop")) {
                    stopLog();
                } else if (intent.getAction().equals("Start")) {
                    startLog();
                }
            }
        };

        registerReceiver(m_receiver, filter);

    }
    //region Start/Stop Setup & Display
    private void displayStartStopButtons() {
        m_startButton.setVisibility( m_logService.isTrackerRunning() ? View.GONE : View.VISIBLE );
        m_stopButton.setVisibility((m_logService.isTrackerRunning() ? View.VISIBLE : View.GONE));
    }

    private void setupStartButton() {
        m_startButton = (Button)findViewById( R.id.startButton );
        m_startButton.setOnClickListener(startButtonOnClickListener());

    }

    private void setupStopButton() {
        m_stopButton = (Button) findViewById( R.id.stopButton );
        m_stopButton.setOnClickListener(stopButtonOnClickListener());

    }
    //endregion

    //region Elapsed Time Setup and Updated
    private void setupLogTimeDisplay() {
        m_logTimeDisplay = (TextView) findViewById( R.id.logTimeDisplay );
    }

    private void updateElapsedTime( String elapsedTime ) {
        m_logTimeDisplay.setText(elapsedTime);
    }

    //endregion

    //region Start/Stop OnClick
    private View.OnClickListener startButtonOnClickListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
               startLog();
            }
        };
    }

    private void startLog() {
        m_logService.start();
        Date startTime = Calendar.getInstance().getTime();
        displayStartStopButtons();
    }

    private View.OnClickListener stopButtonOnClickListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                stopLog();
            }
        };
    }

    private void stopLog() {
        m_logService.stop();
        Date endTime = Calendar.getInstance().getTime();
        displayStartStopButtons();
    }
    //endregion

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
        unbindService(m_logServiceConnection);
        unregisterReceiver(m_receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        updateElapsedTime(m_elapsedTime);
        super.onResume();
    }

    //region Home Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ELAPSED_TIME, m_elapsedTime);
        Log.d(TAG, "onSaveInstanceState: " + m_elapsedTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        m_elapsedTime = savedInstanceState.getString(ELAPSED_TIME);
        Log.d(TAG, "onRestoreInstanceState: " + m_elapsedTime);
        updateElapsedTime(m_elapsedTime);
        super.onRestoreInstanceState(savedInstanceState);
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
        }

        return super.onOptionsItemSelected(item);
    }


    //endregion
}

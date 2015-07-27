package com.fahadalisyed.Home;

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

import com.fahadalisyed.Services.LogService;
import com.fahadalisyed.logit.R;

import android.os.Handler;


public class Home extends ActionBarActivity {
/*
    This class is the first activity the user encounters, contains
    the start/stop button along with the log
 */
    private static final String TAG = Home.class.getSimpleName();

    private Button m_startButton;
    private Button m_stopButton;
    private TextView m_logTimeDisplay;
    private LogService m_logService;
    private ServiceConnection m_logServiceConnection;
    private Handler m_logHandler;
    private final long m_logFrequency = 1000;
    private BroadcastReceiver receiver;


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
                updateElapsedTime();
                sendMessageDelayed(Message.obtain(this, 2), m_logFrequency);
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
                m_logHandler.sendMessageDelayed(Message.obtain(m_logHandler, 2), m_logFrequency);
                Log.d(TAG, " inside setuplogserviceconnection");
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
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("Stop")) {
                    stopLog();
                } else if (intent.getAction().equals("Start")) {
                    Log.d(TAG, "yo in side home start");

                    startLog();
                }
            }
        };

        registerReceiver(receiver, filter);

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

    private void updateElapsedTime() {
        m_logTimeDisplay.setText(m_logService.getFormattedTime());
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
        m_startButton.setVisibility( View.GONE );
        m_stopButton.setVisibility( View.VISIBLE );
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
        m_startButton.setVisibility( View.VISIBLE );
        m_stopButton.setVisibility(View.GONE);
        m_logService.updateSettingNotification(true);
        // TO implement: Summary screen activity
    }
    //endregion

    //region Home Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
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

package com.fahadalisyed.logit.Home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fahadalisyed.logit.Services.LogService;
import com.fahadalisyed.logit.R;
import com.fahadalisyed.logit.Utilities.TimeFormat;
import com.fahadalisyed.logit.morpher.DigitalClockView;
import com.fahadalisyed.logit.morpher.font.DFont;

import android.os.Handler;

import java.util.Calendar;


public class Home extends Activity {
/*
    This class is the first activity the user encounters, contains
    the start/stop button along with the log
 */
    public static final String EXTRA_MORPHING_DURATION = "morphing_duration";
    private static final String TAG = Home.class.getSimpleName();
    private static final String INITIAL_MORPHER_TEXT = "00:00:00";
    private static final String STOP = "Stop";
    private static final String START = "Start";
    private static final String START_DATE = "StartDate";
    private static final String END_DATE = "EndDate";
    private static final String ELAPSED_TIME = "ElapsedTime";
    private final long TRACKER_MILLIS = 1000;

    private Button m_startButton;
    private Button m_stopButton;
    private TextView m_currentDate;
    private LogService m_logService;
    private ServiceConnection m_logServiceConnection;
    private Handler m_logHandler;
    private BroadcastReceiver m_receiver;
    private String m_elapsedTime;

    private DigitalClockView mDigitalClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_home);
        // Start the LogService
        startService(new Intent(this, LogService.class));

        // setup the buttons/handers/display/connection
        setupViews();
        setupLogHandler();
        setupLogServiceConnection();
        setupNotificationReceiver();
        bindLogService();
        setupDigitalClockView();
        m_logHandler.sendMessageDelayed(Message.obtain(m_logHandler, 1), TRACKER_MILLIS);
        updateElapsedTime(INITIAL_MORPHER_TEXT); // Temporary!

    }

    private void setupLogHandler() {
        m_logHandler = new Handler() {
            public void handleMessage(Message m) {
                if (m_logService.isTrackerRunning()) {
                    m_elapsedTime = m_logService.getFormattedElapsedTime();
                    sendMessageDelayed(Message.obtain(this, 2), TRACKER_MILLIS);
                    updateElapsedTime(m_elapsedTime);
                }

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
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                m_logService = null;
            }
        };

    }

    private void setupNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(STOP);
        filter.addAction(START);

        m_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(STOP)) {
                    stopLog();
                } else if (intent.getAction().equals(START)) {
                    startLog();
                }
            }
        };

        registerReceiver(m_receiver, filter);

    }

    private void displayStartStopButtons() {
        m_startButton.setVisibility( m_logService.isTrackerRunning() ? View.GONE : View.VISIBLE );
        m_stopButton.setVisibility((m_logService.isTrackerRunning() ? View.VISIBLE : View.GONE));
    }

    private void setupViews() {
        m_startButton = (Button)findViewById( R.id.startButton );
        m_stopButton = (Button) findViewById( R.id.stopButton );
        m_currentDate = (TextView)findViewById( R.id.currentDate );
        setCurrentDate();

    }

    private void setCurrentDate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Calendar currentCalendar = Calendar.getInstance();
                m_currentDate.setText(TimeFormat.formatDate(currentCalendar.getTime()));
            }
        });
    }
    private void setupDigitalClockView() {

        mDigitalClockView = (DigitalClockView) findViewById(R.id.digitalClock);
        mDigitalClockView.setFont(new DFont(130, 10));

        int morphingDuration = getIntent().getIntExtra(EXTRA_MORPHING_DURATION, DigitalClockView.DEFAULT_MORPHING_DURATION);
        mDigitalClockView.setMorphingDuration(morphingDuration);

    }

    public void updateElapsedTime( final String elapsedTime ) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mDigitalClockView.isMorphingAnimationRunning()) {
                    mDigitalClockView.setTime(elapsedTime);
                }
            }
        });
    }

    private void startLog() {
        m_logHandler.sendMessageDelayed(Message.obtain(m_logHandler, 2), TRACKER_MILLIS);
        m_logService.start();
        displayStartStopButtons();
    }

    public void startLog(View view) {
        startLog();
    }

    public void stopLog(View view) {
        stopLog();
    }

    public void stopLog() {
        m_logService.stop();
        startConfirmScreen();
        displayStartStopButtons();
        updateElapsedTime(INITIAL_MORPHER_TEXT); // Temporary!
    }

    @Override
    public void finish() {
        super.finish();
    }

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
        if (m_logService != null && m_logService.isTrackerRunning()) {
            updateElapsedTime(m_elapsedTime);
        }
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

    private void startConfirmScreen() {
        Intent intent = new Intent(this, Confirm.class);
        intent.putExtra(START_DATE, m_logService.getStartDate().getTime());
        intent.putExtra(END_DATE, m_logService.getEndDate().getTime());
        intent.putExtra(ELAPSED_TIME, m_logService.getDuration());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Home.this.startActivity(intent);
    }
}

package com.fahadalisyed.logit.Home;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fahadalisyed.logit.Log.LogItem;
import com.fahadalisyed.logit.R;
import com.fahadalisyed.logit.Utilities.TimeFormat;

import java.util.Date;

public class Confirm extends ActionBarActivity {
    /**
     * This class is the confirm screen where the user inputs information
     * and is shown information before submitting the event to google calendar
     */
    private static final String START_DATE = "StartDate";
    private static final String END_DATE = "EndDate";
    private static final String ELAPSED_TIME = "ElapsedTime";

    private LogItem m_logItem;
    private Date m_startDate;
    private Date m_endDate;
    private long m_duration;
    private String m_activityName;
    private String m_activityDescription;

    private TextView m_durationTV;
    private TextView m_startDateTV;
    private TextView m_endDateTV;
    private TextView m_startTimeTV;
    private TextView m_endTimeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        Intent intent = getIntent();
        extractIntent(intent);
        setContentView(R.layout.activity_confirm);

        setupGivenInformationViews();
        setViews();
    }

    private void extractIntent( Intent intent ) {
        m_startDate = new Date();
        m_endDate = new Date();

        m_startDate.setTime(intent.getLongExtra(START_DATE, -1));
        m_endDate.setTime(intent.getLongExtra(END_DATE, -1));
        m_duration = intent.getLongExtra(ELAPSED_TIME, -1);
    }

    private void setupGivenInformationViews() {
        m_durationTV = (TextView)findViewById(R.id.duration);
        m_startDateTV = (TextView)findViewById(R.id.startDate);
        m_endDateTV = (TextView)findViewById(R.id.endDate);

        m_startTimeTV = (TextView)findViewById(R.id.startTime);
        m_endTimeTV = (TextView)findViewById(R.id.endTime);
    }

    private void setViews() {
        m_startDateTV.setText( TimeFormat.formatDate(m_startDate) );
        m_endDateTV.setText( TimeFormat.formatDate(m_endDate) );

        m_startTimeTV.setText( TimeFormat.formatDateTime(m_startDate) );
        m_endTimeTV.setText( TimeFormat.formatDateTime(m_endDate) );
        m_durationTV.setText( TimeFormat.formatElapsedTime(m_duration) );
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
            Log.d("CONFIRM", "item id: " + id);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            return true;
        }
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        return super.onOptionsItemSelected(item);
    }
}

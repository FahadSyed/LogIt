package com.fahadalisyed.logit.Services;

import com.fahadalisyed.logit.Utilities.GetTime;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by fahadsyed on 15-07-25.
 */
public class Tracker {
    /**
     * This class binds with the LogService and handles elapsed time
     */
    private enum State { STOPPED, RUNNING};

    private long m_startTime;
    private long m_stopTime;
    private State m_state;

    private Date m_startDate;
    private Date m_endDate;
    private long m_duration;

    private GetTime SystemTime = new GetTime() {
        @Override
        public long currentTime() {
            return System.currentTimeMillis();
        }
    };

    public Tracker() {
        stop();
    }

    /*
     * Start timing if we are in a stopped state.
     */
    public void start() {
        if ( m_state == State.STOPPED ) {
            m_stopTime = 0;
            m_startTime = SystemTime.currentTime();
            m_state = State.RUNNING;
            m_startDate = Calendar.getInstance().getTime();
        }
    }

    /*
     * Reset the Tracker to the initial state, clearing all stored times.
     */
    public void stop() {
        m_duration = getElapsedTime();
        m_state = State.STOPPED;
        m_stopTime = m_startTime = 0;
        m_endDate = Calendar.getInstance().getTime();
    }

    /**
     * @return The amount of time recorded by the Tracker, in milliseconds
     */
    public long getElapsedTime() {
        if ( m_state == State.STOPPED ) {
            return (m_stopTime - m_startTime);
        } else {
            return (SystemTime.currentTime() - m_startTime);
        }
    }

    /**
     * @return returns if Tracker is running or not
     */
    public boolean isRunning() {
        return (m_state == State.RUNNING);
    }

    public Date getStartDate() {
        return m_startDate;
    }

    public Date getEndDate() {
        return m_endDate;
    }

    public long getDuration() {
        return m_duration;
    }
}

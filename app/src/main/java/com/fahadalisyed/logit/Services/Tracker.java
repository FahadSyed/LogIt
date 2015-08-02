package com.fahadalisyed.logit.Services;

import com.fahadalisyed.logit.Utilities.GetTime;

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
        }
    }

    /*
     * Reset the Tracker to the initial state, clearing all stored times.
     */
    public void stop() {
        m_state = State.STOPPED;
        m_stopTime = m_startTime = 0;
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
}

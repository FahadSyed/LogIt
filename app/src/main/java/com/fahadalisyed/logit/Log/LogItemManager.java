package com.fahadalisyed.logit.Log;

import android.util.Log;

import java.util.Date;

/**
 * Created by fahadsyed on 15-08-03.
 */
public class LogItemManager {

    private int m_logItemCounter;
    private static LogItemManager m_logItemManager;

    private void LogItemManager (){
        m_logItemCounter = 0;
    }

    public static LogItemManager getInstance() {

        if (m_logItemManager == null) {
            m_logItemManager = new LogItemManager();
            return m_logItemManager;
        }

        return m_logItemManager;
    }

    public LogItem createLogItem (
            String name,
            String description,
            String location,
            Date startTime,
            Date endTime,
            long duration
    ) {

        m_logItemCounter++;

        return new LogItem(
                name,
                description,
                location,
                startTime,
                endTime,
                duration
        );
    }

    public void printLogItem ( LogItem item ) {
        Log.d("LogItemManager", "name: " + item.getName() + " desc: " + item.getDescription() + " start: " + item.getStartTime() + " end: " + item.getEndTime() + " duration: " + item.getDuration());
    }
}

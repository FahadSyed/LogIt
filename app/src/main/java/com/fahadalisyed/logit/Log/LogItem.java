package com.fahadalisyed.logit.Log;

import java.util.Date;

/**
 * Created by fahadsyed on 15-08-02.
 */
public class LogItem {

    private String m_name;
    private String m_description;
    private Date m_startTime;
    private Date m_endTime;
    private long m_duration;


    public LogItem(
            String m_name,
            String m_description,
            Date m_startTime,
            Date m_endTime,
            long m_duration
    ) {
        this.m_name = m_name;
        this.m_description = m_description;
        this.m_startTime = m_startTime;
        this.m_endTime = m_endTime;
        this.m_duration = m_duration;
    }

    public String getName() {
        return m_name;
    }

    public String getDescription() {
        return m_description;
    }

    public Date getStartTime() {
        return m_startTime;
    }

    public Date getEndTime() {
        return m_endTime;
    }

    public long getDuration() {
        return m_duration;
    }
}

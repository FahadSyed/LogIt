package com.fahadalisyed.logit.Log;

import java.util.Date;

/**
 * Created by fahadsyed on 15-08-02.
 */
public class LogItem {
    /**
     * This class is the log items which are saved to Google calendar.
     */
    private String m_name;
    private String m_description;
    private Date m_startTime;
    private Date m_endTime;
    private long m_duration;


    protected LogItem(
            String name,
            String description,
            Date startTime,
            Date endTime,
            long duration
    ) {
        this.m_name = name;
        this.m_description = description;
        this.m_startTime = startTime;
        this.m_endTime = endTime;
        this.m_duration = duration;
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

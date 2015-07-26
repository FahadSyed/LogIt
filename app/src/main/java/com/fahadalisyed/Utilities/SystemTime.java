package com.fahadalisyed.Utilities;

import com.fahadalisyed.Services.Tracker;

/**
 * Created by fahadsyed on 15-07-25.
 */
public class SystemTime implements GetTime{
    @Override
    public long currentTime() {
        return System.currentTimeMillis();
    }
}

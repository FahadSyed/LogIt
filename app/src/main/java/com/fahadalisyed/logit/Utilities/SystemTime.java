package com.fahadalisyed.logit.Utilities;

import com.fahadalisyed.logit.Services.Tracker;

/**
 * Created by fahadsyed on 15-07-25.
 */
public class SystemTime implements GetTime{
    @Override
    public long currentTime() {
        return System.currentTimeMillis();
    }
}

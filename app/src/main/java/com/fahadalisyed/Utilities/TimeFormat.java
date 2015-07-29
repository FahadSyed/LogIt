package com.fahadalisyed.Utilities;

import android.text.format.DateFormat;

import java.sql.Time;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by fahadsyed on 15-07-26.
 */
public class TimeFormat {

    public static String formatElapsedTime ( long milliseconds ) {
        return String.format("%02d:%02d:%02d",
            elapsedHours( milliseconds ),
            elapsedMinutes( milliseconds ),
            elapsedSeconds( milliseconds )
        );
    }

    public static long elapsedHours ( long milliseconds ) {
        return TimeUnit.MILLISECONDS.toHours(milliseconds);
    }

    public static long elapsedMinutes ( long milliseconds ) {
        return TimeUnit.MILLISECONDS.toMinutes( milliseconds ) -
                TimeUnit.HOURS.toMinutes( TimeUnit.MILLISECONDS.toHours( milliseconds ) );
    }

    public static long elapsedSeconds ( long milliseconds ) {
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
    }

    public static String currentTime () {
        String delegate = "hh:mm aaa";
        return  (String) DateFormat.format(delegate, Calendar.getInstance().getTime());
    }
}

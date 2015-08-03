package com.fahadalisyed.logit.Utilities;

import android.text.format.DateFormat;
import android.util.Log;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by fahadsyed on 15-07-26.
 */
public class TimeFormat {

    public static String formatElapsedTime ( long milliseconds ) {
        if (milliseconds < 60000 ) {
            return String.format("%d seconds",
                    elapsedSeconds( milliseconds )
            );
        } else if ( milliseconds > 60000 && milliseconds < 36000000) {
            return String.format("%d minutes, %d seconds",
                    elapsedMinutes( milliseconds ),
                    elapsedSeconds( milliseconds )
            );
        }

        return String.format("%d hours %d minutes, %d seconds",
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

    public static String formatDateTime( Date time )  {
        String delegate = "hh:mm aaa";
        return  (String) DateFormat.format(delegate, time);
    }

    public static String formatDate( Date date ) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d yyyy");
        return dayFormat.format( date );
    }
}

package sparqles.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class DateFormater {

	public static final String YYYYMMDD = "yyyy-MM-dd";
	public static final String YYYYMMDDHHMM = "yyyy-MM-dd#HH:mm";
	public static final String ISO8601  = "yyyy-MM-dd'T'HH:mm'Z'";
	static final Calendar CAL = GregorianCalendar.getInstance();
	
	public static String getDataAsString(String pattern){
		return getDataAsString(pattern, CAL.getTime());
	}
	
	public static String getDataAsString(String pattern, Date date){
		return new SimpleDateFormat(pattern).format(date);
	}
	
	public static String formatInterval(final long l)
    {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }
}
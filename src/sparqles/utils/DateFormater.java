package sparqles.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
}
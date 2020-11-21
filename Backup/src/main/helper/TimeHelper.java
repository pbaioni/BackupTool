package main.helper;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;

public class TimeHelper {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	
	public static Calendar getNow() {
		return Calendar.getInstance();
	}
	
	public static String getFormattedNow() {
		return sdf.format(getNow().getTime());
	}
	
	public static String format(Calendar cal) {
		return sdf.format(cal.getTime());
	}
	
	public static String getElapsedTime(Calendar start) {
		return getElapsedTime(start, getNow());
	}

	public static String getElapsedTime(Calendar start, Calendar end) {
		
		Duration diff = Duration.between(start.toInstant(), end.toInstant());
		long hours = diff.toHours();
		long minutes = diff.toMinutes() - 60 * hours;
		long seconds = diff.getSeconds() - 60 * minutes - 3600 * hours;

		String duration = hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
		
		return duration;
		
	}
	
}

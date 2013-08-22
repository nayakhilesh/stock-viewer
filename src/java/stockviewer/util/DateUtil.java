package stockviewer.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	private static final Calendar cal = Calendar.getInstance();
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	static {
		cal.setTimeZone(GMT);
	}

	public static Date truncate(Date date) {

		if (date == null)
			return null;

		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);

		return cal.getTime();

	}

}

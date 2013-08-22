package stockviewer.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	private static final Calendar localCal = Calendar.getInstance();
	private static final Calendar gmtCal = Calendar.getInstance();
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	static {
		gmtCal.setTimeZone(GMT);
	}

	public static Date treatAsGmt(Date date) {

		// we take the day, month and year of the date object
		// and treat them as GMT regardless of the time zone

		if (date == null)
			return null;

		localCal.setTime(date);

		gmtCal.set(Calendar.YEAR, localCal.get(Calendar.YEAR));
		gmtCal.set(Calendar.MONTH, localCal.get(Calendar.MONTH));
		gmtCal.set(Calendar.DAY_OF_MONTH, localCal.get(Calendar.DAY_OF_MONTH));
		gmtCal.set(Calendar.HOUR_OF_DAY, 0);
		gmtCal.set(Calendar.MINUTE, 0);
		gmtCal.set(Calendar.SECOND, 0);
		gmtCal.set(Calendar.MILLISECOND, 0);

		// here it is truncated to 00:00:00,000 in GMT

		return gmtCal.getTime();

	}

}

package orient.http;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class HttpResponse {
  static TimeZone tz = TimeZone.getTimeZone("UTC");
  static String days[] = new String[] {
    "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
  };

  public static String gmtDate(long time) {
    SimpleDateFormat sdf = new SimpleDateFormat();
    TimeZone gmt = TimeZone.getTimeZone("GMT");
    sdf.setTimeZone(gmt);
  	Calendar c = Calendar.getInstance(tz);
  	Date d = new Date(time);
    c.setTime(d);

  	return days[c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY] + ", " + sdf.format(d); // <-- toGMTString() is deprecated method.
  }
}

package com.vtuan.gate;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Display-only time utilities. The remote server remains the authority for key validity.
 * Accepts RFC3339 timestamps like 2026-11-06T07:10:00+00:00 or ...Z.
 */
public final class KeyExpiry {
  private static final Pattern ISO=Pattern.compile(
    "^(\\d{4}-\\d{2}-\\d{2})[T ](\\d{2}:\\d{2}:\\d{2})(?:\\.(\\d{1,9}))?(Z|[+-]\\d{2}:?\\d{2})$");

  private KeyExpiry() {}

  /** Epoch milliseconds or null if the time cannot be safely decoded. */
  public static Long epochMillis(String input) {
    if(input==null) return null;
    Matcher m=ISO.matcher(input.trim());
    if(!m.matches()) return null;
    String base=m.group(1)+"T"+m.group(2);
    SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);
    fmt.setLenient(false);
    fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    ParsePosition pos=new ParsePosition(0);
    Date parsed=fmt.parse(base,pos);
    if(parsed==null || pos.getIndex()!=base.length()) return null;
    int millis=0;
    if(m.group(3)!=null) {
      String digits=(m.group(3)+"000").substring(0,3);
      millis=Integer.parseInt(digits);
    }
    String tz=m.group(4);
    long offsetMinutes=0;
    if(!"Z".equals(tz)) {
      String t=tz.substring(1).replace(":","");
      int hours=Integer.parseInt(t.substring(0,2));
      int minutes=Integer.parseInt(t.substring(2,4));
      if(hours>23 || minutes>59) return null;
      offsetMinutes=(hours*60L+minutes)*(tz.charAt(0)=='+'?1:-1);
    }
    return parsed.getTime()+millis-offsetMinutes*60_000L;
  }

  /** Local fail-closed check before accepting server-supplied expiration. */
  public static boolean isFuture(String expiry,long nowMillis) {
    Long end=epochMillis(expiry);
    return end!=null && end>nowMillis;
  }

  /** An estimate based on the device's clock; never used as authorization. */
  public static String remainingText(String expiry,long nowMillis) {
    Long end=epochMillis(expiry);
    if(end==null) return "Không xác định (định dạng thời gian)";
    long left=end-nowMillis;
    if(left<=0) return "Đã tới mốc hết hạn theo giờ thiết bị";
    long mins=(left+59_999L)/60_000L;
    long days=mins/1440, hours=(mins%1440)/60, minutes=mins%60;
    if(days>0) return days+" ngày "+hours+" giờ";
    if(hours>0) return hours+" giờ "+minutes+" phút";
    return minutes+" phút";
  }
}

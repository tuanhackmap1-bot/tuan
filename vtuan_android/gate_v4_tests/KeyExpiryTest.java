package com.vtuan.gate;

/** Pure JVM assertions; no device, key, or production server required. */
public final class KeyExpiryTest {
  private static void expect(boolean passed,String message){
    if(!passed) throw new AssertionError(message);
  }
  public static void main(String[] args){
    Long utc=KeyExpiry.epochMillis("2026-11-06T03:00:00Z");
    Long vn=KeyExpiry.epochMillis("2026-11-06T10:00:00+07:00");
    Long minus=KeyExpiry.epochMillis("2026-11-05T22:00:00-05:00");
    expect(utc!=null && utc.equals(vn) && utc.equals(minus),
           "Timezone offsets do not normalize to the same instant");
    Long millis=KeyExpiry.epochMillis("2026-11-06T03:00:00.123456+00:00");
    expect(millis!=null && millis-utc==123L,"Fractional seconds parse failed");
    expect(KeyExpiry.epochMillis("2026-02-30T01:00:00Z")==null,
           "Invalid calendar day accepted");
    expect(KeyExpiry.epochMillis("2026-11-06T10:00:00")==null,
           "Ambiguous timestamp without timezone accepted");
    expect(KeyExpiry.epochMillis("2026-11-06T10:00:00+25:00")==null,
           "Invalid timezone accepted");
    expect("1 ngày 2 giờ".equals(KeyExpiry.remainingText("2026-11-06T03:00:00Z",
           utc-26L*60*60*1000)),"Remaining time formatting failed");
    expect(KeyExpiry.remainingText("2026-11-06T03:00:00Z",utc+1000L)
           .contains("Đã tới"),"Elapsed estimate failed");
    System.out.println("PASS: 7 key-expiration parsing and display assertions");
  }
}

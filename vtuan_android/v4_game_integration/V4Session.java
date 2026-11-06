package com.vtuan.v4integration;

/**
 * Short-lived in-process VTUAN check. Does not modify the original native
 * V4 license flow. No key material is retained in static fields.
 */
public final class V4Session {
    private static final long MAX_MS=10L*60L*1000L;
    private static long checkedAt=0;
    private static long acceptedUntil=0;
    private static boolean valid=false;

    private V4Session(){}

    public static synchronized void clear(){
        valid=false;checkedAt=0;acceptedUntil=0;
    }

    public static synchronized void approve(long serverExpiryMs,long nowMs){
        clear();
        if(nowMs<=0)return;
        if(serverExpiryMs>0 && serverExpiryMs<=nowMs)return;
        checkedAt=nowMs;
        long ttl=nowMs+MAX_MS;
        if(ttl<nowMs)return;
        acceptedUntil=serverExpiryMs>0?Math.min(serverExpiryMs,ttl):ttl;
        valid=true;
    }

    public static synchronized boolean isValid(long nowMs){
        return valid && nowMs>=checkedAt &&
                nowMs<=acceptedUntil && acceptedUntil>checkedAt;
    }
}

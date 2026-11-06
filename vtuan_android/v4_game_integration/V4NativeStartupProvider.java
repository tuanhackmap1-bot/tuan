package com.vtuan.v4integration;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ysmteam.imgui.GLES3JNIView;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Explicit-opt-in native-menu bootstrap, running in the verified V4 package.
 * Does not change auth inside libysmteam.so.
 */
public final class V4NativeStartupProvider extends ContentProvider {
    private static final String TAG="VTUAN_V4_BOOT";
    private static final String GAME_ACTIVITY="com.garena.game.kgtw.SGameActivity";
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final java.util.Map<Activity,GLES3JNIView> attached=
        new WeakHashMap<Activity,GLES3JNIView>();
    private final Set<Activity> pending=Collections.newSetFromMap(
        new WeakHashMap<Activity,Boolean>());
    private final Set<Activity> resumed=Collections.newSetFromMap(
        new WeakHashMap<Activity,Boolean>());

    @Override public boolean onCreate() {
        Context context=getContext();
        if(context==null)return true;
        if(!(context.getApplicationContext() instanceof Application))return true;
        Application app=(Application)context.getApplicationContext();
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks(){
            @Override public void onActivityCreated(Activity a,Bundle b){}
            @Override public void onActivityStarted(Activity a){}
            @Override public void onActivityResumed(Activity a){
                if(!GAME_ACTIVITY.equals(a.getClass().getName()))return;
                resumed.add(a);
                GLES3JNIView existing=attached.get(a);
                if(existing!=null){
                    existing.resumeRendering();
                    return;
                }
                if(!V4Session.isValid(System.currentTimeMillis())){
                    Log.i(TAG,"No verified VTUAN session; native bridge not loaded");
                    return;
                }
                if(!pending.add(a))return;
                // Let Unity initialize its own surface before attaching the V4 GLES UI.
                ui.postDelayed(() -> {
                    pending.remove(a);
                    if(a.isFinishing()||a.isDestroyed()||!resumed.contains(a))return;
                    if(!V4Session.isValid(System.currentTimeMillis())){
                        Log.i(TAG,"VTUAN session expired before native startup");
                        return;
                    }
                    if(attached.containsKey(a))return;
                    Log.i(TAG,"Attempting original V4 JNI/EGL bootstrap");
                    GLES3JNIView view=GLES3JNIView.attach(a);
                    if(view==null){
                        Log.e(TAG,"Original native renderer was not attached");
                    } else {
                        attached.put(a,view);
                    }
                },2500);
            }
            @Override public void onActivityPaused(Activity a){
                resumed.remove(a);
                GLES3JNIView view=attached.get(a);
                if(view!=null)view.suspend();
            }
            @Override public void onActivityStopped(Activity a){}
            @Override public void onActivitySaveInstanceState(Activity a,Bundle b){}
            @Override public void onActivityDestroyed(Activity a){
                resumed.remove(a);
                pending.remove(a);
                GLES3JNIView view=attached.remove(a);
                if(view!=null)view.detach();
            }
        });
        Log.i(TAG,"V4 activity lifecycle bridge installed");
        return true;
    }
    @Override public String getType(Uri uri){return null;}
    @Override public Cursor query(Uri uri,String[] projection,String selection,
                                  String[] selectionArgs,String sortOrder){return null;}
    @Override public Uri insert(Uri uri,ContentValues values){return null;}
    @Override public int delete(Uri uri,String selection,String[] selectionArgs){return 0;}
    @Override public int update(Uri uri,ContentValues values,String selection,
                                String[] selectionArgs){return 0;}
}

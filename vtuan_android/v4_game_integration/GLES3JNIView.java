package com.ysmteam.imgui;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * JNI bridge for symbols actually exported by verified V4 libysmteam.so.
 *
 * This class restores a missing Java/native interface; it does NOT bypass
 * the V4 native license checks or make its game hooks compatible.
 * Load only on explicit, successfully verified VTUAN request.
 */
public final class GLES3JNIView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final String TAG="VTUAN_V4_JNI";
    private static boolean libraryLoaded=false;
    private static boolean loadAttempted=false;
    private static String loadError="";

    public static synchronized boolean tryLoadNative() {
        if(libraryLoaded)return true;
        if(loadAttempted)return false;
        loadAttempted=true;
        try {
            System.loadLibrary("ysmteam");
            libraryLoaded=true;
            Log.i(TAG,"Original libysmteam.so JNI_OnLoad completed");
        } catch(Throwable failure) {
            loadError=failure.getClass().getSimpleName()+": "+failure.getMessage();
            Log.e(TAG,"Original V4 library failed to load",failure);
        }
        return libraryLoaded;
    }

    public static String nativeLoadError(){return loadError;}

    public static native void init();
    public static native void resize(int width,int height);
    public static native void step();
    public static native boolean onTouch(int action,float x,float y);
    // Verified ARM64 implementation calls JNIEnv->NewStringUTF (JNI vtable offset
    // 0x538) and therefore returns java.lang.String, not a float[].
    public static native String getWindowRect();
    public static native void imgui_Shutdown();

    private boolean initialized=false;
    private final Handler ui=new Handler(Looper.getMainLooper());

    public GLES3JNIView(Activity activity) {
        super(activity);
        setEGLContextClientVersion(3);
        setEGLConfigChooser(8,8,8,8,16,8);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setFocusable(false);
        setClickable(false);
        Log.i(TAG,"GLSurfaceView bridge created");
    }

    @Override public void onSurfaceCreated(GL10 gl,EGLConfig config) {
        try {
            init();
            initialized=true;
            Log.i(TAG,"V4 EGL/ImGui initialization returned");
        } catch(Throwable e) {
            initialized=false;
            Log.e(TAG,"V4 native init failed; bridge disabled",e);
        }
    }

    @Override public void onSurfaceChanged(GL10 gl,int width,int height) {
        if(!initialized)return;
        try {resize(width,height);}
        catch(Throwable e){initialized=false;Log.e(TAG,"Native V4 resize failed",e);}
    }

    @Override public void onDrawFrame(GL10 gl) {
        if(!initialized)return;
        android.opengl.GLES20.glClearColor(0f,0f,0f,0f);
        android.opengl.GLES20.glClear(android.opengl.GLES20.GL_COLOR_BUFFER_BIT);
        try {step();}
        catch(Throwable e){initialized=false;Log.e(TAG,"Native V4 render failed",e);}
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if(!initialized)return false;
        try {
            int action=event.getActionMasked();
            if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_UP||
               action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_CANCEL) {
                return onTouch(action,event.getX(),event.getY());
            }
        }catch(Throwable e){
            Log.e(TAG,"Native touch bridge failed",e);
        }
        return false;
    }

    /** Add an in-process renderer above Unity's activity content, not a system overlay. */
    public static GLES3JNIView attach(Activity activity) {
        if(!tryLoadNative())return null;
        if(activity==null||activity.isFinishing()||activity.isDestroyed())return null;
        View decor=activity.getWindow().getDecorView();
        if(!(decor instanceof ViewGroup))return null;
        GLES3JNIView view=null;
        try {
            view=new GLES3JNIView(activity);
            ((ViewGroup)decor).addView(view,
                    new ViewGroup.LayoutParams(-1,-1));
            view.onResume();
            Log.i(TAG,"V4 JNI renderer attached to "+activity.getClass().getSimpleName());
            return view;
        }catch(Throwable error){
            Log.e(TAG,"Failed attaching V4 JNI renderer",error);
            if(view!=null){
                try {
                    if(view.getParent() instanceof ViewGroup){
                        ((ViewGroup)view.getParent()).removeView(view);
                    }
                }catch(Throwable ignored){}
            }
            return null;
        }
    }

    public void suspend(){
        try {onPause();}
        catch(Throwable e){Log.e(TAG,"Could not pause GL renderer",e);}
    }
    public void resumeRendering(){
        try {onResume();}
        catch(Throwable e){Log.e(TAG,"Could not resume GL renderer",e);}
    }
    public void detach(){
        initialized=false;
        suspend();
        try {
            if(getParent() instanceof ViewGroup){
                ((ViewGroup)getParent()).removeView(this);
            }
        }catch(Throwable e){Log.e(TAG,"Failed to detach GL renderer",e);}
    }
}

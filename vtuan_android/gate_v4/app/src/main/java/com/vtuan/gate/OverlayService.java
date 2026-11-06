package com.vtuan.gate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.Settings;

/**
 * Standalone, opt-in key-status overlay. Does not load, hook, read or modify games.
 * Deliberately short lived: only the last verified server and expiry are supplied;
 * no key or credential is ever sent to the service.
 */
public final class OverlayService extends Service {
  public static final String EXTRA_SERVER = "server";
  public static final String EXTRA_EXPIRY = "expiry";
  public static final String ACTION_STOP = "com.vtuan.gate.STOP_OVERLAY";
  private static final String CHANNEL = "vtuan_key_status";
  private static final int NOTIFICATION_ID = 1704;
  private static final long MAX_LIFETIME_MS = 10L * 60L * 1000L;

  private WindowManager wm;
  private WindowManager.LayoutParams windowParams;
  private LinearLayout root;
  private TextView details;
  private boolean expanded = false;
  private String serverLabel;
  private String expiresAt;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Runnable timeout = () -> stopSelf();

  private int dp(float number) {
    return (int)(getResources().getDisplayMetrics().density * number + 0.5f);
  }
  private GradientDrawable background(int fill, int cornerRadius) {
    GradientDrawable d=new GradientDrawable();
    d.setColor(fill);
    d.setCornerRadius(dp(cornerRadius));
    d.setStroke(dp(1), 0xffa465e9);
    return d;
  }
  private TextView text(String value, int size) {
    TextView t=new TextView(this);
    t.setText(value);
    t.setTextColor(Color.WHITE);
    t.setTextSize(size);
    t.setGravity(Gravity.CENTER);
    return t;
  }
  @Override public void onCreate() {
    super.onCreate();
    if(Build.VERSION.SDK_INT>=26) {
      NotificationManager manager=getSystemService(NotificationManager.class);
      if(manager!=null) {
        NotificationChannel channel=new NotificationChannel(
          CHANNEL,"VTUAN key overlay",NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("User-requested temporary key-status overlay");
        manager.createNotificationChannel(channel);
      }
    }
  }
  private Notification notification() {
    Intent open=new Intent(this,KeyActivity.class);
    PendingIntent pending=PendingIntent.getActivity(this,0,open,
        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    Intent close=new Intent(this,OverlayService.class);
    close.setAction(ACTION_STOP);
    PendingIntent closePending=PendingIntent.getService(this,1,close,
        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    Notification.Builder builder=Build.VERSION.SDK_INT>=26
        ?new Notification.Builder(this,CHANNEL)
        :new Notification.Builder(this);
    return builder.setContentTitle("VTUAN – trạng thái key")
        .setContentText("Menu nổi tạm thời; tự tắt sau tối đa 10 phút")
        .setSmallIcon(android.R.drawable.ic_lock_lock)
        .setOngoing(true).setContentIntent(pending)
        .addAction(new Notification.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,"Tắt menu nổi",closePending).build())
        .build();
  }
  @Override public int onStartCommand(Intent intent,int flags,int startId) {
    if(intent==null || ACTION_STOP.equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
    }
    if(Build.VERSION.SDK_INT>=23 && !Settings.canDrawOverlays(this)) {
      stopSelf();
      return START_NOT_STICKY;
    }
    String server=intent.getStringExtra(EXTRA_SERVER);
    String expiry=intent.getStringExtra(EXTRA_EXPIRY);
    if(!KeyApiPath.isValidServer(server) || KeyExpiry.epochMillis(expiry)==null ||
       KeyExpiry.epochMillis(expiry)<=System.currentTimeMillis()) {
      stopSelf();
      return START_NOT_STICKY;
    }
    serverLabel=server==null || server.isEmpty()?"Admin tổng":server;
    expiresAt=expiry;
    try {
      startForeground(NOTIFICATION_ID,notification());
      if(root==null) createOverlay();
      updateStatus();
      handler.removeCallbacks(timeout);
      handler.postDelayed(timeout,MAX_LIFETIME_MS);
    } catch(RuntimeException e) {
      stopSelf();
    }
    return START_NOT_STICKY;
  }
  private void createOverlay() {
    wm=(WindowManager)getSystemService(WINDOW_SERVICE);
    if(wm==null) throw new IllegalStateException("WindowManager unavailable");
    windowParams=new WindowManager.LayoutParams(
        dp(250),WindowManager.LayoutParams.WRAP_CONTENT,
        Build.VERSION.SDK_INT>=26
          ?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
          :WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT);
    windowParams.gravity=Gravity.TOP|Gravity.START;
    windowParams.x=dp(14);
    windowParams.y=dp(90);
    root=new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(10),dp(6),dp(10),dp(6));
    root.setBackground(background(0xe6221535,12));
    TextView handle=text("VTUAN  •  Key",14);
    root.addView(handle,new LinearLayout.LayoutParams(-1,dp(37)));
    details=text("",12);
    details.setGravity(Gravity.START);
    details.setPadding(dp(8),dp(8),dp(8),dp(10));
    details.setVisibility(View.GONE);
    root.addView(details,new LinearLayout.LayoutParams(-1,-2));
    TextView close=text("Đóng menu nổi",12);
    close.setPadding(dp(8),dp(6),dp(8),dp(6));
    close.setVisibility(View.GONE);
    close.setOnClickListener(v->stopSelf());
    root.addView(close,new LinearLayout.LayoutParams(-1,dp(32)));
    final float[] origin={0,0};
    final int[] start={0,0};
    final boolean[] moved={false};
    handle.setOnTouchListener((v,event)->{
      switch(event.getActionMasked()){
        case MotionEvent.ACTION_DOWN:
          origin[0]=event.getRawX();
          origin[1]=event.getRawY();
          start[0]=windowParams.x;
          start[1]=windowParams.y;
          moved[0]=false;
          return true;
        case MotionEvent.ACTION_MOVE:
          int dx=Math.round(event.getRawX()-origin[0]);
          int dy=Math.round(event.getRawY()-origin[1]);
          if(Math.abs(dx)+Math.abs(dy)>dp(6)) moved[0]=true;
          if(moved[0]){
            windowParams.x=Math.max(0,start[0]+dx);
            windowParams.y=Math.max(0,start[1]+dy);
            if(wm!=null && root!=null) wm.updateViewLayout(root,windowParams);
          }
          return true;
        case MotionEvent.ACTION_UP:
          if(!moved[0]){
            expanded=!expanded;
            details.setVisibility(expanded?View.VISIBLE:View.GONE);
            close.setVisibility(expanded?View.VISIBLE:View.GONE);
            updateStatus();
          }
          return true;
        default: return false;
      }
    });
    wm.addView(root,windowParams);
  }
  private void updateStatus(){
    if(details==null) return;
    details.setText("Server: "+serverLabel+"\n"
      +"Hết hạn: "+expiresAt.replace('T',' ')+"\n"
      +"Còn lại (giờ thiết bị): "+KeyExpiry.remainingText(expiresAt,System.currentTimeMillis())+"\n"
      +"Trạng thái: đã kiểm tra qua HTTPS trước khi mở.\n"
      +"Không có chức năng AIM/ESP.");
  }
  @Override public void onDestroy() {
    handler.removeCallbacks(timeout);
    if(wm!=null && root!=null) {
      try {wm.removeView(root);} catch(IllegalArgumentException ignored){}
    }
    wm=null; root=null; details=null;
    super.onDestroy();
  }
  @Override public IBinder onBind(Intent intent) {return null;}
}

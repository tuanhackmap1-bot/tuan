package com.vtuan.gate;
import android.app.Activity;
import android.provider.Settings;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.content.res.ColorStateList;
import android.view.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class KeyActivity extends Activity {
  private static final String HOST="https://vtuan-103-159-51-231.sslip.io";
  private static final String PREF="v4_key_state";
  private EditText input, svField;
  private CheckBox save,auto;
  private TextView status;
  private Button submit, launch, overlay;
  private SharedPreferences prefs;
  private boolean busy=false;
  private String verifiedKey=null;
  private String verifiedServer=null;
  private String verifiedExpiry=null;

  private int d(float x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable bg(int fill,int outline,int radius){
    GradientDrawable g=new GradientDrawable();
    g.setColor(fill);g.setCornerRadius(d(radius));
    if(outline!=0)g.setStroke(d(1),outline);
    return g;
  }
  private LinearLayout.LayoutParams item(int w,int h,int t){
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w<0?w:d(w),h<0?h:d(h));
    p.topMargin=d(t);return p;
  }
  private TextView text(String s,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
    if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
    t.setGravity(Gravity.CENTER);return t;
  }
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    prefs=getSharedPreferences(PREF,MODE_PRIVATE);
    FrameLayout root=new FrameLayout(this);
    root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff151023,0xff271331,0xff171020}));
    setContentView(root);
    ScrollView scroll=new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setClipToPadding(false);
    scroll.setPadding(d(10),d(10),d(10),d(10));
    root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
    LinearLayout holder=new LinearLayout(this);
    holder.setOrientation(LinearLayout.VERTICAL);
    holder.setGravity(Gravity.CENTER);
    scroll.addView(holder,new ScrollView.LayoutParams(-1,-1));
    LinearLayout panel=new LinearLayout(this);
    panel.setOrientation(1);panel.setPadding(d(16),d(16),d(16),d(15));
    panel.setBackground(bg(0xff291e3d,0xffb06cf3,17));
    panel.setElevation(d(12));
    int width=getResources().getDisplayMetrics().widthPixels;
    LinearLayout.LayoutParams box=new LinearLayout.LayoutParams(Math.min(d(385),width-d(40)),-2);
    holder.addView(panel,box);
    TextView title=text("CHÀO MỪNG BẠN ĐẾN VỚI VTUAN!",14,Color.WHITE,true);
    panel.addView(title,item(-1,-2,0));
    input=new EditText(this);
    input.setSingleLine(true);input.setTextSize(12);input.setTextColor(Color.WHITE);
    input.setHintTextColor(0xffd0aadf);input.setHint("Điền key vào đây");
    input.setPadding(d(15),0,d(15),0);
    input.setBackground(bg(0xff513268,0xffb87bed,22));
    panel.addView(input,item(-1,45,13));
    svField=new EditText(this);
    svField.setSingleLine(true);
    svField.setTextSize(12);
    svField.setTextColor(Color.WHITE);
    svField.setHintTextColor(0xffd0aadf);
    svField.setHint("Mã SV con (để trống nếu key Admin tổng)");
    svField.setPadding(d(15),0,d(15),0);
    svField.setBackground(bg(0xff513268,0xffb87bed,22));
    panel.addView(svField,item(-1,45,9));
    LinearLayout options=new LinearLayout(this);
    options.setGravity(Gravity.CENTER_VERTICAL);
    save=new CheckBox(this);auto=new CheckBox(this);
    save.setText("Lưu Key");auto.setText("Tự Động Bật");
    for(CheckBox c:new CheckBox[]{save,auto}){
      c.setTextSize(11);c.setTextColor(Color.WHITE);c.setButtonTintList(ColorStateList.valueOf(0xffbc5af2));
      options.addView(c,new LinearLayout.LayoutParams(0,d(37),1));
    }
    auto.setOnCheckedChangeListener((v,checked)->{if(checked)save.setChecked(true);});
    panel.addView(options,item(-1,38,4));
    LinearLayout buttons=new LinearLayout(this);
    submit=new Button(this);submit.setText("Xác nhận");submit.setTextSize(12);submit.setAllCaps(false);
    submit.setTextColor(-1);submit.setBackground(bg(0xffaa48ef,0,20));
    Button paste=new Button(this);paste.setText("Dán key");paste.setTextSize(12);paste.setAllCaps(false);
    paste.setTextColor(-1);paste.setBackground(bg(0xff8343c3,0,20));
    LinearLayout.LayoutParams b1=new LinearLayout.LayoutParams(0,d(42),1);b1.rightMargin=d(7);
    buttons.addView(submit,b1);buttons.addView(paste,new LinearLayout.LayoutParams(0,d(42),1));
    panel.addView(buttons,item(-1,-2,4));
    status=text("Nhập key VTUAN để kích hoạt",11,0xffd5b3ef,false);
    panel.addView(status,item(-1,-2,10));
    launch=new Button(this);
    launch.setText("Vào Liên Quân");
    launch.setTextColor(Color.WHITE);
    launch.setAllCaps(false);
    launch.setBackground(bg(0xff7b36c0,0,20));
    launch.setVisibility(View.GONE);
    panel.addView(launch,item(-1,44,9));
    launch.setOnClickListener(v->openGame());
    overlay=new Button(this);
    overlay.setText("Bật menu nổi trạng thái key");
    overlay.setTextColor(Color.WHITE);
    overlay.setTextSize(12);
    overlay.setAllCaps(false);
    overlay.setBackground(bg(0xff61378a,0,20));
    overlay.setVisibility(View.GONE);
    panel.addView(overlay,item(-1,44,9));
    overlay.setOnClickListener(v->startKeyOverlay());
    if(embeddedTarget()!=null){
      launch.setText("Mở ứng dụng");
      overlay.setVisibility(View.GONE);
    }
    input.addTextChangedListener(new TextWatcher(){
      @Override public void beforeTextChanged(CharSequence value,int start,int count,int after){}
      @Override public void onTextChanged(CharSequence value,int start,int before,int count){
        invalidateVerification();
      }
      @Override public void afterTextChanged(Editable value){}
    });
    svField.addTextChangedListener(new TextWatcher(){
      @Override public void beforeTextChanged(CharSequence value,int start,int count,int after){}
      @Override public void onTextChanged(CharSequence value,int start,int before,int count){
        invalidateVerification();
      }
      @Override public void afterTextChanged(Editable value){}
    });
    svField.setText(prefs.getString("server_slug",""));
    String previous=prefs.getString("activated_key","");
    if(previous!=null&&!previous.isEmpty()) {input.setText(previous);save.setChecked(true);}
    auto.setChecked(prefs.getBoolean("auto",false));
    paste.setOnClickListener(v->{
      ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
      if(cm!=null&&cm.hasPrimaryClip()){
        ClipData clip=cm.getPrimaryClip();
        if(clip!=null&&clip.getItemCount()>0) {
          CharSequence s=clip.getItemAt(0).coerceToText(this);
          if(s!=null)input.setText(s.toString().trim());
        }
      }
    });
    submit.setOnClickListener(v->activate());
    if(auto.isChecked() && previous!=null && !previous.isEmpty()) activate();
  }
  private JSONObject request(String endpoint,JSONObject data) throws Exception {
    HttpURLConnection c=null;
    try {
      c=(HttpURLConnection)new URL(HOST+endpoint).openConnection();
      c.setConnectTimeout(8500);
      c.setReadTimeout(8500);
      c.setInstanceFollowRedirects(false);
      c.setRequestMethod("POST");
      c.setDoOutput(true);
      c.setRequestProperty("Content-Type","application/json; charset=utf-8");
      c.setRequestProperty("Accept","application/json");
      byte[] content=data.toString().getBytes(StandardCharsets.UTF_8);
      c.setFixedLengthStreamingMode(content.length);
      try(OutputStream out=c.getOutputStream()){out.write(content);}
      int code=c.getResponseCode();
      boolean httpSuccess=code>=200 && code<300;
      InputStream stream=httpSuccess?c.getInputStream():c.getErrorStream();
      StringBuilder body=new StringBuilder();
      if(stream!=null)try(BufferedReader r=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
        char[] buf=new char[1024];
        int n;
        while((n=r.read(buf))!=-1){
          if(body.length()+n>15000)throw new IOException("Server response too large");
          body.append(buf,0,n);
        }
      }
      JSONObject result;
      try {result=new JSONObject(body.toString());}
      catch(Exception ignored){result=new JSONObject();}
      if(!httpSuccess) {
        result.put("ok",false);
        String message=result.optString("message",result.optString("error",""));
        result.put("message","HTTP "+code+": "+(message.isEmpty()?"Yêu cầu bị từ chối":message));
      }
      return result;
    } finally {
      if(c!=null)c.disconnect();
    }
  }
  private void invalidateVerification(){
    verifiedKey=null;
    verifiedServer=null;
    verifiedExpiry=null;
    if(launch!=null)launch.setVisibility(View.GONE);
    if(overlay!=null)overlay.setVisibility(View.GONE);
    stopService(new Intent(this,OverlayService.class));
  }
  private boolean hasVerifiedKey(){
    return verifiedKey!=null && verifiedServer!=null && verifiedExpiry!=null
        && verifiedKey.equals(input.getText().toString().trim())
        && verifiedServer.equals(KeyApiPath.normalizeServer(svField.getText().toString()))
        && KeyExpiry.isFuture(verifiedExpiry,System.currentTimeMillis());
  }
  private void startKeyOverlay(){
    if(!hasVerifiedKey()){
      status.setText("Cần xác thực key trước khi bật menu nổi.");
      return;
    }
    Long expiry=KeyExpiry.epochMillis(verifiedExpiry);
    if(expiry==null || expiry<=System.currentTimeMillis()){
      status.setText("Đã tới thời điểm hết hạn theo giờ thiết bị; hãy xác thực key lại.");
      invalidateVerification();
      return;
    }
    if(Build.VERSION.SDK_INT>=23 && !Settings.canDrawOverlays(this)){
      Intent settings=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:"+getPackageName()));
      try{
        startActivity(settings);
        status.setText("Cấp quyền Hiển thị trên ứng dụng khác, rồi quay lại bấm Bật menu nổi.");
      }catch(Exception ignored){
        status.setText("Không mở được cài đặt quyền menu nổi.");
      }
      return;
    }
    Intent intent=new Intent(this,OverlayService.class);
    intent.putExtra(OverlayService.EXTRA_SERVER,verifiedServer);
    intent.putExtra(OverlayService.EXTRA_EXPIRY,verifiedExpiry);
    try{
      if(Build.VERSION.SDK_INT>=26)startForegroundService(intent);
      else startService(intent);
      status.setText("Menu nổi trạng thái key đã được yêu cầu bật (tối đa 10 phút).");
    }catch(Exception ignored){
      status.setText("Không thể bật menu nổi trên thiết bị này.");
    }
  }
  private String embeddedTarget(){
    try{
      android.content.pm.ApplicationInfo info=getPackageManager().getApplicationInfo(
        getPackageName(),android.content.pm.PackageManager.GET_META_DATA);
      return info.metaData==null?null:info.metaData.getString("vtuan.original_launcher");
    }catch(Exception ignored){return null;}
  }
  private void openGame(){
    if(busy)return;
    if(!hasVerifiedKey()){
      status.setText("Hãy xác thực key hiện tại trước khi mở ứng dụng.");
      launch.setVisibility(View.GONE);
      return;
    }
    // Do not rely on a prior activation when a key might have been revoked
    // while the screen is still open. Always ask the server again first.
    final String key=verifiedKey;
    final String serverId=verifiedServer;
    busy=true;
    launch.setEnabled(false);
    submit.setEnabled(false);
    status.setText("Đang xác thực key lần cuối trước khi mở...");
    new Thread(()->{
      JSONObject response;
      try{
        JSONObject payload=new JSONObject();
        payload.put("key",key);
        response=request(KeyApiPath.endpoint(serverId,true),payload);
      }catch(Exception failed){
        runOnUiThread(()->{
          if(isFinishing()||isDestroyed())return;
          busy=false;
          launch.setEnabled(true);
          submit.setEnabled(true);
          status.setText("Không kiểm tra lại được key qua máy chủ VTUAN. Chưa mở ứng dụng.");
        });
        return;
      }
      final JSONObject verified=response;
      runOnUiThread(()->{
        if(isFinishing()||isDestroyed())return;
        busy=false;
        launch.setEnabled(true);
        submit.setEnabled(true);
        if(!hasVerifiedKey()||!key.equals(verifiedKey)||!serverId.equals(verifiedServer)){
          status.setText("Key hoặc server đã thay đổi. Hãy xác thực lại.");
          return;
        }
        if(!verified.optBoolean("ok",false)){
          String reason=verified.optString("message",verified.optString("error","Key không hợp lệ"));
          invalidateVerification();
          status.setText("Xác thực key thất bại: "+(reason.length()>120?reason.substring(0,120):reason));
          return;
        }
        String latestExpiry=verified.optString("expires_at","");
        String effectiveExpiry=latestExpiry.isEmpty()?verifiedExpiry:latestExpiry;
        if(!KeyExpiry.isFuture(effectiveExpiry,System.currentTimeMillis())){
          invalidateVerification();
          status.setText("Thời hạn key không hợp lệ hoặc đã hết hạn; chưa mở ứng dụng.");
          return;
        }
        verifiedExpiry=effectiveExpiry;
        status.setText("✓ Máy chủ xác nhận key hợp lệ. Đang mở ứng dụng...");
        launchOriginalApp();
      });
    },"VTUAN-VerifyBeforeLaunch").start();
  }
  private void launchOriginalApp(){
    try{
      // An APK host may declare its original launch target in app metadata.
      String embeddedTarget=embeddedTarget();
      Intent intent=null;
      if(embeddedTarget!=null && !embeddedTarget.isEmpty()){
        intent=new Intent();
        intent.setClassName(getPackageName(),embeddedTarget);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      }else if("com.garena.game.kgvn".equals(getPackageName())){
        intent=new Intent();
        intent.setClassName("com.garena.game.kgvn","com.garena.game.kgtw.SGameActivity");
      }else{
        intent=getPackageManager().getLaunchIntentForPackage("com.garena.game.kgvn1");
        if(intent==null)intent=getPackageManager().getLaunchIntentForPackage("com.garena.game.kgvn");
      }
      if(intent==null){
        status.append("\nKhông tìm thấy ứng dụng đích.");
        return;
      }
      startActivity(intent);
      if(embeddedTarget!=null && !embeddedTarget.isEmpty())finish();
    }catch(Exception ignored){
      status.append("\nKhông thể mở giao diện đích trên thiết bị này.");
    }
  }
  private void activate(){
    if(busy)return;
    String key=input.getText().toString().trim();
    if(key.length()<8 || key.length()>160){status.setText("Key phải dài từ 8 đến 160 ký tự.");return;}
    String serverId=KeyApiPath.normalizeServer(svField.getText().toString());
    if(!KeyApiPath.isValidServer(serverId)){
      status.setText("Mã SV con không hợp lệ (chỉ a-z, 0-9, -, từ 3 đến 32 ký tự).");
      return;
    }
    invalidateVerification();
    busy=true;submit.setEnabled(false);status.setText("Đang kiểm tra key...");
    new Thread(()->{
      try{
        JSONObject payload=new JSONObject();payload.put("key",key);
        JSONObject outcome=request(KeyApiPath.endpoint(serverId,false),payload);
        if(!outcome.optBoolean("ok",false)){
          JSONObject check=request(KeyApiPath.endpoint(serverId,true),payload);
          if(check.optBoolean("ok",false))outcome=check;
        }
        final JSONObject response=outcome;
        runOnUiThread(()->{
          if(isFinishing() || isDestroyed())return;
          busy=false;submit.setEnabled(true);
          if(!key.equals(input.getText().toString().trim()) ||
             !serverId.equals(KeyApiPath.normalizeServer(svField.getText().toString()))){
            status.setText("Key đã thay đổi. Hãy xác thực lại key mới.");
            return;
          }
          if(!response.optBoolean("ok",false)){
            String msg=response.optString("message",response.optString("error","Key không hợp lệ"));
            status.setText(msg.length()>150?msg.substring(0,150):msg);return;
          }
          int days=response.optInt("duration_days",0);
          String expiry=response.optString("expires_at","");
          if(days<=0 || !KeyExpiry.isFuture(expiry,System.currentTimeMillis())){
            status.setText("Máy chủ trả về gói hoặc thời điểm hết hạn không hợp lệ.");
            return;
          }
          if(save.isChecked())prefs.edit().putString("activated_key",key)
              .putString("server_slug",serverId).putBoolean("auto",auto.isChecked()).apply();
          else prefs.edit().remove("activated_key").remove("server_slug")
              .putBoolean("auto",false).apply();
          verifiedKey=key;
          verifiedServer=serverId;
          verifiedExpiry=expiry;
          status.setText("✓ Key hợp lệ\nServer: "+(serverId.isEmpty()?"Admin tổng":serverId)+
                  "\nGói: "+days+" ngày\nHết hạn: "+expiry.replace('T',' ')+
                  "\nCòn lại (ước tính): "+KeyExpiry.remainingText(expiry,System.currentTimeMillis()));
          launch.setVisibility(View.VISIBLE);
          overlay.setVisibility(embeddedTarget()==null?View.VISIBLE:View.GONE);
          if(auto.isChecked())openGame();
        });
      }catch(Exception e){
        runOnUiThread(()->{busy=false;submit.setEnabled(true);status.setText("Không kết nối được máy chủ VTUAN.");});
      }
    },"VTUAN-Activation").start();
  }
}

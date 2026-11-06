package com.vtuan.gate;
import android.app.Activity;
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
  private Button submit, launch;
  private SharedPreferences prefs;
  private boolean busy=false;
  private String verifiedKey=null;
  private String verifiedServer=null;

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
    input.addTextChangedListener(new TextWatcher(){
      @Override public void beforeTextChanged(CharSequence value,int start,int count,int after){}
      @Override public void onTextChanged(CharSequence value,int start,int before,int count){
        verifiedKey=null;
        verifiedServer=null;
        launch.setVisibility(View.GONE);
      }
      @Override public void afterTextChanged(Editable value){}
    });
    svField.addTextChangedListener(new TextWatcher(){
      @Override public void beforeTextChanged(CharSequence value,int start,int count,int after){}
      @Override public void onTextChanged(CharSequence value,int start,int before,int count){
        verifiedKey=null;
        verifiedServer=null;
        launch.setVisibility(View.GONE);
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
  private void openGame(){
    if(verifiedKey==null || !verifiedKey.equals(input.getText().toString().trim()) ||
       verifiedServer==null || !verifiedServer.equals(KeyApiPath.normalizeServer(svField.getText().toString()))){
      status.setText("Hãy xác thực key hiện tại trước khi mở game.");
      launch.setVisibility(View.GONE);
      return;
    }
    try{
      Intent intent=getPackageManager().getLaunchIntentForPackage("com.garena.game.kgvn");
      if(intent==null){
        status.append("\nKhông tìm thấy Liên Quân đã cài.");
        return;
      }
      startActivity(intent);
    }catch(Exception ignored){
      status.append("\nKhông thể mở Liên Quân trên thiết bị này.");
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
    verifiedKey=null;
    verifiedServer=null;
    launch.setVisibility(View.GONE);
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
          if(days<=0 || expiry.isEmpty()){
            status.setText("Phản hồi máy chủ thiếu thông tin thời hạn. Không chấp nhận.");
            return;
          }
          if(save.isChecked())prefs.edit().putString("activated_key",key)
              .putString("server_slug",serverId).putBoolean("auto",auto.isChecked()).apply();
          else prefs.edit().remove("activated_key").remove("server_slug")
              .putBoolean("auto",false).apply();
          verifiedKey=key;
          verifiedServer=serverId;
          status.setText("✓ Key hợp lệ\nServer: "+(serverId.isEmpty()?"Admin tổng":serverId)+
                  "\nGói: "+days+" ngày\nHết hạn: "+expiry.replace('T',' ')+
                  "\nCòn lại (ước tính): "+KeyExpiry.remainingText(expiry,System.currentTimeMillis()));
          launch.setVisibility(View.VISIBLE);
          if(auto.isChecked())openGame();
        });
      }catch(Exception e){
        runOnUiThread(()->{busy=false;submit.setEnabled(true);status.setText("Không kết nối được máy chủ VTUAN.");});
      }
    },"VTUAN-Activation").start();
  }
}

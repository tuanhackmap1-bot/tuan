package com.vtuan.gate;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class KeyActivity extends Activity {
  private static final String HOST="https://vtuan-103-159-51-231.sslip.io";
  private static final String PREF="v4_key_state";
  private EditText input;
  private CheckBox save,auto;
  private TextView status;
  private Button submit;
  private SharedPreferences prefs;
  private boolean busy=false;

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
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    prefs=getSharedPreferences(PREF,MODE_PRIVATE);
    FrameLayout root=new FrameLayout(this);
    root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff151023,0xff271331,0xff171020}));
    setContentView(root);
    LinearLayout panel=new LinearLayout(this);
    panel.setOrientation(1);panel.setPadding(d(16),d(16),d(16),d(15));
    panel.setBackground(bg(0xff291e3d,0xffb06cf3,17));
    panel.setElevation(d(12));
    FrameLayout.LayoutParams box=new FrameLayout.LayoutParams(d(385),-2,Gravity.CENTER);
    int width=getResources().getDisplayMetrics().widthPixels;
    if(width<d(410))box.width=width-d(20);
    root.addView(panel,box);
    TextView title=text("CHÀO MỪNG BẠN ĐẾN VỚI VTUAN!",14,Color.WHITE,true);
    panel.addView(title,item(-1,-2,0));
    input=new EditText(this);
    input.setSingleLine(true);input.setTextSize(12);input.setTextColor(Color.WHITE);
    input.setHintTextColor(0xffd0aadf);input.setHint("Điền key vào đây");
    input.setPadding(d(15),0,d(15),0);
    input.setBackground(bg(0xff513268,0xffb87bed,22));
    panel.addView(input,item(-1,45,13));
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
    HttpURLConnection c=(HttpURLConnection)new URL(HOST+endpoint).openConnection();
    c.setConnectTimeout(8500);c.setReadTimeout(8500);
    c.setRequestMethod("POST");c.setDoOutput(true);
    c.setRequestProperty("Content-Type","application/json; charset=utf-8");
    byte[] content=data.toString().getBytes(StandardCharsets.UTF_8);
    try(OutputStream out=c.getOutputStream()){out.write(content);}
    int code=c.getResponseCode();
    InputStream stream=code<400?c.getInputStream():c.getErrorStream();
    if(stream==null)throw new IOException("Empty response");
    StringBuilder result=new StringBuilder();
    try(BufferedReader r=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){
      String line;while((line=r.readLine())!=null){
        result.append(line);
        if(result.length()>15000)throw new IOException("Too much data");
      }
    } finally{c.disconnect();}
    return new JSONObject(result.toString());
  }
  private void activate(){
    if(busy)return;
    String key=input.getText().toString().trim();
    if(key.isEmpty()){status.setText("Vui lòng nhập key");return;}
    busy=true;submit.setEnabled(false);status.setText("Đang kiểm tra key...");
    new Thread(()->{
      try{
        JSONObject payload=new JSONObject();payload.put("key",key);
        JSONObject outcome=request("/api/activate",payload);
        if(!outcome.optBoolean("ok",false)){
          JSONObject check=request("/api/verify",payload);
          if(check.optBoolean("ok",false))outcome=check;
        }
        final JSONObject response=outcome;
        runOnUiThread(()->{
          busy=false;submit.setEnabled(true);
          if(!response.optBoolean("ok",false)){
            String msg=response.optString("message",response.optString("error","Key không hợp lệ"));
            status.setText(msg.length()>150?msg.substring(0,150):msg);return;
          }
          if(save.isChecked())prefs.edit().putString("activated_key",key).putBoolean("auto",auto.isChecked()).apply();
          else prefs.edit().remove("activated_key").putBoolean("auto",false).apply();
          status.setText("Key hợp lệ - đang mở Liên Quân...");
          try{
            Intent i=getPackageManager().getLaunchIntentForPackage("com.garena.game.kgvn");
            if(i==null) {
              status.setText("Key hợp lệ. Chưa tìm thấy ứng dụng Liên Quân để mở.");
              return;
            }
            startActivity(i);
            finish();
          }catch(Exception e){status.setText("Đã xác thực key nhưng không mở được game.");}
        });
      }catch(Exception e){
        runOnUiThread(()->{busy=false;submit.setEnabled(true);status.setText("Không kết nối được máy chủ VTUAN.");});
      }
    },"VTUAN-Activation").start();
  }
}

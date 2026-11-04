package com.vtuan.lqmenu;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Standalone VTUAN key client (v1.3). Does NOT inject or alter any game. */
public final class MainActivity extends Activity {
    private static final String SERVER = "https://vtuan-103-159-51-231.sslip.io";
    private static final int MAX_BODY_CHARS = 16384;
    private static final int BG=0xff11151e, PANEL=0xff1a2230, WHITE=0xfff3f6fb,
            MUTED=0xffa9b6ca, ACCENT=0xff45a2ff;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private SharedPreferences prefs;
    private LinearLayout content;
    private EditText keyField, svField;
    private String draftKey = "", draftServer = "";
    private Button actionButton;
    private volatile boolean requestRunning = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs=getSharedPreferences("vtuan_lq", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(44), dp(24), dp(24));
        scroll.addView(content);
        setContentView(scroll);
        String key=prefs.getString("key", "");
        if(key.isEmpty()) showLogin(""); else {
            draftKey = key; draftServer = prefs.getString("sv", "");
            request(key, draftServer, false);
        }
    }

    private int dp(float px) {return (int)(px*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;
    }
    private TextView label(String message,int size,int color,boolean bold) {
        TextView v=new TextView(this);v.setText(message);v.setTextColor(color);v.setTextSize(size);
        if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;
    }
    private LinearLayout.LayoutParams lp(int top) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);return p;
    }
    private EditText field(String hint,String initial) {
        EditText e=new EditText(this);e.setSingleLine(true);e.setText(initial);e.setHint(hint);
        e.setHintTextColor(MUTED);e.setTextColor(WHITE);e.setTextSize(15);
        e.setPadding(dp(14),dp(12),dp(14),dp(12));e.setBackground(bg(0xff2a3343,11));return e;
    }
    private Button button(String message) {
        Button b=new Button(this);b.setText(message);b.setAllCaps(false);b.setTextColor(Color.WHITE);
        b.setBackground(bg(ACCENT,11));return b;
    }
    private void heading() {
        content.addView(label("VTUAN LQ",28,WHITE,true),lp(0));
        content.addView(label("Quản lý key trên ứng dụng Android",14,MUTED,false),lp(5));
    }
    private void showLogin(String error) {
        content.removeAllViews();heading();
        if(!error.isEmpty())content.addView(label(error,14,0xffffa4a4,false),lp(20));
        content.addView(label("MÃ KEY",13,MUTED,true),lp(28));
        keyField=field("Nhập key VTUAN",draftKey.isEmpty()?prefs.getString("key",""):draftKey);
        content.addView(keyField,lp(8));
        content.addView(label("MÃ SERVER CON (tùy chọn)",13,MUTED,true),lp(18));
        svField=field("Để trống nếu là key Admin tổng",draftServer.isEmpty()?prefs.getString("sv",""):draftServer);
        content.addView(svField,lp(8));
        actionButton=button("Kích hoạt key");content.addView(actionButton,lp(22));
        actionButton.setOnClickListener(v -> {
            String key=keyField.getText().toString().trim();
            String sv=svField.getText().toString().trim();
            draftKey=key; draftServer=sv;
            if(key.length()<8 || key.length()>160) {showLogin("Key không đúng định dạng.");return;}
            if(!sv.isEmpty() && !sv.matches("[a-z0-9-]{3,32}")) {
                showLogin("Mã SV con không hợp lệ.");return;
            }
            request(key,sv,true);
        });
        content.addView(label("Kết nối HTTPS tới máy chủ VTUAN. Không gửi mã máy.",12,MUTED,false),lp(16));
    }
    private String formatUtc(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        return iso.replace('T',' ').replace("+00:00", " UTC");
    }
    private void showSuccess(JSONObject result,String key,String sv) {
        content.removeAllViews();heading();
        content.addView(label("✓ Key đang hoạt động",20,0xff8ae0a4,true),lp(30));
        content.addView(label("Server: "+(sv.isEmpty()?"Admin tổng":sv),14,MUTED,false),lp(16));
        content.addView(label("Thời hạn: "+result.optInt("duration_days")+" ngày",16,WHITE,false),lp(12));
        String activated=result.optString("activated_at","");
        if (!activated.isEmpty()) content.addView(label("Kích hoạt: "+formatUtc(activated),14,WHITE,false),lp(8));
        content.addView(label("Hết hạn: "+formatUtc(result.optString("expires_at","")),14,WHITE,false),lp(8));
        Button verify=button("Kiểm tra lại key");content.addView(verify,lp(28));
        verify.setOnClickListener(v -> request(key,sv,false));
        Button logout=button("Đổi key khác");content.addView(logout,lp(14));
        logout.setBackground(bg(PANEL,11));logout.setOnClickListener(v -> {
            prefs.edit().remove("key").remove("sv").apply();
            draftKey="";draftServer="";showLogin("");
        });
        content.addView(label("Đây là ứng dụng key độc lập. Chưa có các tính năng can thiệp vào Liên Quân.",13,MUTED,false),lp(28));
    }
    private void request(String key,String sv,boolean activate) {
        if (requestRunning) return;
        requestRunning = true;
        draftKey=key; draftServer=sv;
        content.removeAllViews();heading();
        content.addView(label("Đang "+(activate?"kích hoạt":"kiểm tra")+" key qua HTTPS...",15,MUTED,false),lp(28));
        final String endpoint=sv.isEmpty()?"/api/" : "/api/sv/"+sv+"/";
        final String path=endpoint+(activate?"activate":"verify");
        io.execute(() -> {
            HttpURLConnection connection=null;
            try {
                connection=(HttpURLConnection)new URL(SERVER+path).openConnection();
                connection.setRequestMethod("POST");connection.setDoOutput(true);
                connection.setConnectTimeout(8000);connection.setReadTimeout(8000);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
                byte[] data=new JSONObject().put("key",key).toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream output=connection.getOutputStream()){output.write(data);}
                int status=connection.getResponseCode();
                InputStream input=(status>=200 && status<300)?connection.getInputStream():connection.getErrorStream();
                StringBuilder result=new StringBuilder();
                if(input!=null)try(BufferedReader r=new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8))) {
                    char[] buffer = new char[1024];
                    int n;
                    while ((n=r.read(buffer))!=-1 && result.length()<MAX_BODY_CHARS) {
                        result.append(buffer,0,Math.min(n,MAX_BODY_CHARS-result.length()));
                    }
                }
                JSONObject response;
                try { response = new JSONObject(result.toString()); }
                catch (Exception ignored) { response = new JSONObject(); }
                boolean valid=status>=200 && status<300 && response.optBoolean("ok",false)
                        && response.optInt("duration_days",0)>0
                        && !response.optString("expires_at","").isEmpty();
                String message=response.optString("message", response.optString("error", "Yêu cầu chưa được chấp nhận"));
                if (message.length()>240) message=message.substring(0,240)+"…";
                final String safeMessage=message;
                final JSONObject responseData=response;
                ui.post(() -> {
                    if(isFinishing() || isDestroyed())return;
                    requestRunning = false;
                    if(valid){prefs.edit().putString("key",key).putString("sv",sv).apply();showSuccess(responseData,key,sv);}
                    else {
                        // Never erase a saved key based on HTTP status alone.
                        // The server may use 403 or 409 for a temporary policy conflict.
                        showLogin("Máy chủ trả về HTTP " + status + ": " + safeMessage);
                    }
                });
            } catch(Exception ex) {
                String error;
                if (ex instanceof SocketTimeoutException) error="Kết nối quá thời gian. Hãy thử lại.";
                else if (ex instanceof UnknownHostException) error="Không tìm thấy tên miền máy chủ. Kiểm tra DNS hoặc mạng.";
                else if (ex instanceof SSLException) error="Lỗi chứng chỉ hoặc kết nối HTTPS của máy chủ.";
                else error="Không thể kết nối máy chủ. Kiểm tra mạng rồi thử lại.";
                ui.post(() -> {requestRunning = false; if(!isFinishing() && !isDestroyed())showLogin(error + " Key đã lưu vẫn được giữ nguyên.");});
            } finally {if(connection!=null)connection.disconnect();}
        });
    }
    @Override public void onDestroy(){io.shutdownNow();super.onDestroy();}
}

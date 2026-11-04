package com.vtuan.gate;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
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
import java.nio.charset.StandardCharsets;

public final class KeyActivity extends Activity {
    // Test integration only. The native menu's original authentication is NOT replaced.
    private static final String API = "https://vtuan-103-159-51-231.sslip.io/api/activate";
    private EditText field;
    private TextView detail;
    private Button activate;
    private Button enter;
    private boolean verified = false;

    private GradientDrawable background(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        d.setStroke(2, stroke);
        return d;
    }
    private TextView label(String text, int size, int color) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        return t;
    }
    private Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(background(0xFF6D28D9,0xFF9777E8,20));
        return b;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF101020);
        getWindow().setNavigationBarColor(0xFF101020);
        ScrollView outer = new ScrollView(this);
        outer.setFillViewport(true);
        outer.setBackgroundColor(0xFF090B16);
        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);
        holder.setGravity(Gravity.CENTER);
        holder.setPadding(25,20,25,20);
        outer.addView(holder,new ScrollView.LayoutParams(-1,-1));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(32,28,32,28);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setBackground(background(0xFF17192C,0xFF8B5CF6,24));
        holder.addView(card,new LinearLayout.LayoutParams(-1,-2));
        TextView title=label("KÍCH HOẠT KEY",23,0xFFF5F1FF);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        card.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView hint=label("Nhập key để kiểm tra thời hạn",13,0xFFC9C1E8);
        LinearLayout.LayoutParams h=new LinearLayout.LayoutParams(-1,-2);
        h.topMargin=12;h.bottomMargin=18;
        card.addView(hint,h);
        field=new EditText(this);
        field.setSingleLine(true);
        field.setTextColor(Color.WHITE);
        field.setHintTextColor(0xFFAEA6C7);
        field.setHint("Nhập key...");
        field.setPadding(18,10,18,10);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        field.setBackground(background(0xFF101225,0xFF59507F,16));
        card.addView(field,new LinearLayout.LayoutParams(-1,52));
        activate=button("KÍCH HOẠT");
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,52);
        bp.topMargin=16;
        card.addView(activate,bp);
        detail=label("",14,0xFFF0E9FF);
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,-2);
        dp.topMargin=14;
        card.addView(detail,dp);
        enter=button("VÀO GAME");
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,48);
        ep.topMargin=14;
        card.addView(enter,ep);
        enter.setVisibility(View.GONE);
        enter.setOnClickListener(v -> {
            if (!verified) return;
            // SGameActivity is V4's actual AndroidManifest launcher and
            // performs native library preparation before SGameRealActivity.
            Intent game = new Intent();
            game.setClassName(getPackageName(),"com.garena.game.kgtw.SGameActivity");
            game.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(game);
            finish();
        });
        activate.setOnClickListener(v -> submit());
        setContentView(outer);
    }
    private void status(String s) {
        runOnUiThread(() -> detail.setText(s));
    }
    private static String readFully(InputStream in) throws Exception {
        if (in == null) return "";
        StringBuilder sb=new StringBuilder();
        BufferedReader br=new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line=br.readLine()) != null && sb.length()<10000) sb.append(line);
        br.close();
        return sb.toString();
    }
    private void submit() {
        if (verified) return;
        String key=field.getText().toString().trim();
        if (key.isEmpty() || key.length()>128) {
            detail.setText("Vui lòng nhập key hợp lệ.");
            return;
        }
        activate.setEnabled(false);
        detail.setText("Đang kiểm tra key...");
        new Thread(() -> {
            HttpURLConnection conn=null;
            try {
                JSONObject req=new JSONObject();
                req.put("key",key);
                byte[] payload=req.toString().getBytes(StandardCharsets.UTF_8);
                conn=(HttpURLConnection)new URL(API).openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
                try (OutputStream os=conn.getOutputStream()) {os.write(payload);}
                int code=conn.getResponseCode();
                String content=readFully(code<400 ? conn.getInputStream() : conn.getErrorStream());
                JSONObject data=new JSONObject(content);
                if (code>=200 && code<300 && data.optBoolean("ok",false)) {
                    String days=data.optString("duration_days","?");
                    String activated=data.optString("activated_at","?");
                    String expires=data.optString("expires_at","?");
                    String result="Kích hoạt thành công\nThời hạn: "+days+" ngày\nNgày kích hoạt: "+activated+"\nNgày hết hạn: "+expires;
                    runOnUiThread(() -> {
                        verified=true;
                        detail.setText(result);
                        enter.setVisibility(View.VISIBLE);
                        field.setEnabled(false);
                        activate.setVisibility(View.GONE);
                    });
                } else {
                    status("Không kích hoạt được: "+data.optString("message","Server từ chối key."));
                }
            } catch (Exception ex) {
                // Don't expose secrets or full URLs to the user.
                status("Lỗi mạng hoặc phản hồi server: "+ex.getClass().getSimpleName());
            } finally {
                if (conn!=null)conn.disconnect();
                runOnUiThread(() -> {if(!verified)activate.setEnabled(true);});
            }
        }).start();
    }
}

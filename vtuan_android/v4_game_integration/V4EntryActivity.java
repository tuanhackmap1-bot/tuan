package com.vtuan.v4integration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONObject;
import com.vtuan.gate.KeyApiPath;
import com.vtuan.gate.KeyExpiry;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Experimental launch gate embedded in a copy of the original V4 APK.
 *
 * DOES NOT modify or circumvent native V4 authentication. Successful VTUAN
 * validation permits launch of the ORIGINAL activity; the original V4 menu
 * may still have its own independent login/feature checks.
 */
public final class V4EntryActivity extends Activity {
    private static final String BASE = "https://vtuan-103-159-51-231.sslip.io";
    private static final String ORIGINAL_ACTIVITY = "com.garena.game.kgtw.SGameActivity";
    private EditText keyBox, serverBox;
    private TextView status;
    private Button verifyButton;
    private volatile boolean busy;

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }
    private GradientDrawable background(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(14));
        d.setStroke(dp(1), 0xffab63ef);
        return d;
    }
    private TextView label(String s,int size) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(Color.WHITE);
        v.setGravity(Gravity.CENTER);
        return v;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        V4Session.clear();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        FrameLayout base = new FrameLayout(this);
        base.setBackgroundColor(0xff191026);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        base.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout holder = new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        holder.setOrientation(LinearLayout.VERTICAL);
        holder.setPadding(dp(15),dp(18),dp(15),dp(18));
        scroll.addView(holder,new ScrollView.LayoutParams(-1,-1));
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16),dp(20),dp(16),dp(20));
        panel.setBackground(background(0xff2c1b42));
        int width = getResources().getDisplayMetrics().widthPixels;
        holder.addView(panel,new LinearLayout.LayoutParams(Math.min(dp(390),width-dp(30)),-2));
        panel.addView(label("VTUAN • V4",20));
        panel.addView(label("Xác thực key để mở bản V4 gốc",13));
        keyBox = new EditText(this);
        keyBox.setSingleLine(true);
        keyBox.setTextSize(14);
        keyBox.setHint("Nhập key VTUAN");
        keyBox.setTextColor(Color.WHITE);
        keyBox.setHintTextColor(0xffb9a4c5);
        panel.addView(keyBox,new LinearLayout.LayoutParams(-1,dp(52)));
        serverBox = new EditText(this);
        serverBox.setSingleLine(true);
        serverBox.setTextSize(13);
        serverBox.setTextColor(Color.WHITE);
        serverBox.setHintTextColor(0xffb9a4c5);
        serverBox.setHint("Mã server con (bỏ trống nếu Admin)");
        panel.addView(serverBox,new LinearLayout.LayoutParams(-1,dp(52)));
        verifyButton = new Button(this);
        verifyButton.setText("Xác thực và mở V4");
        verifyButton.setAllCaps(false);
        panel.addView(verifyButton,new LinearLayout.LayoutParams(-1,dp(52)));
        status = label("Sẽ mở ứng dụng V4 gốc, không thay thế hệ thống đăng nhập nội bộ.",12);
        panel.addView(status,new LinearLayout.LayoutParams(-1,-2));
        verifyButton.setOnClickListener(v -> verify());
        setContentView(base);
    }
    private JSONObject post(String endpoint,JSONObject payload) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn=(HttpURLConnection)new URL(BASE+endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
            byte[] request=payload.toString().getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(request.length);
            try(OutputStream os=conn.getOutputStream()) {os.write(request);}
            int code=conn.getResponseCode();
            boolean goodHttp=code>=200&&code<300;
            InputStream responseStream=goodHttp?conn.getInputStream():conn.getErrorStream();
            if(responseStream==null)throw new IllegalStateException("HTTP "+code+" with no body");
            try(InputStream in=responseStream;
                BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))) {
                StringBuilder result=new StringBuilder();
                char[] buf=new char[1024];
                int n;
                while((n=br.read(buf))!=-1) {
                    if(result.length()+n>16384)throw new IllegalStateException("Phản hồi quá lớn");
                    result.append(buf,0,n);
                }
                JSONObject parsed=new JSONObject(result.toString());
                if(!goodHttp)parsed.put("ok",false);
                return parsed;
            }
        } finally {
            if(conn!=null)conn.disconnect();
        }
    }
    private void verify() {
        if(busy)return;
        final String key=keyBox.getText().toString().trim();
        final String server=KeyApiPath.normalizeServer(serverBox.getText().toString());
        if(key.length()<8||key.length()>160) {
            status.setText("Key phải dài từ 8 đến 160 ký tự");return;
        }
        if(!KeyApiPath.isValidServer(server)) {
            status.setText("Mã server con không hợp lệ");return;
        }
        busy=true;
        verifyButton.setEnabled(false);
        status.setText("Đang xác thực với server VTUAN...");
        new Thread(() -> {
            try {
                JSONObject payload=new JSONObject().put("key",key);
                JSONObject response=post(KeyApiPath.endpoint(server,false),payload);
                if(!response.optBoolean("ok",false)) {
                    response=post(KeyApiPath.endpoint(server,true),payload);
                }
                final JSONObject out=response;
                runOnUiThread(() -> {
                    busy=false;
                    if(isFinishing()||isDestroyed())return;
                    verifyButton.setEnabled(true);
                    if(!key.equals(keyBox.getText().toString().trim()) ||
                        !server.equals(KeyApiPath.normalizeServer(serverBox.getText().toString()))) {
                        status.setText("Dữ liệu đã thay đổi. Xác thực lại.");
                        return;
                    }
                    String expiry=out.optString("expires_at","");
                    Long expiryMs=KeyExpiry.epochMillis(expiry);
                    if(!out.optBoolean("ok",false) ||
                       (expiryMs!=null && expiryMs<=System.currentTimeMillis())) {
                        status.setText("Key bị từ chối hoặc đã hết hạn; kiểm tra key/server.");
                        return;
                    }
                    V4Session.approve(expiryMs==null?0L:expiryMs,System.currentTimeMillis());
                    if(!V4Session.isValid(System.currentTimeMillis())){
                        status.setText("Phiên VTUAN không hợp lệ. Hãy xác thực lại.");
                        return;
                    }
                    status.setText("Key VTUAN hợp lệ. Đang mở game V4 gốc...");
                    try {
                        Intent game=new Intent();
                        game.setClassName(getPackageName(),ORIGINAL_ACTIVITY);
                        game.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(game);
                        finish();
                    }catch(Exception e) {
                        V4Session.clear();
                        status.setText("Không mở được Activity gốc: "+ORIGINAL_ACTIVITY);
                    }
                });
            } catch(Exception e) {
                runOnUiThread(() -> {
                    busy=false;
                    if(isFinishing()||isDestroyed())return;
                    verifyButton.setEnabled(true);
                    status.setText("Lỗi xác thực/mạng: "+e.getClass().getSimpleName());
                });
            }
        },"vtuan-v4-key-gate").start();
    }
}

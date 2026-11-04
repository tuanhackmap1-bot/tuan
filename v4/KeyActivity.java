package com.vtuan.gate;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
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
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/*
 * V4 integration TEST, not a replacement for libysmteam.so's native authentication.
 * Success from this Activity does not imply the native menu is unlocked.
 */
public final class KeyActivity extends Activity {
    private static final String BASE = "https://vtuan-103-159-51-231.sslip.io";
    private static final String PREF = "v4_key_state";
    private static final String KEY_NAME = "activated_key";

    private EditText keyField;
    private TextView statusView;
    private Button activateButton;
    private Button enterButton;
    private String machineCode;
    private boolean authorized;
    private SharedPreferences preferences;

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
    private GradientDrawable rounded(int bg, int edge, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(bg);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), edge);
        return d;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }
    private Button button(String title) {
        Button b = new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(rounded(0xFF6D28D9, 0xFF9B7CE8, 12));
        return b;
    }
    private static String sha256Prefix(String raw) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 12; i++) b.append(String.format(Locale.ROOT, "%02X", dig[i] & 0xFF));
        return b.toString();
    }
    private String getStableMachineCode() {
        try {
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId != null && !androidId.isEmpty()) {
                return sha256Prefix("VTUAN-V4:" + androidId);
            }
        } catch (Exception ignored) {}
        String saved = preferences.getString("fallback_device", "");
        if (saved.isEmpty()) {
            saved = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
            preferences.edit().putString("fallback_device", saved).apply();
        }
        return saved;
    }
    private static String displayTime(JSONObject response, String key) {
        Object raw = response.opt(key);
        if (raw == null || raw == JSONObject.NULL) return "Chưa có thông tin";
        try {
            long n;
            if (raw instanceof Number) n = ((Number)raw).longValue();
            else if (String.valueOf(raw).matches("[0-9]{10,13}")) n = Long.parseLong(String.valueOf(raw));
            else return String.valueOf(raw);
            if (n < 100000000000L) n *= 1000L;
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN")).format(new Date(n));
        } catch (Exception ignored) {
            return String.valueOf(raw);
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREF, MODE_PRIVATE);
        machineCode = getStableMachineCode();
        getWindow().setStatusBarColor(0xFF0B0D1A);
        getWindow().setNavigationBarColor(0xFF0B0D1A);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF090B16);
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setPadding(dp(20), dp(18), dp(20), dp(18));
        wrapper.setGravity(Gravity.CENTER);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(wrapper, new ScrollView.LayoutParams(-1, -1));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(20), dp(22), dp(20));
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setBackground(rounded(0xFF17192C, 0xFF8B5CF6, 20));
        int available = getResources().getDisplayMetrics().widthPixels - dp(40);
        wrapper.addView(card, new LinearLayout.LayoutParams(Math.min(available, dp(540)), -2));

        TextView title = text("KÍCH HOẠT KEY", 23, 0xFFF7F3FF);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = text("Nhập key để kiểm tra và kích hoạt", 13, 0xFFC9C1E8);
        LinearLayout.LayoutParams sub = new LinearLayout.LayoutParams(-1, -2);
        sub.setMargins(0, dp(8), 0, dp(12));
        card.addView(subtitle, sub);

        TextView machine = text("Mã máy: " + machineCode, 12, 0xFFC9C1E8);
        card.addView(machine, new LinearLayout.LayoutParams(-1, -2));
        machine.setOnClickListener(v -> {
            ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            if (clip != null) clip.setPrimaryClip(ClipData.newPlainText("Mã máy", machineCode));
            statusView.setText("Đã sao chép mã máy.");
        });
        TextView copyHint = text("Chạm mã máy để sao chép", 11, 0xFF958BB1);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(2), 0, dp(14));
        card.addView(copyHint, cp);

        keyField = new EditText(this);
        keyField.setSingleLine(true);
        keyField.setTextColor(Color.WHITE);
        keyField.setHintTextColor(0xFFA9A2BC);
        keyField.setHint("Nhập key...");
        keyField.setTextSize(16);
        keyField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        keyField.setPadding(dp(14), dp(8), dp(14), dp(8));
        keyField.setBackground(rounded(0xFF101225, 0xFF59507F, 12));
        card.addView(keyField, new LinearLayout.LayoutParams(-1, dp(48)));

        activateButton = button("KÍCH HOẠT");
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(48));
        ap.setMargins(0, dp(12), 0, 0);
        card.addView(activateButton, ap);

        statusView = text("", 14, 0xFFF0E9FF);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, dp(12), 0, 0);
        card.addView(statusView, sp);

        enterButton = button("VÀO GAME");
        enterButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, dp(48));
        ep.setMargins(0, dp(12), 0, 0);
        card.addView(enterButton, ep);
        enterButton.setOnClickListener(v -> {
            if (!authorized) return;
            // Preserve the normal V4 startup and its native-resource extraction path.
            try {
                Intent game = new Intent();
                game.setClassName(getPackageName(), "com.garena.game.kgtw.SGameActivity");
                startActivity(game);
                finish();
            } catch (Exception ignored) {
                statusView.setText("Không mở được game. Vui lòng kiểm tra bản APK.");
            }
        });
        activateButton.setOnClickListener(v -> {
            String key = keyField.getText().toString().trim();
            if (key.isEmpty() || key.length() > 128) {
                statusView.setText("Vui lòng nhập key hợp lệ.");
            } else {
                requestKey(key, false);
            }
        });
        setContentView(scroll);

        // A saved key must be verified online again. No offline authorization.
        String savedKey = preferences.getString(KEY_NAME, "");
        if (!savedKey.isEmpty()) {
            keyField.setText(savedKey);
            requestKey(savedKey, true);
        }
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> statusView.setText(message));
    }
    private static String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder buf = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null && buf.length() < 12000) buf.append(line);
        }
        return buf.toString();
    }
    private void requestKey(String key, boolean verifyOnly) {
        activateButton.setEnabled(false);
        enterButton.setVisibility(View.GONE);
        authorized = false;
        statusView.setText(verifyOnly ? "Đang kiểm tra key đã lưu..." : "Đang kích hoạt...");
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject req = new JSONObject();
                req.put("key", key);
                req.put("machine_code", machineCode);
                req.put("device_id", machineCode);
                byte[] bytes = req.toString().getBytes(StandardCharsets.UTF_8);
                conn = (HttpURLConnection)new URL(BASE + (verifyOnly ? "/api/verify" : "/api/activate")).openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream out = conn.getOutputStream()) { out.write(bytes); }
                int statusCode = conn.getResponseCode();
                String body = readStream(statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream());
                JSONObject response = new JSONObject(body);
                if (statusCode >= 200 && statusCode < 300 && response.optBoolean("ok", false)) {
                    String duration = response.optString("duration_days", response.optString("days", "?"));
                    String activated = displayTime(response, "activated_at");
                    String expired = displayTime(response, "expires_at");
                    runOnUiThread(() -> {
                        authorized = true;
                        preferences.edit().putString(KEY_NAME, key).apply();
                        keyField.setEnabled(false);
                        activateButton.setVisibility(View.GONE);
                        enterButton.setVisibility(View.VISIBLE);
                        statusView.setText("Key hợp lệ\nThời hạn: " + duration + " ngày\nKích hoạt: " + activated + "\nHết hạn: " + expired);
                    });
                } else {
                    // Never proceed to game after a denied or expired key.
                    String message = response.optString("message", "Server từ chối key.");
                    runOnUiThread(() -> {
                        if (verifyOnly) preferences.edit().remove(KEY_NAME).apply();
                        keyField.setEnabled(true);
                        activateButton.setVisibility(View.VISIBLE);
                        statusView.setText("Không xác thực được: " + message);
                    });
                }
            } catch (Exception ex) {
                // Do not grant access when the server cannot be reached.
                updateStatus("Không kết nối hoặc phản hồi server không hợp lệ. Thử lại khi có mạng.");
            } finally {
                if (conn != null) conn.disconnect();
                runOnUiThread(() -> activateButton.setEnabled(true));
            }
        }).start();
    }
}

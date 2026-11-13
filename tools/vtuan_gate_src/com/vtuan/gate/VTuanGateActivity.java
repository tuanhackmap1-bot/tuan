package com.vtuan.gate;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class VTuanGateActivity extends Activity {
    private static final String SERVER = "https://vtuan-103-159-51-231.sslip.io";
    private static final String GAME_ACTIVITY = "com.garena.game.kgtw.SGameActivity";
    private EditText keyInput;
    private TextView status;
    private TextView detail;
    private Button activate;
    private Button enter;
    private boolean authorized = false;
    private static final int PURPLE = Color.rgb(171, 51, 247);
    private static final int PANEL = Color.rgb(31, 17, 54);
    private String validatedKey = "";

    private static GradientDrawable bg(int color, int stroke) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(23f);
        gd.setStroke(2, stroke);
        return gd;
    }

    private static TextView txt(Activity activity, String value, int size, int color, boolean bold) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setPadding(8, 10, 8, 10);
        return view;
    }

    private static void add(LinearLayout container, View view, int height) {
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.topMargin = 9;
        container.addView(view, lp);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(5894 | View.SYSTEM_UI_FLAG_FULLSCREEN);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(9, 4, 23));
        try {
            ImageView picture = new ImageView(this);
            picture.setImageBitmap(BitmapFactory.decodeStream(getAssets().open("vtuan_gate_bg.png")));
            picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
            root.addView(picture, new FrameLayout.LayoutParams(-1, -1));
        } catch (Exception e) {
            // Keep the gradient fallback if the artwork is unavailable.
        }
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout space = new LinearLayout(this);
        space.setOrientation(LinearLayout.VERTICAL);
        space.setGravity(Gravity.CENTER);
        space.setPadding(10, 14, 10, 14);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(24, 16, 24, 16);
        panel.setBackground(bg(PANEL, PURPLE));
        int maxWidth = (int)(getResources().getDisplayMetrics().widthPixels * .61);
        LinearLayout.LayoutParams panelParam = new LinearLayout.LayoutParams(
            Math.max(380, maxWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        panelParam.gravity = Gravity.CENTER;
        space.addView(panel, panelParam);

        TextView header = txt(this, "🗝 VTUAN - Kích Hoạt Key", 23, Color.WHITE, true);
        add(panel, header, -2);
        keyInput = new EditText(this);
        keyInput.setSingleLine(true);
        keyInput.setTextColor(Color.WHITE);
        keyInput.setHintTextColor(0xffa5a0b2);
        keyInput.setTextSize(18);
        keyInput.setPadding(20, 10, 20, 10);
        keyInput.setHint("Nhập key VTUAN...");
        keyInput.setBackground(bg(0xff332447, 0xffa978ff));
        add(panel, keyInput, 58);

        activate = new Button(this);
        activate.setText("KÍCH HOẠT");
        activate.setTextColor(Color.WHITE);
        activate.setTextSize(19);
        activate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        activate.setBackground(bg(0xff8b14e9, 0xffbf42fb));
        add(panel, activate, 58);

        status = txt(this, "Nhập key và nhấn KÍCH HOẠT", 16, 0xffd5b9f0, true);
        add(panel, status, -2);
        detail = txt(this, "Trạng thái key sẽ được kiểm tra trực tiếp trên server VTUAN.", 13, 0xffe4d4f4, false);
        add(panel, detail, -2);

        enter = new Button(this);
        enter.setText("VÀO GAME ➜");
        enter.setTextColor(Color.WHITE);
        enter.setTextSize(20);
        enter.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        enter.setBackground(bg(0xff8b14e9, 0xffbf42fb));
        enter.setEnabled(false);
        add(panel, enter, 60);

        scroll.addView(space);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        activate.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final String key = keyInput.getText().toString().trim();
                if (key.length() < 4) {
                    showError("Vui lòng nhập key hợp lệ");
                    return;
                }
                doValidate(key, true);
            }
        });
        enter.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!authorized) return;
                Intent game = new Intent();
                game.setClassName(getPackageName(), GAME_ACTIVITY);
                try {
                    startActivity(game);
                    finish();
                } catch (Exception e) {
                    showError("Không thể mở game: " + e.getClass().getSimpleName());
                }
            }
        });
        String saved = getPreferences(MODE_PRIVATE).getString("key", "");
        if (!saved.isEmpty()) {
            keyInput.setText(saved);
            doValidate(saved, false);
        }
    }

    private String deviceId() {
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null || id.trim().isEmpty()) id = "unknown";
        return "ANDROID-" + id;
    }

    private JSONObject post(String path, String key) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
            new URL(SERVER + path).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            JSONObject input = new JSONObject();
            input.put("key", key);
            input.put("device_id", deviceId());
            byte[] payload = input.toString().getBytes("UTF-8");
            try (OutputStream output = connection.getOutputStream()) { output.write(payload); }
            InputStream response = connection.getResponseCode() < 400
                ? connection.getInputStream() : connection.getErrorStream();
            if (response == null) throw new Exception("Server không phản hồi");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream r = response) {
                byte[] buf = new byte[4096];
                for (int got = r.read(buf); got != -1; got = r.read(buf)) bytes.write(buf, 0, got);
            }
            JSONObject result = new JSONObject(new String(bytes.toByteArray(), "UTF-8"));
            result.put("_http_status", connection.getResponseCode());
            return result;
        } finally {
            connection.disconnect();
        }
    }

    private String date(JSONObject obj) {
        long expires = obj.optLong("expires_at", 0L);
        if (expires <= 0) return "";
        if (expires < 100000000000L) expires *= 1000;
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(expires));
    }

    private void doValidate(final String key, final boolean activateFirst) {
        authorized = false;
        enter.setEnabled(false);
        activate.setEnabled(false);
        status.setText("Đang kiểm tra key VTUAN...");
        detail.setText("Mã máy: " + deviceId());
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    JSONObject response;
                    if (activateFirst) {
                        response = post("/api/v1/activate", key);
                    }
                    response = post("/api/v1/check", key);
                    final JSONObject result = response;
                    final boolean valid = response.optBoolean("authorized", false)
                        && response.optInt("_http_status", 500) == 200;
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            activate.setEnabled(true);
                            if (valid) {
                                authorized = true;
                                validatedKey = key;
                                enter.setEnabled(true);
                                getPreferences(MODE_PRIVATE).edit().putString("key", key).apply();
                                status.setText("✓ Kích hoạt thành công");
                                status.setTextColor(0xff88f898);
                                String until = date(result);
                                detail.setText(until.isEmpty()
                                    ? "Key còn hiệu lực trên server VTUAN"
                                    : "Ngày hết hạn: " + until);
                            } else {
                                showError(result.optString("error",
                                    result.optString("message", "Key không hợp lệ hoặc không đúng thiết bị")));
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            activate.setEnabled(true);
                            showError("Lỗi xác thực: " + e.getClass().getSimpleName()
                                + (e.getMessage()==null ? "" : " - " + e.getMessage()));
                        }
                    });
                }
            }
        }).start();
    }

    private void showError(String msg) {
        authorized = false;
        enter.setEnabled(false);
        status.setText("✕ Không xác thực được");
        status.setTextColor(0xffff9898);
        detail.setText(msg);
    }
}

package com.example.vtuantesthost;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public final class OriginalActivity extends Activity {
  @Override public void onCreate(Bundle saved) {
    super.onCreate(saved);
    TextView text = new TextView(this);
    text.setText("ORIGINAL APP LAUNCHED");
    setContentView(text);
  }
}

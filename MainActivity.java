package com.akashmodz.wingo;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_NOTIFICATIONS = 501;
    private static final String CHANNEL_ID = "akash_modz_signals";
    private WebView webView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(10, 10, 18));
        getWindow().setNavigationBarColor(Color.rgb(10, 10, 18));
        createNotificationChannel();
        showPasswordLock();
    }

    // Daily password format: English month abbreviation + day number + @# (e.g. NOV2@#).
    private String getDailyPassword() {
        LocalDate today = LocalDate.now();
        String month = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase(Locale.US);
        return month + today.getDayOfMonth() + "@#";
    }

    private void showPasswordLock() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackgroundColor(Color.rgb(10, 10, 18));

        TextView brand = new TextView(this);
        brand.setText("👑 AKASH MODZ");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(27);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("DAILY PASSWORD REQUIRED");
        subtitle.setTextColor(Color.rgb(190, 190, 205));
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(12); sp.bottomMargin = dp(28);
        root.addView(subtitle, sp);

        EditText password = new EditText(this);
        password.setSingleLine(true);
        password.setHint("Enter today's password");
        password.setHintTextColor(Color.GRAY);
        password.setTextColor(Color.WHITE);
        password.setTextSize(17);
        password.setGravity(Gravity.CENTER);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(170, 130, 255)));
        root.addView(password, new LinearLayout.LayoutParams(-1, dp(54)));

        Button unlock = new Button(this);
        unlock.setText("UNLOCK");
        unlock.setTextSize(15);
        unlock.setAllCaps(false);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(52));
        bp.topMargin = dp(22);
        root.addView(unlock, bp);

        TextView footer = new TextView(this);
        footer.setText("Password changes every day");
        footer.setTextColor(Color.rgb(145, 145, 160));
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = dp(22);
        root.addView(footer, fp);

        unlock.setOnClickListener(v -> {
            String entered = password.getText().toString().trim();
            if (getDailyPassword().equalsIgnoreCase(entered)) {
                openWingoApp();
            } else {
                password.setError("Incorrect password");
                password.setText("");
                Toast.makeText(this, "Wrong password. Try today's password.", Toast.LENGTH_SHORT).show();
            }
        });
        password.setOnEditorActionListener((v, actionId, event) -> { unlock.performClick(); return true; });
        setContentView(root);
    }

    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private void openWingoApp() {
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true); settings.setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient()); webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AKASH MODZ Signals", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("New Wingo signals and WIN/LOSS results"); channel.enableVibration(true);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    public class AndroidBridge {
        @JavascriptInterface public void requestNotificationPermission() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
            });
        }
        @JavascriptInterface public void showNotification(String title, String body, String tag) { runOnUiThread(() -> postAppNotification(title, body, tag)); }
        @JavascriptInterface public void requestOverlayPermission() {
            runOnUiThread(() -> { if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(MainActivity.this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())); startActivity(i);
            }});
        }
        @JavascriptInterface public boolean hasOverlayPermission() { return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(MainActivity.this); }
        @JavascriptInterface public void startBackgroundEngine(String mode, String type) {
            Intent i = new Intent(MainActivity.this, WingoBackgroundService.class).setAction(WingoBackgroundService.ACTION_START);
            i.putExtra(WingoBackgroundService.EXTRA_MODE, mode); i.putExtra(WingoBackgroundService.EXTRA_TYPE, type);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        }
        @JavascriptInterface public void stopBackgroundEngine() {
            Intent i = new Intent(MainActivity.this, WingoBackgroundService.class).setAction(WingoBackgroundService.ACTION_STOP); startService(i);
        }
    }

    private void postAppNotification(String title, String body, String tag) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        int id = (tag == null ? (int)System.currentTimeMillis() : tag.hashCode()) & 0x7fffffff;
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title == null ? "AKASH MODZ" : title).setContentText(body == null ? "" : body.replace('\n',' '))
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body == null ? "" : body)).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build();
        getSystemService(NotificationManager.class).notify(id, n);
    }
    @Override public void onBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}

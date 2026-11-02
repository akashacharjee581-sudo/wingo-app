package com.akashmodz.wingo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import androidx.core.app.NotificationCompat;

public class WingoBackgroundService extends Service {
    public static final String ACTION_START = "com.akashmodz.wingo.START";
    public static final String ACTION_STOP = "com.akashmodz.wingo.STOP";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_TYPE = "type";
    private static final String CHANNEL = "akash_modz_engine";
    private static final int ONGOING_ID = 7201;
    private WebView engineView;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopEngine(); stopSelf(); return START_NOT_STICKY;
        }
        SharedPreferences prefs = getSharedPreferences("engine", MODE_PRIVATE);
        String mode = intent != null ? intent.getStringExtra(EXTRA_MODE) : null;
        String type = intent != null ? intent.getStringExtra(EXTRA_TYPE) : null;
        if (mode != null) prefs.edit().putString(EXTRA_MODE, mode).putString(EXTRA_TYPE, type).apply();
        else { mode = prefs.getString(EXTRA_MODE, "30s"); type = prefs.getString(EXTRA_TYPE, "Big/Small"); }
        showOngoingNotification(mode);
        startHeadlessEngine(mode, type == null ? "Big/Small" : type);
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            NotificationChannel ch = new NotificationChannel(CHANNEL, "AKASH MODZ Background Engine", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Shows while Wingo monitoring is running");
            nm.createNotificationChannel(ch);
            NotificationChannel signals = new NotificationChannel("akash_modz_signals", "AKASH MODZ Signals", NotificationManager.IMPORTANCE_HIGH);
            signals.setDescription("New Wingo signals and WIN/LOSS results");
            signals.enableVibration(true);
            nm.createNotificationChannel(signals);
        }
    }

    private void showOngoingNotification(String mode) {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 1, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("AKASH MODZ — Monitoring active")
            .setContentText("Wingo " + ("30s".equals(mode) ? "30 Sec" : "1 Min") + " • Notifications enabled")
            .setContentIntent(pi).setOngoing(true).setOnlyAlertOnce(true).build();
        if (Build.VERSION.SDK_INT >= 29) startForeground(ONGOING_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        else startForeground(ONGOING_ID, n);
    }

    private void startHeadlessEngine(String mode, String type) {
        stopEngine();
        engineView = new WebView(getApplicationContext());
        WebSettings s = engineView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true);
        s.setDatabaseEnabled(true); s.setBlockNetworkLoads(false);
        engineView.addJavascriptInterface(new EngineBridge(), "Android");
        final String m = mode, t = type;
        engineView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript("(function(){try{selectType(" + js(t) + ");startMode(" + js(m) + ");}catch(e){console.log(e);}})();", null);
            }
        });
        engineView.loadUrl("file:///android_asset/index.html");
    }

    private String js(String value) { return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"; }

    public class EngineBridge {
        @JavascriptInterface public void showNotification(String title, String body, String tag) {
            Notification n = new NotificationCompat.Builder(WingoBackgroundService.this, "akash_modz_signals")
                .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title == null ? "AKASH MODZ" : title)
                .setContentText(body == null ? "" : body.replace('\n',' '))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body == null ? "" : body))
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build();
            int id = (tag == null ? (int)System.currentTimeMillis() : tag.hashCode()) & 0x7fffffff;
            getSystemService(NotificationManager.class).notify(id, n);
        }
        @JavascriptInterface public void requestNotificationPermission() { }
        @JavascriptInterface public void requestOverlayPermission() { }
        @JavascriptInterface public boolean hasOverlayPermission() { return false; }
    }

    private void stopEngine() {
        if (engineView != null) {
            try { engineView.evaluateJavascript("if(typeof stopMode==='function')stopMode();", null); engineView.stopLoading(); engineView.loadUrl("about:blank"); engineView.destroy(); } catch (Exception ignored) {}
            engineView = null;
        }
    }

    @Override public void onDestroy() { stopEngine(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}

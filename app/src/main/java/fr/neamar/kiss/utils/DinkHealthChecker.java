package fr.neamar.kiss.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DinkHealthChecker {

    private static final String HEALTH_URL = "http://78.47.49.84:8222/health";
    private static final int TIMEOUT_MS = 5000;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface HealthCallback {
        void onResult(String status, String version, String uptime, String checksFormatted);
        void onError(String message);
    }

    public static void checkHealth(Context context, HealthCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                URL url = new URL(HEALTH_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    mainHandler.post(() -> callback.onError("HTTP " + responseCode));
                    conn.disconnect();
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "unknown");
                String version = json.optString("version", "unknown");
                String uptime = json.optString("uptime", "unknown");

                StringBuilder checks = new StringBuilder();
                JSONObject checksObj = json.optJSONObject("checks");
                if (checksObj != null) {
                    Iterator<String> keys = checksObj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject check = checksObj.getJSONObject(key);
                        String checkStatus = check.optString("status", "unknown");
                        String latency = check.optString("latency", "N/A");
                        checks.append("  ").append(key).append(": ").append(checkStatus)
                                .append(" (").append(latency).append(")\n");
                    }
                }

                String checksFormatted = checks.toString();
                mainHandler.post(() -> callback.onResult(status, version, uptime, checksFormatted));
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : "Unknown error";
                mainHandler.post(() -> callback.onError(message));
            }
        });
    }
}

package fr.neamar.kiss.dink;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.time.Duration;

import fr.neamar.kiss.R;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;

public class DinkNatsService extends Service {

    private static final String TAG = "DinkNatsService";
    private static final String CHANNEL_ID = "dink_nats";
    private static final int NOTIFICATION_ID = 9001;
    private static final String NATS_URL = "nats://78.47.49.84:4222";
    private static final String NATS_TOKEN = "dk_dev_n6z63tu46ghspkw7xsokilxc";

    private Connection natsConnection;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.dink_nats_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }

        startForeground(NOTIFICATION_ID, buildNotification(getString(R.string.dink_nats_connecting)));

        DinkConnectionState.getInstance().setState(
                DinkConnectionState.State.CONNECTING, null);

        new Thread(this::connectToNats, "dink-nats").start();
    }

    private void connectToNats() {
        try {
            Options options = new Options.Builder()
                    .server(NATS_URL)
                    .token(NATS_TOKEN.toCharArray())
                    .reconnectWait(Duration.ofSeconds(2))
                    .maxReconnects(-1)
                    .connectionTimeout(Duration.ofSeconds(5))
                    .connectionListener(this::onConnectionEvent)
                    .errorListener(new ErrorListener() {
                        @Override
                        public void errorOccurred(Connection conn, String error) {
                            Log.e(TAG, "NATS error: " + error);
                            DinkConnectionState.getInstance().setState(
                                    DinkConnectionState.State.RECONNECTING, error);
                        }

                        @Override
                        public void exceptionOccurred(Connection conn, Exception exp) {
                            Log.e(TAG, "NATS exception", exp);
                            DinkConnectionState.getInstance().setState(
                                    DinkConnectionState.State.RECONNECTING,
                                    exp.getMessage());
                        }
                    })
                    .build();

            natsConnection = Nats.connect(options);

            String serverInfo = null;
            if (natsConnection.getServerInfo() != null) {
                serverInfo = natsConnection.getServerInfo().toString();
            }

            DinkConnectionState.getInstance().setState(
                    DinkConnectionState.State.CONNECTED, serverInfo);
            updateNotification(getString(R.string.dink_nats_connected));

        } catch (Exception e) {
            Log.e(TAG, "Failed to connect to NATS", e);
            DinkConnectionState.getInstance().setState(
                    DinkConnectionState.State.DISCONNECTED, e.getMessage());
            updateNotification(getString(R.string.dink_nats_disconnected));
        }
    }

    private void onConnectionEvent(Connection conn, ConnectionListener.Events event) {
        String serverInfo = null;
        try {
            if (conn != null && conn.getServerInfo() != null) {
                serverInfo = conn.getServerInfo().toString();
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get server info", e);
        }

        switch (event) {
            case CONNECTED:
                DinkConnectionState.getInstance().setState(
                        DinkConnectionState.State.CONNECTED, serverInfo);
                updateNotification(getString(R.string.dink_nats_connected));
                break;
            case DISCONNECTED:
                DinkConnectionState.getInstance().setState(
                        DinkConnectionState.State.DISCONNECTED, serverInfo);
                updateNotification(getString(R.string.dink_nats_disconnected));
                break;
            case RECONNECTED:
                DinkConnectionState.getInstance().setState(
                        DinkConnectionState.State.CONNECTED, serverInfo);
                updateNotification(getString(R.string.dink_nats_connected));
                break;
            case RESUBSCRIBED:
                // ignore
                break;
            case CLOSED:
                DinkConnectionState.getInstance().setState(
                        DinkConnectionState.State.DISCONNECTED, serverInfo);
                break;
            default:
                DinkConnectionState.getInstance().setState(
                        DinkConnectionState.State.RECONNECTING, serverInfo);
                updateNotification(getString(R.string.dink_nats_reconnecting));
                break;
        }
    }

    private void updateNotification(String text) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification(text));
        }
    }

    private android.app.Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(text)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (natsConnection != null) {
            try {
                natsConnection.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing NATS connection", e);
            }
            natsConnection = null;
        }

        DinkConnectionState.getInstance().setState(
                DinkConnectionState.State.DISCONNECTED, null);
        stopForeground(STOP_FOREGROUND_REMOVE);
    }
}

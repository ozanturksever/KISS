package fr.neamar.kiss.dink;

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class DinkConnectionState {

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING
    }

    public interface StateListener {
        void onStateChanged(State newState, String info);
    }

    private static class Holder {
        static final DinkConnectionState INSTANCE = new DinkConnectionState();
    }

    public static DinkConnectionState getInstance() {
        return Holder.INSTANCE;
    }

    private volatile State state = State.DISCONNECTED;
    private volatile String lastError;
    private volatile String serverInfo;
    private volatile long connectedSince;
    private volatile long lastReconnect;
    private volatile int reconnectCount;

    private final CopyOnWriteArrayList<StateListener> listeners = new CopyOnWriteArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private DinkConnectionState() {
    }

    public void addListener(StateListener listener) {
        if (listener != null) {
            listeners.addIfAbsent(listener);
        }
    }

    public void removeListener(StateListener listener) {
        listeners.remove(listener);
    }

    public void setState(State newState, String info) {
        this.state = newState;
        this.serverInfo = info;

        switch (newState) {
            case CONNECTED:
                connectedSince = System.currentTimeMillis();
                lastError = null;
                break;
            case RECONNECTING:
                reconnectCount++;
                lastReconnect = System.currentTimeMillis();
                break;
            case DISCONNECTED:
                if (info != null) {
                    lastError = info;
                }
                break;
            default:
                break;
        }

        mainHandler.post(() -> {
            for (StateListener listener : listeners) {
                listener.onStateChanged(newState, info);
            }
        });
    }

    public State getState() {
        return state;
    }

    public String getLastError() {
        return lastError;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public long getConnectedSince() {
        return connectedSince;
    }

    public long getLastReconnect() {
        return lastReconnect;
    }

    public int getReconnectCount() {
        return reconnectCount;
    }

    public String getFormattedStatus() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        StringBuilder sb = new StringBuilder();
        sb.append("State: ").append(state.name()).append('\n');
        if (serverInfo != null) {
            sb.append("Server: ").append(serverInfo).append('\n');
        }
        if (connectedSince > 0) {
            sb.append("Connected since: ").append(sdf.format(new Date(connectedSince))).append('\n');
        }
        if (lastError != null) {
            sb.append("Last error: ").append(lastError).append('\n');
        }
        sb.append("Reconnect count: ").append(reconnectCount).append('\n');
        if (lastReconnect > 0) {
            sb.append("Last reconnect: ").append(sdf.format(new Date(lastReconnect))).append('\n');
        }
        return sb.toString();
    }

    public void reset() {
        state = State.DISCONNECTED;
        lastError = null;
        serverInfo = null;
        connectedSince = 0;
        lastReconnect = 0;
        reconnectCount = 0;
    }
}

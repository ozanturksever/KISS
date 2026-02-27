package fr.neamar.kiss.forwarder;

import android.content.Intent;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.util.Log;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.utils.ShortcutUtil;

class OreoShortcuts extends Forwarder {
    private static final String TAG = "OreoShortcuts";

    OreoShortcuts(MainActivity mainActivity) {
        super(mainActivity);
    }

    void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Shortcuts in Android O
            if (ShortcutUtil.areShortcutsEnabled(mainActivity)) {
                // On first run save all shortcuts
                if (prefs.getBoolean("first-run-shortcuts", true)) {
                    if (mainActivity.isKissDefaultLauncher()) {
                        // Save all shortcuts
                        ShortcutUtil.addAllShortcuts(mainActivity);
                        // Set flag to falseX
                        prefs.edit().putBoolean("first-run-shortcuts", false).apply();
                    }
                }

                handlePinShortcutIntent(mainActivity.getIntent());
            }
        }
    }

    void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent action=" + (intent != null ? intent.getAction() : "null"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ShortcutUtil.areShortcutsEnabled(mainActivity)) {
                handlePinShortcutIntent(intent);
            } else {
                Log.d(TAG, "Shortcuts disabled in settings");
            }
        }
    }

    private void handlePinShortcutIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(TAG, "handlePinShortcutIntent action=" + action);
            if (LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT.equals(action)) {
                Log.d(TAG, "Processing CONFIRM_PIN_SHORTCUT");
                // Save single shortcut via a pin request
                ShortcutUtil.addShortcut(mainActivity, intent);
            }
        }
    }
}

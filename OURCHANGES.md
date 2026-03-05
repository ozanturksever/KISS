# Our Changes (Fork Differences from Upstream)

This file documents all modifications made in our fork compared to upstream KISS Launcher.
Use this as a reference when merging upstream changes.

## Dink Integration

### NATS Service (`DinkNatsService`)
- **Files added:**
  - `app/src/main/java/fr/neamar/kiss/dink/DinkNatsService.java`
  - `app/src/main/java/fr/neamar/kiss/dink/DinkConnectionState.java`
  - `app/src/main/java/fr/neamar/kiss/utils/DinkHealthChecker.java`
  - `app/src/main/res/xml/network_security_config.xml`
- **Files modified:**
  - `app/build.gradle` - Added `io.nats:jnats:2.21.1` dependency
  - `app/proguard-rules.pro` - Added keep rules for `io.nats.client`
  - `app/src/main/AndroidManifest.xml` - Added INTERNET, FOREGROUND_SERVICE, FOREGROUND_SERVICE_DATA_SYNC, POST_NOTIFICATIONS permissions; networkSecurityConfig; DinkNatsService declaration
  - `app/src/main/java/fr/neamar/kiss/KissApplication.java` - Start DinkNatsService on app creation
  - `app/src/main/res/values/strings.xml` - Added dink-related strings (menu_dink_health, nats status strings)

### Dink Health Menu
- **Files modified:**
  - `app/src/main/res/menu/menu_main.xml` - Added "Dink Health" menu item

## UI Changes

### FATAGNUS Branding
- **Files modified:**
  - `app/src/main/res/layout/main.xml` - Added "FATAGNUS" text banner at top of home screen; widgetLayout positioned below it instead of alignParentTop

## Bug Fixes

### Favorites Bar Layout Overflow
- `app/src/main/res/layout/main.xml` - Removed `animateLayoutChanges` from favorites bar to prevent layout overflow issues

### PWA Pin Shortcut via onNewIntent
- `app/src/main/java/fr/neamar/kiss/forwarder/OreoShortcuts.java` - Handle pin shortcut requests arriving via `onNewIntent` (when activity is already running)
- `app/src/main/java/fr/neamar/kiss/forwarder/ForwarderManager.java` - Added `onNewIntent` forwarding
- `app/src/main/java/fr/neamar/kiss/MainActivity.java` - Call `forwarderManager.onNewIntent(intent)` in `onNewIntent`

### Pinned Shortcuts in Gesture App Picker
- `app/src/main/java/fr/neamar/kiss/preference/LaunchPojoSelectPreference.java` - Include pinned shortcuts (PWA etc.) in the gesture launch app selection list, not just regular apps

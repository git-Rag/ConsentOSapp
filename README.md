## Consent OS – Android Companion Demo

Consent OS is a **pre-interaction privacy gate**. It does not block trackers, act as a browser, VPN, or antivirus.  
Its only job is to **pause link interactions and ask for explicit consent before a website opens**.

### Core behavior

- The app registers as a handler for `https` links via Android intents.
- When a link is opened (or shared as text containing a URL), the app:
  - Extracts the URL and domain
  - Looks up any stored consent rule for that domain
  - Either:
    - Opens the **Consent Gate** screen (no rule yet), or
    - Immediately forwards to the browser (Always allow), or
    - Shows the **Blocked** screen (Deny)
- Decisions are stored **on-device only**, using `DataStore` (no network or analytics).

### Screens

- **IntroScreen**
  - Empty/first-time state when no URL is active.
  - Explains: “Consent is a conscious decision, not a checkbox.”
- **ConsentGateScreen**
  - Shows the domain prominently.
  - Explains that the site may collect personal data.
  - Actions:
    - Allow once
    - Always allow for this domain
    - Deny and stay here
- **BlockedScreen**
  - Explains that the link was blocked according to the user’s choice.
  - Reassures that the user remains in control.
- **ConsentRulesScreen**
  - Simple list of `domain → decision` rules stored on the device.

### Tech stack

- **Language**: Kotlin only  
- **UI**: Jetpack Compose + Material 3  
- **Navigation**: `androidx.navigation:navigation-compose`  
- **Storage**: `androidx.datastore:datastore-preferences` (offline, on-device)  
- **Intents**: Standard `ACTION_VIEW` and `ACTION_SEND` for handling links

### How to run

1. Open this folder in **Android Studio** (Giraffe or newer).
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or device (Android 7.0 / API 24+).
4. On the device:
   - Tap a `https` link from another app (e.g., Notes, Messages).
   - When Android asks which app should open the link, choose **Consent OS** (and optionally “Always”).

### Demo suggestions

- **Show first-time behavior**  
  Launch the app directly to show the **IntroScreen** explaining the philosophy.

- **Show the consent gate**  
  Tap a link in another app and route it through Consent OS.  
  Walk through the three choices and emphasize that **nothing loads until the user decides**.

- **Show persistence**  
  Choose “Always allow for this domain”, tap the same link again, and show that it now opens directly in the browser.

- **Show blocking**  
  Choose “Deny” for a domain, then tap the link again to show the **BlockedScreen** and the stored rule in **ConsentRulesScreen**.

Everything in the UI is intentionally minimal, calm, and focused on the moment of decision—  
reinforcing that **consent is a conscious decision, not a checkbox**.



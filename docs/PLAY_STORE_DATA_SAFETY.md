# Play Console — Data Safety Section

## Section 1: Data Types Collected & Shared

Annotate each category in Play Console. Combined with the table below.

### Data collected / shared

| Data Type | Collected? | Shared? | Optional? | Purpose | Third parties |
|-----------|------------|---------|-----------|---------|---------------|
| **Approximate location** | No (app does not request location; Mapbox SDK may) | No | N/A | Permiso declarado para Mapbox, no usado por la app | Mapbox (if SDK enables it) |
| **User IDs** | Yes | Yes (with Firebase, Sentry) | Yes (consent-gated) | Analytics, crash reporting | Firebase, Sentry |
| **App interactions** | Yes | Yes (with Firebase, Sentry) | Yes (consent-gated) | Analytics, product improvement | Firebase, Sentry |
| **Search history** | Yes | Yes (with Firebase, Sentry) | Yes (consent-gated) | Analytics — search queries (text), result counts | Firebase, Sentry |
| **Crash data** | Yes | Yes (Firebase Crashlytics, Sentry) | No (always collected) | Crash reporting, stability monitoring | Firebase, Sentry |
| **Performance data** | Yes | Yes (Firebase, Sentry) | Yes (consent-gated) | Performance monitoring | Firebase, Sentry |
| **Voice recordings** | Yes (via Android SpeechRecognizer) | No | Yes | Voice search — audio sent to Google Speech Services (OS level, not app server). Text result used for search | Google (Android OS) |
| **Photos** | Yes (user-picked) | No | Yes | Journal entries — stored locally only | None |
| **Journal entries** | Yes (user-created text) | No | Yes | Travel journal — stored locally in Room DB | None |
| **Favorites** | Yes (city IDs) | No | Yes | User preferences — stored locally in DataStore | None |
| **Device ID / Advertising ID** | No | No | N/A | Not collected by app code. Firebase/Sentry SDKs may auto-collect OS-level identifiers | Firebase, Sentry |

### Key: What REQUIRES disclosure

- **✅ Collected**: Everything above is collected in some form.
- **✅ Shared** (third-party): Firebase (Analytics + Crashlytics), Sentry, Mapbox.
- **✅ Encryption in transit**: All network traffic is HTTPS. Voice search uses Android's built-in encrypted path.
- **✅ Data deletion**: User can clear app data, uninstall (locally stored data), or stop consent (analytics).

---

## Section 2: Play Console Form — Answers

Fill as follows:

### 1. Does your app collect or share any of the required user data types?
> **Yes**

### 2. Select all data types collected

| Checkbox | Answer |
|----------|--------|
| Location | **Approximate location** — Checked (permission declared for Mapbox SDK). Add in Play Console description: "Permission declared for Mapbox SDK to show user location on map. App does not actively request or use location." |
| Personal info | **User IDs** — Checked (Firebase, Sentry) |
| App activity | **App interactions, Search history, Installed apps** — Checked |
| Crash logs | **Crash data** — Checked |
| Performance | **Performance data** — Checked |
| Audio | **Voice recordings** — Checked (explain: "Audio recording sent to Android SpeechRecognizer (Google); app does not store audio") |
| Photos | **Photos** — Not shared, collected for journal feature |
| Files and docs | Not collected |
| Device or other IDs | Not directly collected by app; Firebase/Sentry SDK may auto-collect. If required, select "Device or other IDs" |

### 3. Is this data collected, shared, or both?

- **User IDs**: Collected and shared (Firebase, Sentry)
- **App interactions**: Collected and shared (Firebase, Sentry)
- **Search history**: Collected and shared (Firebase, Sentry — search text queries)
- **Crash data**: Collected and shared (Firebase Crashlytics, Sentry)
- **Performance data**: Collected and shared (Firebase, Sentry)
- **Voice recordings**: Collected (by Android SpeechRecognizer), NOT shared by app
- **Photos**: Collected (user-picked), NOT shared
- **Journal entries + notes**: Collected locally, NOT shared
- **Location (approximate)**: Collected (by Mapbox SDK), shared with Mapbox

### 4. Is this data encrypted in transit?
> **Yes** — All API calls use HTTPS. Firebase, Sentry, Mapbox SDKs use encrypted connections.

### 5. Do you provide a way for users to request data deletion?
> **Yes** — Users can:
> - Clear app data / uninstall (all local data is removed)
> - Decline analytics consent (stops analytics data sharing)
> - Contact support for server-side deletion (Firebase, Sentry). No user account system exists.

### 6. Data handling for each type

| Data Type | Collected | Shared | Encrypted | Optional | User Control |
|-----------|-----------|-------|-----------|----------|--------------|
| Approximate location | Yes (Mapbox SDK) | Mapbox | Yes | Yes | Micro-permission at OS level |
| User IDs | Firebase, Sentry | Firebase, Sentry | Yes | Yes | Consent dialog on first launch |
| App interactions | Via analytics events | Firebase, Sentry | Yes | Yes | Consent dialog |
| Search history | Search queries logged | Firebase, Sentry | Yes | Yes | Consent dialog |
| Crash data | Firebase Crashlytics, Sentry | Firebase, Sentry | Yes | No (required for stability) | Cannot be disabled (stability) |
| Performance data | Via PerformanceMonitor | Firebase, Sentry | Yes | Yes | Consent dialog |
| Voice recordings | Android SpeechRecognizer | Google (OS) | Yes | Yes | Micro-permission at OS level |
| Photos | User-picked via PhotoPicker | Not shared | N/A | Yes | User control |
| Journal entries | Room DB locally | Not shared | N/A | Yes | User can delete entries |

---

## Section 3: Policy Compliance Checklist

### Required for Play Store

- [x] **Consent dialog** shown on first launch (impl: `ConsentDialog.kt`)
- [x] **Analytics consent enforced** (impl: `setConsentGranted` in `CompositeAnalyticsService`)
- [x] **Privacy policy** — needs URL in Play Console listing
- [x] **Data Safety declarations** — fill using the tables above
- [x] **Target API 35+** — `targetSdk = 37` ✓
- [x] **16 KB page size** — `pageSizeCompat="enabled"` ✓
- [x] **GDPR compliance** — consent dialog covers EU requirements
- [x] **No ads** — no ad SDKs
- [ ] **Privacy policy URL** — host publicly (GitHub Pages, Google Docs, etc.) and link in Play Console
- [ ] **Data deletion mechanism** — document how users request deletion

### Actions needed outside code

1. **Create privacy policy** — include:
   - What data is collected (use tables above)
   - How data is used (analytics, crash reporting, maps)
   - Third-party SDKs (Firebase, Sentry, Mapbox)
   - How to opt out (decline consent, revoke permissions)
   - Contact for data deletion requests

2. **Paste privacy policy URL** in Play Console → Store listing → Privacy Policy

3. **Complete Data Safety form** in Play Console using the answers above

4. **Complete Content Rating** questionnaire in Play Console

5. **Prepare store listing**:
   - App icon (adaptive: 512x512px)
   - Feature graphic (1024x500px)
   - Screenshots (at least 2 phones + 2 tablets)
   - Short description (80 chars max)
   - Full description (4000 chars max)

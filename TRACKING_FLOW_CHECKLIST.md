# End-to-End Parcel Tracking Verification Checklist

This document details the checklist and step-by-step testing script for verifying the end-to-end parcel tracking flow in **Engraced Dispatch**. It spans manual camera scan actions, Zod input validation, database syncing, status timelines, and push notifications.

---

## 1. Functional Verification Checklist

### A. Barcode Input via Camera (`Scanner` Screen)
- [ ] **Camera Permission Request**: Launching the scanner prompts for camera permissions dynamically.
- [ ] **Dynamic Live View Finder**: Display of the high-contrast scanning reticle with a gold outline.
- [ ] **Torch Toggle Button**: Operational toggle (on/off) of the device flash for scanning in low light.
- [ ] **Manual Entry Input Validation**: Outlined text field accepts fallback manual input when camera scan is not used.
- [ ] **Format Validation Integration**: Formats scanned barcodes and checks manual inputs against strict validation schemas before triggering queries.

### B. Input Validation (Zod Schema Validation)
- [ ] **Length Limit Constraints**: Refuses tracking IDs shorter than `7` characters or longer than `12` characters immediately with clear, red helper text warnings.
- [ ] **Format Constraints**: Rejects special characters or punctuation, allowing only letters, numbers, and hyphens.
- [ ] **Real-time Error Resetting**: Typing a key instantly resets active validation errors and error highlights.
- [ ] **Error Toast Fallbacks**: Displays explicit error details in toasts or text inputs when invalid formats are submitted.

### C. Live Database & Sync Flow
- [ ] **Room Database Insertion**: Successfully queries local parcel records if network is unavailable.
- [ ] **Real-time Listener Attachment**: Subscribes dynamically to Firebase Firestore updates for the queried tracking number.
- [ ] **Local Storage Cache Updates**: Saving the parcel locally keeps the UI fully functional and responsive offline.

### D. Status Badges & Progress Timeline UI
- [ ] **Dynamic Status Timelines**: Timelines dynamically progress from `TRANSIT` (0.35f) to `OUT_FOR_DELIVERY` (0.75f) to `DELIVERED` (1.0f).
- [ ] **Contrast Verification**:
  * No white icons or text directly overlaying Gold status elements.
  * Gold badges feature Obsidian black text and icons for proper accessibility scoring.
  * On-surface colors adapt dynamically to Light and Dark system settings.

### E. Notification & Trigger Alerts
- [ ] **Dynamic Status Notifications**: Triggering state transitions successfully invokes local notifications.
- [ ] **Notification Manager Integration**: Local alerts appear in the device notification shade with a matching logo and subtitle.
- [ ] **FCM Background Receiver**: Simulates background push notifications through FCM, registering deep links that launch the application directly into the target active parcel details screen.

---

## 2. End-to-End Manual Testing Script

Perform these steps to manually verify tracking integrity:

| Step | User Action | Expected App Response | Pass / Fail |
| :--- | :--- | :--- | :--- |
| **1** | Tap **Track & Trace** from the bottom capsule dock or Home dashboard. | Tracking dashboard opens with the tracking search bar centered and recent search histories listed beneath it. | |
| **2** | Enter `XYZ` into the tracking input and click **Track**. | Under the input field, a red error label displays: `"Tracking ID must be at least 7 characters."` No network request is initiated. | |
| **3** | Enter `XYZ!@#123` into the input and click **Track**. | Red validation warning: `"Only letters, numbers, and hyphens allowed."` | |
| **4** | Enter a valid tracking ID (e.g. `TRK-8829910`) and click **Track**. | The validation error clears. A success toast or active tracking timeline screen loads, rendering the live route details. | |
| **5** | Tap the **Camera Icon** in the tracking input or navigate to the **Scanner** screen. | Prompts the user to grant camera permissions. Camera preview starts, displaying the scanning reticle. | |
| **6** | Aim the camera at a tracking QR/Barcode or type a valid tracking ID in the manual input box at the bottom. | The barcode is scanned or manual entry is validated, redirecting to the **Active Tracking** view immediately. | |
| **7** | Trigger a status change (simulate status shift in the Admin AIDispatch console or via Mock State). | The timeline indicator animates forward. A status badge updates to the new state with perfect contrast. | |
| **8** | Verify notifications. | A local system notification fires with the header `"Engraced Dispatch"` and the updated status message. | |

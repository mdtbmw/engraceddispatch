# Firebase & Vercel Production Deployment Guide

## Prerequisites
- Firebase project: **engraceddispatch-ffba4** (already created)
- Firebase CLI: `npm install -g firebase-tools`
- Vercel account connected to GitHub

---

## Step 1 — Enable Firebase Services (Console)

1. **Firestore Database**
   - Go to https://console.firebase.google.com/project/engraceddispatch-ffba4/firestore
   - Click **Create Database**
   - Choose location (e.g., `eur3` Europe-west)
   - Start in **Test mode** (rules will be updated on deploy)
   - Wait 2-3 minutes for provisioning

2. **Authentication**
   - Go to https://console.firebase.google.com/project/engraceddispatch-ffba4/authentication
   - Click **Get started**
   - Enable **Email/Password** sign-in provider
   - (Optional) Enable Email Enumeration Protection = OFF for dev

---

## Step 2 — Grant Firebase CLI Permissions

Your Firebase CLI is logged in as `mgtbmww@gmail.com` but it needs project access:

1. Go to https://console.developers.google.com/iam-admin/iam?project=engraceddispatch-ffba4
2. Click **Grant Access** → Add Principal: `mgtbmww@gmail.com`
3. Role: **Firebase Admin** (or **Editor** for full access)
4. Save, wait 2 minutes for propagation

---

## Step 3 — Deploy Firestore Rules + Indexes

```bash
cd /path/to/engraced-smile-dispatch
npx firebase deploy --only firestore:rules,firestore:indexes --project engraceddispatch-ffba4
```

---

## Step 4 — Deploy to Vercel (Admin Dashboard)

1. Push repo to GitHub:
```bash
git add -A
git commit -m "Full production build"
git remote add origin https://github.com/YOUR_USER/engraced-smile-dispatch.git
git push -u origin main
```

2. In Vercel:
   - **Import** the GitHub repo
   - **Root directory**: `admin`
   - **Framework preset**: Vite (auto-detected)
   - **Build command**: `npx vite build`
   - **Output directory**: `dist`
   - **Environment variables** (add these exact values):

| Variable | Value |
|---|---|
| `VITE_FIREBASE_API_KEY` | `AIzaSyCxVWlXrlsXSrJvzlxNiQ4lcBcP05E73Ts` |
| `VITE_FIREBASE_AUTH_DOMAIN` | `engraceddispatch-ffba4.firebaseapp.com` |
| `VITE_FIREBASE_PROJECT_ID` | `engraceddispatch-ffba4` |
| `VITE_FIREBASE_STORAGE_BUCKET` | `engraceddispatch-ffba4.firebasestorage.app` |
| `VITE_FIREBASE_MESSAGING_SENDER_ID` | `858437923778` |
| `VITE_FIREBASE_APP_ID` | `1:858437923778:web:b4d9dc83a96c1f78955c5b` |
| `FIREBASE_SERVICE_ACCOUNT_BASE64` | (base64 of service-account.json — already in `admin/.env`) |

3. **Deploy** — Vercel will build and deploy automatically

---

## Step 5 — Seed the Database

After Firestore is enabled and Vercel is deployed:

```bash
cd /path/to/engraced-smile-dispatch
node firebase/seed-data.js
```

This creates:
- Admin user: `admin@engraced.com` / `Admin@123`
- System settings (auto-assign ON, surge pricing, etc.)
- 4 pricing rules (Express, Economy, Batch, Multi-Pickup)
- 5 delivery zones (Lekki, Ikeja, VI, Surulere, Maryland)
- 3 riders (Sani, Emeka, Tunde)
- 3 customers + 3 sample transactions

---

## Step 6 — Build Android APKs

### Customer App
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
./gradlew --no-daemon --no-configuration-cache :app:assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

### Rider App
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
./gradlew --no-daemon --no-configuration-cache :rider-app:assembleDebug
```
APK: `rider-app/app/build/outputs/apk/debug/app-debug.apk`

---

## Step 7 — Remove Admin Signup (after first user created)

**After you have at least one admin account**, remove the signup capability:

1. **Frontend**: In `admin/src/pages/Login.tsx`, remove the "Create Admin" tab and `handleSignup`
2. **Backend**: In `admin/api/index.ts`, remove the `POST /api/auth/setup-admin` route
3. Redeploy to Vercel

---

## Architecture Summary

```
                    ┌─────────────────────────────────────────┐
                    │           Vercel (admin/)                │
                    │  ┌──────────────┐  ┌──────────────────┐  │
                    │  │ React Frontend│  │ Serverless API   │  │
                    │  │ (Vite build)  │  │ (/api/* routes)  │  │
                    │  │ Reads: Direct │  │ Writes: Admin SDK│  │
                    │  │ Firestore     │  │ + validation     │  │
                    │  └──────┬───────┘  └────────┬─────────┘  │
                    └─────────┼───────────────────┼────────────┘
                              │                   │
                    ┌─────────┴───────────────────┴────────────┐
                    │            Firebase (Google Cloud)        │
                    │  ┌────────────┐  ┌─────────────────────┐  │
                    │  │  Firestore  │  │  Firebase Auth       │  │
                    │  │  (8 cols)   │  │  (Email/Password)   │  │
                    │  └────────────┘  └─────────────────────┘  │
                    └──────────────────────────────────────────┘
                              │
                    ┌─────────┴──────────┐
                    │  Android Apps       │
                    │  (Firebase SDK)     │
                    │  Auth + Firestore   │
                    └────────────────────┘
```

## File Inventory (Production-Ready)

| File | Status |
|---|---|
| `admin/.env` | ✅ All 7 env vars set |
| `app/google-services.json` | ✅ Customer app config |
| `rider-app/app/google-services.json` | ✅ Rider app config |
| `firebase/firestore.rules` | ✅ 8 collections + audit + meta |
| `firebase/firestore.indexes.json` | ✅ 10 indexes for all queries |
| `firebase/seed-data.js` | ✅ Updated for Admin SDK v12 |
| `firebase/service-account.json` | ✅ Service account key |
| `firebase.json` | ✅ Points to rules + indexes |
| `admin/vercel.json` | ✅ SPA + API rewrites |
| `admin/api/index.ts` | ✅ 17 routes including auth setup |
| `admin/src/lib/admin-api.ts` | ✅ Full API client |
| `admin/dist/` | ✅ Vite build (521 modules, 1.24MB) |

# ESDISPATCH — FOCUSED AUDIT: EMAIL/SMTP FAILURES + IDENTITY UNIQUENESS + OTP CONFUSION

**Report date:** 2026-09-27 · **Mode:** read-only source audit + live HTTP/DNS probes · **Repo:** `D:\Smiles Dispatch` (branch `main`, origin at `07c3af6`)

---

## PART 1 — WHY NO EMAILS SEND (and why users see "check your internet connection")

### 1.1 The exact error you see — where it comes from

The string is emitted at **`DeliveryViewModel.kt:4336`**:

> `"Unable to send verification code. Please check your internet connection and try again."`

It fires whenever `dispatched` stays `false` after trying **every** send path. The misleading "internet connection" wording is a **lie to the user** — your internet is fine; every server the app talks to for email is dead. The real reason is recorded one line earlier at `:4321` (`lastError = "Server returned <code>"`) but is never shown.

### 1.2 Root cause ranking (live-verified, most → least likely)

| # | Root cause | Live proof | Explains |
|---|---|---|---|
| **1** | **Your entire Vercel deployment no longer exists** | `GET https://engraceddispatchnew.vercel.app/` → **404 `X-Vercel-Error: DEPLOYMENT_NOT_FOUND`** (also on `/api/email/verification`) | Every REST attempt in the send loop returns 404 → exact toast. The app's primary API host serves **nothing**. |
| **2** | **No Firebase Cloud Functions are deployed** | `POST ...cloudfunctions.net/sendEmailOtp` → Google **404 "Page not found"** (same for `verifyEmailOtp`, `testSmtpConnection`, status triggers) | Strategy A (the callable, `DeliveryViewModel.kt:4274-4291`) always throws; **all** automatic emails (welcome, order status, handover OTP, invoice) never run. CI only *compiles* functions — it never deploys them, and nothing turns red. |
| **3** | **The fallback domain `engracedsmile.com` isn't on Vercel** | `GET https://engracedsmile.com/` → **404 on LiteSpeed** (A record `46.105.211.204`, shared hosting, empty site) | Third and last endpoint in the loop (`DeliveryViewModel.kt:4298` + final fallback `:545`) also 404s. **There is no reachable URL anywhere in the chain.** |
| **4** | **Next.js API routes are structurally undeployable** | `vercel.json` = `framework: vite`, `build: vite build`, output `dist`; `dist/api` does not exist | `src/app/api/email/*/route.ts` files can **never** run in production — only root `api/*.js` would. |
| **5** | **SMTP wiring exists only in the local dev server** | `vite.config.ts:10,181,285` — three email endpoints inside `configureServer` (runs only under `vite dev`) | Classic "works on my machine": Email Studio functions on your laptop, silently fails for everyone on the deployed site. |
| **6** | **Admin's Test SMTP / Send Test Email handlers were deleted** (commit `d0215a4`) | `git log --diff-filter=D` shows `api/email/smtp-verify.ts`, `api/email/test-send.js` + both Next routes deleted; callers remain (`EmailStudioTab.tsx:684,756`) | Admin test buttons = guaranteed 404 everywhere except `vite dev`. |
| **7** | **`SMTP_PASS` may not be set on Vercel** | Uncommitted scrub removed the hardcoded password from handlers; `api/email/verification.js:282-285` now returns **500 "SMTP server credentials are not configured"** if the env var is missing | After you restore the deployment, the error would flip from 404 to 500 unless env vars are configured (same user-facing toast). |
| **8** | **Firebase Hosting target is empty** | `https://engraceddispatch-ffba4.web.app/` → **404 "Site Not Found"**; CI deploy step is silently skipped when its secret is absent (job stays green) | The backup deploy path also deploys nothing. |

**Also relevant:**
- **24 files are uncommitted** in the working tree (including `firestore.rules`, `DeliveryViewModel.kt`, the SMTP secret scrub, `DirectSmtpMailer.kt` deletion). Anything you fixed locally is **not** what CI/Vercel would build.
- **The verification-link flow is dead by design:** `src/app/verified/page.tsx` is a static "Identity Authenticated" page — it performs **no** verification write. The "Check Link" button can never succeed.
- **OTP code mismatch bug:** the app generates a 6-digit code on-device (`DeliveryViewModel.kt:4259`), but the `sendEmailOtp` function **ignores it and mails its own code** (`functions/src/index.ts:913-923`) — so if the function path is restored without fixing this, users get an email whose code the app will reject.
- **DNS check lies:** when the endpoint 404s, `EmailStudioTab.tsx:272-277` **fabricates** "SPF valid, inbox ready, LOW spam risk" instead of showing an error.
- **Every URL inside every email is dead:** templates hardcode `engraceddispatchnew.vercel.app` CTAs (`src/lib/emailTemplates.ts:78`, `functions/src/emails/emailTemplates.ts:78`) — even a delivered email would link to 404s.
- **NOT a cause:** SPF/DKIM are actually valid (SPF authorizes your relay `5.39.69.62`; DKIM `default` selector present; DMARC exists at `p=none`). Mail auth would pass. Emails don't arrive because **nothing ever calls `sendMail`.**
- **⚠️ Security:** `system_settings/smtp` (holding the live SMTP password) is **world-readable** (`firestore.rules:378-381` `allow read: if true`) — same exposure class already fixed for the FCM key.

### 1.3 Email feature status

| Feature | Status | Why |
|---|---|---|
| Verification passcode (in-app) | ❌ **BROKEN** | All 3 paths dead: callable 404, both REST hosts 404 |
| Verification link (`/verified`) | ❌ **DEAD by design** | Static page, no verification write |
| Welcome/signup email | ❌ **NEVER SENT** | Trigger sends a push, not email — and isn't deployed |
| Order status / handover OTP / invoice emails | ❌ **BROKEN** | Function not deployed |
| Password reset (Firebase Auth) | ✅ **WORKS** | Uses Firebase's own SMTP — independent of all this |
| Admin "Test SMTP" / "Send Test Email" | ❌ **BROKEN** | Handlers deleted + dev-only middleware |
| Admin "DNS Check" | ❌ **BROKEN + shows fake success** | Endpoint 404 → fabricated results |

**Direct answer to your question:** it's **not** an admin toggle. The `emailVerificationRequired` toggle only controls whether the flow is *required* (it defaults off); toggling it on would not send anything. The system fails because **your production deployment and Cloud Functions don't exist right now** — a deployment/DevOps failure, plus dead fallback domains.

### 1.4 Fix order (email)

1. **Commit the 24 uncommitted files**; **rotate the SMTP password** (it's in git history and in shipped APKs).
2. **Pick one backend and restore it** (recommended: keep Vite + root `api/` serverless; restore deleted `api/email/test-send.js` + `smtp-verify.ts` as CJS with timeouts).
3. **Redeploy to Vercel** and set env vars: `SMTP_HOST=server.hostnextdns.com`, `SMTP_PORT=465`, `SMTP_SECURE=true`, `SMTP_USER/PASS/FROM_EMAIL/FROM_NAME`, `NEXT_PUBLIC_APP_URL`, Firebase public vars. Confirm `DEPLOYMENT_NOT_FOUND` is gone.
4. **Deploy Cloud Functions**: `cd functions && npm ci && npm run build && firebase deploy --only functions`.
5. **Lock down `system_settings` rules** (`allow read: if isAdmin()`) then deploy rules — the SMTP password is currently public.
6. **Fix domains**: either attach `engracedsmile.com` to Vercel or delete it from the app's endpoint list (`DeliveryViewModel.kt:4298,545`); update the canonical URL in `.env`, templates, and admin.
7. **Fix the OTP mismatch**: app must stop generating its own code, or the function must honor the passed code; wire `verifyEmailOtp` (currently 0 callers).
8. **Add `BACKEND_API_URL` to the CI mobile build env** (currently missing → CI APKs point at the dead domain); raise the 10 s callable timeout to ~25 s.
9. Admin toggles: turn **Email Verification Required** ON only after 1–8 work; save SMTP settings in Email Studio.

### 1.5 Your 5-minute live test (PowerShell)

```powershell
# 1. Deployment alive? (PASS = HTML 200, FAIL = X-Vercel-Error: DEPLOYMENT_NOT_FOUND)
Invoke-WebRequest https://engraceddispatchnew.vercel.app/ -UseBasicParsing

# 2. API endpoint (PASS = 200 {success:true} + email lands; 500 = SMTP_PASS missing; 404 = still dead)
Invoke-WebRequest -Uri https://engraceddispatchnew.vercel.app/api/email/verification `
  -Method POST -ContentType 'application/json' `
  -Body '{"email":"you@example.com","name":"You","userId":"x","otp":"123456"}' -UseBasicParsing

# 3. Cloud Function alive? (PASS = any 400 JSON; FAIL = Google "Page not found")
Invoke-WebRequest -Uri https://us-central1-engraceddispatch-ffba4.cloudfunctions.net/sendEmailOtp `
  -Method POST -ContentType 'application/json' -Body '{}' -UseBasicParsing
```
Then: check the inbox (< 60 s), run **Email Studio → Test SMTP / Send Test Email / DNS Check**, and on a freshly installed release APK tap **Send Verification Passcode** — success toast is `"A 6-digit verification code has been dispatched to..."`. As a control, **Forgot Password** should work even *today* (Firebase's own mail) — if that reaches your inbox, your mailbox is fine and only your pipeline is broken.

---

## PART 2 — PHONE/EMAIL UNIQUENESS + OTP CHANNEL CONFUSION

### 2.1 Verdict

**Uniqueness is enforced nowhere.** There is exactly **one** phone-uniqueness query in the entire repo (`FirebaseManager.kt:214-231`), and it can **never** succeed:
1. Firestore rules make a `users where("phone"==X)` query **permission-denied** for non-admin callers (`firestore.rules:58` allows only owner/admin/dispatcher reads) — during signup the caller is nobody yet.
2. The failure listener converts that denial into "not taken" (`FirebaseManager.kt:228-230`) → `isPhoneTaken = false` **forever**.
3. There is **no re-check at submit time** — the check only runs while typing (600 ms debounce), and is **skipped entirely for Google signups** (`AuthScreens.kt:849,1412,1416`).

**Email "taken"** appears only from Firebase Auth at submit (`auth/email-already-in-use`) or admin-side checks — never real-time. The live-typing email check (`fetchSignInMethodsForEmail`) is deprecated/unreliable and its flag gets force-reset on keystrokes (`AuthScreens.kt:840-841`).

**No SMS provider exists anywhere in the repo** (zero hits for Twilio/Termii/Africa's Talking/Vonage/Messagebird/etc.). **Phone OTP is impossible today**, and `isPhoneVerified` doesn't exist — phone is never verified, not once.

### 2.2 Why ~10 users share one phone number — ranked causes

| # | Cause | Evidence |
|---|---|---|
| **1** | **Hard-coded placeholder phone written into profiles** — when a user doc has no `phone`, the app adopts the literal `"+234 803 123 4567"` (which is also your published support hotline!) and **writes it back to Firestore** | `DeliveryViewModel.kt:3902-3904,3957,3970` → `saveUserProfileToFirestore` |
| **2** | **Device-local phone copied across accounts** — `local_phone` in SharedPreferences is replayed into whichever account signs in on that device (shared/tester phone = N accounts, one number) | `DeliveryViewModel.kt:2762,2771,2833,3867-3876,3800-3803` |
| **3** | **The uniqueness check is a silent no-op** (proof above) + no submit-time validation | `FirebaseManager.kt:214-231`, `AuthScreens.kt:1400-1451` |
| **4** | **Format variance hides duplicates** — `0803…`, `234803…`, `+234803…`, `+234 803 123 4567` all exist; no E.164 normalizer anywhere (`normalizePhone` = 0 hits) so exact-match queries can't see across formats | `AuthScreens.kt:1330-1357`, `FormatUtils.kt` (display-only) |
| **5** | **Web signup writes `phone: ""` for every user** — a whole cohort shares the empty-phone identity | `SignUp.jsx:33`, `SignIn.jsx:62` |
| **6** | **Admin create/edit phone is completely unchecked** | `AdminDashboard.tsx:3073,3176-3195` |
| **7** | **Phone is post-signup mutable with no guard** — rules protect `email` but **not `phone`**; profile edit has no check → users can converge onto one number after the fact | `firestore.rules:67-69`, `ProfileScreens.kt:4259` |

### 2.3 The OTP confusion — what's actually happening

| Flow | Channel | Transport | Works? |
|---|---|---|---|
| "ID Verification" passcode (the only in-app OTP) | **Email only** (hard-coded) | 3-path strategy — all broken (Part 1) | ❌ |
| Phone/SMS OTP | — | **No provider exists**; the admin toggles that promise it are dead controls | ❌ Impossible |
| "Phone Number Verification Required — Firebase OTP" toggle | — | Actually only runs a **regex format check**; no Firebase Phone Auth anywhere (0 hits) | ⚠️ Mislabelled |
| Web admin `phoneVerificationEnabled` toggle | — | Mobile reads a **different key** (`phoneVerificationRequired`) — the web toggle is **wired to nothing** | ❌ Dead control |
| `emailVerificationRequired` toggle | — | Write-only — no auth flow ever reads it | ❌ Dead control |
| Verification sheet UI | — | Shows your **phone number** next to a purely **email** verification — implying a phone verification that never happens | ⚠️ Confusing |
| "Check Link" vs "Resend Passcode" buttons | link vs email | Two half-flows side by side; the link flow can never succeed | ❌ |

**Why the system "doesn't know which to use":** there is **no channel-selection logic at all** — no toggle, no `channel` parameter, nothing. The UI just hard-codes email while displaying phone and labelling things "Firebase OTP," and the two admin toggles are dead or miswired. The confusion is real but it's *cosmetic wiring*, not an algorithm choosing wrong.

**Signup asymmetry:** Android requires **both** email + phone; Web requires **email only**; admin-created users require **email only** — three different identity standards.

### 2.4 What breaks because of duplicate phones (fraud paths)

1. **Rider calls the wrong human** — dial/SMS uses `receiverPhone` (`RiderScreens.kt:2354-2376`, `TrackingScreen.kt:2694-5540`).
2. **Handover OTP goes to the wrong party** — codes are pushed to the *account* while the physical recipient is identified only by phone.
3. **Referral/wallet farming, unbounded** — `redeemReferralCode` has no per-user dedupe and credits ₦3,000 + 300 pts (`DeliveryViewModel.kt:8420-8433`); shared identities = one person × N signups.
4. **OTP is self-assertable** — users can write their own `verification_otp` doc and "verify" without any email existing (`firestore.rules:76-78` owner-writable).
5. **Admin phone search becomes unusable** — client-side substring over 10 identical rows; edits overwrite the wrong record.
6. **Phone-based security claims are cosmetic** — no SMS, no `isPhoneVerified`, ever.

### 2.5 Fix order (identity + OTP)

**Immediate (days):**
1. **Kill placeholder phone writes** — `DeliveryViewModel.kt:3902-3904,3956-3957,3970,7916,7963,7965`; `TrackingScreen.kt:480,850`. Never persist empty/placeholder phones.
2. **Stop cross-account phone propagation** — `DeliveryViewModel.kt:2762,2771,3867-3876,3800-3803`.
3. **Make the existing check honest**: tri-state (unknown/exists/free) instead of failure→"free"; re-validate at submit; remove Google bypasses (`AuthScreens.kt:849,1412,1416`).
4. **Guard profile-edit and admin phone changes** with the same check (`ProfileScreens.kt:4259`, `AdminDashboard.tsx:3176-3195`).
5. **Web**: add phone field with E.164 validation; stop writing `phone: ""`.
6. **Delete/relabel dead toggles**: "Firebase OTP" → "format check"; align `phoneVerificationEnabled` → `phoneVerificationRequired`; wire or remove `emailVerificationRequired`.
7. **Fix OTP mismatch**: one source of truth for code generation (prefer server `verifyEmailOtp`, already correct but unused); remove the impossible "Check Link" branch.
8. **Rules**: add `phone` to the protected-update key list (`firestore.rules:67-69`).

**Structural (weeks):**
9. **Unique-identity ledger** — `identities/{e164Phone}` docs with `allow create: if !exists(...)` (the only uniqueness primitive Firestore rules support) + a `users` `onCreate` Cloud Function that transactionally claims identity and quarantines conflicts.
10. **Single OTP service** — `requestOtp({channel, purpose, destination})` / `verifyOtp(...)`: email via nodemailer (exists), `sms` returns "not available" until a provider (Termii/Africa's Talking) is licensed — **don't ship UI promises before then**. Server-generated, hashed, rate-limited codes; delete client-side compare.
11. **Phone normalization (E.164)** applied at every write site, canonical `+234`.
12. **Referral dedupe** server-side inside a transaction.

### 2.6 Find your ~10 duplicates now

**Instant (Firestore console):** run `phone == "+234 803 123 4567"`, then `phone == ""`, then `phone == "08000000000"` on `users` — these are the three literals the code writes. Re-run suspected real numbers in all 4 format variants.

**Complete:** run this from `D:\Smiles Dispatch\functions` (firebase-admin already installed), after `gcloud auth application-default login`:

```js
// scripts/find-duplicate-phones.js
const { initializeApp, applicationDefault } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
initializeApp({ credential: applicationDefault() });
const db = getFirestore();
const e164 = (raw) => {
  if (!raw) return '';
  const plus = raw.trim().startsWith('+');
  const d = raw.replace(/\D/g, '');
  if (plus && d.startsWith('234')) return '+' + d;
  if (d.startsWith('234')) return '+234' + d.slice(3);
  if (d.startsWith('0')) return '+234' + d.slice(1);
  return '+' + d;
};
(async () => {
  const groups = new Map();
  const snap = await db.collection('users').limit(50000).get();
  for (const d of snap.docs) {
    const u = d.data() || {};
    if (u.isDeleted) continue;
    const key = e164(u.phone || '');
    if (!key) continue;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push({ id: d.id, name: u.name, email: u.email, role: u.role, raw: u.phone });
  }
  const dups = [...groups.entries()].filter(([, v]) => v.length > 1)
                                    .sort((a, b) => b[1].length - a[1].length);
  console.log(`Duplicate phone numbers: ${dups.length}`);
  for (const [phone, users] of dups) {
    console.log(`\n${phone}  (${users.length} accounts)`);
    users.forEach(u => console.log(`   ${u.id}  ${u.role || '-'}  ${u.email || '-'}  ${u.name || '-'}  raw="${u.raw}"`));
  }
  process.exit(0);
})();
```

---

## PART 3 — CATASTROPHIC-RISK FLAGS ACROSS BOTH AREAS

| # | Risk | Severity | Evidence |
|---|---|---|---|
| 1 | Entire production deployment missing — the app currently talks to nothing | **Catastrophic** | live 404 `DEPLOYMENT_NOT_FOUND` |
| 2 | Cloud Functions never deployed; CI can't notice | **Catastrophic** | live 404; deploy steps silently skipped |
| 3 | SMTP password world-readable in Firestore + in git history + in shipped APKs | **Catastrophic** | `firestore.rules:378-381`, commits `8b50cb9`/`d0215a4` |
| 4 | Identity uniqueness unenforced → fraud/referral/wallet abuse | **Catastrophic** | Part 2 |
| 5 | OTP self-assertable (client can write its own code) | **High** | `firestore.rules:76-78` |
| 6 | OTP code generated client-side vs server ignoring it — will break the moment paths restore | **High** | `DeliveryViewModel.kt:4259` vs `functions/src/index.ts:913-923` |
| 7 | User-facing error actively misleads ("check your internet") while hiding the real server error | **High** | `DeliveryViewModel.kt:4321,4336` |
| 8 | Admin DNS check fabricates success on failure | **High** | `EmailStudioTab.tsx:272-277` |
| 9 | Dead admin toggles create false confidence (phone/email verification "enabled" but inert) | **High** | `AdminDashboard.tsx:8080`, `AIDispatchManagerScreen.kt:1571` |
| 10 | 24 uncommitted files — local fixes invisible to CI/deploy | **High** | `git status` |
| 11 | Verification link flow is a static page (never verifies) | **Medium** | `src/app/verified/page.tsx` |
| 12 | All CTA links inside emails point at dead hosts | **Medium** | `emailTemplates.ts:78` both copies |

---

**Report ends.** No source files were modified; live probes were read-only HTTP/DNS lookups. Next step: confirm whether you want the fixes implemented (starting with Part 1 steps 1–4: commit, rotate password, restore handlers, redeploy) — deployment credentials and Vercel/Firebase console actions must be done by the project owner.

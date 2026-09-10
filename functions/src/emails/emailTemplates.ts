/**
 * ESDispatch Luxury Email Design System
 * 
 * Strict Brand Lock:
 * - Brand Name: "ESDISPATCH"
 * - Official Slogan: "PREMIUM LOGISTICS & DISPATCH"
 * - Palette: Obsidian (#0B0B0E, #14141A, #1C1C24), Brand Gold (#FFB800), Crisp White (#FFFFFF), Text Muted (#8E8E9F)
 * - Contrast Rules: NO white on gold (use Obsidian on Gold), NO gold on white.
 * - Zero emoji policy (except authorized '👋' greeting).
 */

interface BaseEmailWrapperOptions {
  title: string;
  preheader: string;
  contentHtml: string;
}

/**
 * Wraps content in the master responsive luxury layout.
 */
function wrapInLuxuryTemplate({ title, preheader, contentHtml }: BaseEmailWrapperOptions): string {
  const currentYear = new Date().getFullYear();
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>${title}</title>
  <style>
    /* Reset */
    body, table, td, a { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }
    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
    img { -ms-interpolation-mode: bicubic; border: 0; outline: none; text-decoration: none; }
    body { margin: 0; padding: 0; width: 100% !important; background-color: #070709; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    @media only screen and (max-width: 600px) {
      .email-container { width: 100% !important; border-radius: 0 !important; }
      .content-padding { padding: 28px 20px !important; }
      .otp-digit { font-size: 28px !important; letter-spacing: 6px !important; }
    }
  </style>
</head>
<body style="margin: 0; padding: 0; background-color: #070709;">
  <!-- Preheader text (invisible preview) -->
  <div style="display: none; max-height: 0px; overflow: hidden; font-size: 1px; line-height: 1px; color: #070709; mso-hide: all;">
    ${preheader}
  </div>

  <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #070709; padding: 36px 12px;">
    <tr>
      <td align="center">
        <!-- Main Card Container -->
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="email-container" style="max-width: 600px; background-color: #121217; border-radius: 18px; border: 1px solid #262632; overflow: hidden; box-shadow: 0 12px 40px rgba(0,0,0,0.6);">
          
          <!-- Brand Header -->
          <tr>
            <td align="center" style="background: linear-gradient(180deg, #181822 0%, #121217 100%); padding: 36px 24px 28px 24px; border-bottom: 1px solid #232330;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center">
                    <!-- Gold Brand Square Container -->
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                      <tr>
                        <td align="center" style="background-color: #FFB800; width: 44px; height: 44px; border-radius: 12px; font-weight: 900; font-size: 20px; color: #0B0B0E; letter-spacing: -0.5px; line-height: 44px;">
                          ES
                        </td>
                        <td style="padding-left: 14px; text-align: left;">
                          <div style="color: #FFFFFF; font-size: 20px; font-weight: 800; letter-spacing: 3px; line-height: 1.1;">ESDISPATCH</div>
                          <div style="color: #FFB800; font-size: 9px; font-weight: 700; letter-spacing: 2.2px; margin-top: 3px;">PREMIUM LOGISTICS &amp; DISPATCH</div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Content Body -->
          <tr>
            <td class="content-padding" style="padding: 36px 36px 28px 36px;">
              ${contentHtml}
            </td>
          </tr>

          <!-- Master Footer -->
          <tr>
            <td style="background-color: #0E0E13; padding: 28px 32px; border-top: 1px solid #1E1E28; text-align: center;">
              <p style="margin: 0 0 10px 0; color: #8C8CA1; font-size: 11px; line-height: 1.6; letter-spacing: 0.2px;">
                Thank you for choosing ESDispatch — Premium Logistics &amp; Dispatch.<br>
                For questions regarding your delivery or account, contact <a href="mailto:support@esdispatch.com" style="color: #FFB800; text-decoration: none; font-weight: 600;">support@esdispatch.com</a> or our dispatch support desk.
              </p>
              <div style="height: 1px; background-color: #1A1A24; margin: 16px 0;"></div>
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <td align="left" style="color: #58586E; font-size: 10px; font-weight: 500;">
                    &copy; ${currentYear} ESDISPATCH. All rights reserved.
                  </td>
                  <td align="right" style="color: #58586E; font-size: 10px; font-weight: 500;">
                    Encrypted SSL Delivery
                  </td>
                </tr>
              </table>
            </td>
          </tr>

        </table>
      </td>
    </tr>
  </table>
</body>
</html>`;
}

/**
 * 1. Sign-Up Verification OTP Template
 */
export function renderSignUpOtpEmail(params: { name: string; otp: string; expiryMinutes?: number }): string {
  const expiry = params.expiryMinutes || 10;
  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: rgba(255, 184, 0, 0.12); color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid rgba(255, 184, 0, 0.25);">
        ACCOUNT VERIFICATION
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">Verify Your Email Address</h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        Hello ${params.name || 'Valued Client'}, welcome to ESDispatch. Enter the one-time authentication code below to activate your premium logistics account.
      </p>
    </div>

    <!-- OTP Display Box -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 28px 0; background-color: #171720; border-radius: 14px; border: 1px solid #2B2B3A;">
      <tr>
        <td align="center" style="padding: 24px 16px;">
          <div style="color: #7E7E94; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 10px;">
            ONE-TIME PASSCODE (OTP)
          </div>
          <div class="otp-digit" style="color: #FFB800; font-size: 38px; font-weight: 900; letter-spacing: 10px; font-family: 'Courier New', Courier, monospace; text-shadow: 0 0 20px rgba(255, 184, 0, 0.25);">
            ${params.otp}
          </div>
          <div style="color: #8C8CA0; font-size: 12px; margin-top: 12px;">
            Valid for the next <strong style="color: #FFFFFF;">${expiry} minutes</strong>. Do not disclose this code.
          </div>
        </td>
      </tr>
    </table>

    <div style="background-color: #15151E; border-left: 3px solid #FFB800; padding: 14px 16px; border-radius: 6px; margin-top: 24px;">
      <p style="margin: 0; color: #8F8FA4; font-size: 12px; line-height: 1.5;">
        <strong style="color: #FFFFFF;">Security Notice:</strong> ESDispatch officers will never ask for your authentication passcode. If you did not initiate this registration, please disregard this email.
      </p>
    </div>
  `;

  return wrapInLuxuryTemplate({
    title: 'Verify Your ESDispatch Account',
    preheader: `Your ESDispatch verification code is ${params.otp}. Valid for ${expiry} minutes.`,
    contentHtml: content,
  });
}

/**
 * 2. Password Reset OTP Template
 */
export function renderPasswordResetOtpEmail(params: { name: string; otp: string; expiryMinutes?: number }): string {
  const expiry = params.expiryMinutes || 10;
  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: rgba(255, 75, 75, 0.12); color: #FF6666; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid rgba(255, 75, 75, 0.25);">
        SECURITY ALERT
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">Password Reset Request</h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        Hello ${params.name || 'User'}, we received a request to reset your ESDispatch account password. Use the authorization code below to proceed with the reset.
      </p>
    </div>

    <!-- OTP Display Box -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 28px 0; background-color: #171720; border-radius: 14px; border: 1px solid #2B2B3A;">
      <tr>
        <td align="center" style="padding: 24px 16px;">
          <div style="color: #7E7E94; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 10px;">
            RESET AUTHORIZATION CODE
          </div>
          <div class="otp-digit" style="color: #FFB800; font-size: 38px; font-weight: 900; letter-spacing: 10px; font-family: 'Courier New', Courier, monospace;">
            ${params.otp}
          </div>
          <div style="color: #8C8CA0; font-size: 12px; margin-top: 12px;">
            Expires in <strong style="color: #FFFFFF;">${expiry} minutes</strong>.
          </div>
        </td>
      </tr>
    </table>

    <div style="background-color: #181517; border-left: 3px solid #FF5555; padding: 14px 16px; border-radius: 6px; margin-top: 24px;">
      <p style="margin: 0; color: #A08C8C; font-size: 12px; line-height: 1.5;">
        <strong style="color: #FFFFFF;">Didn't request this?</strong> If you did not request a password reset, your account credentials may be compromised. Change your password immediately or reach out to our security desk.
      </p>
    </div>
  `;

  return wrapInLuxuryTemplate({
    title: 'ESDispatch Password Reset Request',
    preheader: `Your password reset code is ${params.otp}. Expires in ${expiry} minutes.`,
    contentHtml: content,
  });
}

/**
 * 3. Two-Factor Authentication (2FA) Template
 */
export function renderTwoFactorOtpEmail(params: { name: string; otp: string; expiryMinutes?: number; ipOrDevice?: string }): string {
  const expiry = params.expiryMinutes || 5;
  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: rgba(255, 184, 0, 0.12); color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid rgba(255, 184, 0, 0.25);">
        TWO-FACTOR LOGIN
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">Second-Factor Challenge</h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        Hello ${params.name || 'User'}, a sign-in attempt was detected${params.ipOrDevice ? ` from <strong>${params.ipOrDevice}</strong>` : ''}. Enter the code below to complete your authentication.
      </p>
    </div>

    <!-- OTP Display Box -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 28px 0; background-color: #171720; border-radius: 14px; border: 1px solid #2B2B3A;">
      <tr>
        <td align="center" style="padding: 24px 16px;">
          <div style="color: #7E7E94; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 10px;">
            2FA LOGIN PASSCODE
          </div>
          <div class="otp-digit" style="color: #FFB800; font-size: 38px; font-weight: 900; letter-spacing: 10px; font-family: 'Courier New', Courier, monospace;">
            ${params.otp}
          </div>
          <div style="color: #8C8CA0; font-size: 12px; margin-top: 12px;">
            Valid for <strong style="color: #FFFFFF;">${expiry} minutes</strong>.
          </div>
        </td>
      </tr>
    </table>
  `;

  return wrapInLuxuryTemplate({
    title: 'ESDispatch 2FA Login Code',
    preheader: `Your two-factor login code is ${params.otp}.`,
    contentHtml: content,
  });
}

/**
 * 4. PIN Reset OTP Template
 */
export function renderPinResetOtpEmail(params: { name: string; otp: string; expiryMinutes?: number }): string {
  const expiry = params.expiryMinutes || 10;
  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: rgba(255, 184, 0, 0.12); color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid rgba(255, 184, 0, 0.25);">
        WALLET SECURITY
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">Wallet PIN Reset Authorization</h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        Hello ${params.name || 'User'}, you requested to change or reset your transaction security PIN. Use the verification code below to authorize this critical security update.
      </p>
    </div>

    <!-- OTP Display Box -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 28px 0; background-color: #171720; border-radius: 14px; border: 1px solid #2B2B3A;">
      <tr>
        <td align="center" style="padding: 24px 16px;">
          <div style="color: #7E7E94; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 10px;">
            PIN CHANGE CODE
          </div>
          <div class="otp-digit" style="color: #FFB800; font-size: 38px; font-weight: 900; letter-spacing: 10px; font-family: 'Courier New', Courier, monospace;">
            ${params.otp}
          </div>
          <div style="color: #8C8CA0; font-size: 12px; margin-top: 12px;">
            Expires in <strong style="color: #FFFFFF;">${expiry} minutes</strong>.
          </div>
        </td>
      </tr>
    </table>
  `;

  return wrapInLuxuryTemplate({
    title: 'Authorize Wallet PIN Reset',
    preheader: `Your PIN reset authorization code is ${params.otp}.`,
    contentHtml: content,
  });
}

/**
 * 5. Delivery Handover OTP Template (Recipient Proof of Delivery)
 */
export function renderDeliveryHandoverOtpEmail(params: {
  trackingNumber: string;
  recipientName: string;
  pickupAddress: string;
  dropoffAddress: string;
  handoverOtp: string;
}): string {
  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: rgba(0, 200, 115, 0.12); color: #00E676; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid rgba(0, 200, 115, 0.25);">
        SHIPMENT IN TRANSIT
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">Delivery Handover Code</h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        Hello ${params.recipientName}, your ESDispatch shipment is arriving. Provide this confidential handover code to the courier upon arrival to release your parcel.
      </p>
    </div>

    <!-- Handover Code Display Box -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 24px 0; background: linear-gradient(180deg, #1A1A24 0%, #15151E 100%); border-radius: 14px; border: 1px solid #FFB800;">
      <tr>
        <td align="center" style="padding: 24px 16px;">
          <div style="color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 8px;">
            OFFICIAL HANDOVER PASSCODE
          </div>
          <div class="otp-digit" style="color: #FFFFFF; font-size: 42px; font-weight: 900; letter-spacing: 12px; font-family: 'Courier New', Courier, monospace;">
            ${params.handoverOtp}
          </div>
          <div style="color: #8C8CA0; font-size: 12px; margin-top: 10px;">
            Disclose ONLY in person when you receive and inspect your package.
          </div>
        </td>
      </tr>
    </table>

    <!-- Shipment Details Table -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #15151E; border-radius: 12px; border: 1px solid #232330; padding: 18px 20px; margin-top: 20px;">
      <tr>
        <td style="padding: 6px 0; color: #76768B; font-size: 12px;">Tracking Number</td>
        <td style="padding: 6px 0; color: #FFFFFF; font-size: 13px; font-weight: 700; text-align: right; font-family: monospace;">${params.trackingNumber}</td>
      </tr>
      <tr>
        <td style="padding: 6px 0; color: #76768B; font-size: 12px;">Pickup Origin</td>
        <td style="padding: 6px 0; color: #CCCCCC; font-size: 12px; text-align: right;">${params.pickupAddress}</td>
      </tr>
      <tr>
        <td style="padding: 6px 0; color: #76768B; font-size: 12px;">Dropoff Destination</td>
        <td style="padding: 6px 0; color: #CCCCCC; font-size: 12px; text-align: right;">${params.dropoffAddress}</td>
      </tr>
    </table>
  `;

  return wrapInLuxuryTemplate({
    title: `Delivery Handover Code for #${params.trackingNumber}`,
    preheader: `Your ESDispatch handover code is ${params.handoverOtp} for shipment #${params.trackingNumber}.`,
    contentHtml: content,
  });
}

/**
 * 6. Delivery Official Invoice & Proof of Booking
 */
export function renderDeliveryInvoiceEmail(params: {
  trackingNumber: string;
  recipientName: string;
  senderName: string;
  serviceType: string;
  amountPaid: string;
  date: string;
  paymentMethod: string;
  breakdown: { label: string; amount: string }[];
}): string {
  const breakdownRows = params.breakdown
    .map(
      (b) => `
      <tr>
        <td style="padding: 10px 0; color: #9A9AB0; font-size: 13px; border-bottom: 1px solid #1E1E28;">${b.label}</td>
        <td style="padding: 10px 0; color: #FFFFFF; font-size: 13px; font-weight: 600; text-align: right; border-bottom: 1px solid #1E1E28;">${b.amount}</td>
      </tr>`
    )
    .join('');

  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: rgba(255, 184, 0, 0.12); color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid rgba(255, 184, 0, 0.25);">
        PAYMENT CONFIRMED
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">Official Delivery Receipt</h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        Thank you for booking with ESDispatch. Your payment has been received and confirmed.
      </p>
    </div>

    <!-- Amount Hero Card -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #171720; border-radius: 14px; border: 1px solid #2B2B3A; margin: 20px 0;">
      <tr>
        <td align="center" style="padding: 24px;">
          <div style="color: #7E7E94; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase;">AMOUNT PAID</div>
          <div style="color: #FFB800; font-size: 36px; font-weight: 900; margin: 8px 0;">${params.amountPaid}</div>
          <div style="color: #8C8CA0; font-size: 12px;">Method: <strong style="color: #FFFFFF;">${params.paymentMethod}</strong> &bull; ${params.date}</div>
        </td>
      </tr>
    </table>

    <!-- Line Item Breakdown -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-top: 10px;">
      <tr>
        <td colspan="2" style="color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase; padding-bottom: 8px;">
          CHARGES BREAKDOWN
        </td>
      </tr>
      ${breakdownRows}
      <tr>
        <td style="padding: 14px 0 6px 0; color: #FFFFFF; font-size: 14px; font-weight: 700;">Total Settlement</td>
        <td style="padding: 14px 0 6px 0; color: #FFB800; font-size: 16px; font-weight: 800; text-align: right;">${params.amountPaid}</td>
      </tr>
    </table>

    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-top: 24px; background-color: #13131A; border-radius: 10px; padding: 14px 16px;">
      <tr>
        <td style="color: #78788D; font-size: 12px;">Tracking Reference:</td>
        <td style="color: #FFFFFF; font-size: 12px; font-weight: 700; text-align: right; font-family: monospace;">${params.trackingNumber}</td>
      </tr>
      <tr>
        <td style="color: #78788D; font-size: 12px; padding-top: 6px;">Service Class:</td>
        <td style="color: #FFFFFF; font-size: 12px; font-weight: 600; text-align: right; padding-top: 6px;">${params.serviceType}</td>
      </tr>
    </table>
  `;

  return wrapInLuxuryTemplate({
    title: `Receipt for Shipment #${params.trackingNumber}`,
    preheader: `Payment confirmed for shipment #${params.trackingNumber}. Total: ${params.amountPaid}.`,
    contentHtml: content,
  });
}

/**
 * 7. Wallet Transaction Alert (Credit/Debit)
 */
export function renderWalletTransactionEmail(params: {
  name: string;
  transactionType: 'CREDIT' | 'DEBIT';
  amount: string;
  newBalance: string;
  reference: string;
  date: string;
  description: string;
}): string {
  const isCredit = params.transactionType === 'CREDIT';
  const badgeColor = isCredit ? '#00E676' : '#FFB800';
  const badgeBg = isCredit ? 'rgba(0, 230, 118, 0.12)' : 'rgba(255, 184, 0, 0.12)';
  const badgeBorder = isCredit ? 'rgba(0, 230, 118, 0.25)' : 'rgba(255, 184, 0, 0.25)';

  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: ${badgeBg}; color: ${badgeColor}; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid ${badgeBorder};">
        WALLET ${params.transactionType}
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">
        ${isCredit ? 'Funds Credited to Wallet' : 'Wallet Debit Alert'}
      </h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        Hello ${params.name}, your ESDispatch wallet balance has been updated.
      </p>
    </div>

    <!-- Amount Display -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #171720; border-radius: 14px; border: 1px solid #2B2B3A; margin: 20px 0;">
      <tr>
        <td align="center" style="padding: 24px;">
          <div style="color: #7E7E94; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase;">
            TRANSACTION AMOUNT
          </div>
          <div style="color: ${badgeColor}; font-size: 36px; font-weight: 900; margin: 8px 0;">
            ${isCredit ? '+' : '-'}${params.amount}
          </div>
          <div style="color: #8C8CA0; font-size: 12px;">
            Available Balance: <strong style="color: #FFFFFF;">${params.newBalance}</strong>
          </div>
        </td>
      </tr>
    </table>

    <!-- Meta Details -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #15151E; border-radius: 12px; border: 1px solid #232330; padding: 18px 20px;">
      <tr>
        <td style="padding: 6px 0; color: #76768B; font-size: 12px;">Description</td>
        <td style="padding: 6px 0; color: #FFFFFF; font-size: 12px; font-weight: 600; text-align: right;">${params.description}</td>
      </tr>
      <tr>
        <td style="padding: 6px 0; color: #76768B; font-size: 12px;">Reference</td>
        <td style="padding: 6px 0; color: #CCCCCC; font-size: 12px; text-align: right; font-family: monospace;">${params.reference}</td>
      </tr>
      <tr>
        <td style="padding: 6px 0; color: #76768B; font-size: 12px;">Timestamp</td>
        <td style="padding: 6px 0; color: #CCCCCC; font-size: 12px; text-align: right;">${params.date}</td>
      </tr>
    </table>
  `;

  return wrapInLuxuryTemplate({
    title: `ESDispatch Wallet ${params.transactionType}: ${params.amount}`,
    preheader: `Wallet ${params.transactionType.toLowerCase()} of ${params.amount}. New balance: ${params.newBalance}.`,
    contentHtml: content,
  });
}

/**
 * 8. Driver / Vendor Welcome & Onboarding Letter
 */
export function renderPartnerWelcomeEmail(params: {
  name: string;
  role: 'rider' | 'vendor' | 'customer';
  portalUrl?: string;
}): string {
  const roleTitle =
    params.role === 'rider'
      ? 'Fleet Courier Partner'
      : params.role === 'vendor'
      ? 'Verified Merchant Partner'
      : 'Premium Logistics Client';

  const content = `
    <div style="text-align: center; margin-bottom: 24px;">
      <span style="display: inline-block; background-color: rgba(255, 184, 0, 0.12); color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; padding: 6px 14px; border-radius: 100px; border: 1px solid rgba(255, 184, 0, 0.25);">
        WELCOME TO THE FLEET
      </span>
      <h1 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 16px 0 8px 0;">Welcome, ${params.name} &#x1F44B;</h1>
      <p style="color: #9C9CB2; font-size: 14px; line-height: 1.6; margin: 0;">
        You have officially onboarded as an authorized <strong style="color: #FFB800;">${roleTitle}</strong> with ESDispatch.
      </p>
    </div>

    <!-- Info Block -->
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #171720; border-radius: 14px; border: 1px solid #2B2B3A; padding: 24px; margin: 24px 0;">
      <tr>
        <td>
          <div style="color: #FFB800; font-size: 13px; font-weight: 700; margin-bottom: 8px;">
            The Gold Standard in Precision Logistics
          </div>
          <p style="margin: 0; color: #9A9AB2; font-size: 13px; line-height: 1.6;">
            With ESDispatch, you experience fast fleet dispatch, transparent package tracking, secure settlements, and responsive dispatch support.
          </p>
        </td>
      </tr>
    </table>

    ${
      params.portalUrl
        ? `
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 28px 0;">
      <tr>
        <td align="center">
          <a href="${params.portalUrl}" target="_blank" style="display: inline-block; background-color: #FFB800; color: #0B0B0E; font-size: 13px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase; text-decoration: none; padding: 14px 32px; border-radius: 10px;">
            ACCESS PARTNER CONSOLE
          </a>
        </td>
      </tr>
    </table>`
        : ''
    }
  `;

  return wrapInLuxuryTemplate({
    title: `Welcome to ESDISPATCH, ${params.name}!`,
    preheader: `Welcome to ESDispatch as our ${roleTitle}. Start delivering excellence today.`,
    contentHtml: content,
  });
}

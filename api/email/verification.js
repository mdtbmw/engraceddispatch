const nodemailer = require("nodemailer");

function extractPlainText(html) {
  if (!html) return "";
  return html
    .replace(/<style[^>]*>[\s\S]*?<\/style>/gi, "")
    .replace(/<script[^>]*>[\s\S]*?<\/script>/gi, "")
    .replace(/<br\s*[\/]?>/gi, "\n")
    .replace(/<\/p>/gi, "\n\n")
    .replace(/<\/h[1-6]>/gi, "\n\n")
    .replace(/<\/div>/gi, "\n")
    .replace(/<\/li>/gi, "\n")
    .replace(/<li>/gi, "• ")
    .replace(/<\/tr>/gi, "\n")
    .replace(/<td[^>]*>/gi, " ")
    .replace(/<a[^>]*href=["']([^"']+)["'][^>]*>([\s\S]*?)<\/a>/gi, "$2 ($1)")
    .replace(/<[^>]+>/g, "")
    .replace(/&bull;/g, "•")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&nbsp;/g, " ")
    .replace(/[ \t]+/g, " ")
    .replace(/\n\s+\n/g, "\n\n")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

function buildLuxuryVerificationEmailHtml({ name, otp, verificationLink, expiryMinutes = 15, currentDomain = "engracedsmile.com" }) {
  const currentYear = new Date().getFullYear();
  const directLink = verificationLink || `https://${currentDomain}/verified?email=${encodeURIComponent(name || "")}&otp=${otp}`;

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>Verify Your ESDispatch Account (${otp})</title>
  <style>
    body, table, td, a { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }
    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
    img { -ms-interpolation-mode: bicubic; border: 0; outline: none; text-decoration: none; }
    body { margin: 0; padding: 0; width: 100% !important; background-color: #F3F4F6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    @media only screen and (max-width: 600px) {
      .outer-wrapper { padding: 10px 4px !important; }
      .outer-frame { width: 100% !important; border-radius: 8px !important; }
      .header-padding { padding: 20px 16px !important; }
      .body-padding { padding: 20px 16px 12px 16px !important; }
      .headline-text { font-size: 20px !important; line-height: 26px !important; }
      .passcode-display { font-size: 32px !important; letter-spacing: 6px !important; }
      .three-cards-table, .three-cards-table tbody, .three-cards-table tr { display: block !important; width: 100% !important; }
      .three-card-col { display: block !important; width: 100% !important; padding: 0 0 10px 0 !important; box-sizing: border-box !important; }
      .cta-button { width: 100% !important; display: block !important; text-align: center !important; }
    }
  </style>
</head>
<body style="margin: 0; padding: 0; background-color: #F3F4F6;">
  <!-- Preheader -->
  <div style="display: none; font-size: 1px; color: #F3F4F6; line-height: 1px; max-height: 0px; max-width: 0px; opacity: 0; overflow: hidden;">
    Your confidential ESDispatch verification passcode is ${otp}. Valid for ${expiryMinutes} minutes.
  </div>

  <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="outer-wrapper" style="background-color: #F3F4F6; padding: 32px 12px;">
    <tr>
      <td align="center">
        <!-- Main Luxury Card Container -->
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="outer-frame" style="max-width: 640px; background-color: #FFFFFF; border-radius: 14px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.08); border: 1px solid #E5E7EB;">
          
          <!-- Top Obsidian & Gold Brand Bar -->
          <tr>
            <td style="background-color: #050505; padding: 24px 28px; border-bottom: 3px solid #FFB800;" class="header-padding">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <td align="left" vertical-align="middle">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                      <tr>
                        <td style="background-color: #FFB800; border-radius: 8px; width: 38px; height: 38px; text-align: center; vertical-align: middle;">
                          <span style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 19px; font-weight: 900; color: #050505; letter-spacing: -0.5px;">ES</span>
                        </td>
                        <td style="padding-left: 12px; vertical-align: middle;">
                          <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 18px; font-weight: 900; letter-spacing: 2px; color: #FFFFFF; text-transform: uppercase;">
                            ESDISPATCH
                          </div>
                          <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 9px; font-weight: 700; letter-spacing: 1.5px; color: #FFB800; text-transform: uppercase; padding-top: 2px;">
                            PREMIUM LOGISTICS & DISPATCH
                          </div>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td align="right" vertical-align="middle">
                    <span style="display: inline-block; background-color: rgba(255, 184, 0, 0.15); border: 1px solid #FFB800; border-radius: 6px; padding: 5px 10px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; font-weight: 800; color: #FFB800; letter-spacing: 1px;">
                      VERIFIED ACCESS
                    </span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Category Banner -->
          <tr>
            <td style="background-color: #FFFDF5; border-bottom: 1px solid #FEF3C7; padding: 10px 28px;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <td style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; font-weight: 800; color: #92400E; letter-spacing: 1.5px; text-transform: uppercase;">
                    IDENTITY VERIFICATION • SINGLE USE PASSCODE
                  </td>
                  <td align="right" style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; font-weight: 700; color: #B45309;">
                    ⏱ ${expiryMinutes} MIN VALIDITY
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Main Body Content -->
          <tr>
            <td style="padding: 28px 28px 16px 28px;" class="body-padding">
              <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 22px; font-weight: 800; color: #111827; line-height: 28px; margin-bottom: 14px;" class="headline-text">
                Welcome to ESDispatch, ${name || "Client"}
              </div>

              <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 14px; line-height: 22px; color: #4B5563; margin-bottom: 20px;">
                <p style="margin: 0 0 10px 0;">
                  Welcome to <strong>ESDispatch</strong> — the gold standard in express logistics and courier dispatch across Edo State and nationwide.
                </p>
                <p style="margin: 0;">
                  To authenticate your client profile and activate VIP dispatch features, please enter the confidential one-time verification passcode below into your mobile app or click the verification button:
                </p>
              </div>

              <!-- Passcode Display Card -->
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #F9FAFB; border: 1.5px solid #FFB800; border-radius: 12px; margin-bottom: 24px; box-shadow: 0 4px 14px rgba(255, 184, 0, 0.12);">
                <tr>
                  <td style="padding: 24px 20px; text-align: center;">
                    <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; font-weight: 800; color: #92400E; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 8px;">
                      YOUR ONE-TIME PASSCODE
                    </div>
                    <div class="passcode-display" style="font-family: 'Courier New', Courier, monospace; font-size: 42px; font-weight: 900; letter-spacing: 10px; color: #050505; line-height: 1; padding: 6px 0;">
                      ${otp}
                    </div>
                    <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; color: #6B7280; font-weight: 600; margin-top: 8px;">
                      Expires in ${expiryMinutes} minutes &bull; Confidential single-use code
                    </div>
                  </td>
                </tr>
              </table>

              <!-- Action CTA Button -->
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 24px;">
                <tr>
                  <td align="center">
                    <a href="${directLink}" target="_blank" rel="noopener" class="cta-button" style="display: inline-block; background-color: #FFB800; color: #050505; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 14px; font-weight: 900; letter-spacing: 1.5px; text-transform: uppercase; text-decoration: none; padding: 14px 34px; border-radius: 8px; box-shadow: 0 4px 12px rgba(255, 184, 0, 0.35);">
                      AUTHENTICATE ACCOUNT
                    </a>
                  </td>
                </tr>
              </table>

              <!-- Security Notice Callout -->
              <div style="background-color: #FEF3C7; border-left: 4px solid #F59E0B; padding: 12px 16px; border-radius: 6px; margin-bottom: 24px;">
                <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; line-height: 18px; color: #92400E;">
                  <strong>Security Advisory:</strong> ESDispatch dispatchers, customer care agents, and riders will never ask for your verification passcode. Never share this code with anyone.
                </div>
              </div>

              <!-- 3 Service Pillars -->
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="three-cards-table" style="margin-bottom: 12px;">
                <tr>
                  <td width="32%" class="three-card-col" style="padding-right: 8px; vertical-align: top;">
                    <div style="background-color: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 8px; padding: 14px 10px; text-align: center;">
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; font-weight: 800; color: #B45309; letter-spacing: 1px;">SECURITY</div>
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; font-weight: 700; color: #111827; margin: 4px 0 2px 0;">256-Bit SSL</div>
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; color: #6B7280; line-height: 14px;">End-to-end encrypted dispatch</div>
                    </div>
                  </td>
                  <td width="32%" class="three-card-col" style="padding-right: 8px; vertical-align: top;">
                    <div style="background-color: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 8px; padding: 14px 10px; text-align: center;">
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; font-weight: 800; color: #B45309; letter-spacing: 1px;">DISPATCH</div>
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; font-weight: 700; color: #111827; margin: 4px 0 2px 0;">Benin City</div>
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; color: #6B7280; line-height: 14px;">Active verified fleet</div>
                    </div>
                  </td>
                  <td width="32%" class="three-card-col" style="vertical-align: top;">
                    <div style="background-color: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 8px; padding: 14px 10px; text-align: center;">
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; font-weight: 800; color: #B45309; letter-spacing: 1px;">ESCROW</div>
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; font-weight: 700; color: #111827; margin: 4px 0 2px 0;">Protected</div>
                      <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; color: #6B7280; line-height: 14px;">Safe payment custody</div>
                    </div>
                  </td>
                </tr>
              </table>

            </td>
          </tr>

          <!-- Footer -->
          <tr>
            <td style="background-color: #050505; padding: 24px 28px; border-top: 1px solid #1F2937;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <td align="center" style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; color: #9CA3AF; line-height: 18px;">
                    <div style="font-weight: 700; color: #FFFFFF; margin-bottom: 4px;">
                      ESDISPATCH PREMIUM LOGISTICS & DISPATCH
                    </div>
                    <div>
                      Headquarters: Benin City, Edo State, Nigeria &bull; Support: support@${currentDomain}
                    </div>
                    <div style="margin-top: 10px; color: #6B7280; font-size: 10px;">
                      &copy; ${currentYear} ESDispatch. All rights reserved. &bull; 
                      <a href="https://${currentDomain}/privacy" style="color: #FFB800; text-decoration: none;">Privacy Policy</a> &bull; 
                      <a href="https://${currentDomain}/terms" style="color: #FFB800; text-decoration: none;">Terms of Service</a>
                    </div>
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

const rateLimitMap = new Map();
function isRateLimited(key, maxLimit = 3, windowMs = 5 * 60 * 1000) {
  const now = Date.now();
  const timestamps = (rateLimitMap.get(key) || []).filter((ts) => now - ts < windowMs);
  if (timestamps.length >= maxLimit) {
    return true;
  }
  timestamps.push(now);
  rateLimitMap.set(key, timestamps);
  return false;
}

module.exports = async function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(200).end();
  }

  if (req.method !== "POST") {
    return res.status(405).json({ success: false, error: "Method not allowed" });
  }

  try {
    const body = typeof req.body === "string" ? JSON.parse(req.body || "{}") : (req.body || {});
    const { email, name, userId, otp: providedOtp, verificationLink } = body;

    const recipientEmail = (email || "").trim().toLowerCase();
    if (!recipientEmail || !recipientEmail.includes("@")) {
      return res.status(400).json({ success: false, error: "A valid recipient email address is required." });
    }

    const clientIp = req.headers["x-forwarded-for"] || req.socket?.remoteAddress || "unknown";
    if (isRateLimited(`ip:${clientIp}`, 6, 5 * 60 * 1000) || isRateLimited(`email:${recipientEmail}`, 3, 5 * 60 * 1000)) {
      return res.status(429).json({ success: false, error: "Too many verification requests. Please wait a few minutes." });
    }

    const recipientName = (name || "Valued Client").trim();
    const otp = (providedOtp || "").toString().trim() || Math.floor(100000 + Math.random() * 900000).toString();

    const host = (process.env.SMTP_HOST || "server.hostnextdns.com").trim();
    const port = Number(process.env.SMTP_PORT || 465);
    const secure = process.env.SMTP_SECURE === "false" ? false : port === 465;
    const user = (process.env.SMTP_USER || process.env.SMTP_EMAIL || "noreply@engracedsmile.com").trim();
    const pass = (process.env.SMTP_PASS || process.env.SMTP_PASSWORD || "").trim();
    const fromEmail = (process.env.SMTP_FROM_EMAIL || "noreply@engracedsmile.com").trim();
    const fromName = (process.env.SMTP_FROM_NAME || "ESDispatch Logistics").trim();

    if (!pass) {
      console.error("[Verification Mailer Error] SMTP_PASS or SMTP_PASSWORD is not configured in environment variables.");
      return res.status(500).json({ success: false, error: "SMTP server credentials are not configured on the server." });
    }

    const domain = fromEmail.includes("@") ? fromEmail.split("@")[1] : "engracedsmile.com";
    const randomHex = Math.random().toString(36).substring(2, 10);
    const subject = "Your ESDispatch Verification Code";

    const htmlContent = buildLuxuryVerificationEmailHtml({
      name: recipientName,
      otp,
      verificationLink,
      expiryMinutes: 15,
      currentDomain: domain,
    });

    const plainText = extractPlainText(htmlContent);

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: {
        rejectUnauthorized: false,
      },
      connectionTimeout: 15000,
      greetingTimeout: 15000,
      socketTimeout: 20000,
    });

    const mailOptions = {
      from: `"${fromName}" <${fromEmail}>`,
      sender: fromEmail,
      replyTo: `"ESDispatch Support" <support@${domain}>`,
      to: recipientEmail,
      subject,
      text: plainText,
      html: htmlContent,
      messageId,
      envelope: {
        from: fromEmail,
        to: [recipientEmail],
      },
      headers: {
        "X-Mailer": "ESDispatch Logistics Mailer/2026",
        "X-Priority": "1",
        "List-Unsubscribe": `<mailto:support@${domain}?subject=unsubscribe>, <https://www.${domain}/unsubscribe>`,
        "List-Unsubscribe-Post": "List-Unsubscribe=One-Click",
        "Feedback-ID": `esdispatch:security-verification:${Date.now()}`,
        "X-Entity-Ref-ID": `${Date.now()}-${randomHex}`,
      },
    };

    const info = await transporter.sendMail(mailOptions);
    console.log(`[Verification Email Success] Dispatched to ${recipientEmail} with ID: ${info.messageId}`);

    return res.status(200).json({
      success: true,
      message: `Verification passcode dispatched to ${recipientEmail}.`,
      messageId: info.messageId,
    });
  } catch (error) {
    console.error("[Verification Email Handler Error]", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to dispatch verification email.",
    });
  }
};

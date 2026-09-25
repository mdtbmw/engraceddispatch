/**
 * ESDISPATCH LUXURY EMAIL ENGINE & TEMPLATE SUITE
 * 
 * Strict Brand & Quality Locks:
 * - Brand Name: "ESDISPATCH"
 * - Slogan: "PREMIUM LOGISTICS & DISPATCH"
 * - Palette: Brand Gold (#FFB800 / #D4AF37), Obsidian (#111111), Crisp White (#FFFFFF), Warm Luxury Surface (#fdfcf7 / #e8e3d3)
 * - Contrast Rules: NO white text on gold background (Use Obsidian on Gold). NO gold on white.
 * - Universal Client Compatibility: Email-safe HTML, nested tables, inline CSS, fluid media-queries for mobile.
 */

export interface EmailInfoCard {
  category?: string;
  heading: string;
  bodyText?: string;
  codeDisplay?: string;
  codeSubtext?: string;
  rows?: { label: string; value: string; isBold?: boolean; isHighlight?: boolean }[];
}

export interface EmailThreeCard {
  tag: string;
  title: string;
  description: string;
}

export interface MasterEmailOptions {
  title: string;
  preheader: string;
  categoryTag?: string;
  recipientName?: string;
  headline?: string;
  heroImageUrl?: string;
  heroImageAlt?: string;
  heroImageCaption?: string;
  contentHtml: string;
  infoCard?: EmailInfoCard;
  threeCards?: [EmailThreeCard, EmailThreeCard, EmailThreeCard];
  ctaText?: string;
  ctaUrl?: string;
}

export function wrapInMasterLuxuryTemplate(opts: MasterEmailOptions): string {
  const currentYear = new Date().getFullYear();
  const preheader = opts.preheader || "ESDispatch Premium Logistics & Dispatch";
  const categoryTag = (opts.categoryTag || "CUSTOMER COMMUNICATION").toUpperCase();
  const headline = opts.headline || (opts.recipientName ? `Hello ${opts.recipientName},` : "Hello Valued Client,");

  // Hero Image Block
  const heroImageHtml = opts.heroImageUrl
    ? `
      <tr>
        <td style="padding: 16px 28px 10px 28px; background: #ffffff;" align="center">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
              <td align="center" style="border-radius: 8px; overflow: hidden; border: 1px solid #e8ebee; background-color: #f7f9fa;">
                <img 
                  src="${opts.heroImageUrl}" 
                  alt="${opts.heroImageAlt || 'ESDispatch'}" 
                  style="display: block; width: 100%; max-width: 664px; height: auto; border: 0; outline: none; text-decoration: none;"
                  class="responsive-img"
                />
              </td>
            </tr>
            ${
              opts.heroImageCaption
                ? `<tr><td style="padding: 6px 4px 0 4px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; color: #747d88; text-align: center;">${opts.heroImageCaption}</td></tr>`
                : ''
            }
          </table>
        </td>
      </tr>
    `
    : '';

  // Info Card Block
  let infoCardHtml = '';
  if (opts.infoCard) {
    const card = opts.infoCard;
    let cardInnerRows = '';

    if (card.codeDisplay) {
      cardInnerRows += `
        <div style="margin: 16px 0; background: #ffffff; border: 1px dashed #D4AF37; border-radius: 8px; padding: 18px; text-align: center;">
          <div style="font-family: 'Courier New', Courier, monospace; font-size: 36px; font-weight: 900; letter-spacing: 8px; color: #111111; line-height: 1;">
            ${card.codeDisplay}
          </div>
          ${
            card.codeSubtext
              ? `<div style="padding-top: 8px; font-family: Arial,Helvetica,sans-serif; font-size: 12px; color: #747d88;">${card.codeSubtext}</div>`
              : ''
          }
        </div>
      `;
    }

    if (card.bodyText) {
      cardInnerRows += `
        <div style="padding-top: 8px; font-family: Arial,Helvetica,sans-serif; font-size: 13px; line-height: 21px; color: #525a66;">
          ${card.bodyText}
        </div>
      `;
    }

    if (card.rows && card.rows.length > 0) {
      const rowsHtml = card.rows
        .map(
          (r) => `
            <tr>
              <td style="padding: 9px 0; color: #747d88; font-family: Arial,Helvetica,sans-serif; font-size: 13px; border-bottom: 1px solid #ebe6d8;">
                ${r.label}
              </td>
              <td style="padding: 9px 0; color: ${r.isHighlight ? '#B78103' : '#171b20'}; font-family: ${r.isBold ? 'Arial,Helvetica,sans-serif' : 'Arial,Helvetica,sans-serif'}; font-weight: ${r.isBold ? 'bold' : 'normal'}; font-size: 13px; text-align: right; border-bottom: 1px solid #ebe6d8;">
                ${r.value}
              </td>
            </tr>
          `
        )
        .join('');

      cardInnerRows += `
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-top: 12px; border-collapse: collapse;">
          ${rowsHtml}
        </table>
      `;
    }

    infoCardHtml = `
      <tr>
        <td style="padding: 12px 28px 12px 28px; background: #ffffff;">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-collapse: separate; background: #fdfcf7; border: 1px solid #e8e3d3; border-radius: 9px;">
            <tr>
              <td style="padding: 20px;">
                ${
                  card.category
                    ? `<div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #B78103; font-weight: bold; letter-spacing: 1.5px; text-transform: uppercase;">${card.category}</div>`
                    : ''
                }
                <div style="padding-top: ${card.category ? '6px' : '0'}; font-family: Arial,Helvetica,sans-serif; font-size: 17px; line-height: 23px; color: #171b20; font-weight: bold;">
                  ${card.heading}
                </div>
                ${cardInnerRows}
              </td>
            </tr>
          </table>
        </td>
      </tr>
    `;
  }

  // 3-Column Feature Cards
  let threeCardsHtml = '';
  if (opts.threeCards && opts.threeCards.length === 3) {
    const [c1, c2, c3] = opts.threeCards;
    threeCardsHtml = `
      <tr>
        <td style="padding: 12px 28px 20px 28px; background: #ffffff;">
          <!-- 3-Column Container Table -->
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="three-cards-table" style="border-collapse: collapse;">
            <tr>
              <!-- Card 1 -->
              <td class="three-card-col" style="width: 33.33%; padding: 0 6px 0 0; vertical-align: top;" valign="top">
                <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="height: 100%; border-collapse: separate; background: #ffffff; border: 1px solid #e5e9ef; border-radius: 8px;">
                  <tr>
                    <td style="padding: 15px;">
                      <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #B78103; font-weight: bold; letter-spacing: 1px; text-transform: uppercase;">${c1.tag}</div>
                      <div style="padding-top: 5px; font-family: Arial,Helvetica,sans-serif; font-size: 13px; line-height: 19px; color: #222222; font-weight: bold;">${c1.title}</div>
                      <div style="padding-top: 4px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; line-height: 16px; color: #747d88;">${c1.description}</div>
                    </td>
                  </tr>
                </table>
              </td>
              <!-- Card 2 -->
              <td class="three-card-col" style="width: 33.33%; padding: 0 3px; vertical-align: top;" valign="top">
                <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="height: 100%; border-collapse: separate; background: #ffffff; border: 1px solid #e5e9ef; border-radius: 8px;">
                  <tr>
                    <td style="padding: 15px;">
                      <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #B78103; font-weight: bold; letter-spacing: 1px; text-transform: uppercase;">${c2.tag}</div>
                      <div style="padding-top: 5px; font-family: Arial,Helvetica,sans-serif; font-size: 13px; line-height: 19px; color: #222222; font-weight: bold;">${c2.title}</div>
                      <div style="padding-top: 4px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; line-height: 16px; color: #747d88;">${c2.description}</div>
                    </td>
                  </tr>
                </table>
              </td>
              <!-- Card 3 -->
              <td class="three-card-col" style="width: 33.33%; padding: 0 0 0 6px; vertical-align: top;" valign="top">
                <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="height: 100%; border-collapse: separate; background: #ffffff; border: 1px solid #e5e9ef; border-radius: 8px;">
                  <tr>
                    <td style="padding: 15px;">
                      <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #B78103; font-weight: bold; letter-spacing: 1px; text-transform: uppercase;">${c3.tag}</div>
                      <div style="padding-top: 5px; font-family: Arial,Helvetica,sans-serif; font-size: 13px; line-height: 19px; color: #222222; font-weight: bold;">${c3.title}</div>
                      <div style="padding-top: 4px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; line-height: 16px; color: #747d88;">${c3.description}</div>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    `;
  }

  // Call to Action Button
  let ctaHtml = '';
  if (opts.ctaText && opts.ctaUrl) {
    ctaHtml = `
      <tr>
        <td style="padding: 8px 28px 28px 28px; background: #ffffff;" align="center">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td align="center" style="border-radius: 8px; background-color: #FFB800;">
                <a 
                  href="${opts.ctaUrl}" 
                  target="_blank" 
                  rel="noopener" 
                  style="display: inline-block; background-color: #FFB800; color: #111111; font-family: Arial,Helvetica,sans-serif; font-size: 13px; line-height: 16px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase; text-decoration: none; padding: 14px 32px; border-radius: 8px; border: 1px solid #E5A600;"
                >
                  ${opts.ctaText}
                </a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    `;
  }

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>${opts.title}</title>
  <!--[if mso]>
  <noscript>
    <xml>
      <o:OfficeDocumentSettings>
        <o:PixelsPerInch>96</o:PixelsPerInch>
      </o:OfficeDocumentSettings>
    </xml>
  </noscript>
  <![endif]-->
  <style>
    body, table, td, a { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }
    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
    img { -ms-interpolation-mode: bicubic; border: 0; outline: none; text-decoration: none; }
    body { margin: 0; padding: 0; width: 100% !important; background-color: #fdfdfd; font-family: Arial, Helvetica, sans-serif; }
    @media only screen and (max-width: 600px) {
      .outer-wrapper { padding: 12px 6px !important; }
      .outer-frame { width: 100% !important; border-radius: 6px !important; }
      .header-padding { padding: 16px 16px !important; }
      .body-padding { padding: 22px 18px 10px 18px !important; }
      .headline-text { font-size: 20px !important; line-height: 27px !important; }
      .three-cards-table, .three-cards-table tbody, .three-cards-table tr { display: block !important; width: 100% !important; }
      .three-card-col { display: block !important; width: 100% !important; padding: 0 0 10px 0 !important; box-sizing: border-box !important; }
      .contact-table, .contact-table tbody, .contact-table tr { display: block !important; width: 100% !important; }
      .contact-col { display: block !important; width: 100% !important; padding: 0 0 12px 0 !important; box-sizing: border-box !important; }
      .responsive-img { width: 100% !important; max-width: 100% !important; height: auto !important; }
    }
  </style>
</head>
<body style="margin: 0; padding: 0; background: #fdfdfd; font-family: Arial,Helvetica,sans-serif;">
  <!-- Preheader text (preview summary in email clients) -->
  <div style="display: none; max-height: 0px; overflow: hidden; font-size: 1px; line-height: 1px; color: #fdfdfd; mso-hide: all;">
    ${preheader}
  </div>

  <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background: #fdfdfd;">
    <tr>
      <td align="center" class="outer-wrapper" style="padding: 30px 12px; background: #fdfdfd;">
        
        <!-- OUTER FRAME (Max 720px) -->
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="outer-frame" style="max-width: 720px; background: #ffffff; border: 1px solid #e2e7ed; border-radius: 12px; border-collapse: separate; overflow: hidden; box-shadow: 0 4px 18px rgba(0,0,0,0.06);">
          
          <!-- BRAND HEADER -->
          <tr>
            <td class="header-padding" style="padding: 22px 28px; background: #ffffff; border-bottom: 1px solid #edf0f4;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <!-- Logo Lockup -->
                  <td style="width: 185px; vertical-align: middle; padding: 0;" valign="middle">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                      <tr>
                        <!-- Icon Square -->
                        <td style="width: 48px; vertical-align: middle; padding: 0 10px 0 0;" valign="middle">
                          <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                            <tr>
                              <td align="center" style="background-color: #FFB800; width: 44px; height: 44px; border-radius: 10px; font-family: Arial,Helvetica,sans-serif; font-weight: 900; font-size: 20px; color: #111111; line-height: 44px; letter-spacing: -0.5px;">
                                ES
                              </td>
                            </tr>
                          </table>
                        </td>
                        <!-- Wordmark -->
                        <td style="vertical-align: middle; padding: 0; white-space: nowrap;" valign="middle">
                          <div style="font-family: Arial,Helvetica,sans-serif; font-size: 20px; line-height: 20px; font-weight: 900; letter-spacing: 1.2px; color: #111111;">
                            ES
                          </div>
                          <div style="padding-top: 3px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; line-height: 14px; font-weight: bold; letter-spacing: 3.1px; color: #D4AF37;">
                            DISPATCH
                          </div>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <!-- Header Right -->
                  <td style="vertical-align: middle;" align="right" valign="middle">
                    <div style="font-family: Arial,Helvetica,sans-serif; font-size: 9px; line-height: 14px; color: #8993a0; text-transform: uppercase; letter-spacing: 1.5px; font-weight: bold;">
                      PREMIUM LOGISTICS &amp; DISPATCH
                    </div>
                    <a style="font-family: Arial,Helvetica,sans-serif; font-size: 12px; line-height: 18px; color: #D4AF37; text-decoration: none; font-weight: bold;" href="https://www.esdispatch.com.ng" target="_blank" rel="noopener">
                      esdispatch.com.ng
                    </a>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- GOLDEN YELLOW BRAND STRIPE -->
          <tr>
            <td style="height: 5px; background: linear-gradient(90deg, #FFB800 0%, #D4AF37 100%); background-color: #D4AF37; font-size: 0; line-height: 0;">&nbsp;</td>
          </tr>

          <!-- HERO BANNER IMAGE (OPTIONAL) -->
          ${heroImageHtml}

          <!-- EMAIL MAIN BODY -->
          <tr>
            <td class="body-padding" style="padding: 28px 28px 12px 28px; background: #ffffff;">
              <!-- Category Pill Tag -->
              <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td style="background-color: rgba(212, 175, 55, 0.12); border: 1px solid rgba(212, 175, 55, 0.35); border-radius: 100px; padding: 4px 12px;">
                    <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; font-weight: bold; letter-spacing: 1.5px; text-transform: uppercase; color: #B78103;">
                      ${categoryTag}
                    </div>
                  </td>
                </tr>
              </table>
              
              <!-- Subject / Headline -->
              <div class="headline-text" style="padding-top: 10px; font-family: Arial,Helvetica,sans-serif; font-size: 24px; line-height: 31px; font-weight: bold; color: #111111;">
                ${headline}
              </div>

              <!-- Message Body -->
              <div style="padding-top: 16px; font-family: Arial,Helvetica,sans-serif; font-size: 14px; line-height: 24px; color: #3f4650;">
                ${opts.contentHtml}
              </div>
            </td>
          </tr>

          <!-- HIGHLIGHT INFORMATION CARD (OPTIONAL) -->
          ${infoCardHtml}

          <!-- THREE FEATURE CARDS (OPTIONAL) -->
          ${threeCardsHtml}

          <!-- CALL TO ACTION (OPTIONAL) -->
          ${ctaHtml}

          <!-- CONTACT SECTION -->
          <tr>
            <td style="padding: 22px 28px; background: #fdfcf7; border-top: 1px solid #e8e3d3;">
              <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #B78103; font-weight: bold; letter-spacing: 1.5px; text-transform: uppercase;">
                CONTACT ESDISPATCH
              </div>
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="contact-table" style="margin-top: 12px; border-collapse: collapse;">
                <tr>
                  <!-- EMAIL -->
                  <td class="contact-col" style="width: 33.33%; padding: 0 15px 0 0; vertical-align: top;" valign="top">
                    <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #8a939e; font-weight: bold; text-transform: uppercase;">EMAIL</div>
                    <a style="display: block; padding-top: 4px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; line-height: 17px; color: #222222; text-decoration: none;" href="mailto:fleets@esdispatch.com.ng">
                      fleets@esdispatch.com.ng
                    </a>
                  </td>
                  <!-- PHONE -->
                  <td class="contact-col" style="width: 33.33%; padding: 0 15px; vertical-align: top;" valign="top">
                    <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #8a939e; font-weight: bold; text-transform: uppercase;">PHONE</div>
                    <a style="display: block; padding-top: 4px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; line-height: 17px; color: #222222; text-decoration: none;" href="tel:+2349056263010">
                      +234 905 626 3010
                    </a>
                  </td>
                  <!-- ADDRESS -->
                  <td class="contact-col" style="width: 33.33%; padding: 0 0 0 15px; vertical-align: top;" valign="top">
                    <div style="font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 14px; color: #8a939e; font-weight: bold; text-transform: uppercase;">OFFICE</div>
                    <div style="padding-top: 4px; font-family: Arial,Helvetica,sans-serif; font-size: 11px; line-height: 17px; color: #222222;">
                      17 Upper Adesuwa Road,<br>GRA, Benin City.
                    </div>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- GOLDEN BRAND FOOTER -->
          <tr>
            <td style="padding: 18px 28px; background: #D4AF37;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <td style="vertical-align: middle;" valign="middle">
                    <div style="font-family: Arial,Helvetica,sans-serif; font-size: 12px; line-height: 18px; color: #ffffff; font-weight: bold; letter-spacing: 1px;">
                      ESDISPATCH
                    </div>
                    <div style="padding-top: 3px; font-family: Arial,Helvetica,sans-serif; font-size: 10px; line-height: 15px; color: #fffdf5;">
                      Speed &amp; Precision in Every Delivery.
                    </div>
                  </td>
                  <td style="vertical-align: middle;" align="right" valign="middle">
                    <!-- Facebook -->
                    <a style="display: inline-block; width: 30px; height: 30px; margin-left: 5px; background: #ffffff; border-radius: 50%; text-decoration: none; text-align: center; vertical-align: middle;" href="https://web.facebook.com/esdispatch" target="_blank" rel="noopener">
                      <span style="font-family: Arial,Helvetica,sans-serif; font-size: 14px; line-height: 30px; color: #D4AF37; font-weight: bold;">f</span>
                    </a>
                    <!-- Instagram -->
                    <a style="display: inline-block; width: 30px; height: 30px; margin-left: 5px; background: #ffffff; border-radius: 50%; text-decoration: none; text-align: center; vertical-align: middle;" href="https://instagram.com/esdispatch" target="_blank" rel="noopener">
                      <span style="font-family: Arial,Helvetica,sans-serif; font-size: 12px; line-height: 30px; color: #D4AF37; font-weight: bold;">IG</span>
                    </a>
                    <!-- Portal Link -->
                    <a style="display: inline-block; width: 30px; height: 30px; margin-left: 5px; background: #ffffff; border-radius: 50%; text-decoration: none; text-align: center; vertical-align: middle;" href="https://www.esdispatch.com.ng" target="_blank" rel="noopener">
                      <span style="font-family: Arial,Helvetica,sans-serif; font-size: 14px; line-height: 30px; color: #D4AF37; font-weight: bold;">&#8599;</span>
                    </a>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- COPYRIGHT STRIP -->
          <tr>
            <td style="padding: 9px 20px; background: #B78103; font-family: Arial,Helvetica,sans-serif; font-size: 9px; line-height: 13px; color: #fffdf5;" align="center">
              &copy; ${currentYear} ESDispatch &nbsp;&bull;&nbsp; Premium Dispatch &amp; Logistics &nbsp;&bull;&nbsp; Encrypted SSL Delivery
            </td>
          </tr>

        </table>

      </td>
    </tr>
  </table>
</body>
</html>`;
}

// ============================================================================
// CONCRETE EMAIL INSTANCE RENDERERS
// ============================================================================

/**
 * 1. Sign-Up Account Verification OTP
 */
export function renderSignUpOtpEmail(params: {
  name: string;
  otp: string;
  expiryMinutes?: number;
  heroImageUrl?: string;
}): string {
  const expiry = params.expiryMinutes || 10;
  return wrapInMasterLuxuryTemplate({
    title: `Verify Your ESDispatch Account (${params.otp})`,
    preheader: `Your verification code is ${params.otp}. Valid for ${expiry} minutes.`,
    categoryTag: 'ACCOUNT VERIFICATION',
    recipientName: params.name || 'Valued Client',
    headline: `Welcome to ESDispatch, ${params.name || 'Client'}`,
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        Thank you for choosing <strong style="color: #111111;">ESDispatch</strong> — the gold standard in precision logistics and express courier dispatch.
      </p>
      <p style="margin: 0 0 14px 0;">
        To authenticate and activate your account, please enter the confidential one-time verification passcode below into the registration screen.
      </p>
    `,
    infoCard: {
      category: 'AUTHENTICATION CODE',
      heading: 'One-Time Verification Passcode',
      codeDisplay: params.otp,
      codeSubtext: `Expires in ${expiry} minutes • Do not disclose to anyone.`,
      bodyText: 'ESDispatch officers or dispatch staff will never ask for your authentication passcode.',
    },
    threeCards: [
      { tag: 'SECURITY', title: '256-Bit SSL', description: 'End-to-end encrypted dispatch network.' },
      { tag: 'VALIDITY', title: `${expiry} Minutes`, description: 'Single-use dynamic authentication.' },
      { tag: 'HEADQUARTERS', title: 'Benin City', description: 'Active fleet operating across Edo State.' },
    ],
    ctaText: 'OPEN ESDISPATCH APP',
    ctaUrl: 'https://www.esdispatch.com.ng',
  });
}

/**
 * 2. Password Reset OTP
 */
export function renderPasswordResetOtpEmail(params: {
  name: string;
  otp: string;
  expiryMinutes?: number;
  heroImageUrl?: string;
}): string {
  const expiry = params.expiryMinutes || 10;
  return wrapInMasterLuxuryTemplate({
    title: `Password Reset Request (${params.otp})`,
    preheader: `Your password reset code is ${params.otp}. Expires in ${expiry} minutes.`,
    categoryTag: 'SECURITY ALERT',
    recipientName: params.name || 'User',
    headline: 'Password Reset Authorization',
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        We received a formal request to reset the password for your ESDispatch logistics account.
      </p>
      <p style="margin: 0 0 14px 0;">
        If you initiated this request, authorize the update using the single-use security passcode below. If you did not make this request, please change your password immediately or alert our security desk.
      </p>
    `,
    infoCard: {
      category: 'AUTHORIZATION PASSCODE',
      heading: 'Password Reset Code',
      codeDisplay: params.otp,
      codeSubtext: `Valid for ${expiry} minutes • Single-use authorization.`,
    },
    threeCards: [
      { tag: 'NOTICE', title: 'Single Use', description: 'Code invalidates after first successful entry.' },
      { tag: 'SECURITY', title: 'Account Shield', description: 'Multi-layer device verification active.' },
      { tag: 'SUPPORT', title: 'Emergency', description: 'Contact fleets@esdispatch.com.ng immediately.' },
    ],
    ctaText: 'VISIT SECURITY PORTAL',
    ctaUrl: 'https://www.esdispatch.com.ng',
  });
}

/**
 * 3. Two-Factor Authentication (2FA) Challenge
 */
export function renderTwoFactorOtpEmail(params: {
  name: string;
  otp: string;
  expiryMinutes?: number;
  ipOrDevice?: string;
  heroImageUrl?: string;
}): string {
  const expiry = params.expiryMinutes || 5;
  return wrapInMasterLuxuryTemplate({
    title: `2FA Login Challenge: ${params.otp}`,
    preheader: `Your 2FA login code is ${params.otp}.`,
    categoryTag: 'TWO-FACTOR LOGIN',
    recipientName: params.name || 'User',
    headline: 'Second-Factor Verification',
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        A new sign-in attempt was detected for your account${params.ipOrDevice ? ` from <strong>${params.ipOrDevice}</strong>` : ''}.
      </p>
      <p style="margin: 0 0 14px 0;">
        Enter the two-factor authentication passcode below to confirm your identity and access your dashboard.
      </p>
    `,
    infoCard: {
      category: '2FA PASSCODE',
      heading: 'Login Authentication Code',
      codeDisplay: params.otp,
      codeSubtext: `Expires in ${expiry} minutes • Intended solely for your current login session.`,
    },
    threeCards: [
      { tag: 'DEVICE', title: 'Identity Verified', description: 'Session locked to active client signature.' },
      { tag: 'EXPIRY', title: `${expiry} Minutes`, description: 'Instant auto-expiration window.' },
      { tag: 'DEFENSE', title: 'Zero Sharing', description: 'Never forward or send to dispatchers.' },
    ],
    ctaText: 'CONFIRM ON APP',
    ctaUrl: 'https://www.esdispatch.com.ng',
  });
}

/**
 * 4. Wallet Security PIN Reset
 */
export function renderPinResetOtpEmail(params: {
  name: string;
  otp: string;
  expiryMinutes?: number;
  heroImageUrl?: string;
}): string {
  const expiry = params.expiryMinutes || 10;
  return wrapInMasterLuxuryTemplate({
    title: `Authorize Wallet PIN Reset (${params.otp})`,
    preheader: `Your PIN reset authorization code is ${params.otp}.`,
    categoryTag: 'WALLET SECURITY',
    recipientName: params.name || 'User',
    headline: 'Wallet PIN Change Request',
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        You have initiated a change or reset of your ESDispatch transaction security PIN.
      </p>
      <p style="margin: 0 0 14px 0;">
        Your wallet PIN secures all balance deductions, delivery escrows, and tip settlements. Use this one-time authorization code to finalize your new PIN.
      </p>
    `,
    infoCard: {
      category: 'TRANSACTION AUTHORIZATION',
      heading: 'PIN Change Authorization Code',
      codeDisplay: params.otp,
      codeSubtext: `Valid for ${expiry} minutes.`,
    },
    threeCards: [
      { tag: 'ESCROW', title: 'Fund Protection', description: 'Secures your Naira wallet balance.' },
      { tag: 'SECURITY', title: 'Hardware Lock', description: 'Validated against device keystore.' },
      { tag: 'SUPPORT', title: 'Questions?', description: 'Call +234 905 626 3010 for immediate support.' },
    ],
    ctaText: 'MANAGE WALLET SETTINGS',
    ctaUrl: 'https://www.esdispatch.com.ng',
  });
}

/**
 * 5. Delivery Handover OTP (Recipient Proof of Delivery)
 */
export function renderDeliveryHandoverOtpEmail(params: {
  trackingNumber: string;
  recipientName: string;
  courierName?: string;
  pickupAddress: string;
  dropoffAddress: string;
  handoverOtp: string;
  heroImageUrl?: string;
}): string {
  return wrapInMasterLuxuryTemplate({
    title: `Delivery Handover Code for #${params.trackingNumber}`,
    preheader: `Your ESDispatch handover code is ${params.handoverOtp} for parcel #${params.trackingNumber}.`,
    categoryTag: 'SHIPMENT IN TRANSIT',
    recipientName: params.recipientName,
    headline: `Your Delivery is Arriving (#${params.trackingNumber})`,
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        Your courier <strong style="color: #111111;">${params.courierName || 'ESDispatch Fleet Courier'}</strong> is arriving with your parcel.
      </p>
      <p style="margin: 0 0 14px 0;">
        For your safety and protection, do not release this code until you physically inspect your parcel at your designated destination.
      </p>
    `,
    infoCard: {
      category: 'OFFICIAL HANDOVER PASSCODE',
      heading: 'Parcel Handover Code',
      codeDisplay: params.handoverOtp,
      codeSubtext: 'Provide in person to courier upon parcel arrival.',
      rows: [
        { label: 'Tracking Number', value: params.trackingNumber, isBold: true },
        { label: 'Pickup Origin', value: params.pickupAddress },
        { label: 'Dropoff Destination', value: params.dropoffAddress },
      ],
    },
    threeCards: [
      { tag: 'COURIER', title: params.courierName || 'Fleet Rider', description: 'Authorized & tracked courier.' },
      { tag: 'INSPECTION', title: 'Check Package', description: 'Inspect before providing code.' },
      { tag: 'LOCATION', title: 'Benin City', description: 'Direct GPS track available.' },
    ],
    ctaText: 'TRACK LIVE ON MAP',
    ctaUrl: `https://www.esdispatch.com.ng/track?id=${params.trackingNumber}`,
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
  heroImageUrl?: string;
}): string {
  const rows: { label: string; value: string; isBold?: boolean; isHighlight?: boolean }[] = params.breakdown.map((b) => ({
    label: b.label,
    value: b.amount,
  }));
  rows.push({
    label: 'Total Settlement Paid',
    value: params.amountPaid,
    isBold: true,
    isHighlight: true,
  });

  return wrapInMasterLuxuryTemplate({
    title: `Payment Receipt: Shipment #${params.trackingNumber}`,
    preheader: `Payment confirmed for shipment #${params.trackingNumber}. Total: ${params.amountPaid}.`,
    categoryTag: 'PAYMENT CONFIRMED',
    recipientName: params.senderName,
    headline: `Official Delivery Receipt (#${params.trackingNumber})`,
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        Thank you for booking with <strong style="color: #111111;">ESDispatch</strong>. Your payment of <strong style="color: #B78103;">${params.amountPaid}</strong> has been confirmed and escrowed for delivery.
      </p>
      <p style="margin: 0 0 14px 0;">
        Please find your itemized settlement details and tracking confirmation below.
      </p>
    `,
    infoCard: {
      category: 'SETTLEMENT SUMMARY',
      heading: params.amountPaid,
      bodyText: `Settled via ${params.paymentMethod} • ${params.date}`,
      rows: rows,
    },
    threeCards: [
      { tag: 'SERVICE', title: params.serviceType || 'Express Dispatch', description: 'Door-to-door delivery.' },
      { tag: 'TRACKING', title: `#${params.trackingNumber}`, description: 'Live tracking enabled.' },
      { tag: 'STATUS', title: 'Dispatched', description: 'Fleet assigned.' },
    ],
    ctaText: 'VIEW LIVE TRACKING',
    ctaUrl: `https://www.esdispatch.com.ng/track?id=${params.trackingNumber}`,
  });
}

/**
 * 7. Wallet Transaction Alert (Credit / Debit)
 */
export function renderWalletTransactionEmail(params: {
  name: string;
  transactionType: 'CREDIT' | 'DEBIT';
  amount: string;
  newBalance: string;
  reference: string;
  date: string;
  description: string;
  heroImageUrl?: string;
}): string {
  const isCredit = params.transactionType === 'CREDIT';
  return wrapInMasterLuxuryTemplate({
    title: `Wallet ${params.transactionType}: ${params.amount}`,
    preheader: `Wallet ${params.transactionType.toLowerCase()} of ${params.amount}. New balance: ${params.newBalance}.`,
    categoryTag: isCredit ? 'WALLET CREDIT ALERT' : 'WALLET DEBIT ALERT',
    recipientName: params.name,
    headline: isCredit ? 'Funds Credited to Your Wallet' : 'Wallet Debit Notification',
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        Your ESDispatch wallet balance has been updated successfully.
      </p>
      <p style="margin: 0 0 14px 0;">
        Transaction summary: <strong style="color: #111111;">${params.description}</strong>.
      </p>
    `,
    infoCard: {
      category: 'TRANSACTION AUDIT',
      heading: `${isCredit ? '+' : '-'}${params.amount}`,
      rows: [
        { label: 'Transaction Type', value: params.transactionType, isBold: true },
        { label: 'Available Balance', value: params.newBalance, isBold: true, isHighlight: true },
        { label: 'Reference Code', value: params.reference },
        { label: 'Date & Time', value: params.date },
      ],
    },
    threeCards: [
      { tag: 'SECURITY', title: 'Instant Ledger', description: 'Immutable transaction logging.' },
      { tag: 'SPEED', title: 'Real-Time', description: 'Zero waiting for wallet updates.' },
      { tag: 'BENIN CITY', title: 'ESDispatch', description: 'Fast settlement for couriers & merchants.' },
    ],
    ctaText: 'OPEN WALLET IN APP',
    ctaUrl: 'https://www.esdispatch.com.ng',
  });
}

/**
 * 8. Partner Welcome & Onboarding Letter
 */
export function renderPartnerWelcomeEmail(params: {
  name: string;
  role: 'rider' | 'vendor' | 'customer';
  portalUrl?: string;
  heroImageUrl?: string;
}): string {
  const roleTitle =
    params.role === 'rider'
      ? 'Fleet Courier Partner'
      : params.role === 'vendor'
      ? 'Verified Merchant Partner'
      : 'Premium Logistics Client';

  return wrapInMasterLuxuryTemplate({
    title: `Welcome to ESDISPATCH, ${params.name}!`,
    preheader: `Welcome to ESDispatch as our ${roleTitle}. Start delivering excellence today.`,
    categoryTag: 'FLEET ONBOARDING',
    recipientName: params.name,
    headline: `Welcome to the Fleet, ${params.name}`,
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 14px 0;">
        Congratulations! You are officially onboarded as an authorized <strong style="color: #111111;">${roleTitle}</strong> with ESDispatch.
      </p>
      <p style="margin: 0 0 14px 0;">
        As a partner in precision logistics, you have access to prompt fleet assignments, transparent tracking, automated daily escrow settlements, and direct dispatcher assistance.
      </p>
    `,
    infoCard: {
      category: 'PARTNER CREDENTIALS',
      heading: roleTitle,
      bodyText: 'Your account is fully verified and connected to our live dispatch network in Benin City.',
      rows: [
        { label: 'Authorized Role', value: roleTitle, isBold: true },
        { label: 'Operations Zone', value: 'Benin City & Greater Edo' },
        { label: 'Dispatch Desk', value: '+234 905 626 3010' },
      ],
    },
    threeCards: [
      { tag: 'STANDARDS', title: 'Zero Compromise', description: 'Strict timing and secure deliveries.' },
      { tag: 'EARNINGS', title: 'Prompt Payouts', description: 'Direct wallet settlements & tips.' },
      { tag: 'SAFETY', title: 'Live Telemetry', description: 'Continuous location tracking.' },
    ],
    ctaText: 'ACCESS PARTNER CONSOLE',
    ctaUrl: params.portalUrl || 'https://www.esdispatch.com.ng',
  });
}

/**
 * 9. Custom Broadcast & Marketing Announcement
 */
export function renderCustomBroadcastEmail(params: {
  recipientName?: string;
  categoryTag?: string;
  headline: string;
  heroImageUrl?: string;
  heroImageCaption?: string;
  messageHtml: string;
  infoCardHeading?: string;
  infoCardBody?: string;
  infoCardRows?: { label: string; value: string; isBold?: boolean; isHighlight?: boolean }[];
  threeCards?: [EmailThreeCard, EmailThreeCard, EmailThreeCard];
  ctaText?: string;
  ctaUrl?: string;
}): string {
  return wrapInMasterLuxuryTemplate({
    title: params.headline,
    preheader: params.headline,
    categoryTag: params.categoryTag || 'OFFICIAL ANNOUNCEMENT',
    recipientName: params.recipientName,
    headline: params.headline,
    heroImageUrl: params.heroImageUrl,
    heroImageCaption: params.heroImageCaption,
    contentHtml: params.messageHtml,
    infoCard: params.infoCardHeading
      ? {
          category: 'DETAILS',
          heading: params.infoCardHeading,
          bodyText: params.infoCardBody,
          rows: params.infoCardRows,
        }
      : undefined,
    threeCards: params.threeCards,
    ctaText: params.ctaText,
    ctaUrl: params.ctaUrl,
  });
}

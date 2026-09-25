/**
 * ESDISPATCH LUXURY EMAIL ENGINE & TEMPLATE SUITE
 * 
 * Strict Brand & Quality Locks:
 * - Brand Name: "ESDISPATCH"
 * - Official Slogan: "PREMIUM LOGISTICS & DISPATCH"
 * - Palette: Brand Gold (#FFB800 / #D4AF37), Obsidian (#050505), Crisp White (#FFFFFF), Clean Luxury Porcelain (#F9FAFB)
 * - Contrast Rules: NO white text on gold background (Use Obsidian on Gold). NO gold on white.
 * - Universal Client Compatibility: Email-safe HTML, nested tables, inline CSS, fluid media-queries for mobile.
 */

export interface EmailInfoRow {
  label: string;
  value: string;
  isBold?: boolean;
  isHighlight?: boolean;
}

export interface EmailInfoCard {
  category?: string;
  heading: string;
  bodyText?: string;
  codeDisplay?: string;
  codeSubtext?: string;
  badgeText?: string;
  badgeType?: 'warning' | 'success' | 'info';
  rows?: EmailInfoRow[];
}

export interface EmailThreeCard {
  tag: string;
  title: string;
  description: string;
  iconName?: string;
}

export interface EmailStep {
  stepNumber: string;
  title: string;
  description: string;
}

export interface EmailVoucher {
  code: string;
  discount: string;
  subtext: string;
  validUntil?: string;
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
  steps?: EmailStep[];
  voucher?: EmailVoucher;
  threeCards?: [EmailThreeCard, EmailThreeCard, EmailThreeCard] | EmailThreeCard[];
  ctaText?: string;
  ctaUrl?: string;
}

/**
 * Resolves relative image paths to public URLs accessible by external email clients.
 */
export function resolveEmailImageUrl(path?: string): string {
  if (!path) return '';
  if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('data:')) {
    return path;
  }
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  // Primary: GitHub Raw repository storage (accessible worldwide)
  return `https://raw.githubusercontent.com/mdtbmw/engraceddispatch/main/public${cleanPath}`;
}

/**
 * Universal Master Luxury Email Template
 */
export function wrapInMasterLuxuryTemplate(opts: MasterEmailOptions): string {
  const currentYear = new Date().getFullYear();
  const preheader = opts.preheader || "ESDispatch Premium Logistics & Dispatch";
  const categoryTag = (opts.categoryTag || "PREMIUM DISPATCH").toUpperCase();
  const headline = opts.headline || (opts.recipientName ? `Hello ${opts.recipientName},` : "Hello Valued Client,");
  const logoUrl = resolveEmailImageUrl('/images/logo/brand-header-logo.png');

  // Hero Image Block
  const heroImageHtml = opts.heroImageUrl
    ? `
      <tr>
        <td style="padding: 16px 28px 12px 28px; background: #ffffff;" align="center">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
              <td align="center" style="border-radius: 12px; overflow: hidden; border: 1px solid #E5E7EB; background-color: #F9FAFB;">
                <img 
                  src="${resolveEmailImageUrl(opts.heroImageUrl)}" 
                  alt="${opts.heroImageAlt || 'ESDispatch'}" 
                  width="624"
                  style="display: block; width: 100%; max-width: 624px; height: auto; border: 0; outline: none; text-decoration: none;"
                  class="responsive-img"
                />
              </td>
            </tr>
            ${
              opts.heroImageCaption
                ? `<tr><td style="padding: 8px 4px 0 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; color: #6B7280; text-align: center;">${opts.heroImageCaption}</td></tr>`
                : ''
            }
          </table>
        </td>
      </tr>
    `
    : '';

  // Voucher Card Block (e.g. for promotions)
  let voucherHtml = '';
  if (opts.voucher) {
    voucherHtml = `
      <tr>
        <td style="padding: 12px 28px 16px 28px; background: #ffffff;">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-collapse: separate; background-color: #FFFDF5; border: 2px dashed #FFB800; border-radius: 12px;">
            <tr>
              <td style="padding: 22px 20px; text-align: center;" align="center">
                <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 15px; color: #92400E; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;">
                  EXCLUSIVE PROMO VOUCHER
                </div>
                <div style="padding-top: 10px; font-family: 'Courier New', Courier, monospace; font-size: 34px; line-height: 38px; font-weight: 900; letter-spacing: 6px; color: #050505;">
                  ${opts.voucher.code}
                </div>
                <div style="padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 16px; line-height: 22px; color: #B45309; font-weight: bold;">
                  ${opts.voucher.discount}
                </div>
                <div style="padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; line-height: 18px; color: #6B7280;">
                  ${opts.voucher.subtext}${opts.voucher.validUntil ? ` &bull; Valid until ${opts.voucher.validUntil}` : ''}
                </div>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    `;
  }

  // Info Card Block (Passcodes, Invoices, Routing, Ledger)
  let infoCardHtml = '';
  if (opts.infoCard) {
    const card = opts.infoCard;
    let cardInnerRows = '';

    // Code Display (for OTP, Handover code, PIN)
    if (card.codeDisplay) {
      cardInnerRows += `
        <div style="margin: 16px 0; background: #ffffff; border: 1.5px solid #FFB800; border-radius: 10px; padding: 20px 14px; text-align: center; box-shadow: 0 2px 8px rgba(255, 184, 0, 0.12);">
          <div style="font-family: 'Courier New', Courier, monospace; font-size: 40px; font-weight: 900; letter-spacing: 10px; color: #050505; line-height: 1;">
            ${card.codeDisplay}
          </div>
          ${
            card.codeSubtext
              ? `<div style="padding-top: 10px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; color: #6B7280; font-weight: 500;">${card.codeSubtext}</div>`
              : ''
          }
        </div>
      `;
    }

    if (card.badgeText) {
      const badgeBg = card.badgeType === 'warning' ? '#FEF3C7' : card.badgeType === 'success' ? '#DCFCE7' : '#EFF6FF';
      const badgeColor = card.badgeType === 'warning' ? '#92400E' : card.badgeType === 'success' ? '#166534' : '#1E40AF';
      const badgeBorder = card.badgeType === 'warning' ? '#FDE68A' : card.badgeType === 'success' ? '#BBF7D0' : '#BFDBFE';

      cardInnerRows += `
        <div style="margin-bottom: 12px;">
          <span style="display: inline-block; background-color: ${badgeBg}; color: ${badgeColor}; border: 1px solid ${badgeBorder}; border-radius: 6px; padding: 4px 10px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; font-weight: 700;">
            ${card.badgeText}
          </span>
        </div>
      `;
    }

    if (card.bodyText) {
      cardInnerRows += `
        <div style="padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 20px; color: #4B5563;">
          ${card.bodyText}
        </div>
      `;
    }

    if (card.rows && card.rows.length > 0) {
      const rowsHtml = card.rows
        .map(
          (r) => `
            <tr>
              <td style="padding: 10px 0; color: #6B7280; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; border-bottom: 1px solid #F3F4F6;">
                ${r.label}
              </td>
              <td style="padding: 10px 0; color: ${r.isHighlight ? '#B45309' : '#111827'}; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-weight: ${r.isBold ? '700' : '500'}; font-size: ${r.isHighlight ? '15px' : '13px'}; text-align: right; border-bottom: 1px solid #F3F4F6;">
                ${r.value}
              </td>
            </tr>
          `
        )
        .join('');

      cardInnerRows += `
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-top: 10px; border-collapse: collapse;">
          ${rowsHtml}
        </table>
      `;
    }

    infoCardHtml = `
      <tr>
        <td style="padding: 12px 28px 14px 28px; background: #ffffff;">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-collapse: separate; background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 12px;">
            <tr>
              <td style="padding: 22px 20px;">
                ${
                  card.category
                    ? `<div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #B45309; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;">${card.category}</div>`
                    : ''
                }
                <div style="padding-top: ${card.category ? '6px' : '0'}; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 18px; line-height: 24px; color: #111827; font-weight: 800;">
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

  // Visual Step Guide Cards (e.g. 1-2-3 How It Works)
  let stepsHtml = '';
  if (opts.steps && opts.steps.length > 0) {
    const stepCards = opts.steps
      .map(
        (st) => `
        <td class="step-col" style="width: ${100 / opts.steps!.length}%; padding: 0 4px; vertical-align: top;" valign="top">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="height: 100%; border-collapse: separate; background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 10px;">
            <tr>
              <td style="padding: 16px 14px; text-align: left;">
                <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 10px;">
                  <tr>
                    <td align="center" style="width: 28px; height: 28px; background-color: #FFB800; border-radius: 50%; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; font-weight: 900; color: #050505; line-height: 28px; text-align: center;">
                      ${st.stepNumber}
                    </td>
                  </tr>
                </table>
                <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 18px; color: #111827; font-weight: 800;">
                  ${st.title}
                </div>
                <div style="padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 16px; color: #6B7280;">
                  ${st.description}
                </div>
              </td>
            </tr>
          </table>
        </td>
      `
      )
      .join('');

    stepsHtml = `
      <tr>
        <td style="padding: 10px 28px 16px 28px; background: #ffffff;">
          <div style="padding-bottom: 12px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 15px; color: #92400E; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;">
            HOW IT WORKS &bull; 3-STEP DISPATCH
          </div>
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="steps-table" style="border-collapse: collapse;">
            <tr>
              ${stepCards}
            </tr>
          </table>
        </td>
      </tr>
    `;
  }

  // 3-Column Feature Highlight Cards
  let threeCardsHtml = '';
  if (opts.threeCards && opts.threeCards.length > 0) {
    const cardsCols = opts.threeCards
      .map(
        (c) => `
        <td class="three-card-col" style="width: ${100 / opts.threeCards!.length}%; padding: 0 4px; vertical-align: top;" valign="top">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="height: 100%; border-collapse: separate; background: #ffffff; border: 1px solid #E5E7EB; border-radius: 10px;">
            <tr>
              <td style="padding: 16px 14px;">
                <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #B45309; font-weight: 800; letter-spacing: 1px; text-transform: uppercase;">
                  ${c.tag}
                </div>
                <div style="padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 19px; color: #111827; font-weight: 800;">
                  ${c.title}
                </div>
                <div style="padding-top: 5px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 16px; color: #6B7280;">
                  ${c.description}
                </div>
              </td>
            </tr>
          </table>
        </td>
      `
      )
      .join('');

    threeCardsHtml = `
      <tr>
        <td style="padding: 10px 28px 18px 28px; background: #ffffff;">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="three-cards-table" style="border-collapse: collapse;">
            <tr>
              ${cardsCols}
            </tr>
          </table>
        </td>
      </tr>
    `;
  }

  // Call to Action Button (Obsidian text on Gold background strictly adhering to AGENTS.md)
  let ctaHtml = '';
  if (opts.ctaText && opts.ctaUrl) {
    ctaHtml = `
      <tr>
        <td style="padding: 12px 28px 28px 28px; background: #ffffff;" align="center">
          <table role="presentation" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td align="center" style="border-radius: 10px; background-color: #FFB800;">
                <a 
                  href="${opts.ctaUrl}" 
                  target="_blank" 
                  rel="noopener" 
                  style="display: inline-block; background-color: #FFB800; color: #050505; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 16px; font-weight: 900; letter-spacing: 1px; text-transform: uppercase; text-decoration: none; padding: 15px 36px; border-radius: 10px; border: 1px solid #E5A600;"
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
    body { margin: 0; padding: 0; width: 100% !important; background-color: #F3F4F6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    @media only screen and (max-width: 600px) {
      .outer-wrapper { padding: 10px 4px !important; }
      .outer-frame { width: 100% !important; border-radius: 8px !important; }
      .header-padding { padding: 16px 16px !important; }
      .body-padding { padding: 20px 16px 8px 16px !important; }
      .headline-text { font-size: 20px !important; line-height: 26px !important; }
      .steps-table, .steps-table tbody, .steps-table tr { display: block !important; width: 100% !important; }
      .step-col { display: block !important; width: 100% !important; padding: 0 0 10px 0 !important; box-sizing: border-box !important; }
      .three-cards-table, .three-cards-table tbody, .three-cards-table tr { display: block !important; width: 100% !important; }
      .three-card-col { display: block !important; width: 100% !important; padding: 0 0 10px 0 !important; box-sizing: border-box !important; }
      .contact-table, .contact-table tbody, .contact-table tr { display: block !important; width: 100% !important; }
      .contact-col { display: block !important; width: 100% !important; padding: 0 0 12px 0 !important; box-sizing: border-box !important; }
      .responsive-img { width: 100% !important; max-width: 100% !important; height: auto !important; }
    }
  </style>
</head>
<body style="margin: 0; padding: 0; background: #F3F4F6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
  <!-- Preheader text (preview summary in email clients) -->
  <div style="display: none; max-height: 0px; overflow: hidden; font-size: 1px; line-height: 1px; color: #F3F4F6; mso-hide: all;">
    ${preheader}
  </div>

  <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background: #F3F4F6;">
    <tr>
      <td align="center" class="outer-wrapper" style="padding: 28px 10px; background: #F3F4F6;">
        
        <!-- OUTER FRAME (Max 680px) -->
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="outer-frame" style="max-width: 680px; background: #ffffff; border: 1px solid #E5E7EB; border-radius: 16px; border-collapse: separate; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05);">
          
          <!-- OBSIDIAN LUXURY HEADER -->
          <tr>
            <td class="header-padding" style="padding: 18px 28px; background: #050505;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <!-- Official Brand Logo -->
                  <td style="vertical-align: middle; padding: 0;" valign="middle">
                    <a href="https://www.engracedsmile.com" target="_blank" rel="noopener" style="text-decoration: none; display: inline-block;">
                      <img 
                        src="${logoUrl}" 
                        alt="ESDISPATCH - PREMIUM LOGISTICS & DISPATCH" 
                        width="220" 
                        height="50" 
                        style="display: block; width: 220px; height: auto; max-width: 100%; border: 0;"
                      />
                    </a>
                  </td>
                  <!-- Header Right Status Pill -->
                  <td style="vertical-align: middle;" align="right" valign="middle">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                      <tr>
                        <td style="background-color: rgba(255, 184, 0, 0.12); border: 1px solid rgba(255, 184, 0, 0.35); border-radius: 100px; padding: 5px 12px;">
                          <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 9.5px; line-height: 13px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; color: #FFB800;">
                            BENIN CITY FLEET
                          </div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- BRAND GOLD TRACK ACCENT STRIPE -->
          <tr>
            <td style="height: 3px; background: linear-gradient(90deg, #FFB800 0%, #D4AF37 50%, #FFB800 100%); background-color: #FFB800; font-size: 0; line-height: 0;">&nbsp;</td>
          </tr>

          <!-- HERO BANNER IMAGE (OPTIONAL) -->
          ${heroImageHtml}

          <!-- EMAIL MAIN BODY -->
          <tr>
            <td class="body-padding" style="padding: 26px 28px 12px 28px; background: #ffffff;">
              <!-- Category Pill Tag -->
              <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td style="background-color: #FFFBEB; border: 1px solid #FDE68A; border-radius: 100px; padding: 4px 12px;">
                    <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; color: #92400E;">
                      ${categoryTag}
                    </div>
                  </td>
                </tr>
              </table>
              
              <!-- Subject / Headline -->
              <div class="headline-text" style="padding-top: 12px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 23px; line-height: 30px; font-weight: 800; color: #111827;">
                ${headline}
              </div>

              <!-- Message Body -->
              <div style="padding-top: 14px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 14px; line-height: 23px; color: #374151;">
                ${opts.contentHtml}
              </div>
            </td>
          </tr>

          <!-- VOUCHER / PROMO CARD (OPTIONAL) -->
          ${voucherHtml}

          <!-- HIGHLIGHT INFORMATION CARD (OPTIONAL) -->
          ${infoCardHtml}

          <!-- VISUAL STEPS GUIDE (OPTIONAL) -->
          ${stepsHtml}

          <!-- THREE FEATURE CARDS (OPTIONAL) -->
          ${threeCardsHtml}

          <!-- CALL TO ACTION (OPTIONAL) -->
          ${ctaHtml}

          <!-- CONTACT / SUPPORT SECTION -->
          <tr>
            <td style="padding: 20px 28px; background: #F9FAFB; border-top: 1px solid #E5E7EB;">
              <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #B45309; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;">
                ESDISPATCH DESK &bull; DIRECT CONTACT
              </div>
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" class="contact-table" style="margin-top: 12px; border-collapse: collapse;">
                <tr>
                  <!-- EMAIL -->
                  <td class="contact-col" style="width: 33.33%; padding: 0 12px 0 0; vertical-align: top;" valign="top">
                    <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF; font-weight: 800; text-transform: uppercase;">EMAIL DESK</div>
                    <a style="display: block; padding-top: 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11.5px; line-height: 17px; color: #111827; text-decoration: none; font-weight: 600;" href="mailto:support@engracedsmile.com">
                      support@engracedsmile.com
                    </a>
                  </td>
                  <!-- PHONE -->
                  <td class="contact-col" style="width: 33.33%; padding: 0 12px; vertical-align: top;" valign="top">
                    <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF; font-weight: 800; text-transform: uppercase;">HOTLINE</div>
                    <a style="display: block; padding-top: 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11.5px; line-height: 17px; color: #111827; text-decoration: none; font-weight: 600;" href="tel:+2349056263010">
                      +234 905 626 3010
                    </a>
                  </td>
                  <!-- ADDRESS -->
                  <td class="contact-col" style="width: 33.33%; padding: 0 0 0 12px; vertical-align: top;" valign="top">
                    <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF; font-weight: 800; text-transform: uppercase;">HEADQUARTERS</div>
                    <div style="padding-top: 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11.5px; line-height: 17px; color: #111827; font-weight: 600;">
                      17 Upper Adesuwa Rd, GRA, Benin City.
                    </div>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- OBSIDIAN LUXURY FOOTER -->
          <tr>
            <td style="padding: 18px 28px; background: #050505;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                  <td style="vertical-align: middle;" valign="middle">
                    <div style="font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; line-height: 17px; color: #FFFFFF; font-weight: 800; letter-spacing: 0.8px;">
                      ESDISPATCH
                    </div>
                    <div style="padding-top: 2px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF;">
                      PREMIUM LOGISTICS &bull; SPEED &amp; PRECISION
                    </div>
                  </td>
                  <td style="vertical-align: middle;" align="right" valign="middle">
                    <a style="display: inline-block; padding: 6px 14px; background: #1F2937; border-radius: 6px; text-decoration: none; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; font-weight: 700; color: #FFB800;" href="https://engraceddispatchnew.vercel.app" target="_blank" rel="noopener">
                      PORTAL &#8599;
                    </a>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- ENCRYPTED COPYRIGHT & CAN-SPAM COMPLIANCE BAR -->
          <tr>
            <td style="padding: 12px 20px; background: #000000; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 9.5px; line-height: 15px; color: #6B7280;" align="center">
              <div>&copy; ${currentYear} ESDISPATCH &bull; Premium Logistics &amp; Dispatch &bull; Encrypted 256-Bit SSL Telemetry</div>
              <div style="padding-top: 4px;">
                <a href="https://www.engracedsmile.com" style="color: #9CA3AF; text-decoration: underline;" target="_blank" rel="noopener">Official Website</a> &bull;
                <a href="https://engraceddispatchnew.vercel.app" style="color: #9CA3AF; text-decoration: underline;" target="_blank" rel="noopener">Web Portal</a> &bull;
                <a href="mailto:support@engracedsmile.com" style="color: #9CA3AF; text-decoration: underline;">Support Desk</a> &bull;
                <a href="https://www.engracedsmile.com/privacy" style="color: #9CA3AF; text-decoration: underline;" target="_blank" rel="noopener">Privacy</a> &bull;
                <a href="https://www.engracedsmile.com/unsubscribe" style="color: #9CA3AF; text-decoration: underline;" target="_blank" rel="noopener">Unsubscribe</a>
              </div>
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
 * Strips HTML tags, converts structural elements to clean linebreaks,
 * and decodes basic entities for a crisp plain text email alternative.
 */
export function extractPlainTextFromHtml(html: string): string {
  if (!html) return '';
  return html
    .replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '')
    .replace(/<script[^>]*>[\s\S]*?<\/script>/gi, '')
    .replace(/<br\s*[\/]?>/gi, '\n')
    .replace(/<\/p>/gi, '\n\n')
    .replace(/<\/h[1-6]>/gi, '\n\n')
    .replace(/<\/div>/gi, '\n')
    .replace(/<\/li>/gi, '\n')
    .replace(/<li>/gi, '• ')
    .replace(/<\/tr>/gi, '\n')
    .replace(/<td[^>]*>/gi, ' ')
    .replace(/<a[^>]*href=["']([^"']+)["'][^>]*>([\s\S]*?)<\/a>/gi, '$2 ($1)')
    .replace(/<[^>]+>/g, '')
    .replace(/&bull;/g, '•')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&nbsp;/g, ' ')
    .replace(/[ \t]+/g, ' ')
    .replace(/\n\s+\n/g, '\n\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

/**
 * Generates an authoritative, beautifully structured ASCII plain-text version
 * of any email template, guaranteeing 1:1 MIME parity with zero HTML-to-text skew.
 */
export function generatePlainTextEmail(opts: MasterEmailOptions): string {
  const lines: string[] = [];

  lines.push('============================================================');
  lines.push('ESDISPATCH | PREMIUM LOGISTICS & DISPATCH');
  lines.push('Benin City Fleet Telemetry • Secured 256-Bit Dispatch');
  lines.push('============================================================');
  lines.push('');

  if (opts.categoryTag) {
    lines.push(`[${opts.categoryTag.toUpperCase()}]`);
  }
  lines.push(opts.headline || opts.title);
  lines.push('------------------------------------------------------------');
  lines.push('');

  lines.push(`Dear ${opts.recipientName || 'Valued Client'},`);
  lines.push('');

  if (opts.contentHtml) {
    lines.push(extractPlainTextFromHtml(opts.contentHtml));
    lines.push('');
  }

  // Voucher Card
  if (opts.voucher) {
    lines.push('************************************************************');
    lines.push(`PROMO CODE: ${opts.voucher.code}`);
    lines.push(`DISCOUNT: ${opts.voucher.discount}`);
    lines.push(opts.voucher.subtext);
    if (opts.voucher.validUntil) {
      lines.push(`Valid Until: ${opts.voucher.validUntil}`);
    }
    lines.push('************************************************************');
    lines.push('');
  }

  // Info Card
  if (opts.infoCard) {
    lines.push('------------------------------------------------------------');
    if (opts.infoCard.category) {
      lines.push(`[${opts.infoCard.category.toUpperCase()}]`);
    }
    lines.push(opts.infoCard.heading);
    if (opts.infoCard.codeDisplay) {
      lines.push('');
      lines.push(`>>> CODE: ${opts.infoCard.codeDisplay} <<<`);
      lines.push('');
    }
    if (opts.infoCard.codeSubtext) {
      lines.push(extractPlainTextFromHtml(opts.infoCard.codeSubtext));
    }
    if (opts.infoCard.bodyText) {
      lines.push(extractPlainTextFromHtml(opts.infoCard.bodyText));
    }
    if (opts.infoCard.rows && opts.infoCard.rows.length > 0) {
      lines.push('');
      opts.infoCard.rows.forEach((r) => {
        lines.push(`  • ${r.label}: ${r.value}`);
      });
    }
    lines.push('------------------------------------------------------------');
    lines.push('');
  }

  // Workflow Steps
  if (opts.steps && opts.steps.length > 0) {
    lines.push('OPERATIONAL WORKFLOW:');
    opts.steps.forEach((s) => {
      lines.push(`[Step ${s.stepNumber}] ${s.title}`);
      lines.push(`  ${s.description}`);
    });
    lines.push('');
  }

  // Service Highlights (Three Cards)
  if (opts.threeCards && opts.threeCards.length > 0) {
    lines.push('SERVICE HIGHLIGHTS:');
    opts.threeCards.forEach((c) => {
      lines.push(`• [${c.tag}] ${c.title}: ${c.description}`);
    });
    lines.push('');
  }

  // Call to Action
  if (opts.ctaText && opts.ctaUrl) {
    lines.push('============================================================');
    lines.push(`ACTION: ${opts.ctaText}`);
    lines.push(`LINK: ${opts.ctaUrl}`);
    lines.push('============================================================');
    lines.push('');
  }

  // Direct Desk Contact
  lines.push('------------------------------------------------------------');
  lines.push('ESDISPATCH DESK • DIRECT CONTACT');
  lines.push('Email: support@engracedsmile.com');
  lines.push('Hotline: +234 905 626 3010');
  lines.push('Headquarters: 17 Upper Adesuwa Rd, GRA, Benin City, Edo State, Nigeria');
  lines.push('Web Portal: https://engraceddispatchnew.vercel.app');
  lines.push('Official Website: https://www.engracedsmile.com');
  lines.push('');
  lines.push('To manage preferences or unsubscribe:');
  lines.push('https://www.engracedsmile.com/unsubscribe');
  lines.push('');
  lines.push(`© ${new Date().getFullYear()} ESDISPATCH. Encrypted 256-Bit SSL Telemetry.`);
  lines.push('============================================================');

  return lines.join('\n');
}

// ============================================================================
// CONCRETE EMAIL INSTANCE RENDERERS
// ============================================================================

/**
 * 1. Sign-Up Account Verification OTP
 * Clean, punchy, beautiful, high-contrast passcode box, no long walls of text.
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
    preheader: `Your verification passcode is ${params.otp}. Valid for ${expiry} minutes.`,
    categoryTag: 'ACCOUNT VERIFICATION',
    recipientName: params.name || 'Valued Client',
    headline: `Welcome to ESDispatch, ${params.name || 'Client'}`,
    heroImageUrl: params.heroImageUrl,
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        Welcome to <strong>ESDispatch</strong> — the gold standard in express logistics and courier dispatch.
      </p>
      <p style="margin: 0 0 4px 0;">
        Enter the one-time passcode below into your mobile application to activate your account:
      </p>
    `,
    infoCard: {
      category: 'CONFIDENTIAL PASSCODE',
      heading: 'One-Time Verification Code',
      codeDisplay: params.otp,
      codeSubtext: `Expires in ${expiry} minutes &bull; Single-use only`,
      badgeText: `⏱ ${expiry} MINUTES VALIDITY`,
      badgeType: 'warning',
      bodyText: 'ESDispatch dispatchers, riders, and support staff will never ask for your verification passcode. Do not disclose it to anyone.',
    },
    threeCards: [
      { tag: 'SECURITY', title: '256-Bit SSL', description: 'End-to-end encrypted dispatch network.' },
      { tag: 'DISPATCH', title: 'Benin City', description: 'Active fleet operating across Edo State.' },
      { tag: 'WALLET', title: 'Escrow Safe', description: 'Automated fund protection on bookings.' },
    ],
    ctaText: 'OPEN ESDISPATCH APP',
    ctaUrl: 'https://engraceddispatchnew.vercel.app',
  });
}

/**
 * 2. Password Reset OTP
 * Direct security alert with single-use authorization code.
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
    preheader: `Your password reset code is ${params.otp}. Valid for ${expiry} minutes.`,
    categoryTag: 'SECURITY ALERT',
    recipientName: params.name || 'User',
    headline: 'Password Reset Authorization',
    heroImageUrl: params.heroImageUrl || '/images/emails/security_light.jpg',
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        We received a formal request to reset the password for your ESDispatch logistics account.
      </p>
      <p style="margin: 0 0 4px 0;">
        If you initiated this request, authorize the update using your confidential passcode below:
      </p>
    `,
    infoCard: {
      category: 'AUTHORIZATION PASSCODE',
      heading: 'Password Reset Code',
      codeDisplay: params.otp,
      codeSubtext: `Valid for ${expiry} minutes &bull; Single-use authorization`,
      badgeText: 'SECURITY DESK ALERT',
      badgeType: 'warning',
      bodyText: 'If you did not initiate this password reset, please secure your account immediately or notify support@engracedsmile.com.',
    },
    threeCards: [
      { tag: 'NOTICE', title: 'Single Use', description: 'Code invalidates after first successful entry.' },
      { tag: 'SHIELD', title: 'Keystore Lock', description: 'Multi-layer device verification active.' },
      { tag: 'HOTLINE', title: 'Emergency', description: 'Direct call to +234 905 626 3010.' },
    ],
    ctaText: 'VISIT SECURITY PORTAL',
    ctaUrl: 'https://engraceddispatchnew.vercel.app',
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
      <p style="margin: 0 0 12px 0;">
        A new sign-in attempt was detected for your account${params.ipOrDevice ? ` from <strong>${params.ipOrDevice}</strong>` : ''}.
      </p>
      <p style="margin: 0 0 4px 0;">
        Enter the two-factor authentication passcode below to confirm your session:
      </p>
    `,
    infoCard: {
      category: '2FA PASSCODE',
      heading: 'Login Authentication Code',
      codeDisplay: params.otp,
      codeSubtext: `Expires in ${expiry} minutes &bull; Session-locked`,
      badgeText: 'CONFIDENTIAL CHALLENGE',
      badgeType: 'info',
    },
    threeCards: [
      { tag: 'DEVICE', title: 'Identity Locked', description: 'Session locked to active client signature.' },
      { tag: 'EXPIRY', title: `${expiry} Minutes`, description: 'Instant auto-expiration window.' },
      { tag: 'PROTECTION', title: 'Zero Sharing', description: 'Never forward or send to dispatchers.' },
    ],
    ctaText: 'CONFIRM ON APP',
    ctaUrl: 'https://engraceddispatchnew.vercel.app',
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
    heroImageUrl: params.heroImageUrl || '/images/emails/onboarding_wallet.png',
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        You have initiated a change or reset of your ESDispatch transaction security PIN.
      </p>
      <p style="margin: 0 0 4px 0;">
        Your wallet PIN secures all balance deductions, delivery escrows, and tip settlements. Use this one-time authorization code to finalize your update:
      </p>
    `,
    infoCard: {
      category: 'TRANSACTION AUTHORIZATION',
      heading: 'PIN Change Authorization Code',
      codeDisplay: params.otp,
      codeSubtext: `Valid for ${expiry} minutes`,
      badgeText: 'WALLET SHIELD',
      badgeType: 'warning',
    },
    threeCards: [
      { tag: 'ESCROW', title: 'Fund Protection', description: 'Secures your Naira wallet balance.' },
      { tag: 'SECURITY', title: 'Hardware Lock', description: 'Validated against device keystore.' },
      { tag: 'SUPPORT', title: 'Questions?', description: 'Call +234 905 626 3010 for immediate support.' },
    ],
    ctaText: 'MANAGE WALLET SETTINGS',
    ctaUrl: 'https://engraceddispatchnew.vercel.app',
  });
}

/**
 * 5. Customer Welcome & Onboarding Guide
 * Rich cards, 3-step visual roadmap, feature cards, and luxury parcel hero.
 */
export function renderCustomerWelcomeEmail(params: {
  name: string;
  heroImageUrl?: string;
  promoCode?: string;
}): string {
  return wrapInMasterLuxuryTemplate({
    title: `Welcome to ESDispatch, ${params.name}!`,
    preheader: `Welcome to ESDispatch. Premium logistics, instant express booking, and live GPS tracking.`,
    categoryTag: 'CUSTOMER WELCOME',
    recipientName: params.name,
    headline: `Welcome to Premium Logistics, ${params.name}`,
    heroImageUrl: params.heroImageUrl || '/images/emails/welcome_light.jpg',
    heroImageCaption: 'Luxury Satin Gold Box &bull; High-Key Studio Logistics',
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        Welcome to <strong>ESDispatch</strong> — Benin City’s premier on-demand delivery network. Whether you are sending fragile goods, eCommerce merchandise, confidential documents, or urgent parcels, our fleet guarantees precision timing and white-glove handling.
      </p>
      <p style="margin: 0 0 4px 0;">
        Here is how seamless dispatching is with your new account:
      </p>
    `,
    steps: [
      {
        stepNumber: '01',
        title: 'Instant Booking',
        description: 'Set your pickup & dropoff coordinates. Review fixed transparent fares with zero hidden charges.',
      },
      {
        stepNumber: '02',
        title: 'Live GPS Telemetry',
        description: 'Track your assigned courier in real time on the interactive map as they navigate across Benin City.',
      },
      {
        stepNumber: '03',
        title: 'Secure Handover PIN',
        description: 'Provide the 4-digit handover PIN to release escrow only after your parcel arrives safely.',
      },
    ],
    voucher: params.promoCode
      ? {
          code: params.promoCode,
          discount: '20% OFF YOUR FIRST 3 BOOKINGS',
          subtext: 'Auto-applies at checkout in the ESDispatch app',
        }
      : undefined,
    threeCards: [
      { tag: 'SPEED', title: '45-Min Express', description: 'Rapid dispatch routes across GRA, Ugbowo, and city center.' },
      { tag: 'SECURITY', title: 'All-Risk Insured', description: 'Every dispatched package is backed by escrow protection.' },
      { tag: 'WALLET', title: 'Unified Balance', description: 'Fund your wallet with Paystack for seamless 1-tap bookings.' },
    ],
    ctaText: 'BOOK YOUR FIRST DISPATCH',
    ctaUrl: 'https://engraceddispatchnew.vercel.app',
  });
}

/**
 * 6. Promotional Campaign & Special Voucher
 * High-tech delivery van hero, gold dashed voucher card, priority rider perks.
 */
export function renderPromotionalCampaignEmail(params: {
  recipientName?: string;
  campaignTitle: string;
  discountHeadline: string;
  voucherCode: string;
  validUntil: string;
  detailsHtml?: string;
  heroImageUrl?: string;
}): string {
  return wrapInMasterLuxuryTemplate({
    title: params.campaignTitle,
    preheader: `Exclusive offer: ${params.discountHeadline} with code ${params.voucherCode}.`,
    categoryTag: 'SPECIAL PROMOTION',
    recipientName: params.recipientName,
    headline: params.campaignTitle,
    heroImageUrl: params.heroImageUrl || '/images/emails/promo_light.jpg',
    heroImageCaption: 'Futuristic Electric Delivery Van &bull; ESDispatch Gilded Fleet',
    contentHtml: params.detailsHtml || `
      <p style="margin: 0 0 12px 0;">
        Experience the fastest, most reliable logistics in Edo State with our exclusive partner discount. For a limited time, enjoy priority dispatch and reduced booking fares on all deliveries.
      </p>
      <p style="margin: 0 0 4px 0;">
        Redeem your promotion code on the ESDispatch app before checkout:
      </p>
    `,
    voucher: {
      code: params.voucherCode,
      discount: params.discountHeadline,
      subtext: 'Valid for express deliveries and scheduled corporate dispatches.',
      validUntil: params.validUntil,
    },
    threeCards: [
      { tag: 'ZERO SURGE', title: 'Locked Pricing', description: 'No rainy day or peak-hour price surges.' },
      { tag: 'PRIORITY', title: 'Express Assignment', description: 'Nearest verified courier assigned within 60 seconds.' },
      { tag: 'LIVE MAP', title: 'Track Anywhere', description: 'Share live tracking links with your parcel recipients.' },
    ],
    ctaText: 'CLAIM PROMO DISCOUNT',
    ctaUrl: 'https://engraceddispatchnew.vercel.app',
  });
}

/**
 * 7. Delivery Handover OTP (Recipient Proof of Delivery)
 * Sleek motorbike courier hero, 4-digit handover passcode, origin/destination route card.
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
    heroImageUrl: params.heroImageUrl || '/images/emails/biker_light.jpg',
    heroImageCaption: 'ESDispatch Courier Fleet &bull; Precision Express Delivery',
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        Your courier <strong style="color: #111827;">${params.courierName || 'ESDispatch Fleet Courier'}</strong> is approaching your delivery destination.
      </p>
      <p style="margin: 0 0 4px 0;">
        <strong>Important:</strong> Provide this 4-digit confirmation code to your courier <em>only after</em> you have physically inspected your parcel:
      </p>
    `,
    infoCard: {
      category: 'PROOF OF DELIVERY PASSCODE',
      heading: 'Parcel Handover Code',
      codeDisplay: params.handoverOtp,
      codeSubtext: 'Provide in person to courier upon parcel arrival',
      badgeText: 'SECURITY HANDOVER',
      badgeType: 'warning',
      rows: [
        { label: 'Tracking Number', value: `#${params.trackingNumber}`, isBold: true },
        { label: 'Assigned Courier', value: params.courierName || 'Verified Fleet Rider' },
        { label: 'Pickup Origin', value: params.pickupAddress },
        { label: 'Dropoff Destination', value: params.dropoffAddress },
      ],
    },
    threeCards: [
      { tag: 'VERIFY', title: 'Inspect Package', description: 'Check seal and condition before sharing code.' },
      { tag: 'ESCROW', title: 'Protected Settlement', description: 'Funds release only upon valid OTP submission.' },
      { tag: 'BENIN CITY', title: 'Live GPS Telemetry', description: 'Active turn-by-turn map tracking.' },
    ],
    ctaText: 'TRACK LIVE ON MAP',
    ctaUrl: `https://engraceddispatchnew.vercel.app/track?id=${params.trackingNumber}`,
  });
}

/**
 * 8. Delivery Official Invoice & Proof of Booking
 * Luxury sealed package hero, itemized fare breakdown table, live tracking.
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
  const rows: EmailInfoRow[] = params.breakdown.map((b) => ({
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
    heroImageUrl: params.heroImageUrl || '/images/emails/receipt_light.jpg',
    heroImageCaption: 'Official Verification & Certified Secure Settlement',
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        Thank you for booking with <strong>ESDispatch</strong>. Your payment of <strong style="color: #B45309;">${params.amountPaid}</strong> has been confirmed and escrowed for delivery.
      </p>
      <p style="margin: 0 0 4px 0;">
        Please find your itemized settlement details and tracking confirmation below:
      </p>
    `,
    infoCard: {
      category: 'TAX INVOICE & SETTLEMENT',
      heading: params.amountPaid,
      bodyText: `Settled via ${params.paymentMethod} &bull; ${params.date}`,
      badgeText: 'ESCROW CONFIRMED',
      badgeType: 'success',
      rows: rows,
    },
    threeCards: [
      { tag: 'SERVICE', title: params.serviceType || 'Express Dispatch', description: 'Door-to-door citywide delivery.' },
      { tag: 'TRACKING', title: `#${params.trackingNumber}`, description: 'Real-time telemetry enabled.' },
      { tag: 'STATUS', title: 'Dispatched', description: 'Fleet assigned & en route.' },
    ],
    ctaText: 'VIEW LIVE TRACKING',
    ctaUrl: `https://engraceddispatchnew.vercel.app/track?id=${params.trackingNumber}`,
  });
}

/**
 * 9. Wallet Transaction Alert (Credit / Debit)
 * Real-time ledger statement with wallet thumbnail, balance, and reference.
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
    heroImageUrl: params.heroImageUrl || '/images/emails/onboarding_wallet.png',
    heroImageCaption: 'ESDispatch Unified Wallet & Secure Escrow Ledger',
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        Your ESDispatch wallet balance has been updated successfully.
      </p>
      <p style="margin: 0 0 4px 0;">
        Transaction summary: <strong>${params.description}</strong>
      </p>
    `,
    infoCard: {
      category: 'TRANSACTION AUDIT',
      heading: `${isCredit ? '+' : '-'}${params.amount}`,
      badgeText: isCredit ? 'CREDIT APPLIED' : 'SETTLEMENT DEBITED',
      badgeType: isCredit ? 'success' : 'info',
      rows: [
        { label: 'Transaction Type', value: params.transactionType, isBold: true },
        { label: 'Updated Balance', value: params.newBalance, isBold: true, isHighlight: true },
        { label: 'Reference Code', value: params.reference },
        { label: 'Timestamp', value: params.date },
      ],
    },
    threeCards: [
      { tag: 'LEDGER', title: 'Instant Audit', description: 'Immutable transaction logging.' },
      { tag: 'SPEED', title: 'Real-Time', description: 'Zero waiting for balance updates.' },
      { tag: 'SETTLEMENT', title: 'Paystack Sync', description: 'Direct automated banking gateway.' },
    ],
    ctaText: 'OPEN WALLET IN APP',
    ctaUrl: 'https://engraceddispatchnew.vercel.app',
  });
}

/**
 * 10. Courier & Merchant Fleet Partner Welcome
 * Courier helmet & key hero, verified partner credentials, direct dispatch hotline.
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
      : 'Corporate Logistics Client';

  return wrapInMasterLuxuryTemplate({
    title: `Welcome to the Fleet, ${params.name}!`,
    preheader: `Welcome to ESDispatch as our ${roleTitle}. Start delivering excellence today.`,
    categoryTag: 'FLEET ONBOARDING',
    recipientName: params.name,
    headline: `Welcome to the Fleet, ${params.name}`,
    heroImageUrl: params.heroImageUrl || '/images/emails/fleet_light.jpg',
    heroImageCaption: 'Official Partner Key & 5-Star Courier Helmet',
    contentHtml: `
      <p style="margin: 0 0 12px 0;">
        Congratulations! You have been officially verified and onboarded as an authorized <strong>${roleTitle}</strong> with ESDispatch.
      </p>
      <p style="margin: 0 0 4px 0;">
        As an esteemed member of our logistics family, you enjoy prompt fleet assignments, automated escrow settlements, transparent earnings, and direct dispatcher guidance across Benin City.
      </p>
    `,
    infoCard: {
      category: 'PARTNER CREDENTIALS',
      heading: roleTitle,
      bodyText: 'Your account is active and connected to our live dispatch network in Benin City.',
      badgeText: 'VERIFIED PARTNER',
      badgeType: 'success',
      rows: [
        { label: 'Authorized Role', value: roleTitle, isBold: true },
        { label: 'Operations Zone', value: 'Benin City & Greater Edo' },
        { label: 'Dispatch Desk', value: '+234 905 626 3010' },
        { label: 'Settlement Cycle', value: 'Daily Wallet Escrow', isHighlight: true },
      ],
    },
    threeCards: [
      { tag: 'STANDARDS', title: 'Zero Compromise', description: 'Strict timing and secure deliveries.' },
      { tag: 'EARNINGS', title: 'Prompt Payouts', description: 'Direct wallet settlements & cumulative tips.' },
      { tag: 'SAFETY', title: 'Live Telemetry', description: 'Continuous GPS safety monitoring.' },
    ],
    ctaText: 'ACCESS PARTNER CONSOLE',
    ctaUrl: params.portalUrl || 'https://engraceddispatchnew.vercel.app',
  });
}

/**
 * 11. Custom Broadcast & Marketing Announcement
 * Fully flexible renderer for custom marketing, announcements, and corporate briefs.
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
  infoCardRows?: EmailInfoRow[];
  steps?: EmailStep[];
  voucher?: EmailVoucher;
  threeCards?: [EmailThreeCard, EmailThreeCard, EmailThreeCard] | EmailThreeCard[];
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
    steps: params.steps,
    voucher: params.voucher,
    threeCards: params.threeCards,
    ctaText: params.ctaText,
    ctaUrl: params.ctaUrl,
  });
}

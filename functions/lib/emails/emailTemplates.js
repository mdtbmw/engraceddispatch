"use strict";
/**
 * ESDISPATCH LUXURY EMAIL ENGINE & TEMPLATES (CLOUD FUNCTIONS BACKEND)
 *
 * Strict Brand & Quality Locks:
 * - Brand Name: "ESDISPATCH"
 * - Official Slogan: "PREMIUM LOGISTICS & DISPATCH"
 * - Palette: Brand Gold (#FFB800 / #D4AF37), Obsidian (#050505), Crisp White (#FFFFFF), Clean Luxury Porcelain (#F9FAFB)
 * - Contrast Rules: NO white text on gold background (Use Obsidian on Gold). NO gold on white.
 * - Universal Client Compatibility: Email-safe HTML, nested tables, inline CSS, fluid media-queries for mobile.
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.resolveEmailImageUrl = resolveEmailImageUrl;
exports.wrapInMasterLuxuryTemplate = wrapInMasterLuxuryTemplate;
exports.extractPlainTextFromHtml = extractPlainTextFromHtml;
exports.generatePlainTextEmail = generatePlainTextEmail;
exports.renderSignUpOtpEmail = renderSignUpOtpEmail;
exports.renderPasswordResetOtpEmail = renderPasswordResetOtpEmail;
exports.renderTwoFactorOtpEmail = renderTwoFactorOtpEmail;
exports.renderPinResetOtpEmail = renderPinResetOtpEmail;
exports.renderCustomerWelcomeEmail = renderCustomerWelcomeEmail;
exports.renderPromotionalCampaignEmail = renderPromotionalCampaignEmail;
exports.renderDeliveryHandoverOtpEmail = renderDeliveryHandoverOtpEmail;
exports.renderDeliveryInvoiceEmail = renderDeliveryInvoiceEmail;
exports.renderWalletTransactionEmail = renderWalletTransactionEmail;
exports.renderPartnerWelcomeEmail = renderPartnerWelcomeEmail;
exports.renderCustomBroadcastEmail = renderCustomBroadcastEmail;
/**
 * Resolves relative image paths to public URLs accessible by external email clients.
 */
function resolveEmailImageUrl(path) {
    if (!path)
        return '';
    if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('data:')) {
        return path;
    }
    var cleanPath = path.startsWith('/') ? path : "/".concat(path);
    // Primary: GitHub Raw repository storage (accessible worldwide)
    return "https://raw.githubusercontent.com/mdtbmw/engraceddispatch/main/public".concat(cleanPath);
}
/**
 * Universal Master Luxury Email Template
 */
function wrapInMasterLuxuryTemplate(opts) {
    var currentYear = new Date().getFullYear();
    var preheader = opts.preheader || "ESDispatch Premium Logistics & Dispatch";
    var categoryTag = (opts.categoryTag || "PREMIUM DISPATCH").toUpperCase();
    var headline = opts.headline || (opts.recipientName ? "Hello ".concat(opts.recipientName, ",") : "Hello Valued Client,");
    var logoUrl = resolveEmailImageUrl('/images/logo/brand-header-logo.png');
    // Hero Image Block
    var heroImageHtml = opts.heroImageUrl
        ? "\n      <tr>\n        <td style=\"padding: 16px 28px 12px 28px; background: #ffffff;\" align=\"center\">\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n            <tr>\n              <td align=\"center\" style=\"border-radius: 12px; overflow: hidden; border: 1px solid #E5E7EB; background-color: #F9FAFB;\">\n                <img \n                  src=\"".concat(resolveEmailImageUrl(opts.heroImageUrl), "\" \n                  alt=\"").concat(opts.heroImageAlt || 'ESDispatch', "\" \n                  width=\"624\"\n                  style=\"display: block; width: 100%; max-width: 624px; height: auto; border: 0; outline: none; text-decoration: none;\"\n                  class=\"responsive-img\"\n                />\n              </td>\n            </tr>\n            ").concat(opts.heroImageCaption
            ? "<tr><td style=\"padding: 8px 4px 0 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; color: #6B7280; text-align: center;\">".concat(opts.heroImageCaption, "</td></tr>")
            : '', "\n          </table>\n        </td>\n      </tr>\n    ")
        : '';
    // Voucher Card Block (e.g. for promotions)
    var voucherHtml = '';
    if (opts.voucher) {
        voucherHtml = "\n      <tr>\n        <td style=\"padding: 12px 28px 16px 28px; background: #ffffff;\">\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse: separate; background-color: #FFFDF5; border: 2px dashed #FFB800; border-radius: 12px;\">\n            <tr>\n              <td style=\"padding: 22px 20px; text-align: center;\" align=\"center\">\n                <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 15px; color: #92400E; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;\">\n                  EXCLUSIVE PROMO VOUCHER\n                </div>\n                <div style=\"padding-top: 10px; font-family: 'Courier New', Courier, monospace; font-size: 34px; line-height: 38px; font-weight: 900; letter-spacing: 6px; color: #050505;\">\n                  ".concat(opts.voucher.code, "\n                </div>\n                <div style=\"padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 16px; line-height: 22px; color: #B45309; font-weight: bold;\">\n                  ").concat(opts.voucher.discount, "\n                </div>\n                <div style=\"padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; line-height: 18px; color: #6B7280;\">\n                  ").concat(opts.voucher.subtext).concat(opts.voucher.validUntil ? " &bull; Valid until ".concat(opts.voucher.validUntil) : '', "\n                </div>\n              </td>\n            </tr>\n          </table>\n        </td>\n      </tr>\n    ");
    }
    // Info Card Block (Passcodes, Invoices, Routing, Ledger)
    var infoCardHtml = '';
    if (opts.infoCard) {
        var card = opts.infoCard;
        var cardInnerRows = '';
        // Code Display (for OTP, Handover code, PIN)
        if (card.codeDisplay) {
            cardInnerRows += "\n        <div style=\"margin: 16px 0; background: #ffffff; border: 1.5px solid #FFB800; border-radius: 10px; padding: 20px 14px; text-align: center; box-shadow: 0 2px 8px rgba(255, 184, 0, 0.12);\">\n          <div style=\"font-family: 'Courier New', Courier, monospace; font-size: 40px; font-weight: 900; letter-spacing: 10px; color: #050505; line-height: 1;\">\n            ".concat(card.codeDisplay, "\n          </div>\n          ").concat(card.codeSubtext
                ? "<div style=\"padding-top: 10px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; color: #6B7280; font-weight: 500;\">".concat(card.codeSubtext, "</div>")
                : '', "\n        </div>\n      ");
        }
        if (card.badgeText) {
            var badgeBg = card.badgeType === 'warning' ? '#FEF3C7' : card.badgeType === 'success' ? '#DCFCE7' : '#EFF6FF';
            var badgeColor = card.badgeType === 'warning' ? '#92400E' : card.badgeType === 'success' ? '#166534' : '#1E40AF';
            var badgeBorder = card.badgeType === 'warning' ? '#FDE68A' : card.badgeType === 'success' ? '#BBF7D0' : '#BFDBFE';
            cardInnerRows += "\n        <div style=\"margin-bottom: 12px;\">\n          <span style=\"display: inline-block; background-color: ".concat(badgeBg, "; color: ").concat(badgeColor, "; border: 1px solid ").concat(badgeBorder, "; border-radius: 6px; padding: 4px 10px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; font-weight: 700;\">\n            ").concat(card.badgeText, "\n          </span>\n        </div>\n      ");
        }
        if (card.bodyText) {
            cardInnerRows += "\n        <div style=\"padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 20px; color: #4B5563;\">\n          ".concat(card.bodyText, "\n        </div>\n      ");
        }
        if (card.rows && card.rows.length > 0) {
            var rowsHtml = card.rows
                .map(function (r) { return "\n            <tr>\n              <td style=\"padding: 10px 0; color: #6B7280; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; border-bottom: 1px solid #F3F4F6;\">\n                ".concat(r.label, "\n              </td>\n              <td style=\"padding: 10px 0; color: ").concat(r.isHighlight ? '#B45309' : '#111827', "; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-weight: ").concat(r.isBold ? '700' : '500', "; font-size: ").concat(r.isHighlight ? '15px' : '13px', "; text-align: right; border-bottom: 1px solid #F3F4F6;\">\n                ").concat(r.value, "\n              </td>\n            </tr>\n          "); })
                .join('');
            cardInnerRows += "\n        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"margin-top: 10px; border-collapse: collapse;\">\n          ".concat(rowsHtml, "\n        </table>\n      ");
        }
        infoCardHtml = "\n      <tr>\n        <td style=\"padding: 12px 28px 14px 28px; background: #ffffff;\">\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse: separate; background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 12px;\">\n            <tr>\n              <td style=\"padding: 22px 20px;\">\n                ".concat(card.category
            ? "<div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #B45309; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;\">".concat(card.category, "</div>")
            : '', "\n                <div style=\"padding-top: ").concat(card.category ? '6px' : '0', "; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 18px; line-height: 24px; color: #111827; font-weight: 800;\">\n                  ").concat(card.heading, "\n                </div>\n                ").concat(cardInnerRows, "\n              </td>\n            </tr>\n          </table>\n        </td>\n      </tr>\n    ");
    }
    // Visual Step Guide Cards (e.g. 1-2-3 How It Works)
    var stepsHtml = '';
    if (opts.steps && opts.steps.length > 0) {
        var stepCards = opts.steps
            .map(function (st) { return "\n        <td class=\"step-col\" style=\"width: ".concat(100 / opts.steps.length, "%; padding: 0 4px; vertical-align: top;\" valign=\"top\">\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"height: 100%; border-collapse: separate; background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 10px;\">\n            <tr>\n              <td style=\"padding: 16px 14px; text-align: left;\">\n                <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 10px;\">\n                  <tr>\n                    <td align=\"center\" style=\"width: 28px; height: 28px; background-color: #FFB800; border-radius: 50%; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; font-weight: 900; color: #050505; line-height: 28px; text-align: center;\">\n                      ").concat(st.stepNumber, "\n                    </td>\n                  </tr>\n                </table>\n                <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 18px; color: #111827; font-weight: 800;\">\n                  ").concat(st.title, "\n                </div>\n                <div style=\"padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 16px; color: #6B7280;\">\n                  ").concat(st.description, "\n                </div>\n              </td>\n            </tr>\n          </table>\n        </td>\n      "); })
            .join('');
        stepsHtml = "\n      <tr>\n        <td style=\"padding: 10px 28px 16px 28px; background: #ffffff;\">\n          <div style=\"padding-bottom: 12px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 15px; color: #92400E; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;\">\n            HOW IT WORKS &bull; 3-STEP DISPATCH\n          </div>\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"steps-table\" style=\"border-collapse: collapse;\">\n            <tr>\n              ".concat(stepCards, "\n            </tr>\n          </table>\n        </td>\n      </tr>\n    ");
    }
    // 3-Column Feature Highlight Cards
    var threeCardsHtml = '';
    if (opts.threeCards && opts.threeCards.length > 0) {
        var cardsCols = opts.threeCards
            .map(function (c) { return "\n        <td class=\"three-card-col\" style=\"width: ".concat(100 / opts.threeCards.length, "%; padding: 0 4px; vertical-align: top;\" valign=\"top\">\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"height: 100%; border-collapse: separate; background: #ffffff; border: 1px solid #E5E7EB; border-radius: 10px;\">\n            <tr>\n              <td style=\"padding: 16px 14px;\">\n                <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #B45309; font-weight: 800; letter-spacing: 1px; text-transform: uppercase;\">\n                  ").concat(c.tag, "\n                </div>\n                <div style=\"padding-top: 6px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 19px; color: #111827; font-weight: 800;\">\n                  ").concat(c.title, "\n                </div>\n                <div style=\"padding-top: 5px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; line-height: 16px; color: #6B7280;\">\n                  ").concat(c.description, "\n                </div>\n              </td>\n            </tr>\n          </table>\n        </td>\n      "); })
            .join('');
        threeCardsHtml = "\n      <tr>\n        <td style=\"padding: 10px 28px 18px 28px; background: #ffffff;\">\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"three-cards-table\" style=\"border-collapse: collapse;\">\n            <tr>\n              ".concat(cardsCols, "\n            </tr>\n          </table>\n        </td>\n      </tr>\n    ");
    }
    // Call to Action Button (Obsidian text on Gold background strictly adhering to AGENTS.md)
    var ctaHtml = '';
    if (opts.ctaText && opts.ctaUrl) {
        ctaHtml = "\n      <tr>\n        <td style=\"padding: 12px 28px 28px 28px; background: #ffffff;\" align=\"center\">\n          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n            <tr>\n              <td align=\"center\" style=\"border-radius: 10px; background-color: #FFB800;\">\n                <a \n                  href=\"".concat(opts.ctaUrl, "\" \n                  target=\"_blank\" \n                  rel=\"noopener\" \n                  style=\"display: inline-block; background-color: #FFB800; color: #050505; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 13px; line-height: 16px; font-weight: 900; letter-spacing: 1px; text-transform: uppercase; text-decoration: none; padding: 15px 36px; border-radius: 10px; border: 1px solid #E5A600;\"\n                >\n                  ").concat(opts.ctaText, "\n                </a>\n              </td>\n            </tr>\n          </table>\n        </td>\n      </tr>\n    ");
    }
    return "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n  <title>".concat(opts.title, "</title>\n  <!--[if mso]>\n  <noscript>\n    <xml>\n      <o:OfficeDocumentSettings>\n        <o:PixelsPerInch>96</o:PixelsPerInch>\n      </o:OfficeDocumentSettings>\n    </xml>\n  </noscript>\n  <![endif]-->\n  <style>\n    body, table, td, a { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }\n    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }\n    img { -ms-interpolation-mode: bicubic; border: 0; outline: none; text-decoration: none; }\n    body { margin: 0; padding: 0; width: 100% !important; background-color: #F3F4F6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }\n    @media only screen and (max-width: 600px) {\n      .outer-wrapper { padding: 10px 4px !important; }\n      .outer-frame { width: 100% !important; border-radius: 8px !important; }\n      .header-padding { padding: 16px 16px !important; }\n      .body-padding { padding: 20px 16px 8px 16px !important; }\n      .headline-text { font-size: 20px !important; line-height: 26px !important; }\n      .steps-table, .steps-table tbody, .steps-table tr { display: block !important; width: 100% !important; }\n      .step-col { display: block !important; width: 100% !important; padding: 0 0 10px 0 !important; box-sizing: border-box !important; }\n      .three-cards-table, .three-cards-table tbody, .three-cards-table tr { display: block !important; width: 100% !important; }\n      .three-card-col { display: block !important; width: 100% !important; padding: 0 0 10px 0 !important; box-sizing: border-box !important; }\n      .contact-table, .contact-table tbody, .contact-table tr { display: block !important; width: 100% !important; }\n      .contact-col { display: block !important; width: 100% !important; padding: 0 0 12px 0 !important; box-sizing: border-box !important; }\n      .responsive-img { width: 100% !important; max-width: 100% !important; height: auto !important; }\n    }\n  </style>\n</head>\n<body style=\"margin: 0; padding: 0; background: #F3F4F6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">\n  <!-- Preheader text (preview summary in email clients) -->\n  <div style=\"display: none; max-height: 0px; overflow: hidden; font-size: 1px; line-height: 1px; color: #F3F4F6; mso-hide: all;\">\n    ").concat(preheader, "\n  </div>\n\n  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background: #F3F4F6;\">\n    <tr>\n      <td align=\"center\" class=\"outer-wrapper\" style=\"padding: 28px 10px; background: #F3F4F6;\">\n        \n        <!-- OUTER FRAME (Max 680px) -->\n        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"outer-frame\" style=\"max-width: 680px; background: #ffffff; border: 1px solid #E5E7EB; border-radius: 16px; border-collapse: separate; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05);\">\n          \n          <!-- OBSIDIAN LUXURY HEADER -->\n          <tr>\n            <td class=\"header-padding\" style=\"padding: 18px 28px; background: #050505;\">\n              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n                <tr>\n                  <!-- Official Brand Logo -->\n                  <td style=\"vertical-align: middle; padding: 0;\" valign=\"middle\">\n                    <a href=\"https://www.engracedsmile.com\" target=\"_blank\" rel=\"noopener\" style=\"text-decoration: none; display: inline-block;\">\n                      <img \n                        src=\"").concat(logoUrl, "\" \n                        alt=\"ESDISPATCH - PREMIUM LOGISTICS & DISPATCH\" \n                        width=\"220\" \n                        height=\"50\" \n                        style=\"display: block; width: 220px; height: auto; max-width: 100%; border: 0;\"\n                      />\n                    </a>\n                  </td>\n                  <!-- Header Right Status Pill -->\n                  <td style=\"vertical-align: middle;\" align=\"right\" valign=\"middle\">\n                    <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n                      <tr>\n                        <td style=\"background-color: rgba(255, 184, 0, 0.12); border: 1px solid rgba(255, 184, 0, 0.35); border-radius: 100px; padding: 5px 12px;\">\n                          <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 9.5px; line-height: 13px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; color: #FFB800;\">\n                            BENIN CITY FLEET\n                          </div>\n                        </td>\n                      </tr>\n                    </table>\n                  </td>\n                </tr>\n              </table>\n            </td>\n          </tr>\n\n          <!-- BRAND GOLD TRACK ACCENT STRIPE -->\n          <tr>\n            <td style=\"height: 3px; background: linear-gradient(90deg, #FFB800 0%, #D4AF37 50%, #FFB800 100%); background-color: #FFB800; font-size: 0; line-height: 0;\">&nbsp;</td>\n          </tr>\n\n          <!-- HERO BANNER IMAGE (OPTIONAL) -->\n          ").concat(heroImageHtml, "\n\n          <!-- EMAIL MAIN BODY -->\n          <tr>\n            <td class=\"body-padding\" style=\"padding: 26px 28px 12px 28px; background: #ffffff;\">\n              <!-- Category Pill Tag -->\n              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n                <tr>\n                  <td style=\"background-color: #FFFBEB; border: 1px solid #FDE68A; border-radius: 100px; padding: 4px 12px;\">\n                    <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; color: #92400E;\">\n                      ").concat(categoryTag, "\n                    </div>\n                  </td>\n                </tr>\n              </table>\n              \n              <!-- Subject / Headline -->\n              <div class=\"headline-text\" style=\"padding-top: 12px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 23px; line-height: 30px; font-weight: 800; color: #111827;\">\n                ").concat(headline, "\n              </div>\n\n              <!-- Message Body -->\n              <div style=\"padding-top: 14px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 14px; line-height: 23px; color: #374151;\">\n                ").concat(opts.contentHtml, "\n              </div>\n            </td>\n          </tr>\n\n          <!-- VOUCHER / PROMO CARD (OPTIONAL) -->\n          ").concat(voucherHtml, "\n\n          <!-- HIGHLIGHT INFORMATION CARD (OPTIONAL) -->\n          ").concat(infoCardHtml, "\n\n          <!-- VISUAL STEPS GUIDE (OPTIONAL) -->\n          ").concat(stepsHtml, "\n\n          <!-- THREE FEATURE CARDS (OPTIONAL) -->\n          ").concat(threeCardsHtml, "\n\n          <!-- CALL TO ACTION (OPTIONAL) -->\n          ").concat(ctaHtml, "\n\n          <!-- CONTACT / SUPPORT SECTION -->\n          <tr>\n            <td style=\"padding: 20px 28px; background: #F9FAFB; border-top: 1px solid #E5E7EB;\">\n              <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #B45309; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;\">\n                ESDISPATCH DESK &bull; DIRECT CONTACT\n              </div>\n              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"contact-table\" style=\"margin-top: 12px; border-collapse: collapse;\">\n                <tr>\n                  <!-- EMAIL -->\n                  <td class=\"contact-col\" style=\"width: 33.33%; padding: 0 12px 0 0; vertical-align: top;\" valign=\"top\">\n                    <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF; font-weight: 800; text-transform: uppercase;\">EMAIL DESK</div>\n                    <a style=\"display: block; padding-top: 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11.5px; line-height: 17px; color: #111827; text-decoration: none; font-weight: 600;\" href=\"mailto:support@engracedsmile.com\">\n                      support@engracedsmile.com\n                    </a>\n                  </td>\n                  <!-- PHONE -->\n                  <td class=\"contact-col\" style=\"width: 33.33%; padding: 0 12px; vertical-align: top;\" valign=\"top\">\n                    <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF; font-weight: 800; text-transform: uppercase;\">HOTLINE</div>\n                    <a style=\"display: block; padding-top: 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11.5px; line-height: 17px; color: #111827; text-decoration: none; font-weight: 600;\" href=\"tel:+2349056263010\">\n                      +234 905 626 3010\n                    </a>\n                  </td>\n                  <!-- ADDRESS -->\n                  <td class=\"contact-col\" style=\"width: 33.33%; padding: 0 0 0 12px; vertical-align: top;\" valign=\"top\">\n                    <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF; font-weight: 800; text-transform: uppercase;\">HEADQUARTERS</div>\n                    <div style=\"padding-top: 4px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11.5px; line-height: 17px; color: #111827; font-weight: 600;\">\n                      17 Upper Adesuwa Rd, GRA, Benin City.\n                    </div>\n                  </td>\n                </tr>\n              </table>\n            </td>\n          </tr>\n\n          <!-- OBSIDIAN LUXURY FOOTER -->\n          <tr>\n            <td style=\"padding: 18px 28px; background: #050505;\">\n              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n                <tr>\n                  <td style=\"vertical-align: middle;\" valign=\"middle\">\n                    <div style=\"font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 12px; line-height: 17px; color: #FFFFFF; font-weight: 800; letter-spacing: 0.8px;\">\n                      ESDISPATCH\n                    </div>\n                    <div style=\"padding-top: 2px; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 10px; line-height: 14px; color: #9CA3AF;\">\n                      PREMIUM LOGISTICS &bull; SPEED &amp; PRECISION\n                    </div>\n                  </td>\n                  <td style=\"vertical-align: middle;\" align=\"right\" valign=\"middle\">\n                    <a style=\"display: inline-block; padding: 6px 14px; background: #1F2937; border-radius: 6px; text-decoration: none; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 11px; font-weight: 700; color: #FFB800;\" href=\"https://engraceddispatchnew.vercel.app\" target=\"_blank\" rel=\"noopener\">\n                      PORTAL &#8599;\n                    </a>\n                  </td>\n                </tr>\n              </table>\n            </td>\n          </tr>\n\n          <!-- ENCRYPTED COPYRIGHT & CAN-SPAM COMPLIANCE BAR -->\n          <tr>\n            <td style=\"padding: 12px 20px; background: #000000; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; font-size: 9.5px; line-height: 15px; color: #6B7280;\" align=\"center\">\n              <div>&copy; ").concat(currentYear, " ESDISPATCH &bull; Premium Logistics &amp; Dispatch &bull; Encrypted 256-Bit SSL Telemetry</div>\n              <div style=\"padding-top: 4px;\">\n                <a href=\"https://www.engracedsmile.com\" style=\"color: #9CA3AF; text-decoration: underline;\" target=\"_blank\" rel=\"noopener\">Official Website</a> &bull;\n                <a href=\"https://engraceddispatchnew.vercel.app\" style=\"color: #9CA3AF; text-decoration: underline;\" target=\"_blank\" rel=\"noopener\">Web Portal</a> &bull;\n                <a href=\"mailto:support@engracedsmile.com\" style=\"color: #9CA3AF; text-decoration: underline;\">Support Desk</a> &bull;\n                <a href=\"https://www.engracedsmile.com/privacy\" style=\"color: #9CA3AF; text-decoration: underline;\" target=\"_blank\" rel=\"noopener\">Privacy</a> &bull;\n                <a href=\"https://www.engracedsmile.com/unsubscribe\" style=\"color: #9CA3AF; text-decoration: underline;\" target=\"_blank\" rel=\"noopener\">Unsubscribe</a>\n              </div>\n            </td>\n          </tr>\n\n        </table>\n\n      </td>\n    </tr>\n  </table>\n</body>\n</html>");
}
/**
 * Strips HTML tags, converts structural elements to clean linebreaks,
 * and decodes basic entities for a crisp plain text email alternative.
 */
function extractPlainTextFromHtml(html) {
    if (!html)
        return '';
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
function generatePlainTextEmail(opts) {
    var lines = [];
    lines.push('============================================================');
    lines.push('ESDISPATCH | PREMIUM LOGISTICS & DISPATCH');
    lines.push('Benin City Fleet Telemetry • Secured 256-Bit Dispatch');
    lines.push('============================================================');
    lines.push('');
    if (opts.categoryTag) {
        lines.push("[".concat(opts.categoryTag.toUpperCase(), "]"));
    }
    lines.push(opts.headline || opts.title);
    lines.push('------------------------------------------------------------');
    lines.push('');
    lines.push("Dear ".concat(opts.recipientName || 'Valued Client', ","));
    lines.push('');
    if (opts.contentHtml) {
        lines.push(extractPlainTextFromHtml(opts.contentHtml));
        lines.push('');
    }
    // Voucher Card
    if (opts.voucher) {
        lines.push('************************************************************');
        lines.push("PROMO CODE: ".concat(opts.voucher.code));
        lines.push("DISCOUNT: ".concat(opts.voucher.discount));
        lines.push(opts.voucher.subtext);
        if (opts.voucher.validUntil) {
            lines.push("Valid Until: ".concat(opts.voucher.validUntil));
        }
        lines.push('************************************************************');
        lines.push('');
    }
    // Info Card
    if (opts.infoCard) {
        lines.push('------------------------------------------------------------');
        if (opts.infoCard.category) {
            lines.push("[".concat(opts.infoCard.category.toUpperCase(), "]"));
        }
        lines.push(opts.infoCard.heading);
        if (opts.infoCard.codeDisplay) {
            lines.push('');
            lines.push(">>> CODE: ".concat(opts.infoCard.codeDisplay, " <<<"));
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
            opts.infoCard.rows.forEach(function (r) {
                lines.push("  \u2022 ".concat(r.label, ": ").concat(r.value));
            });
        }
        lines.push('------------------------------------------------------------');
        lines.push('');
    }
    // Workflow Steps
    if (opts.steps && opts.steps.length > 0) {
        lines.push('OPERATIONAL WORKFLOW:');
        opts.steps.forEach(function (s) {
            lines.push("[Step ".concat(s.stepNumber, "] ").concat(s.title));
            lines.push("  ".concat(s.description));
        });
        lines.push('');
    }
    // Service Highlights (Three Cards)
    if (opts.threeCards && opts.threeCards.length > 0) {
        lines.push('SERVICE HIGHLIGHTS:');
        opts.threeCards.forEach(function (c) {
            lines.push("\u2022 [".concat(c.tag, "] ").concat(c.title, ": ").concat(c.description));
        });
        lines.push('');
    }
    // Call to Action
    if (opts.ctaText && opts.ctaUrl) {
        lines.push('============================================================');
        lines.push("ACTION: ".concat(opts.ctaText));
        lines.push("LINK: ".concat(opts.ctaUrl));
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
    lines.push("\u00A9 ".concat(new Date().getFullYear(), " ESDISPATCH. Encrypted 256-Bit SSL Telemetry."));
    lines.push('============================================================');
    return lines.join('\n');
}
// ============================================================================
// CONCRETE EMAIL INSTANCE RENDERERS
// ============================================================================
function renderSignUpOtpEmail(params) {
    var expiry = params.expiryMinutes || 10;
    return wrapInMasterLuxuryTemplate({
        title: "Verify Your ESDispatch Account (".concat(params.otp, ")"),
        preheader: "Your verification passcode is ".concat(params.otp, ". Valid for ").concat(expiry, " minutes."),
        categoryTag: 'ACCOUNT VERIFICATION',
        recipientName: params.name || 'Valued Client',
        headline: "Welcome to ESDispatch, ".concat(params.name || 'Client'),
        heroImageUrl: params.heroImageUrl,
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        Welcome to <strong>ESDispatch</strong> \u2014 the gold standard in express logistics and courier dispatch.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        Enter the one-time passcode below into your mobile application to activate your account:\n      </p>\n    ",
        infoCard: {
            category: 'CONFIDENTIAL PASSCODE',
            heading: 'One-Time Verification Code',
            codeDisplay: params.otp,
            codeSubtext: "Expires in ".concat(expiry, " minutes &bull; Single-use only"),
            badgeText: "\u23F1 ".concat(expiry, " MINUTES VALIDITY"),
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
function renderPasswordResetOtpEmail(params) {
    var expiry = params.expiryMinutes || 10;
    return wrapInMasterLuxuryTemplate({
        title: "Password Reset Request (".concat(params.otp, ")"),
        preheader: "Your password reset code is ".concat(params.otp, ". Valid for ").concat(expiry, " minutes."),
        categoryTag: 'SECURITY ALERT',
        recipientName: params.name || 'User',
        headline: 'Password Reset Authorization',
        heroImageUrl: params.heroImageUrl || '/images/emails/security_light.jpg',
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        We received a formal request to reset the password for your ESDispatch logistics account.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        If you initiated this request, authorize the update using your confidential passcode below:\n      </p>\n    ",
        infoCard: {
            category: 'AUTHORIZATION PASSCODE',
            heading: 'Password Reset Code',
            codeDisplay: params.otp,
            codeSubtext: "Valid for ".concat(expiry, " minutes &bull; Single-use authorization"),
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
function renderTwoFactorOtpEmail(params) {
    var expiry = params.expiryMinutes || 5;
    return wrapInMasterLuxuryTemplate({
        title: "2FA Login Challenge: ".concat(params.otp),
        preheader: "Your 2FA login code is ".concat(params.otp, "."),
        categoryTag: 'TWO-FACTOR LOGIN',
        recipientName: params.name || 'User',
        headline: 'Second-Factor Verification',
        heroImageUrl: params.heroImageUrl,
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        A new sign-in attempt was detected for your account".concat(params.ipOrDevice ? " from <strong>".concat(params.ipOrDevice, "</strong>") : '', ".\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        Enter the two-factor authentication passcode below to confirm your session:\n      </p>\n    "),
        infoCard: {
            category: '2FA PASSCODE',
            heading: 'Login Authentication Code',
            codeDisplay: params.otp,
            codeSubtext: "Expires in ".concat(expiry, " minutes &bull; Session-locked"),
            badgeText: 'CONFIDENTIAL CHALLENGE',
            badgeType: 'info',
        },
        threeCards: [
            { tag: 'DEVICE', title: 'Identity Locked', description: 'Session locked to active client signature.' },
            { tag: 'EXPIRY', title: "".concat(expiry, " Minutes"), description: 'Instant auto-expiration window.' },
            { tag: 'PROTECTION', title: 'Zero Sharing', description: 'Never forward or send to dispatchers.' },
        ],
        ctaText: 'CONFIRM ON APP',
        ctaUrl: 'https://engraceddispatchnew.vercel.app',
    });
}
function renderPinResetOtpEmail(params) {
    var expiry = params.expiryMinutes || 10;
    return wrapInMasterLuxuryTemplate({
        title: "Authorize Wallet PIN Reset (".concat(params.otp, ")"),
        preheader: "Your PIN reset authorization code is ".concat(params.otp, "."),
        categoryTag: 'WALLET SECURITY',
        recipientName: params.name || 'User',
        headline: 'Wallet PIN Change Request',
        heroImageUrl: params.heroImageUrl || '/images/emails/onboarding_wallet.png',
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        You have initiated a change or reset of your ESDispatch transaction security PIN.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        Your wallet PIN secures all balance deductions, delivery escrows, and tip settlements. Use this one-time authorization code to finalize your update:\n      </p>\n    ",
        infoCard: {
            category: 'TRANSACTION AUTHORIZATION',
            heading: 'PIN Change Authorization Code',
            codeDisplay: params.otp,
            codeSubtext: "Valid for ".concat(expiry, " minutes"),
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
function renderCustomerWelcomeEmail(params) {
    return wrapInMasterLuxuryTemplate({
        title: "Welcome to ESDispatch, ".concat(params.name, "!"),
        preheader: "Welcome to ESDispatch. Premium logistics, instant express booking, and live GPS tracking.",
        categoryTag: 'CUSTOMER WELCOME',
        recipientName: params.name,
        headline: "Welcome to Premium Logistics, ".concat(params.name),
        heroImageUrl: params.heroImageUrl || '/images/emails/welcome_light.jpg',
        heroImageCaption: 'Luxury Satin Gold Box &bull; High-Key Studio Logistics',
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        Welcome to <strong>ESDispatch</strong> \u2014 Benin City\u2019s premier on-demand delivery network. Whether you are sending fragile goods, eCommerce merchandise, confidential documents, or urgent parcels, our fleet guarantees precision timing and white-glove handling.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        Here is how seamless dispatching is with your new account:\n      </p>\n    ",
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
function renderPromotionalCampaignEmail(params) {
    return wrapInMasterLuxuryTemplate({
        title: params.campaignTitle,
        preheader: "Exclusive offer: ".concat(params.discountHeadline, " with code ").concat(params.voucherCode, "."),
        categoryTag: 'SPECIAL PROMOTION',
        recipientName: params.recipientName,
        headline: params.campaignTitle,
        heroImageUrl: params.heroImageUrl || '/images/emails/promo_light.jpg',
        heroImageCaption: 'Futuristic Electric Delivery Van &bull; ESDispatch Gilded Fleet',
        contentHtml: params.detailsHtml || "\n      <p style=\"margin: 0 0 12px 0;\">\n        Experience the fastest, most reliable logistics in Edo State with our exclusive partner discount. For a limited time, enjoy priority dispatch and reduced booking fares on all deliveries.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        Redeem your promotion code on the ESDispatch app before checkout:\n      </p>\n    ",
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
function renderDeliveryHandoverOtpEmail(params) {
    return wrapInMasterLuxuryTemplate({
        title: "Delivery Handover Code for #".concat(params.trackingNumber),
        preheader: "Your ESDispatch handover code is ".concat(params.handoverOtp, " for parcel #").concat(params.trackingNumber, "."),
        categoryTag: 'SHIPMENT IN TRANSIT',
        recipientName: params.recipientName,
        headline: "Your Delivery is Arriving (#".concat(params.trackingNumber, ")"),
        heroImageUrl: params.heroImageUrl || '/images/emails/biker_light.jpg',
        heroImageCaption: 'ESDispatch Courier Fleet &bull; Precision Express Delivery',
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        Your courier <strong style=\"color: #111827;\">".concat(params.courierName || 'ESDispatch Fleet Courier', "</strong> is approaching your delivery destination.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        <strong>Important:</strong> Provide this 4-digit confirmation code to your courier <em>only after</em> you have physically inspected your parcel:\n      </p>\n    "),
        infoCard: {
            category: 'PROOF OF DELIVERY PASSCODE',
            heading: 'Parcel Handover Code',
            codeDisplay: params.handoverOtp,
            codeSubtext: 'Provide in person to courier upon parcel arrival',
            badgeText: 'SECURITY HANDOVER',
            badgeType: 'warning',
            rows: [
                { label: 'Tracking Number', value: "#".concat(params.trackingNumber), isBold: true },
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
        ctaUrl: "https://engraceddispatchnew.vercel.app/track?id=".concat(params.trackingNumber),
    });
}
function renderDeliveryInvoiceEmail(params) {
    var rows = params.breakdown.map(function (b) { return ({
        label: b.label,
        value: b.amount,
    }); });
    rows.push({
        label: 'Total Settlement Paid',
        value: params.amountPaid,
        isBold: true,
        isHighlight: true,
    });
    return wrapInMasterLuxuryTemplate({
        title: "Payment Receipt: Shipment #".concat(params.trackingNumber),
        preheader: "Payment confirmed for shipment #".concat(params.trackingNumber, ". Total: ").concat(params.amountPaid, "."),
        categoryTag: 'PAYMENT CONFIRMED',
        recipientName: params.senderName,
        headline: "Official Delivery Receipt (#".concat(params.trackingNumber, ")"),
        heroImageUrl: params.heroImageUrl || '/images/emails/receipt_light.jpg',
        heroImageCaption: 'Official Verification & Certified Secure Settlement',
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        Thank you for booking with <strong>ESDispatch</strong>. Your payment of <strong style=\"color: #B45309;\">".concat(params.amountPaid, "</strong> has been confirmed and escrowed for delivery.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        Please find your itemized settlement details and tracking confirmation below:\n      </p>\n    "),
        infoCard: {
            category: 'TAX INVOICE & SETTLEMENT',
            heading: params.amountPaid,
            bodyText: "Settled via ".concat(params.paymentMethod, " &bull; ").concat(params.date),
            badgeText: 'ESCROW CONFIRMED',
            badgeType: 'success',
            rows: rows,
        },
        threeCards: [
            { tag: 'SERVICE', title: params.serviceType || 'Express Dispatch', description: 'Door-to-door citywide delivery.' },
            { tag: 'TRACKING', title: "#".concat(params.trackingNumber), description: 'Real-time telemetry enabled.' },
            { tag: 'STATUS', title: 'Dispatched', description: 'Fleet assigned & en route.' },
        ],
        ctaText: 'VIEW LIVE TRACKING',
        ctaUrl: "https://engraceddispatchnew.vercel.app/track?id=".concat(params.trackingNumber),
    });
}
function renderWalletTransactionEmail(params) {
    var isCredit = params.transactionType === 'CREDIT';
    return wrapInMasterLuxuryTemplate({
        title: "Wallet ".concat(params.transactionType, ": ").concat(params.amount),
        preheader: "Wallet ".concat(params.transactionType.toLowerCase(), " of ").concat(params.amount, ". New balance: ").concat(params.newBalance, "."),
        categoryTag: isCredit ? 'WALLET CREDIT ALERT' : 'WALLET DEBIT ALERT',
        recipientName: params.name,
        headline: isCredit ? 'Funds Credited to Your Wallet' : 'Wallet Debit Notification',
        heroImageUrl: params.heroImageUrl || '/images/emails/onboarding_wallet.png',
        heroImageCaption: 'ESDispatch Unified Wallet & Secure Escrow Ledger',
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        Your ESDispatch wallet balance has been updated successfully.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        Transaction summary: <strong>".concat(params.description, "</strong>\n      </p>\n    "),
        infoCard: {
            category: 'TRANSACTION AUDIT',
            heading: "".concat(isCredit ? '+' : '-').concat(params.amount),
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
function renderPartnerWelcomeEmail(params) {
    var roleTitle = params.role === 'rider'
        ? 'Fleet Courier Partner'
        : params.role === 'vendor'
            ? 'Verified Merchant Partner'
            : 'Corporate Logistics Client';
    return wrapInMasterLuxuryTemplate({
        title: "Welcome to the Fleet, ".concat(params.name, "!"),
        preheader: "Welcome to ESDispatch as our ".concat(roleTitle, ". Start delivering excellence today."),
        categoryTag: 'FLEET ONBOARDING',
        recipientName: params.name,
        headline: "Welcome to the Fleet, ".concat(params.name),
        heroImageUrl: params.heroImageUrl || '/images/emails/fleet_light.jpg',
        heroImageCaption: 'Official Partner Key & 5-Star Courier Helmet',
        contentHtml: "\n      <p style=\"margin: 0 0 12px 0;\">\n        Congratulations! You have been officially verified and onboarded as an authorized <strong>".concat(roleTitle, "</strong> with ESDispatch.\n      </p>\n      <p style=\"margin: 0 0 4px 0;\">\n        As an esteemed member of our logistics family, you enjoy prompt fleet assignments, automated escrow settlements, transparent earnings, and direct dispatcher guidance across Benin City.\n      </p>\n    "),
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
function renderCustomBroadcastEmail(params) {
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

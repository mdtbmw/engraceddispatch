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
export declare function resolveEmailImageUrl(path?: string): string;
/**
 * Universal Master Luxury Email Template
 */
export declare function wrapInMasterLuxuryTemplate(opts: MasterEmailOptions): string;
/**
 * Strips HTML tags, converts structural elements to clean linebreaks,
 * and decodes basic entities for a crisp plain text email alternative.
 */
export declare function extractPlainTextFromHtml(html: string): string;
/**
 * Generates an authoritative, beautifully structured ASCII plain-text version
 * of any email template, guaranteeing 1:1 MIME parity with zero HTML-to-text skew.
 */
export declare function generatePlainTextEmail(opts: MasterEmailOptions): string;
export declare function renderSignUpOtpEmail(params: {
    name: string;
    otp: string;
    expiryMinutes?: number;
    heroImageUrl?: string;
}): string;
export declare function renderPasswordResetOtpEmail(params: {
    name: string;
    otp: string;
    expiryMinutes?: number;
    heroImageUrl?: string;
}): string;
export declare function renderTwoFactorOtpEmail(params: {
    name: string;
    otp: string;
    expiryMinutes?: number;
    ipOrDevice?: string;
    heroImageUrl?: string;
}): string;
export declare function renderPinResetOtpEmail(params: {
    name: string;
    otp: string;
    expiryMinutes?: number;
    heroImageUrl?: string;
}): string;
export declare function renderCustomerWelcomeEmail(params: {
    name: string;
    heroImageUrl?: string;
    promoCode?: string;
}): string;
export declare function renderPromotionalCampaignEmail(params: {
    recipientName?: string;
    campaignTitle: string;
    discountHeadline: string;
    voucherCode: string;
    validUntil: string;
    detailsHtml?: string;
    heroImageUrl?: string;
}): string;
export declare function renderDeliveryHandoverOtpEmail(params: {
    trackingNumber: string;
    recipientName: string;
    courierName?: string;
    pickupAddress: string;
    dropoffAddress: string;
    handoverOtp: string;
    heroImageUrl?: string;
}): string;
export declare function renderDeliveryInvoiceEmail(params: {
    trackingNumber: string;
    recipientName: string;
    senderName: string;
    serviceType: string;
    amountPaid: string;
    date: string;
    paymentMethod: string;
    breakdown: {
        label: string;
        amount: string;
    }[];
    heroImageUrl?: string;
}): string;
export declare function renderWalletTransactionEmail(params: {
    name: string;
    transactionType: 'CREDIT' | 'DEBIT';
    amount: string;
    newBalance: string;
    reference: string;
    date: string;
    description: string;
    heroImageUrl?: string;
}): string;
export declare function renderPartnerWelcomeEmail(params: {
    name: string;
    role: 'rider' | 'vendor' | 'customer';
    portalUrl?: string;
    heroImageUrl?: string;
}): string;
export declare function renderCustomBroadcastEmail(params: {
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
}): string;

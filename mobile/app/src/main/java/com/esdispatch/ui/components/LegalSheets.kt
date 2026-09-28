package com.esdispatch.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceSheet(
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val surfaceColor = if (isDark) Charcoal else GoldenWhiteLight
    val textColor = if (isDark) Color.White else Obsidian
    val borderColor = if (isDark) Gold.copy(alpha = 0.3f) else Slate

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isDark) Gold else Obsidian, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = null,
                            tint = if (isDark) Obsidian else Gold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Terms of Service",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "ESDispatch Operational Standards",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Gold else TextGray
                    )
                }
            }

            HorizontalDivider(color = borderColor)

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegalClauseCard(
                    number = "01",
                    title = "Services & Scope of Agreement",
                    content = "ESDispatch provides premium intra-city on-demand motorcycle courier, scheduled package transit, and multi-stop enterprise delivery services. These Terms of Service govern all consignment bookings, fare calculations, and dispatch orders placed via the mobile app or web platform across Benin City and partner network corridors.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "02",
                    title = "Pricing Parameters & Digital Escrow",
                    content = "All delivery fares are determined by real-time distance calculations, package weight tiers, operational density, and optional express surcharges. When booking, fares are held in digital escrow from your in-app wallet and settled upon verified delivery confirmation.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "03",
                    title = "Courier Vetting & Handover Verification",
                    content = "All fleet couriers undergo identity verification, background screening, and vehicle roadworthiness checks. Handover requires recipient input of a unique 4-digit security PIN and tamper-evident photo capture to finalize delivery.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "04",
                    title = "Cancellations & Fee Adjustments",
                    content = "Orders may be cancelled without penalty while in Queued status. Once a courier is assigned and en route to the pickup location, a ₦500 dispatch mobilization fee applies. Consignments already in transit cannot be cancelled.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "05",
                    title = "Prohibited Cargo & Liability",
                    content = "Customers must not tender narcotics, contraband, hazardous chemicals, illegal firearms, or stolen goods. Consignments are insured up to declared item value upon transit verification. Head office: 17 Upper Adesuwa Road, GRA, Benin City, Edo State.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Gold else Obsidian,
                    contentColor = if (isDark) Obsidian else Gold
                )
            ) {
                Text("UNDERSTOOD & AGREE", fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicySheet(
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val surfaceColor = if (isDark) Charcoal else GoldenWhiteLight
    val textColor = if (isDark) Color.White else Obsidian
    val borderColor = if (isDark) Gold.copy(alpha = 0.3f) else Slate

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isDark) Gold else Obsidian, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = if (isDark) Obsidian else Gold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Privacy Policy",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "Data Protection & Telemetry Standards",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Gold else TextGray
                    )
                }
            }

            HorizontalDivider(color = borderColor)

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegalClauseCard(
                    number = "01",
                    title = "Data Collection & Identity Scope",
                    content = "We collect your verified mobile phone number, account email, full name, pickup coordinates, and recipient contact details solely to execute logistics bookings, calculate accurate route estimations, and prevent fraudulent dispatch bookings.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "02",
                    title = "GPS Telemetry & Live Tracking",
                    content = "During active orders, the application records real-time courier GPS coordinates, transit bearings, and geofence boundary arrivals (within 50 meters of recipient). Telemetry is streamed securely to the sender and recipient until handover completion.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "03",
                    title = "PIN Security & Biometric Isolation",
                    content = "Your 4-digit security PIN is encrypted with one-way cryptographic hashing. Biometric credentials (fingerprint or device face unlock) are processed exclusively on your device's hardware keystore and are never transmitted to our remote servers.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "04",
                    title = "Third-Party Data Privacy",
                    content = "We do not sell, rent, or lease customer data to external advertising networks. Recipient phone numbers are exposed exclusively to the assigned courier via masked or direct dialer actions strictly for delivery communication.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )

                LegalClauseCard(
                    number = "05",
                    title = "Compliance & Account Control",
                    content = "Users have full rights to request transaction history audits, correct inaccurate information, or request account data erasure by contacting our compliance desk at compliance@esdispatch.ng.",
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Gold else Obsidian,
                    contentColor = if (isDark) Obsidian else Gold
                )
            ) {
                Text("CLOSE PRIVACY POLICY", fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun LegalClauseCard(
    number: String,
    title: String,
    content: String,
    isDark: Boolean,
    surfaceColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, if (isDark) Color(0x26FFFFFF) else Slate)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = number,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Gold else Obsidian
                )
                Text(
                    text = "•",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = if (isDark) TextGray else Obsidian.copy(alpha = 0.8f)
            )
        }
    }
}

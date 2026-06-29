package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ReferralEntry
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*

@Composable
fun ReferralScreen(viewModel: DeliveryViewModel) {
    val context = LocalContext.current
    val referralCode by viewModel.referralCode.collectAsState()
    val referralStats by viewModel.referralStats.collectAsState()
    val referralHistory by viewModel.referralHistory.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadReferralData() }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader(title = "REFER & EARN", onBack = { viewModel.navigateBack() })

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 120.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().background(BrandGradient, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(72.dp).background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Refer a Friend", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Earn ₦2,000 for every friend who completes their first delivery", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(20.dp))

                            Text("Your Referral Code", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(referralCode, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                            Spacer(Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", referralCode))
                                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White); Spacer(Modifier.width(6.dp)); Text("Copy Code", color = Color.White) }

                                Button(
                                    onClick = {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Join Engraced Smile Dispatch using my referral code: $referralCode\nDownload the app and get your first delivery free!")
                                        }
                                        context.startActivity(Intent.createChooser(share, "Refer a Friend"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) { Icon(Icons.Default.Share, contentDescription = null, tint = BiroBlue); Spacer(Modifier.width(6.dp)); Text("Share", color = BiroBlue) }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                AnimatedCounter(targetValue = referralStats.totalEarned, prefix = "\u20A6", fontSize = 22.sp)
                                Text("Total Earned", color = TextGray, fontSize = 11.sp)
                            }
                        }
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${referralStats.totalReferrals}", color = BiroBlue, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Referrals", color = TextGray, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Referral History", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(12.dp))
                    if (referralHistory.isEmpty()) {
                        EmptyState(Icons.Default.PeopleOutline, "No referrals yet", "Share your code to start earning rewards")
                    } else {
                        referralHistory.forEachIndexed { index, entry ->
                            StaggeredItem(index) {
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(36.dp).clip(CircleShape).background(SuccessGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp)) }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) { Text(entry.name, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(entry.date, color = TextGray, fontSize = 10.sp) }
                                        Text("+\u20A6${entry.reward.toInt()}", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

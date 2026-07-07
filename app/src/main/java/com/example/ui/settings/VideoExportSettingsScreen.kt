package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.SettingsManager
import com.example.ScreenBg
import com.example.CardBg
import com.example.TextSoftColor
import com.example.LuxuryGold
import com.example.TextMutedColor
import kotlinx.coroutines.launch

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoExportSettingsScreen(
    settingsManager: SettingsManager,
    isArabic: Boolean,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentResolution by settingsManager.videoResolution.collectAsState(initial = "1080p")
    val currentFps by settingsManager.videoFps.collectAsState(initial = 30)

    val resolutions = listOf(
        Pair("720p", if (isArabic) "720p (عادي)" else "720p (Normal)"),
        Pair("1080p", if (isArabic) "1080p (عالي الدقة)" else "1080p (HD)"),
        Pair("1440p", if (isArabic) "1440p (2K جودة فائقة)" else "1440p (2K Ultra)"),
        Pair("2160p", if (isArabic) "2160p (4K سينمائي)" else "2160p (4K Cinematic)")
    )
    
    val frameRates = listOf(
        Pair(30, if (isArabic) "30 إطار/ثانية (قياسي)" else "30 FPS (Standard)"),
        Pair(60, if (isArabic) "60 إطار/ثانية (سلس)" else "60 FPS (Smooth)"),
        Pair(90, if (isArabic) "90 إطار/ثانية (سلس جداً)" else "90 FPS (Very Smooth)"),
        Pair(120, if (isArabic) "120 إطار/ثانية (حركة بطيئة)" else "120 FPS (Slo-Mo)")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = if (isArabic) "سيتم تطبيق هذه الإعدادات على مقاطع الفيديو الناتجة في الصفحة الرئيسية وصفحة المقاطع الرائجة."
                       else "These settings will be applied to videos generated on the Home screen and Popular Clips screen.",
                color = TextMutedColor,
                fontSize = 14.sp
            )

            // Resolution Section
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.HighQuality, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isArabic) "جودة الفيديو (الدقة)" else "Video Resolution", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    
                    resolutions.forEach { (resValue, resLabel) ->
                        val isSelected = currentResolution == resValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { scope.launch { settingsManager.setVideoResolution(resValue) } }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(resLabel, color = if (isSelected) LuxuryGold else TextSoftColor, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp))
                            } else {
                                Spacer(modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // FPS Section
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Speed, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isArabic) "معدل الإطارات (FPS)" else "Frame Rate (FPS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    
                    frameRates.forEach { (fpsValue, fpsLabel) ->
                        val isSelected = currentFps == fpsValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { scope.launch { settingsManager.setVideoFps(fpsValue) } }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fpsLabel, color = if (isSelected) LuxuryGold else TextSoftColor, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp))
                            } else {
                                Spacer(modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

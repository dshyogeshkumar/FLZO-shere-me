package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TurboGold
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Interactive3dDeveloperCard(
    modifier: Modifier = Modifier
) {
    val rotationY = remember { Animatable(0f) }
    val rotationX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newY = rotationY.value + dragAmount.x * 0.45f
                            val newX = (rotationX.value - dragAmount.y * 0.25f).coerceIn(-30f, 30f)
                            rotationY.snapTo(newY)
                            rotationX.snapTo(newX)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            // Snap to nearest 0 (front) or 180 / -180 (back)
                            val normalized = (rotationY.value % 360f + 360f) % 360f
                            val targetY = if (normalized in 90f..270f) {
                                (rotationY.value / 360f).toInt() * 360f + (if (rotationY.value >= 0) 180f else -180f)
                            } else {
                                (rotationY.value / 360f).toInt() * 360f
                            }
                            rotationY.animateTo(
                                targetY,
                                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                            )
                            rotationX.animateTo(0f, animationSpec = tween(300))
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val currentDeg = (rotationY.value % 360f + 360f) % 360f
        val isBackFacing = currentDeg in 90f..270f

        Card(
            modifier = Modifier
                .width(320.dp)
                .height(440.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = ElectricCyan.copy(alpha = 0.5f),
                    ambientColor = CyberPurple.copy(alpha = 0.3f)
                )
                .graphicsLayer {
                    this.rotationY = rotationY.value
                    this.rotationX = rotationX.value
                    cameraDistance = 14f * density
                }
                .testTag("interactive_3d_developer_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1420))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                ElectricCyan.copy(alpha = 0.8f),
                                CyberPurple.copy(alpha = 0.6f),
                                Color.Transparent,
                                ElectricCyan.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                if (!isBackFacing) {
                    // FRONT OF CARD
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Brand Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricCyan.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "GEN-Z",
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "FOUNDER",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Developer Image Showcase
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            // Glowing Outer Ring
                            Box(
                                modifier = Modifier
                                    .size(164.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                ElectricCyan,
                                                NeonBlue,
                                                CyberPurple,
                                                TurboGold,
                                                ElectricCyan
                                            )
                                        )
                                    )
                            )
                            // Developer Photo
                            Image(
                                painter = painterResource(id = R.drawable.img_developer_yogesh),
                                contentDescription = "Yogesh - Founder & Developer",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(154.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFF0F1420), CircleShape)
                            )
                        }

                        // Info Details
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Yogesh",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Founder & Full-Stack Developer",
                                color = ElectricCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF161F30),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26354D))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "YouTube",
                                        tint = Color(0xFFFF334B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "@Codingwithflzo",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Bottom Flip Hint
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Rotate",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Drag to rotate 360° for skills",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }
                } else {
                    // BACK OF CARD (Flipped)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { this.rotationY = 180f }
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ABOUT DEVELOPER",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "GEN-Z",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "\"Yogesh is the Founder & Full-Stack Developer at GEN-Z, focused on building modern, high-performance digital products with clean design, practical technology, and a strong focus on user experience.\"",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Column {
                            Text(
                                text = "CORE EXPERTISE",
                                color = CyberPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Full-Stack Dev",
                                    "Android Dev",
                                    "Web Dev",
                                    "UI/UX Design",
                                    "Optimization",
                                    "Architecture",
                                    "Product Dev"
                                ).forEach { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF172033),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3A55))
                                    ) {
                                        Text(
                                            text = skill,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF121927),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TurboGold.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Philosophy",
                                        tint = TurboGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PHILOSOPHY",
                                        color = TurboGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"Build simple. Think bigger. Create technology that people actually love to use.\"",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.thigki.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thigki.ui.theme.UserPrimary

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onViewOrders: () -> Unit,
    onContinueShopping: () -> Unit,
) {
    // Animation cho icon check
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F6)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Icon check animated
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .background(Color(0xFFFFE0D4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Thành công",
                    tint = UserPrimary,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Đặt hàng thành công!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Cảm ơn bạn đã tin tưởng\nThigki Coffee ☕",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Mã đơn hàng
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mã đơn hàng", fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        orderId.take(12).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = UserPrimary,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Đơn hàng đang chờ xác nhận",
                        fontSize = 13.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Nút xem đơn hàng
            Button(
                onClick = onViewOrders,
                colors = ButtonDefaults.buttonColors(containerColor = UserPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Xem đơn hàng của tôi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Nút tiếp tục mua
            OutlinedButton(
                onClick = onContinueShopping,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Tiếp tục mua sắm", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = UserPrimary)
            }
        }
    }
}

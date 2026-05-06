package com.example.thigki.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thigki.data.Order
import com.example.thigki.ui.theme.UserPrimary
import com.example.thigki.ui.viewmodel.AuthViewModel
import com.example.thigki.ui.viewmodel.OrderViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Ánh xạ status code → (nhãn hiển thị, màu) */
private fun statusDisplay(status: String): Pair<String, Color> = when (status) {
    "pending"   -> "⏳ Chờ xác nhận" to Color(0xFFFF9800)
    "confirmed" -> "✅ Đã xác nhận"  to Color(0xFF2196F3)
    "done"      -> "🎉 Hoàn thành"   to Color(0xFF4CAF50)
    "cancelled" -> "❌ Đã huỷ"       to Color(0xFFF44336)
    else        -> status to Color.Gray
}

/** Ánh xạ payment code → nhãn */
private fun paymentLabel(code: String): String = when (code) {
    "COD"           -> "💵 Tiền mặt (COD)"
    "BANK_TRANSFER" -> "🏦 Chuyển khoản"
    "MOMO"          -> "📱 MoMo"
    "ZALO_PAY"      -> "🔵 ZaloPay"
    else            -> code
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val myOrders by orderViewModel.myOrders.collectAsStateWithLifecycle()

    // Bắt đầu lắng nghe khi screen hiển thị
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { orderViewModel.observeMyOrders(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UserPrimary)
            )
        }
    ) { padding ->
        if (myOrders.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Chưa có đơn hàng nào", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(myOrders, key = { it.id }) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val formatter = DecimalFormat("#,###")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi"))
    val (statusLabel, statusColor) = statusDisplay(order.status)

    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Đơn #${order.id.take(8).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        dateFormat.format(Date(order.createdAt)),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Tóm tắt items + tổng
            Text(
                "${order.items.size} sản phẩm • ${paymentLabel(order.paymentMethod)}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tổng: ${formatter.format(order.totalAmount)}đ",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = UserPrimary
                )
                Text(
                    if (expanded) "Thu gọn ▲" else "Chi tiết ▼",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Expandable chi tiết
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
                Spacer(Modifier.height(8.dp))
                // Địa chỉ
                if (order.recipientName.isNotEmpty()) {
                    InfoRow("👤 Người nhận:", order.recipientName)
                    InfoRow("📞 SĐT:", order.phone)
                    InfoRow("📍 Địa chỉ:", order.address)
                }
                if (order.note.isNotEmpty()) {
                    InfoRow("📝 Ghi chú:", order.note)
                }
                Spacer(Modifier.height(8.dp))
                Text("Sản phẩm:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                order.items.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "• ${item.tenSanPham} x${item.quantity}",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${formatter.format(item.gia * item.quantity)}đ",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
        Text(value, fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
    }
}

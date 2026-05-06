package com.example.thigki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.thigki.ui.theme.AdminPrimary
import com.example.thigki.ui.viewmodel.OrderViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tabs lọc trạng thái */
private val STATUS_TABS = listOf(
    "all"       to "Tất cả",
    "pending"   to "Chờ xử lý",
    "confirmed" to "Đã xác nhận",
    "done"      to "Hoàn thành",
    "cancelled" to "Đã huỷ",
)

/** Các trạng thái có thể chuyển sang */
private val STATUS_ACTIONS = listOf(
    "pending"   to "⏳ Chờ xác nhận",
    "confirmed" to "✅ Xác nhận đơn",
    "done"      to "🎉 Hoàn thành",
    "cancelled" to "❌ Huỷ đơn",
)

/** Màu theo trạng thái */
private fun adminStatusColor(status: String): Color = when (status) {
    "pending"   -> Color(0xFFFF9800)
    "confirmed" -> Color(0xFF2196F3)
    "done"      -> Color(0xFF4CAF50)
    "cancelled" -> Color(0xFFF44336)
    else        -> Color.Gray
}

private fun adminStatusLabel(status: String): String = when (status) {
    "pending"   -> "⏳ Chờ xử lý"
    "confirmed" -> "✅ Đã xác nhận"
    "done"      -> "🎉 Hoàn thành"
    "cancelled" -> "❌ Đã huỷ"
    else        -> status
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderListScreen(
    orderViewModel: OrderViewModel,
    onBack: () -> Unit,
) {
    val allOrders by orderViewModel.allOrders.collectAsStateWithLifecycle()

    // Bắt đầu lắng nghe khi vào màn hình
    LaunchedEffect(Unit) {
        orderViewModel.observeAllOrders()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val selectedStatus = STATUS_TABS[selectedTabIndex].first

    // Lọc đơn hàng theo tab
    val filteredOrders = remember(allOrders, selectedStatus) {
        if (selectedStatus == "all") allOrders
        else allOrders.filter { it.status == selectedStatus }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminPrimary)
                )
                // Tab filter
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = AdminPrimary,
                    contentColor = Color.White,
                    edgePadding = 8.dp,
                ) {
                    STATUS_TABS.forEachIndexed { index, (_, label) ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    label,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (filteredOrders.isEmpty()) {
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
                    Text("Không có đơn hàng", fontSize = 16.sp, color = Color.Gray)
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
                items(filteredOrders, key = { it.id }) { order ->
                    AdminOrderCard(
                        order = order,
                        onStatusChange = { newStatus ->
                            orderViewModel.updateOrderStatus(order.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onStatusChange: (String) -> Unit,
) {
    val formatter = DecimalFormat("#,###")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi"))
    var showMenu by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val statusColor = adminStatusColor(order.status)

    Card(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
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
                        order.userEmail,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        dateFormat.format(Date(order.createdAt)),
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            adminStatusLabel(order.status),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Thao tác",
                                tint = Color.Gray
                            )
                        }
                        // Dropdown đổi trạng thái
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            STATUS_ACTIONS.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            color = if (code == order.status)
                                                adminStatusColor(code)
                                            else Color.DarkGray,
                                            fontWeight = if (code == order.status)
                                                FontWeight.Bold
                                            else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        if (code != order.status) onStatusChange(code)
                                    },
                                    enabled = code != order.status
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${order.items.size} sản phẩm",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Text(
                    "${formatter.format(order.totalAmount)}đ",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = AdminPrimary
                )
            }

            // Expandable: thông tin người nhận + sản phẩm
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
                Spacer(Modifier.height(8.dp))
                if (order.recipientName.isNotEmpty()) {
                    AdminInfoRow("👤 Người nhận:", order.recipientName)
                    AdminInfoRow("📞 SĐT:", order.phone)
                    AdminInfoRow("📍 Địa chỉ:", order.address)
                }
                if (order.note.isNotEmpty()) {
                    AdminInfoRow("📝 Ghi chú:", order.note)
                }
                AdminInfoRow("💳 Thanh toán:", when (order.paymentMethod) {
                    "COD" -> "Tiền mặt (COD)"
                    "BANK_TRANSFER" -> "Chuyển khoản"
                    "MOMO" -> "MoMo"
                    "ZALO_PAY" -> "ZaloPay"
                    else -> order.paymentMethod
                })
                Spacer(Modifier.height(8.dp))
                Text("Sản phẩm:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                order.items.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "• ${item.tenSanPham} x${item.quantity}",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${formatter.format(item.gia * item.quantity)}đ",
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Hint expand
            Text(
                if (expanded) "Thu gọn ▲" else "Xem chi tiết ▼",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AdminInfoRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(100.dp))
        Text(value, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}

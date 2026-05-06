package com.example.thigki.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thigki.ui.theme.UserPrimary
import com.example.thigki.ui.viewmodel.AuthViewModel
import com.example.thigki.ui.viewmodel.CartViewModel
import com.example.thigki.ui.viewmodel.OrderViewModel
import java.text.DecimalFormat

/** Các phương thức thanh toán */
private val PAYMENT_METHODS = listOf(
    Triple("COD", "💵", "Tiền mặt khi nhận (COD)"),
    Triple("BANK_TRANSFER", "🏦", "Chuyển khoản ngân hàng"),
    Triple("MOMO", "📱", "Ví MoMo"),
    Triple("ZALO_PAY", "🔵", "ZaloPay"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel,
    onSuccess: (orderId: String) -> Unit,
    onBack: () -> Unit,
) {
    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
    val totalAmount by cartViewModel.totalAmount.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by orderViewModel.isLoading.collectAsStateWithLifecycle()
    val uiMessage by orderViewModel.uiMessage.collectAsStateWithLifecycle()

    val formatter = DecimalFormat("#,###")

    // Form fields
    var recipientName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf("COD") }

    // Validation flags
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }

    // Hiển thị lỗi từ ViewModel
    if (uiMessage != null) {
        LaunchedEffect(uiMessage) {
            // Reset sau 3 giây
            kotlinx.coroutines.delay(3000)
            orderViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đơn hàng", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UserPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lỗi từ server
            if (uiMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        uiMessage ?: "",
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // ── Thông tin người nhận ──
            SectionTitle("📍 Thông tin giao hàng")
            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it; nameError = false },
                label = { Text("Họ và tên người nhận *") },
                isError = nameError,
                supportingText = if (nameError) ({ Text("Vui lòng nhập tên người nhận") }) else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = false },
                label = { Text("Số điện thoại *") },
                isError = phoneError,
                supportingText = if (phoneError) ({ Text("Vui lòng nhập số điện thoại hợp lệ") }) else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; addressError = false },
                label = { Text("Địa chỉ giao hàng *") },
                isError = addressError,
                supportingText = if (addressError) ({ Text("Vui lòng nhập địa chỉ") }) else null,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Phương thức thanh toán ──
            SectionTitle("💳 Phương thức thanh toán")
            PAYMENT_METHODS.forEach { (code, emoji, label) ->
                PaymentMethodItem(
                    emoji = emoji,
                    label = label,
                    selected = selectedPayment == code,
                    onClick = { selectedPayment = code }
                )
            }

            // ── Tóm tắt đơn ──
            SectionTitle("🧾 Tóm tắt đơn hàng")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F6))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cartItems.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${item.product.tenSanPham} x${item.quantity}",
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${formatter.format(item.product.gia * item.quantity)}đ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(
                            "${formatter.format(totalAmount)}đ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = UserPrimary
                        )
                    }
                }
            }

            // ── Nút xác nhận ──
            Button(
                onClick = {
                    // Validate
                    nameError = recipientName.isBlank()
                    phoneError = phone.isBlank() || phone.length < 9
                    addressError = address.isBlank()

                    if (!nameError && !phoneError && !addressError) {
                        val uid = currentUser?.uid ?: return@Button
                        val email = currentUser?.email ?: ""
                        orderViewModel.placeOrder(
                            userId = uid,
                            userEmail = email,
                            cartItems = cartItems,
                            paymentMethod = selectedPayment,
                            recipientName = recipientName.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            note = note.trim(),
                            onSuccess = { orderId ->
                                cartViewModel.clearCart()
                                onSuccess(orderId)
                            }
                        )
                    }
                },
                enabled = !isLoading && cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = UserPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Xác nhận đặt hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
}

@Composable
private fun PaymentMethodItem(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(2.dp, UserPrimary) else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) UserPrimary.copy(alpha = 0.08f) else Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) UserPrimary else Color.DarkGray,
                fontSize = 15.sp
            )
            Spacer(Modifier.weight(1f))
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = UserPrimary)
            )
        }
    }
}

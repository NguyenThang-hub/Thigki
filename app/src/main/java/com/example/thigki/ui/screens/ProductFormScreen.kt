package com.example.thigki.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.thigki.data.Product
import com.example.thigki.ui.theme.AdminPrimary
import com.example.thigki.ui.theme.BackgroundGrey
import com.example.thigki.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    initial: Product,
    productViewModel: ProductViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var ten by rememberSaveable(initial.id) { mutableStateOf(initial.tenSanPham) }
    var loai by rememberSaveable(initial.id) { mutableStateOf(initial.loaiSanPham) }
    var giaStr by rememberSaveable(initial.id) { mutableStateOf(if (initial.gia == 0.0) "" else initial.gia.toString()) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        pickedUri = uri
    }

    val msg by productViewModel.uiMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize().background(BackgroundGrey),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (initial.id.isEmpty()) "THÊM MỚI" else "CHỈNH SỬA",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminPrimary
                )
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            msg?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                }
            }

            // Section: Ảnh
            Text("ẢNH SẢN PHẨM", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminPrimary)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable { pickLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val currentImage: Any? = pickedUri ?: if (initial.file.isNotEmpty()) initial.file else null

                if (currentImage != null) {
                    AsyncImage(
                        model = currentImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Nút đổi ảnh nhỏ phía dưới
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(50.dp), tint = AdminPrimary)
                        Text("Chọn ảnh sản phẩm", color = AdminPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Section: Thông tin
            Text("CHI TIẾT SẢN PHẨM", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminPrimary)

            OutlinedTextField(
                value = ten,
                onValueChange = { ten = it },
                label = { Text("Tên gọi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimary, focusedLabelColor = AdminPrimary)
            )

            OutlinedTextField(
                value = loai,
                onValueChange = { loai = it },
                label = { Text("Phân loại") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimary, focusedLabelColor = AdminPrimary)
            )

            OutlinedTextField(
                value = giaStr,
                onValueChange = { giaStr = it },
                label = { Text("Giá bán (đ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimary, focusedLabelColor = AdminPrimary)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val gia = giaStr.toDoubleOrNull() ?: 0.0
                    val p = initial.copy(
                        tenSanPham = ten.trim(),
                        loaiSanPham = loai.trim(),
                        gia = gia
                    )
                    productViewModel.saveProduct(context, p, pickedUri, onSuccess = onBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                enabled = ten.isNotBlank() && giaStr.isNotBlank()
            ) {
                Text("XÁC NHẬN LƯU", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

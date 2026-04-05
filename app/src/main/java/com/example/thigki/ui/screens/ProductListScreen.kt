package com.example.thigki.ui.screens

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.thigki.data.Product
import com.example.thigki.ui.theme.*
import java.text.DecimalFormat

// Hàm hỗ trợ giải mã Base64 để Coil hiển thị mượt hơn
@Composable
fun rememberImageModel(fileString: String): Any {
    return remember(fileString) {
        if (fileString.startsWith("data:image")) {
            try {
                val base64Data = fileString.substringAfter("base64,")
                Base64.decode(base64Data, Base64.DEFAULT)
            } catch (e: Exception) {
                fileString
            }
        } else if (fileString.isEmpty()) {
            "https://via.placeholder.com/150"
        } else {
            fileString
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductListScreen(
    products: List<Product>,
    onAdd: () -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = DecimalFormat("#,###")

    Scaffold(
        modifier = modifier.fillMaxSize().background(BackgroundGrey),
        topBar = {
            TopAppBar(
                title = { Text("HỆ THỐNG QUẢN TRỊ", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = AdminPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color.White)
            }
        },
    ) { padding ->
        if (products.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Chưa có sản phẩm nào", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(products, key = { it.id }) { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEdit(p) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (p.file.isNotEmpty()) {
                                AsyncImage(
                                    model = rememberImageModel(p.file),
                                    contentDescription = p.tenSanPham,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                                Spacer(Modifier.width(16.dp))
                            }
                            
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = p.tenSanPham.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = p.loaiSanPham,
                                    color = AdminPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${formatter.format(p.gia)} VNĐ",
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                            IconButton(onClick = { onDelete(p) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Xóa",
                                    tint = Color.Red,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProductListScreen(
    products: List<Product>,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().background(BackgroundGrey),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SHOP MUA SẮM", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = UserPrimary
                )
            )
        },
    ) { padding ->
        if (products.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UserPrimary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(products, key = { it.id }) { p ->
                    UserProductItem(p)
                }
            }
        }
    }
}

@Composable
fun UserProductItem(p: Product) {
    val formatter = DecimalFormat("#,###")
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = rememberImageModel(p.file),
                contentDescription = p.tenSanPham,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(p.tenSanPham, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 16.sp)
                Text(p.loaiSanPham, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = UserContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = " ${formatter.format(p.gia)} VNĐ ",
                        color = UserPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

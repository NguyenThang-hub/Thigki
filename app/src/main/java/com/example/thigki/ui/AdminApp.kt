package com.example.thigki.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.thigki.data.Product
import com.example.thigki.ui.screens.AdminOrderListScreen
import com.example.thigki.ui.screens.AdminProductListScreen
import com.example.thigki.ui.screens.ProductFormScreen
import com.example.thigki.ui.theme.AdminPrimary
import com.example.thigki.ui.viewmodel.AuthViewModel
import com.example.thigki.ui.viewmodel.OrderViewModel
import com.example.thigki.ui.viewmodel.ProductViewModel

private data class AdminNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val adminNavItems = listOf(
    AdminNavItem("admin_list", "Sản phẩm", Icons.Default.Inventory),
    AdminNavItem("admin_orders", "Đơn hàng", Icons.Default.Receipt),
)

@Composable
fun AdminProductNavHost(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    orderViewModel: OrderViewModel,
) {
    val navController = rememberNavController()
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Chỉ hiện BottomBar ở 2 màn hình chính của admin
    val showBottomBar = currentRoute in listOf("admin_list", "admin_orders")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    adminNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("admin_list") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AdminPrimary,
                                selectedTextColor = AdminPrimary,
                                indicatorColor = AdminPrimary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "admin_list",
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Danh sách sản phẩm ──
            composable("admin_list") {
                AdminProductListScreen(
                    products = products,
                    onAdd = { navController.navigate("admin_form/new") },
                    onEdit = { p -> navController.navigate("admin_form/${p.id}") },
                    onDelete = { productViewModel.deleteProduct(it.id) },
                    onSignOut = { authViewModel.signOut() },
                )
            }

            // ── Form thêm/sửa sản phẩm ──
            composable(
                route = "admin_form/{productKey}",
                arguments = listOf(navArgument("productKey") { type = NavType.StringType }),
            ) { entry ->
                val key = entry.arguments?.getString("productKey") ?: "new"
                val initial = if (key == "new") {
                    Product()
                } else {
                    products.find { it.id == key } ?: Product(id = key)
                }
                ProductFormScreen(
                    initial = initial,
                    productViewModel = productViewModel,
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Quản lý đơn hàng ──
            composable("admin_orders") {
                AdminOrderListScreen(
                    orderViewModel = orderViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}


package com.example.thigki.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.thigki.ui.screens.*
import com.example.thigki.ui.theme.UserPrimary
import com.example.thigki.ui.viewmodel.AuthViewModel
import com.example.thigki.ui.viewmodel.CartViewModel
import com.example.thigki.ui.viewmodel.OrderViewModel
import com.example.thigki.ui.viewmodel.ProductViewModel

private data class UserNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val userNavItems = listOf(
    UserNavItem("user_home", "Trang chủ", Icons.Default.Home),
    UserNavItem("user_cart", "Giỏ hàng", Icons.Default.ShoppingCart),
    UserNavItem("user_orders", "Đơn hàng", Icons.Default.Receipt),
)

/**
 * NavHost + BottomNavBar cho luồng User.
 */
@Composable
fun UserApp(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val products by productViewModel.products.collectAsStateWithLifecycle()
    val cartCount by cartViewModel.cartCount.collectAsStateWithLifecycle()

    // Chỉ hiện BottomBar ở 3 màn hình chính
    val showBottomBar = currentRoute in listOf("user_home", "user_cart", "user_orders")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    userNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("user_home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        // Badge số lượng trên icon giỏ hàng
                                        if (item.route == "user_cart" && cartCount > 0) {
                                            Badge { Text(cartCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = UserPrimary,
                                selectedTextColor = UserPrimary,
                                indicatorColor = UserPrimary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "user_home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Trang chủ: danh sách sản phẩm ──
            composable("user_home") {
                UserProductListScreen(
                    products = products,
                    cartCount = cartCount,
                    onAddToCart = { cartViewModel.addToCart(it) },
                    onCartClick = { navController.navigate("user_cart") },
                    onSignOut = { authViewModel.signOut() },
                )
            }

            // ── Giỏ hàng ──
            composable("user_cart") {
                CartScreen(
                    cartViewModel = cartViewModel,
                    onCheckout = { navController.navigate("user_checkout") },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Checkout ──
            composable("user_checkout") {
                CheckoutScreen(
                    cartViewModel = cartViewModel,
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel,
                    onSuccess = { orderId ->
                        navController.navigate("user_order_success/$orderId") {
                            // Xóa checkout khỏi back stack
                            popUpTo("user_cart") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Đặt hàng thành công ──
            composable(
                route = "user_order_success/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { entry ->
                val orderId = entry.arguments?.getString("orderId") ?: ""
                OrderSuccessScreen(
                    orderId = orderId,
                    onViewOrders = {
                        navController.navigate("user_orders") {
                            popUpTo("user_home") { inclusive = false }
                        }
                    },
                    onContinueShopping = {
                        navController.navigate("user_home") {
                            popUpTo("user_home") { inclusive = true }
                        }
                    },
                )
            }

            // ── Lịch sử đơn hàng ──
            composable("user_orders") {
                MyOrdersScreen(
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}


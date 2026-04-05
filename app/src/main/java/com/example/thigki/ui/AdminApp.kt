package com.example.thigki.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.thigki.data.Product
import com.example.thigki.ui.screens.AdminProductListScreen
import com.example.thigki.ui.screens.ProductFormScreen
import com.example.thigki.ui.viewmodel.AuthViewModel
import com.example.thigki.ui.viewmodel.ProductViewModel

@Composable
fun AdminProductNavHost(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()
    val products by productViewModel.products.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = "admin_list") {
        composable("admin_list") {
            AdminProductListScreen(
                products = products,
                onAdd = { navController.navigate("admin_form/new") },
                onEdit = { p -> navController.navigate("admin_form/${p.id}") },
                onDelete = { productViewModel.deleteProduct(it.id) },
                onSignOut = { authViewModel.signOut() },
            )
        }
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
    }
}

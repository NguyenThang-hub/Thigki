package com.example.thigki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thigki.ui.AdminProductNavHost
import com.example.thigki.ui.screens.LoginScreen
import com.example.thigki.ui.screens.SignUpScreen
import com.example.thigki.ui.screens.UserProductListScreen
import com.example.thigki.ui.theme.ThiGKITheme
import com.example.thigki.ui.viewmodel.AuthViewModel
import com.example.thigki.ui.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThiGKITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val productViewModel: ProductViewModel = viewModel()
                    
                    val user by authViewModel.currentUser.collectAsStateWithLifecycle()
                    val role by authViewModel.role.collectAsStateWithLifecycle()
                    val authMsg by authViewModel.uiMessage.collectAsStateWithLifecycle()
                    val products by productViewModel.products.collectAsStateWithLifecycle()

                    // State để chuyển đổi giữa Login và SignUp khi chưa đăng nhập
                    var showSignUp by remember { mutableStateOf(false) }

                    when {
                        // 1. Chưa đăng nhập
                        user == null -> {
                            if (showSignUp) {
                                SignUpScreen(
                                    message = authMsg,
                                    onSignUp = { e, p -> authViewModel.signUp(e, p) },
                                    onBack = { 
                                        showSignUp = false
                                        authViewModel.clearMessage() 
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                LoginScreen(
                                    message = authMsg,
                                    onSignIn = { e, p -> authViewModel.signIn(e, p) },
                                    onGoToSignUp = { 
                                        showSignUp = true
                                        authViewModel.clearMessage()
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        
                        // 2. Đang tải quyền (Role)
                        role == null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        
                        // 3. Là Admin
                        role == "admin" -> {
                            AdminProductNavHost(
                                productViewModel = productViewModel,
                                authViewModel = authViewModel,
                            )
                        }
                        
                        // 4. Là User thường
                        else -> {
                            UserProductListScreen(
                                products = products,
                                onSignOut = { authViewModel.signOut() },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}

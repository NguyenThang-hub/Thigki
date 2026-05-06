package com.example.thigki.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thigki.data.CartItem
import com.example.thigki.data.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Quản lý giỏ hàng in-memory (không lưu Firestore).
 * Được dùng chung giữa các screen user thông qua Activity scope.
 */
class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    /** Tổng tiền, tự động tính lại khi giỏ hàng thay đổi */
    val totalAmount: StateFlow<Double> = _cartItems
        .map { items -> items.sumOf { it.product.gia * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** Tổng số lượng sản phẩm trong giỏ (dùng cho badge icon) */
    val cartCount: StateFlow<Int> = _cartItems
        .map { items -> items.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Thêm 1 sản phẩm vào giỏ. Nếu đã có thì tăng số lượng thêm 1. */
    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = current
    }

    /** Xóa hoàn toàn một sản phẩm khỏi giỏ */
    fun removeFromCart(product: Product) {
        _cartItems.value = _cartItems.value.filter { it.product.id != product.id }
    }

    /**
     * Cập nhật số lượng một sản phẩm.
     * Nếu quantity <= 0 thì tự xóa khỏi giỏ.
     */
    fun updateQuantity(product: Product, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(product)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = quantity)
        }
        _cartItems.value = current
    }

    /** Xóa toàn bộ giỏ hàng (gọi sau khi đặt hàng thành công) */
    fun clearCart() {
        _cartItems.value = emptyList()
    }
}

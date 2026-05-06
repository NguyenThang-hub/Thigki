package com.example.thigki.data

/**
 * Đại diện cho một sản phẩm trong giỏ hàng, lưu in-memory.
 */
data class CartItem(
    val product: Product,
    val quantity: Int = 1,
)

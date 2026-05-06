package com.example.thigki.data

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Một mặt hàng trong đơn hàng (snapshot tại thời điểm đặt).
 */
data class OrderItem(
    val productId: String = "",
    val tenSanPham: String = "",
    val gia: Double = 0.0,
    val quantity: Int = 1,
)

/**
 * Đơn hàng lưu trên Firestore /orders/{orderId}.
 * status: "pending" | "confirmed" | "done" | "cancelled"
 * paymentMethod: "COD" | "BANK_TRANSFER" | "MOMO" | "ZALO_PAY"
 */
data class Order(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val recipientName: String = "",
    val phone: String = "",
    val address: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "COD",
    val status: String = "pending",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

/** Chuyển Order thành Map để ghi lên Firestore */
fun Order.toFirestoreMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "userEmail" to userEmail,
    "recipientName" to recipientName,
    "phone" to phone,
    "address" to address,
    "items" to items.map {
        mapOf(
            "productId" to it.productId,
            "tenSanPham" to it.tenSanPham,
            "gia" to it.gia,
            "quantity" to it.quantity,
        )
    },
    "totalAmount" to totalAmount,
    "paymentMethod" to paymentMethod,
    "status" to status,
    "note" to note,
    "createdAt" to createdAt,
)

/** Chuyển DocumentSnapshot của Firestore thành Order */
@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toOrder(): Order? {
    val docId = id ?: return null
    val rawItems = get("items") as? List<Map<String, Any>> ?: emptyList()
    val items = rawItems.map { m ->
        OrderItem(
            productId = m["productId"] as? String ?: "",
            tenSanPham = m["tenSanPham"] as? String ?: "",
            gia = (m["gia"] as? Number)?.toDouble() ?: 0.0,
            quantity = (m["quantity"] as? Number)?.toInt() ?: 1,
        )
    }
    return Order(
        id = docId,
        userId = getString("userId") ?: "",
        userEmail = getString("userEmail") ?: "",
        recipientName = getString("recipientName") ?: "",
        phone = getString("phone") ?: "",
        address = getString("address") ?: "",
        items = items,
        totalAmount = getDouble("totalAmount")
            ?: getLong("totalAmount")?.toDouble() ?: 0.0,
        paymentMethod = getString("paymentMethod") ?: "COD",
        status = getString("status") ?: "pending",
        note = getString("note") ?: "",
        createdAt = getLong("createdAt") ?: 0L,
    )
}

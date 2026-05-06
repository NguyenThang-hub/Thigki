package com.example.thigki.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository xử lý toàn bộ thao tác đơn hàng với Firestore.
 * Collection: /orders
 */
class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val col get() = firestore.collection("orders")

    /**
     * Đặt đơn hàng mới, trả về orderId vừa tạo.
     */
    suspend fun placeOrder(order: Order): String {
        val docRef = col.document()
        val final = order.copy(id = docRef.id)
        docRef.set(final.toFirestoreMap()).await()
        return docRef.id
    }

    /**
     * Lắng nghe real-time danh sách đơn hàng của một user,
     * sắp xếp mới nhất lên trước.
     */
    fun observeOrdersByUser(userId: String): Flow<List<Order>> = callbackFlow {
        // Không dùng orderBy ở đây để tránh cần composite index trên Firestore.
        // Sắp xếp sẽ được thực hiện ở phía client trong ViewModel.
        val reg = col
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toOrder() }.orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    /**
     * Lắng nghe real-time TẤT CẢ đơn hàng (dùng cho admin).
     */
    fun observeAllOrders(): Flow<List<Order>> = callbackFlow {
        // Sắp xếp sẽ được thực hiện ở phía client trong ViewModel.
        val reg = col
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toOrder() }.orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    /**
     * Admin cập nhật trạng thái đơn hàng.
     */
    suspend fun updateOrderStatus(orderId: String, status: String) {
        col.document(orderId).update("status", status).await()
    }
}

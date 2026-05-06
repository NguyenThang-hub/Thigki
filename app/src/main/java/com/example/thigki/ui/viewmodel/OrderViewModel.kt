package com.example.thigki.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thigki.data.CartItem
import com.example.thigki.data.Order
import com.example.thigki.data.OrderItem
import com.example.thigki.data.OrderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Xử lý nghiệp vụ đặt hàng và xem lịch sử đơn.
 */
class OrderViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
) : ViewModel() {

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _myOrders = MutableStateFlow<List<Order>>(emptyList())
    val myOrders: StateFlow<List<Order>> = _myOrders.asStateFlow()

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders.asStateFlow()

    // Job riêng để có thể cancel khi gọi lại
    private var myOrdersJob: Job? = null
    private var allOrdersJob: Job? = null

    /**
     * Đặt hàng: chuyển CartItem → OrderItem, ghi lên Firestore.
     * Gọi onSuccess(orderId) khi thành công.
     */
    fun placeOrder(
        userId: String,
        userEmail: String,
        cartItems: List<CartItem>,
        paymentMethod: String,
        recipientName: String,
        phone: String,
        address: String,
        note: String,
        onSuccess: (orderId: String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val orderItems = cartItems.map { ci ->
                    OrderItem(
                        productId = ci.product.id,
                        tenSanPham = ci.product.tenSanPham,
                        gia = ci.product.gia,
                        quantity = ci.quantity,
                    )
                }
                val total = cartItems.sumOf { it.product.gia * it.quantity }
                val order = Order(
                    userId = userId,
                    userEmail = userEmail,
                    recipientName = recipientName,
                    phone = phone,
                    address = address,
                    items = orderItems,
                    totalAmount = total,
                    paymentMethod = paymentMethod,
                    note = note,
                    status = "pending",
                    createdAt = System.currentTimeMillis(),
                )
                val orderId = orderRepository.placeOrder(order)
                _uiMessage.value = null
                onSuccess(orderId)
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: "Đặt hàng thất bại, vui lòng thử lại"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Lắng nghe real-time đơn hàng của user.
     * Tự hủy job cũ nếu gọi lại.
     */
    fun observeMyOrders(userId: String) {
        myOrdersJob?.cancel()
        myOrdersJob = viewModelScope.launch {
            try {
                orderRepository.observeOrdersByUser(userId).collect { orders ->
                    // Sắp xếp mới nhất lên trên (thay cho orderBy đã bị xóa)
                    _myOrders.value = orders.sortedByDescending { it.createdAt }
                }
            } catch (e: Exception) {
                _uiMessage.value = "Không thể tải đơn hàng: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Admin: lắng nghe tất cả đơn hàng real-time.
     */
    fun observeAllOrders() {
        allOrdersJob?.cancel()
        allOrdersJob = viewModelScope.launch {
            try {
                orderRepository.observeAllOrders().collect { orders ->
                    _allOrders.value = orders.sortedByDescending { it.createdAt }
                }
            } catch (e: Exception) {
                _uiMessage.value = "Không thể tải danh sách đơn: ${e.localizedMessage}"
            }
        }
    }

    /** Admin: đổi trạng thái đơn hàng */
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, status)
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: "Cập nhật trạng thái thất bại"
            }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}

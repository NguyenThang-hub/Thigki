package com.example.thigki.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thigki.data.FileStorageRepository
import com.example.thigki.data.Product
import com.example.thigki.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    val products = productRepository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiMessage = MutableStateFlow<String?>(null)

    fun clearMessage() {
        uiMessage.value = null
    }

    fun saveProduct(context: Context, product: Product, newFileUri: Uri?, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val docRef = productRepository.documentFor(product.id)
                val id = docRef.id
                val fileUrl = if (newFileUri != null) {
                    // Sử dụng FileStorageRepository mới với Context để tạo Base64
                    FileStorageRepository(context).uploadProductFile(id, newFileUri)
                } else {
                    product.file
                }
                val final = product.copy(id = id, file = fileUrl)
                productRepository.write(docRef, final)
                uiMessage.value = null
                onSuccess()
            } catch (e: Exception) {
                uiMessage.value = e.message ?: "Lỗi lưu sản phẩm"
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            try {
                productRepository.delete(id)
            } catch (e: Exception) {
                uiMessage.value = e.message ?: "Lỗi xóa"
            }
        }
    }
}

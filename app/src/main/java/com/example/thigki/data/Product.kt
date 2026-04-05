package com.example.thigki.data

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Chỉ các trường theo đề: tên SP, loại SP, giá, file (URL ảnh/tệp trên Storage).
 */
data class Product(
    val id: String = "",
    val tenSanPham: String = "",
    val loaiSanPham: String = "",
    val gia: Double = 0.0,
    val file: String = "",
)

fun DocumentSnapshot.toProduct(): Product? {
    val docId = id ?: return null
    return Product(
        id = docId,
        tenSanPham = getString("tenSanPham") ?: "",
        loaiSanPham = getString("loaiSanPham") ?: "",
        gia = getDouble("gia") ?: getLong("gia")?.toDouble() ?: 0.0,
        file = getString("file") ?: "",
    )
}

fun Product.toFirestoreMap(): Map<String, Any?> = mapOf(
    "tenSanPham" to tenSanPham,
    "loaiSanPham" to loaiSanPham,
    "gia" to gia,
    "file" to file,
)

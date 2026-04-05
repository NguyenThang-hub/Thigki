package com.example.thigki.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class FileStorageRepository(private val context: Context) {
    suspend fun uploadProductFile(productId: String, localUri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(localUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext ""

                // Nén ảnh để không vượt quá giới hạn 1MB của Firestore
                val outputStream = ByteArrayOutputStream()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                val bytes = outputStream.toByteArray()

                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64String"
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}

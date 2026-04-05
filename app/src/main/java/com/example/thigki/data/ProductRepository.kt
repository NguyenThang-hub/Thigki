package com.example.thigki.data

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val col get() = firestore.collection("products")

    fun documentFor(existingId: String): DocumentReference =
        if (existingId.isEmpty()) col.document() else col.document(existingId)

    fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val reg = col.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toProduct() }.orEmpty()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun write(docRef: DocumentReference, product: Product) {
        docRef.set(product.toFirestoreMap()).await()
    }

    suspend fun delete(id: String) {
        col.document(id).delete().await()
    }
}

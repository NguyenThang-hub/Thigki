package com.example.thigki.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Phân quyền lưu trên Realtime Database: /roles/{uid} = "admin" | "user" */
class RoleRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    fun observeRole(userId: String): Flow<String> = callbackFlow {
        val ref = database.reference.child("roles").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java) ?: "user"
                trySend(value)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}

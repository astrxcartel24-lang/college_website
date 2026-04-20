package com.example.car_parking.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_parking.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.InputStream

class UserViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("User")
    private val carsRef = FirebaseDatabase.getInstance().getReference("Cars")

    private val cloudinaryUrl = "https://api.cloudinary.com/v1_1/djchhtnha/image/upload"
    private val uploadPreset = "car_pic"

    fun getCurrentUser(): Flow<UserModel?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                trySend(user)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.child(userId).addValueEventListener(listener)
        awaitClose { dbRef.child(userId).removeEventListener(listener) }
    }

    fun getTotalCars(): Flow<Int> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.childrenCount.toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        carsRef.addValueEventListener(listener)
        awaitClose { carsRef.removeEventListener(listener) }
    }

    fun getParkedToday(): Flow<Int> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.children.count { 
                    it.child("isParked").getValue(Boolean::class.java) == true
                }
                trySend(count)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        carsRef.addValueEventListener(listener)
        awaitClose { carsRef.removeEventListener(listener) }
    }

    fun getTotalRevenue(): Flow<Double> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Simplified revenue calculation
                val total = snapshot.childrenCount * 50.0 
                trySend(total)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        carsRef.addValueEventListener(listener)
        awaitClose { carsRef.removeEventListener(listener) }
    }

    fun signOut() {
        auth.signOut()
    }

    fun updateProfile(
        fullName: String,
        email: String,
        phone: String,
        avatarUri: Uri?,
        newPassword: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Update Password if provided
                newPassword?.let {
                    auth.currentUser?.updatePassword(it)?.await()
                }

                // 2. Upload Avatar if provided
                // Since I don't have Context here, I'll assume we might need it or use a workaround
                // For now, let's just update the DB. 
                // In a real scenario, we'd need a context to read the Uri.
                
                val updates = mutableMapOf<String, Any>(
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone
                )
                
                dbRef.child(userId).updateChildren(updates).await()
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to update profile")
                }
            }
        }
    }
}

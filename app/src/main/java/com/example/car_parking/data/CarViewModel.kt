package com.example.car_parking.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_parking.models.CarModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class CarViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ── Cloudinary config ─────────────────────────────────────────────────────
    private val cloudinaryUrl  = "https://api.cloudinary.com/v1_1/djchhtnha/image/upload"
    private val uploadPreset   = "car_pic"

    // Optimized OkHttpClient with proper timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // ── UI state ──────────────────────────────────────────────────────────────
    sealed class CarState {
        object Idle    : CarState()
        object Loading : CarState()
        object Success : CarState()
        data class Error(val message: String) : CarState()
    }

    private val _carState = MutableStateFlow<CarState>(CarState.Idle)
    val carState: StateFlow<CarState> = _carState.asStateFlow()

    private val _cars = MutableStateFlow<List<CarModel>>(emptyList())
    val cars: StateFlow<List<CarModel>> = _cars.asStateFlow()

    // ── Save car (Optimized with timeout and compression) ─────────────────────
    fun saveCar(
        car       : CarModel,
        imageUri  : Uri?,
        context   : Context,
        onSuccess : () -> Unit,
        onError   : (String) -> Unit
    ) {
        _carState.value = CarState.Loading
        Log.d("CarViewModel", "========== SAVE CAR STARTED ==========")
        Log.d("CarViewModel", "Car: ${car.make} ${car.model}")
        Log.d("CarViewModel", "Has image: ${imageUri != null}")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Upload image to Cloudinary (if selected) with timeout
                val photoUrl: String = if (imageUri != null) {
                    Log.d("CarViewModel", "Starting image upload...")
                    withTimeout(30000) { // 30 second timeout
                        uploadToCloudinary(context, imageUri)
                    }
                } else {
                    Log.d("CarViewModel", "No image to upload")
                    ""
                }

                Log.d("CarViewModel", "Image upload completed. URL: ${if (photoUrl.isNotEmpty()) "Received" else "None"}")

                // 2. Prepare final car with photo URL
                val carId = if (car.carId.isNotEmpty()) car.carId
                else firestore.collection("cars").document().id

                val finalCar = car.copy(
                    carId    = carId,
                    photoUrl = photoUrl
                )

                // 3. Save to Firestore
                Log.d("CarViewModel", "Saving to Firestore...")
                firestore.collection("cars")
                    .document(carId)
                    .set(finalCar)
                    .await()

                Log.d("CarViewModel", "========== SAVE CAR COMPLETED SUCCESSFULLY ==========")

                withContext(Dispatchers.Main) {
                    _carState.value = CarState.Success
                    onSuccess()
                }

            } catch (e: java.util.concurrent.TimeoutException) {
                Log.e("CarViewModel", "Upload timeout", e)
                withContext(Dispatchers.Main) {
                    val errorMsg = "Image upload timed out. Please try with a smaller image or better connection."
                    _carState.value = CarState.Error(errorMsg)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("CarViewModel", "Save failed", e)
                val msg = when {
                    e.message?.contains("timeout") == true -> "Upload timed out. Please check your internet connection."
                    e.message?.contains("413") == true -> "Image is too large. Please choose a smaller image (max 5MB)."
                    e.message?.contains("network") == true -> "Network error. Please check your connection."
                    e.message?.contains("socket") == true -> "Connection issue. Please try again."
                    else -> e.message ?: "Failed to save car. Please try again."
                }
                withContext(Dispatchers.Main) {
                    _carState.value = CarState.Error(msg)
                    onError(msg)
                }
            }
        }
    }

    // ── Fetch cars for current user ───────────────────────────────────────────
    fun fetchUserCars() {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Log.d("CarViewModel", "No user logged in")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("CarViewModel", "Fetching cars for user: $userId")
                val snapshot = firestore.collection("cars")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val carsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CarModel::class.java)
                }

                Log.d("CarViewModel", "Fetched ${carsList.size} cars")

                withContext(Dispatchers.Main) {
                    _cars.value = carsList
                }
            } catch (e: Exception) {
                Log.e("CarViewModel", "Failed to fetch cars", e)
                withContext(Dispatchers.Main) {
                    _carState.value = CarState.Error(e.message ?: "Failed to fetch cars")
                }
            }
        }
    }

    // ── Delete car ────────────────────────────────────────────────────────────
    fun deleteCar(
        carId     : String,
        onSuccess : () -> Unit,
        onError   : (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("CarViewModel", "Deleting car: $carId")
                firestore.collection("cars").document(carId).delete().await()
                fetchUserCars()   // refresh list

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("CarViewModel", "Failed to delete car", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to delete car")
                }
            }
        }
    }

    // ── Cloudinary upload (Optimized with compression) ────────────────────────
    private suspend fun uploadToCloudinary(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Compress image to reduce upload time
                Log.d("CarViewModel", "Compressing image...")
                val compressedBytes = compressImage(context, uri)
                val sizeInKB = compressedBytes.size / 1024
                Log.d("CarViewModel", "Image compressed to $sizeInKB KB")

                // Step 2: Create multipart request
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "car_image_${System.currentTimeMillis()}.jpg",
                        RequestBody.create("image/jpeg".toMediaTypeOrNull(), compressedBytes)
                    )
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build()

                // Step 3: Execute upload
                val request = Request.Builder()
                    .url(cloudinaryUrl)
                    .post(requestBody)
                    .build()

                Log.d("CarViewModel", "Uploading to Cloudinary...")
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("CarViewModel", "Cloudinary error response: $errorBody")
                    throw Exception("Cloudinary upload failed with code: ${response.code}")
                }

                val responseBody = response.body?.string()
                    ?: throw Exception("Empty response from Cloudinary")

                Log.d("CarViewModel", "Cloudinary response received")

                // Step 4: Parse URL from response
                val url = extractImageUrl(responseBody)
                Log.d("CarViewModel", "Image URL extracted successfully")

                url
            } catch (e: Exception) {
                Log.e("CarViewModel", "Cloudinary upload failed", e)
                throw Exception("Failed to upload image: ${e.message}")
            }
        }
    }

    // ── Compress image before upload (Critical for speed) ─────────────────────
    private fun compressImage(context: Context, uri: Uri): ByteArray {
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open image")

        // Get original image dimensions
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        // Calculate sample size to reduce dimensions (target max 1200px)
        var sampleSize = 1
        val maxDimension = 1200
        while ((options.outWidth / sampleSize) > maxDimension ||
            (options.outHeight / sampleSize) > maxDimension) {
            sampleSize *= 2
        }

        Log.d("CarViewModel", "Original size: ${options.outWidth}x${options.outHeight}, Sample size: $sampleSize")

        // Decode with sample size
        val newInputStream = contentResolver.openInputStream(uri)
        val bitmapOptions = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565 // Saves memory
        }
        val bitmap = android.graphics.BitmapFactory.decodeStream(newInputStream, null, bitmapOptions)
        newInputStream?.close()

        if (bitmap == null) {
            throw Exception("Failed to decode image")
        }

        // Compress to JPEG with 70% quality
        val stream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, stream)
        bitmap.recycle()

        val compressedSize = stream.size() / 1024
        Log.d("CarViewModel", "Compressed size: $compressedSize KB")

        return stream.toByteArray()
    }

    // ── Extract URL from Cloudinary response ──────────────────────────────────
    private fun extractImageUrl(response: String): String {
        // Try multiple patterns to extract URL
        val patterns = listOf(
            "\"secure_url\":\"(.*?)\"",
            "\"url\":\"(.*?)\"",
            "https://res\\.cloudinary\\.com/[^\"]+"
        )

        for (pattern in patterns) {
            val regex = pattern.toRegex()
            val matchResult = regex.find(response)
            if (matchResult != null) {
                var url = if (matchResult.groupValues.size > 1)
                    matchResult.groupValues[1]
                else
                    matchResult.value

                url = url.replace("\\/", "/")
                    .replace("\\\\", "")

                if (url.startsWith("http")) {
                    return url
                }
            }
        }

        throw Exception("Could not parse image URL from Cloudinary response")
    }

    // ── Helper: Save car without image (for testing) ─────────────────────────
    fun saveCarWithoutImage(
        car       : CarModel,
        onSuccess : () -> Unit,
        onError   : (String) -> Unit
    ) {
        _carState.value = CarState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val carId = if (car.carId.isNotEmpty()) car.carId
                else firestore.collection("cars").document().id

                val finalCar = car.copy(carId = carId)

                firestore.collection("cars")
                    .document(carId)
                    .set(finalCar)
                    .await()

                withContext(Dispatchers.Main) {
                    _carState.value = CarState.Success
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to save car")
                }
            }
        }
    }
}
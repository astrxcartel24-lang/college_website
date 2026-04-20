package com.example.car_parking.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.car_parking.models.CarModel
import com.example.car_parking.navigation.ROUTE_ADD_CAR
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

class CarViewModel : ViewModel() {
    private val cloudinaryUrl = "https://api.cloudinary.com/v1_1/djchhtnha/image/upload"
    private val uploadPreset = "car_pic"

    fun getAllCars(): Flow<List<CarModel>> = callbackFlow {
        val ref = FirebaseDatabase.getInstance().getReference("Cars")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cars = snapshot.children.mapNotNull { it.getValue(CarModel::class.java) }
                trySend(cars)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getCarById(carId: String): Flow<CarModel?> = callbackFlow {
        val ref = FirebaseDatabase.getInstance().getReference("Cars").child(carId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val car = snapshot.getValue(CarModel::class.java)
                trySend(car)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun saveCar(
        car: CarModel,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = imageUri?.let { uploadToCloudinary(context, it) } ?: ""
                val updatedCar = car.copy(photoUrl = imageUrl)
                
                val ref = FirebaseDatabase.getInstance().getReference("Cars").child(updatedCar.carId)
                ref.setValue(updatedCar).await()
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to save car")
                }
            }
        }
    }

    fun uploadCar(
        imageUri: Uri?,
        plateNumber: String,
        vehicleType: String,
        driverName: String,
        phoneNumber: String,
        context: Context,
        navController: NavController
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = imageUri?.let { uploadToCloudinary(context, it) }
                val ref = FirebaseDatabase.getInstance().getReference("Cars").push()
                val carData = mapOf(
                    "id" to ref.key,
                    "plateNumber" to plateNumber,
                    "vehicleType" to vehicleType,
                    "driverName" to driverName,
                    "phoneNumber" to phoneNumber,
                    "imageUrl" to imageUrl
                )
                ref.setValue(carData).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Car saved Successfully", Toast.LENGTH_LONG).show()
                    navController.navigate(ROUTE_ADD_CAR)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Car not saved", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun uploadToCloudinary(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val fileBytes = inputStream?.readBytes() ?: throw Exception("Image read failed")
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "image.jpg",
                RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes)
            )
            .addFormDataPart("upload_preset", uploadPreset).build()
        val request = Request.Builder().url(cloudinaryUrl).post(requestBody).build()
        val response = OkHttpClient().newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Upload failed")
        val responseBody = response.body?.string()
        val secureUrl = Regex("\"secure_url\":\"(.*?)\"")
            .find(responseBody ?: "")?.groupValues?.get(1)
        return secureUrl ?: throw Exception("Failed to get image URL")
    }
   
    fun updateCar(car: CarModel, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ref = FirebaseDatabase.getInstance().getReference("Cars").child(car.carId)
                ref.setValue(car).await()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteCar(carId: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ref = FirebaseDatabase.getInstance().getReference("Cars").child(carId)
                ref.removeValue().await()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

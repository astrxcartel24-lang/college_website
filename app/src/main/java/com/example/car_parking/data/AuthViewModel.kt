package com.example.car_parking.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_parking.models.UserModel
import com.example.car_parking.navigation.ROUTE_HOME
import com.example.car_parking.navigation.ROUTE_LOGIN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("User")

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val route: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signup(username: String, email: String, password: String, confirmPassword: String) {
        when {
            username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
            { _authState.value = AuthState.Error("Please fill all the fields"); return }
            password.length < 8 ->
            { _authState.value = AuthState.Error("Password must be at least 8 characters"); return }
            password != confirmPassword ->
            { _authState.value = AuthState.Error("Passwords do not match"); return }
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("User ID not found")
                val user = UserModel(username = username, email = email, userId = userId)
                dbRef.child(userId).setValue(user).await()
                _authState.value = AuthState.Success(ROUTE_LOGIN)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        when {
            email.isBlank() || password.isBlank() ->
            { _authState.value = AuthState.Error("Email and Password required"); return }
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success(ROUTE_HOME)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Success(ROUTE_LOGIN)
    }
}

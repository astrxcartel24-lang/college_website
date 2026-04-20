package com.example.car_parking.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "parking_settings")

class ParkingPreferencesManager(private val context: Context) {

    object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val LICENSE_PLATE = stringPreferencesKey("license_plate")
        val VEHICLE_MODEL = stringPreferencesKey("vehicle_model")
        val DEFAULT_DURATION = intPreferencesKey("default_duration")
        val AUTO_EXTEND = booleanPreferencesKey("auto_extend")
        val SHOW_AVAILABLE_ONLY = booleanPreferencesKey("show_available_only")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val LOW_BALANCE_ALERTS = booleanPreferencesKey("low_balance_alerts")
        val MAP_VIEW_TYPE = stringPreferencesKey("map_view_type")
        val NAVIGATION_APP = stringPreferencesKey("navigation_app")
        val AVOID_TOLLS = booleanPreferencesKey("avoid_tolls")
        val AUTO_PAY = booleanPreferencesKey("auto_pay")
        val DEFAULT_PAYMENT_METHOD = stringPreferencesKey("default_payment_method")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val LOCATION_MODE = stringPreferencesKey("location_mode")
        val SHARE_ANONYMOUS_DATA = booleanPreferencesKey("share_anonymous_data")
        val VOICE_GUIDANCE = booleanPreferencesKey("voice_guidance")
        val LARGE_TIMER = booleanPreferencesKey("large_timer")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            UserPreferences(
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                licensePlate = preferences[PreferencesKeys.LICENSE_PLATE] ?: "",
                vehicleModel = preferences[PreferencesKeys.VEHICLE_MODEL] ?: "",
                defaultDuration = preferences[PreferencesKeys.DEFAULT_DURATION] ?: 60,
                autoExtend = preferences[PreferencesKeys.AUTO_EXTEND] ?: false,
                showAvailableOnly = preferences[PreferencesKeys.SHOW_AVAILABLE_ONLY] ?: true,
                remindersEnabled = preferences[PreferencesKeys.REMINDERS_ENABLED] ?: true,
                lowBalanceAlerts = preferences[PreferencesKeys.LOW_BALANCE_ALERTS] ?: true,
                mapViewType = preferences[PreferencesKeys.MAP_VIEW_TYPE] ?: "Standard",
                navigationApp = preferences[PreferencesKeys.NAVIGATION_APP] ?: "Google Maps",
                avoidTolls = preferences[PreferencesKeys.AVOID_TOLLS] ?: false,
                autoPay = preferences[PreferencesKeys.AUTO_PAY] ?: false,
                defaultPaymentMethod = preferences[PreferencesKeys.DEFAULT_PAYMENT_METHOD] ?: "None",
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "system",
                highContrast = preferences[PreferencesKeys.HIGH_CONTRAST] ?: false,
                locationMode = preferences[PreferencesKeys.LOCATION_MODE] ?: "High Accuracy",
                shareAnonymousData = preferences[PreferencesKeys.SHARE_ANONYMOUS_DATA] ?: true,
                voiceGuidance = preferences[PreferencesKeys.VOICE_GUIDANCE] ?: false,
                largeTimer = preferences[PreferencesKeys.LARGE_TIMER] ?: false
            )
        }

    suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[key] = value
        }
    }

    suspend fun clearCache() {
        context.dataStore.edit { it.clear() }
    }
}

data class UserPreferences(
    val userName: String,
    val licensePlate: String,
    val vehicleModel: String,
    val defaultDuration: Int,
    val autoExtend: Boolean,
    val showAvailableOnly: Boolean,
    val remindersEnabled: Boolean,
    val lowBalanceAlerts: Boolean,
    val mapViewType: String,
    val navigationApp: String,
    val avoidTolls: Boolean,
    val autoPay: Boolean,
    val defaultPaymentMethod: String,
    val themeMode: String,
    val highContrast: Boolean,
    val locationMode: String,
    val shareAnonymousData: Boolean,
    val voiceGuidance: Boolean,
    val largeTimer: Boolean
)

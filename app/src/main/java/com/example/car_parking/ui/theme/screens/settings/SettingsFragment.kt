package com.example.car_parking.ui.theme.screens.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.car_parking.R
import com.example.car_parking.data.ParkingPreferencesManager
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var prefsManager: ParkingPreferencesManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey)
        prefsManager = ParkingPreferencesManager(requireContext())

        // Clear Cache functionality
        findPreference<Preference>("clear_cache")?.setOnPreferenceClickListener {
            try {
                context?.cacheDir?.deleteRecursively()
                Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to clear cache", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == null || key == null) return
        
        lifecycleScope.launch {
            val keys = ParkingPreferencesManager.PreferencesKeys
            when (key) {
                "user_name" -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    prefsManager.updatePreference(keys.USER_NAME, value)
                }
                "license_plate" -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    prefsManager.updatePreference(keys.LICENSE_PLATE, value)
                }
                "vehicle_model" -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    prefsManager.updatePreference(keys.VEHICLE_MODEL, value)
                }
                "map_view_type" -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    prefsManager.updatePreference(keys.MAP_VIEW_TYPE, value)
                }
                "navigation_app" -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    prefsManager.updatePreference(keys.NAVIGATION_APP, value)
                }
                "theme_mode" -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    prefsManager.updatePreference(keys.THEME_MODE, value)
                }
                "location_mode" -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    prefsManager.updatePreference(keys.LOCATION_MODE, value)
                }
                "auto_extend" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.AUTO_EXTEND, value)
                }
                "show_available_only" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.SHOW_AVAILABLE_ONLY, value)
                }
                "reminders_enabled" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.REMINDERS_ENABLED, value)
                }
                "low_balance_alerts" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.LOW_BALANCE_ALERTS, value)
                }
                "avoid_tolls" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.AVOID_TOLLS, value)
                }
                "high_contrast" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.HIGH_CONTRAST, value)
                }
                "share_anonymous_data" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.SHARE_ANONYMOUS_DATA, value)
                }
                "voice_guidance" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.VOICE_GUIDANCE, value)
                }
                "large_timer" -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    prefsManager.updatePreference(keys.LARGE_TIMER, value)
                }
                "default_duration" -> {
                    val value = sharedPreferences.getString(key, "60")?.toIntOrNull() ?: 60
                    prefsManager.updatePreference(keys.DEFAULT_DURATION, value)
                }
            }
        }
    }
}

package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "synthetica_user_prefs"
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val INSTALL_BANNER_DISMISSED = booleanPreferencesKey("install_banner_dismissed")
        val COMPANION_INSTALLED = booleanPreferencesKey("companion_installed")
        val OFFLINE_SIMULATION = booleanPreferencesKey("offline_simulation")
        val ADMIN_WORKSPACE_ENABLED = booleanPreferencesKey("admin_workspace_enabled")
        val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
    }

    val darkThemeEnabled: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[Keys.DARK_THEME] ?: false }

    val installBannerDismissed: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[Keys.INSTALL_BANNER_DISMISSED] ?: false }

    val companionInstalled: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[Keys.COMPANION_INSTALLED] ?: false }

    val offlineSimulationMode: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[Keys.OFFLINE_SIMULATION] ?: false }

    val adminWorkspaceEnabled: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[Keys.ADMIN_WORKSPACE_ENABLED] ?: false }

    val lastSyncMillis: Flow<Long> =
        context.userPrefsDataStore.data.map { it[Keys.LAST_SYNC_MILLIS] ?: 0L }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun dismissInstallBanner() {
        context.userPrefsDataStore.edit { it[Keys.INSTALL_BANNER_DISMISSED] = true }
    }

    suspend fun setCompanionInstalled(installed: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.COMPANION_INSTALLED] = installed }
    }

    suspend fun setOfflineSimulation(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.OFFLINE_SIMULATION] = enabled }
    }

    suspend fun setAdminWorkspace(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.ADMIN_WORKSPACE_ENABLED] = enabled }
    }

    suspend fun updateLastSync(timestampMillis: Long) {
        context.userPrefsDataStore.edit { it[Keys.LAST_SYNC_MILLIS] = timestampMillis }
    }
}

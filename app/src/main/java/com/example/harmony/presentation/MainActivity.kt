package com.example.harmony.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.harmony.core.theme.HarmonyTheme
import com.example.harmony.presentation.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            HarmonyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction: String? = intent.action
        val appLinkData: Uri? = intent.data

        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            // It's an invite link! Extract the server ID.
            val serverId = appLinkData.lastPathSegment // Gets the last part of the path

            if (!serverId.isNullOrBlank()) {
                Log.d("AppLinks", "Received server invite for ID: $serverId")
                // Now you have the serverId, you can:
                // 1. Store it somewhere accessible (e.g., a SharedViewModel, SavedStateHandle).
                // 2. Pass it as an argument to your NavHost startDestination if handling immediately.
                // 3. Navigate directly to a specific "Join Server" screen using the NavController.

                // Example: Navigate (assuming NavGraph is set up to handle this)
                // FindNavController().navigate(NavRoutes.getJoinServerRoute(serverId))
                // Or pass it to the ViewModel that handles joining
                // joinServerViewModel.processInvite(serverId)

                // For now, just log it
                Log.i("HarmonyInvite", "Server ID from link: $serverId")

                // TODO: Implement navigation or action based on serverId

            } else {
                Log.w("AppLinks", "Could not extract server ID from URI: $appLinkData")
            }
        } else {
            Log.d("AppLinks", "Intent is not an App Link VIEW action or data is null.")
        }
    }
}
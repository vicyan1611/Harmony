// harmony/presentation/main/home/HomeScreen.kt
package com.example.harmony.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.harmony.domain.model.Channel // Import Channel
import com.example.harmony.presentation.main.home.components.ChannelList
import com.example.harmony.presentation.main.home.components.ServerListSidebar
import com.example.harmony.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    // Pass NavController for navigation actions
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    // Handle Navigation Events from ViewModel
    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            when (route) {
                NavRoutes.LOGIN -> {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                        launchSingleTop = true // Prevent multiple login screens
                    }
                }
                NavRoutes.CREATE_SERVER -> {
                    navController.navigate(NavRoutes.CREATE_SERVER)
                }
                // Handle other navigation if needed
            }
        }
    }

    Scaffold { paddingValues ->
        // Main Row Layout: Server Sidebar | Channel List | Main Content Area (placeholder)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply scaffold padding
        ) {
            // Server List Sidebar
            ServerListSidebar(
                servers = state.serversWithChannels.map { it.server }, // Extract servers
                selectedServerId = state.selectedServer?.server?.id,
                onServerClick = { server ->
                    // Find the corresponding ServerWithChannels object to update state
                    state.serversWithChannels.find { it.server.id == server.id }?.let {
                        viewModel.onEvent(HomeEvent.OnServerSelected(it))
                    }
                },
                onAddServerClick = { viewModel.onEvent(HomeEvent.OnAddServerClicked) },
                isLoading = state.isLoadingServers,
                modifier = Modifier.fillMaxHeight()
            )

            // Channel List
            ChannelList(
                serverName = state.selectedServer?.server?.name,
                channels = state.selectedServer?.channels ?: emptyList(), // Get channels from selected server
                currentUser = state.user, // Pass user for user panel
                onChannelClick = { channel ->
                    // TODO: Navigate to channel detail screen or handle channel selection
                    println("Channel clicked: ${channel.name}")
                    // navController.navigate(NavRoutes.getChannelDetailRoute(channel.id))
                },
                onAddChannelClick = { /* TODO */ },
                onUserSettingsClick = { /* TODO: Navigate to user settings/profile */
                    // You might need a PROFILE route: navController.navigate(NavRoutes.PROFILE)
                    // Temporary logout action for testing:
                    viewModel.onEvent(HomeEvent.OnLogoutClicked)
                },
                modifier = Modifier.fillMaxHeight().weight(0.3f) // Give channel list some weight
            )

            // Main Content Area (Chat View, etc.) - Placeholder for now
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(0.7f) // Remaining space
//                    .background(MaterialTheme.colorScheme.background), // Or chat background color
//                contentAlignment = Alignment.Center
//            ) {
//                if (state.isLoadingServers || state.isUserLoading) {
//                    CircularProgressIndicator()
//                } else if (state.serversLoadError != null) {
//                    Text("Error loading servers: ${state.serversLoadError}", color = MaterialTheme.colorScheme.error)
//                } else if (state.userLoadError != null && state.user == null) {
//                    Text("Error: ${state.userLoadError}", color = MaterialTheme.colorScheme.error)
//                }
//                else if (state.selectedServer != null) {
//                    val selectedChannel = /* Remember selected channel state here */ null
//                    if (selectedChannel != null) {
//                        Text("Chat for Channel: ${ (selectedChannel as Channel).name }") // Replace with actual chat composable
//                    } else {
//                        Text("Select a channel in ${state.selectedServer?.server?.name}")
//                    }
//
//                } else {
//                    Text("Select a Server or Start a Direct Message") // Default view
//                }
//
//                // Add Logout button temporarily if needed for testing
//                // Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                //      Text("Main Content Area")
//                //      Spacer(Modifier.height(20.dp))
//                //      if (state.user != null) {
//                //         HarmonyButton(
//                //              text = "Logout",
//                //              onClick = { viewModel.onEvent(HomeEvent.OnLogoutClicked) },
//                //              isLoading = isLoggingOut,
//                //              enabled = !isLoggingOut,
//                //              modifier = Modifier.width(200.dp)
//                //          )
//                //      }
//                // }
//            }
        }
    }
}
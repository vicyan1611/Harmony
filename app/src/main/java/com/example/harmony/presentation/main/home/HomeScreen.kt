// harmony/presentation/main/home/HomeScreen.kt
package com.example.harmony.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.livedata.observeAsState
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

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    // Pass NavController for navigation actions
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // --- Use collectAsStateWithLifecycle for better lifecycle awareness ---
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoggingOut by viewModel.isLoggingOut.collectAsStateWithLifecycle()

    // --- State for Pull-to-Refresh ---
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = { viewModel.onEvent(HomeEvent.OnRefresh) }
    )

    // --- LaunchedEffect for Navigation ---
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
                    // Navigate and expect a result
                    navController.navigate(NavRoutes.CREATE_SERVER)
                }
                // Handle other navigation if needed
            }
        }
    }

    // --- LaunchedEffect for Reloading after CreateServerScreen ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val serverCreatedResult = navBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("server_created")?.observeAsState()

    LaunchedEffect(serverCreatedResult?.value) {
        if (serverCreatedResult?.value == true) {
            viewModel.refreshData() // Call the refresh function in ViewModel
            // Reset the result in SavedStateHandle to avoid repeated refreshes
            navBackStackEntry?.savedStateHandle?.set("server_created", false)
        }
    }


    Scaffold { paddingValues ->
        // --- Wrap content in Box for PullRefreshIndicator ---
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
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
                    // Pass refreshing state instead of isLoading for sidebar visual
                    isLoading = state.isLoadingServers && !state.isRefreshing,
                    modifier = Modifier.fillMaxHeight()
                )

                // Channel List
                ChannelList(
                    serverName = state.selectedServer?.server?.name,
                    channels = state.selectedServer?.channels ?: emptyList(), // Get channels from selected server
                    currentUser = state.user, // Pass user for user panel
                    onChannelClick = { channel ->
                        val currentServerId = state.selectedServer?.server?.id
                        if (currentServerId != null) {
                            val route = NavRoutes.getChatRoute(currentServerId, channel.id) //
                            navController.navigate(route)
                        } else {
                            // Handle case where no server is selected (optional)
                            println("Error: No server selected to navigate to channel.")
                        }
                    },
                    onAddChannelClick = { /* TODO */ },
                    onUserSettingsClick = { /* TODO: Navigate to user settings/profile */
                        // You might need a PROFILE route: navController.navigate(NavRoutes.PROFILE)
                        // Temporary logout action for testing:
                        viewModel.onEvent(HomeEvent.OnLogoutClicked)
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.3f) // Give channel list some weight
                )

                // --- Commented out Main Content Area for clarity ---
                // Box(
                //    modifier = Modifier
                //        .fillMaxSize()
                //        .weight(0.7f) // Remaining space
                //        .background(MaterialTheme.colorScheme.background), // Or chat background color
                //    contentAlignment = Alignment.Center
                //) { ... }
            }

            // --- PullRefresh Indicator ---
            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
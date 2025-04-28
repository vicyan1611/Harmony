// harmony/presentation/main/home/HomeScreen.kt
package com.example.harmony.presentation.main.home

// Remove Material imports for bottom sheet
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.harmony.presentation.main.home.components.AddChannelSheetContent
import com.example.harmony.presentation.main.home.components.ChannelList
import com.example.harmony.presentation.main.home.components.ServerListSidebar
import com.example.harmony.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

import androidx.compose.material.ExperimentalMaterialApi // Still needed for PullRefresh

import androidx.compose.material3.SheetValue
import androidx.navigation.NavBackStackEntry

import com.example.harmony.presentation.main.my_profile.MyProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class) // Keep ExperimentalMaterialApi for PullRefresh
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToJoinServer: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoggingOut by viewModel.isLoggingOut.collectAsStateWithLifecycle()
    val currentUser = state.user // Get user from state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = { viewModel.onEvent(HomeEvent.OnRefresh) }
    )
    val scope = rememberCoroutineScope()

    // --- State for Material 3 Modal Bottom Sheet ---
    val addChannelSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Use M3 parameter
    )

    val myProfileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- Effect to Sync ViewModel state with Bottom Sheet State ---
    LaunchedEffect(state.isAddChannelSheetVisible, addChannelSheetState.isVisible) {
        scope.launch {
            if (state.isAddChannelSheetVisible && !addChannelSheetState.isVisible) {
                addChannelSheetState.show()
            } else if (!state.isAddChannelSheetVisible && addChannelSheetState.isVisible) {
                // Check if addChannelSheetState thinks it's visible but shouldn't be
                addChannelSheetState.hide()
            }
        }
    }

    // --- Effect to Sync ViewModel state with My Profile Bottom Sheet State ---
    LaunchedEffect(state.isMyProfileSheetVisible, myProfileSheetState.isVisible) {
        scope.launch {
            if (state.isMyProfileSheetVisible && !myProfileSheetState.isVisible) {
                myProfileSheetState.show()
            } else if (!state.isMyProfileSheetVisible && myProfileSheetState.isVisible) {
                myProfileSheetState.hide()
            }
        }
    }
    // Handle sheet dismissal confirmation for MyProfileSheet
    LaunchedEffect(myProfileSheetState.targetValue) {
        if (myProfileSheetState.targetValue == SheetValue.Hidden && state.isMyProfileSheetVisible) {
            // User swiped down or clicked outside
            viewModel.onEvent(HomeEvent.OnDismissMyProfileSheet)
        }
    }

    // --- Get NavBackStackEntry ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // --- LaunchedEffect for Reloading after CreateServerScreen ---
    val serverCreatedResult = navBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("server_created")?.observeAsState()
    LaunchedEffect(serverCreatedResult?.value) {
        if (serverCreatedResult?.value == true) {
            viewModel.refreshData() // Call existing refresh
            navBackStackEntry?.savedStateHandle?.remove<Boolean>("server_created") // Use remove instead of set to false
        }
    }

    // --- NEW: LaunchedEffect for Reloading after ConfigServerScreen ---
    val serverUpdatedResult = navBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("server_list_updated")?.observeAsState()
    LaunchedEffect(serverUpdatedResult?.value) {
        if (serverUpdatedResult?.value == true) {
            viewModel.refreshData() // Call existing refresh
            navBackStackEntry?.savedStateHandle?.remove<Boolean>("server_list_updated") // Clear the result
        }
    }

    // --- LaunchedEffect for Navigation (Handling NavigationCommand) ---
    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collectLatest { command ->
            when (command) {
                is NavigationCommand.NavigateTo -> {
                    // Handle standard navigation
                    if (command.route == NavRoutes.LOGIN) { // Specific handling for logout
                        navController.navigate(command.route) {
                            popUpTo(NavRoutes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        // General navigation for other routes (CreateServer, Settings, ConfigServer etc.)
                        navController.navigate(command.route)
                    }
                }
                is NavigationCommand.NavigateBack -> {
                    navController.popBackStack()
                }
                is NavigationCommand.NavigateBackWithResult -> {
                    // Set result on previous entry and pop back stack
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        command.result.first,
                        command.result.second
                    )
                    navController.popBackStack()
                }
                // Add other command types if needed
            }
        }
    }

    // --- Main Screen Content ---
    Scaffold { paddingValues ->
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ServerListSidebar(
                    servers = state.serversWithChannels.map { it.server },
                    selectedServerId = state.selectedServer?.server?.id,
                    onServerClick = { server ->
                        state.serversWithChannels.find { it.server.id == server.id }?.let {
                            viewModel.onEvent(HomeEvent.OnServerSelected(it))
                        }
                    },
                    onAddServerClick = { viewModel.onEvent(HomeEvent.OnAddServerClicked) },
                    onDmButtonClick = { navController.navigate(NavRoutes.DM_LIST) },
                    isLoading = state.isLoadingServers && !state.isRefreshing,
                    modifier = Modifier.fillMaxHeight(),
                    onJoinServerClick = onNavigateToJoinServer
                )

                ChannelList(
                    serverName = state.selectedServer?.server?.name,
                    channels = state.selectedServer?.channels ?: emptyList(),
                    currentUser = state.user,
                    onTextChannelClick = { channel ->
                        val currentServerId = state.selectedServer?.server?.id
                        if (currentServerId != null) {
                            val route = NavRoutes.getChatRoute(currentServerId, channel.id) // Existing chat route
                            navController.navigate(route)
                        } else {
                            // Handle error: No server selected
                        }
                    },
                    onVoiceChannelClick = { channel ->
                        val currentServerId = state.selectedServer?.server?.id
                        if (currentServerId != null) {
                            val route = NavRoutes.getVoiceChannelRoute(currentServerId, channel.id) // New voice route
                            navController.navigate(route)
                        } else {
                            // Handle error: No server selected
                        }
                    },
                    onAddChannelClick = { viewModel.onEvent(HomeEvent.OnShowAddChannelSheet) },
                    onUserSettingsClick = { viewModel.onEvent(HomeEvent.OnLogoutClicked) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.3f),
                    isHost = state.selectedServer?.server?.ownerId != null && state.user?.id != null && state.selectedServer?.server?.ownerId == state.user?.id,
                    onAvatarClick = { viewModel.onEvent(HomeEvent.OnShowMyProfileSheet) },
                    viewModel = viewModel
                )
            }

            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }

    // --- Material 3 Modal Bottom Sheet ---
    if (state.isAddChannelSheetVisible) { // Conditionally render the M3 Bottom Sheet
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(HomeEvent.OnDismissAddChannelSheet) }, // Handle dismiss
            sheetState = addChannelSheetState,
            containerColor = MaterialTheme.colorScheme.surface, // Use M3 color scheme
        ) {
            // Content of the bottom sheet
            AddChannelSheetContent(
                channelName = state.newChannelName,
                channelDescription = state.newChannelDescription,
                selectedChannelType = state.newChannelType,
                isLoading = state.isCreatingChannel,
                error = state.createChannelError,
                onNameChange = { viewModel.onEvent(HomeEvent.OnNewChannelNameChange(it)) },
                onDescriptionChange = { viewModel.onEvent(HomeEvent.OnNewChannelDescriptionChange(it)) },
                onTypeSelected = { selectedType ->
                    viewModel.onEvent(HomeEvent.OnNewChannelTypeChange(selectedType))
                },
                onCreateClick = { viewModel.onEvent(HomeEvent.OnCreateChannelClicked) },
                onDismissRequest = { viewModel.onEvent(HomeEvent.OnDismissAddChannelSheet) } // Pass dismiss handler
            )
        }
    }

    if (state.isMyProfileSheetVisible && state.user != null) {
        MyProfile(
            modifier = Modifier,
            displayedName = state.user!!.displayName,
            username = state.user!!.email, // Assuming email as username for now
            bio = state.user!!.bio, // Add bio to User model if needed
            avatarUrl = state.user!!.photoUrl ?: "",
            onDismissRequest = { viewModel.onEvent(HomeEvent.OnDismissMyProfileSheet) },
            onSettingsClick = { viewModel.onEvent(HomeEvent.OnNavigateToSettings) }
        )
    }
}
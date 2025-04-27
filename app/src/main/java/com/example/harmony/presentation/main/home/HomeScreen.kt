// harmony/presentation/main/home/HomeScreen.kt
package com.example.harmony.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Use Material 3 imports
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.harmony.domain.model.Channel
import com.example.harmony.presentation.main.home.components.ChannelList
import com.example.harmony.presentation.main.home.components.ServerListSidebar
import com.example.harmony.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

import androidx.compose.material.ExperimentalMaterialApi // Still needed for PullRefresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
// Remove Material imports for bottom sheet
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.harmony.presentation.main.home.components.AddChannelSheetContent
import com.example.harmony.presentation.main.my_profile.MyProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class) // Keep ExperimentalMaterialApi for PullRefresh
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
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


    // --- LaunchedEffect for Navigation ---
    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            when (route) {
                NavRoutes.LOGIN -> {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                NavRoutes.CREATE_SERVER -> {
                    navController.navigate(NavRoutes.CREATE_SERVER)
                }
                // --- Add Navigation for Settings ---
                NavRoutes.SETTINGS -> {
                    // Make sure NavRoutes.SETTINGS is defined
                    // and you have a composable for it in NavGraph.kt
                    navController.navigate(NavRoutes.SETTINGS)
                }
            }
        }
    }

    // --- LaunchedEffect for Reloading after CreateServerScreen ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val serverCreatedResult = navBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("server_created")?.observeAsState()

    LaunchedEffect(serverCreatedResult?.value) {
        if (serverCreatedResult?.value == true) {
            viewModel.refreshData()
            navBackStackEntry?.savedStateHandle?.set("server_created", false)
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
                    isLoading = state.isLoadingServers && !state.isRefreshing,
                    modifier = Modifier.fillMaxHeight()
                )

                ChannelList(
                    serverName = state.selectedServer?.server?.name,
                    channels = state.selectedServer?.channels ?: emptyList(),
                    currentUser = state.user,
                    onChannelClick = { channel ->
                        val currentServerId = state.selectedServer?.server?.id
                        if (currentServerId != null) {
                            val route = NavRoutes.getChatRoute(currentServerId, channel.id)
                            navController.navigate(route)
                        } else {
                            println("Error: No server selected to navigate to channel.")
                        }
                    },
                    onAddChannelClick = { viewModel.onEvent(HomeEvent.OnShowAddChannelSheet) },
                    onUserSettingsClick = { viewModel.onEvent(HomeEvent.OnLogoutClicked) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.3f),
                    isHost = state.selectedServer?.server?.ownerId != null && state.user?.id != null && state.selectedServer?.server?.ownerId == state.user?.id,
                    onAvatarClick = { viewModel.onEvent(HomeEvent.OnShowMyProfileSheet) }
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
            // You can adjust drag handle, window insets etc. here if needed
            // dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            // Content of the bottom sheet
            AddChannelSheetContent(
                channelName = state.newChannelName,
                channelDescription = state.newChannelDescription,
                isLoading = state.isCreatingChannel,
                error = state.createChannelError,
                onNameChange = { viewModel.onEvent(HomeEvent.OnNewChannelNameChange(it)) },
                onDescriptionChange = { viewModel.onEvent(HomeEvent.OnNewChannelDescriptionChange(it)) },
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
package com.example.harmony.presentation.main.voice

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.data.rtc.AgoraRtcManager
import com.example.harmony.data.rtc.VoiceParticipant
import com.example.harmony.core.components.RoundedAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChannelScreen(
    navController: NavController,
    viewModel: VoiceChannelViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current // Get context

    // --- Permission Handling ---
    var hasAllPermissions by remember { mutableStateOf(false) }
    var missingPermissions by remember { mutableStateOf<List<String>>(emptyList()) }
    val requiredPermissions = remember {
        mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        ).apply {
            // Add BLUETOOTH_CONNECT only if running on Android 12 (API 31) or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }.toList() // Convert back to immutable list
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsResultMap ->
            // Check if all required permissions were granted
            val allGranted = permissionsResultMap.all { it.value }
            hasAllPermissions = allGranted
            if (allGranted) {
                missingPermissions = emptyList()
                println("VoiceChannelScreen: All required permissions granted.")
                // Attempt to join if state allows
                if(state.connectionState == AgoraRtcManager.ConnectionState.DISCONNECTED || state.connectionState == AgoraRtcManager.ConnectionState.FAILED) {
                    viewModel.onEvent(VoiceChannelEvent.JoinChannel)
                }
            } else {
                missingPermissions = permissionsResultMap.filter { !it.value }.keys.toList()
                println("VoiceChannelScreen: Some permissions denied: $missingPermissions")
                // Update UI or show explanation
            }
        }
    )

    // Function to check current permission status
    fun checkPermissions() {
        val grantedPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        hasAllPermissions = grantedPermissions.size == requiredPermissions.size
        missingPermissions = requiredPermissions - grantedPermissions.toSet() // Set difference
    }

    // Check and Request Permissions on Launch
    LaunchedEffect(key1 = Unit) {
        checkPermissions() // Check initial status
        if (!hasAllPermissions) {
            permissionsLauncher.launch(requiredPermissions.toTypedArray())
        } else {
            // Already have permissions, attempt join if state allows
            if(state.connectionState == AgoraRtcManager.ConnectionState.DISCONNECTED || state.connectionState == AgoraRtcManager.ConnectionState.FAILED) {
                viewModel.onEvent(VoiceChannelEvent.JoinChannel)
            }
        }
    }

    // Handle leaving channel when screen is disposed or lifecycle stops
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Consider leaving only on STOP or DESTROY, not PAUSE
            // if (event == Lifecycle.Event.ON_STOP) {
            //     viewModel.onEvent(VoiceChannelEvent.LeaveChannel)
            // }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            // This is called when the composable leaves the composition
            viewModel.onEvent(VoiceChannelEvent.LeaveChannel)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.channelName) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(VoiceChannelEvent.LeaveChannel) // Ensure leaving
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant // Match ChannelList header
                )
            )
        },
        bottomBar = {
            VoiceControlBar(
                connectionState = state.connectionState,
                isMuted = state.isLocalMuted,
                onMuteToggle = { viewModel.onEvent(VoiceChannelEvent.ToggleMute) },
                onDisconnect = {
                    viewModel.onEvent(VoiceChannelEvent.LeaveChannel)
                    navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Connection Status Indicator (Optional)
            when (state.connectionState) {
                AgoraRtcManager.ConnectionState.CONNECTING -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting...")
                    }
                }
                AgoraRtcManager.ConnectionState.CONNECTED -> {
                    Text("Connected", color = Color.Green) // Adjust color
                }
                AgoraRtcManager.ConnectionState.FAILED -> {
                    Text("Connection Failed", color = MaterialTheme.colorScheme.error)
                }
                AgoraRtcManager.ConnectionState.DISCONNECTED -> {
                    Text("Disconnected")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Participants (${state.participants.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading && state.participants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.participants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You're the first one here!")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.participants, key = { it.uid }) { participant ->
                        ParticipantItem(participant = participant)
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantItem(participant: VoiceParticipant/*, isSelf: Boolean = false*/) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundedAvatar(
            avatarImageUrl = participant.photoUrl ?: "",
            // Use first letter of display name if available, else '?'
            char = participant.displayName?.firstOrNull()?.uppercaseChar() ?: '?',
            size = 40.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = participant.displayName ?: "User (ID: ${participant.uid})",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )
        if (participant.isMuted) {
            Icon(
                Icons.Default.MicOff,
                contentDescription = "Muted",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), // Subtle tint
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
fun VoiceControlBar(
    connectionState: AgoraRtcManager.ConnectionState,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onDisconnect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Consistent background
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute Button
        IconButton(
            onClick = onMuteToggle,
            enabled = connectionState == AgoraRtcManager.ConnectionState.CONNECTED,
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isMuted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                modifier = Modifier.size(28.dp)
                // tint = if (isMuted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Disconnect Button
        Button(
            onClick = onDisconnect,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Default.CallEnd,
                contentDescription = "Disconnect",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}
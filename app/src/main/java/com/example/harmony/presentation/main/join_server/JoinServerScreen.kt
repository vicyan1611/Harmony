package com.example.harmony.presentation.main.join_server

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.harmony.core.components.ErrorText
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.core.components.HarmonyTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinServerScreen(
    navController: NavController,
    viewModel: JoinServerViewModel = hiltViewModel(),
    onJoinSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.joinSuccess) {
        if (state.joinSuccess) {
            Toast.makeText(context, "Successfully joined server!", Toast.LENGTH_SHORT).show()
            onJoinSuccess() // Call the callback to navigate back
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
            // Optionally dismiss the error in the VM after showing toast
            // viewModel.onEvent(JoinServerEvent.OnDismissError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join a Server") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Enter an invite link to join an existing server.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            HarmonyTextField(
                value = state.inviteLink,
                onValueChange = { viewModel.onEvent(JoinServerEvent.OnLinkChange(it)) },
                label = "Invite Link",
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = "Link Icon") },
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done,
                isEditable = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.error != null) {
                ErrorText(error = state.error!!)
                Spacer(modifier = Modifier.height(8.dp))
            }

            HarmonyButton(
                text = "Join Server",
                onClick = { viewModel.onEvent(JoinServerEvent.OnJoinClick) },
                isLoading = state.isLoading,
                enabled = state.inviteLink.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
// Create file: harmony/presentation/main/config_server/ConfigServerScreen.kt
package com.example.harmony.presentation.main.config_server

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.harmony.R
import com.example.harmony.core.components.ErrorText
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.core.components.HarmonyTextField
import com.example.harmony.core.components.RoundedAvatar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigServerScreen(
    navController: NavController,
    viewModel: ConfigServerViewModel = hiltViewModel() // Inject ViewModel
) {
    val state by viewModel.state.collectAsState()
    val operationSuccess by viewModel.operationSuccess.collectAsState() // Observe the success flag
    val context = LocalContext.current

    // Navigate back logic using SavedStateHandle for result
    LaunchedEffect(state.navigateBack, operationSuccess) {
        if (state.navigateBack && operationSuccess) {
            // Set the result on the previous screen's SavedStateHandle
            navController.previousBackStackEntry?.savedStateHandle?.set("server_list_updated", true)
            // Notify the ViewModel that navigation has been handled
            viewModel.onEvent(ConfigServerEvent.OnNavigationHandled)
        } else if (state.navigateBack && !operationSuccess) {
            // Handle simple back navigation if needed (e.g., user presses back button manually)
            // or if navigation was triggered without success (less likely now)
            navController.popBackStack()
            // Optionally notify ViewModel if simple back needs handling
            viewModel.onEvent(ConfigServerEvent.OnNavigationHandled)
        }
    }

    // Show general errors (keep as is)
    LaunchedEffect(state.error) { /* ... */ }
    // Show update errors (keep as is)
    LaunchedEffect(state.updateError) { /* ... */ }
    // Show delete errors (keep as is)
    LaunchedEffect(state.deleteError) { /* ... */ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Standard back press
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if(state.canUserConfigure) {
                        IconButton(
                            onClick = { viewModel.onEvent(ConfigServerEvent.OnSaveChangesClick) },
                            enabled = !state.isUpdating && !state.isDeleting && state.canUserConfigure
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save Changes")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- Rest of the UI remains largely the same ---
        // ... (Loading indicator, Server not found text)
        if (state.isLoadingDetails) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.serverDetails == null && state.error == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Server not found.")
            }
        }
        // Only show content if details are loaded and no initial error prevented loading
        else if (state.serverDetails != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... (ServerProfileImagePicker)
                ServerProfileImagePicker(
                    currentImageUrl = state.currentProfileUrl,
                    selectedImageUri = state.selectedImageUri,
                    onImageSelected = { uri -> viewModel.onEvent(ConfigServerEvent.OnProfilePictureSelected(uri)) },
                    enabled = state.canUserConfigure && !state.isUpdating && !state.isDeleting
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ... (HarmonyTextField for Name)
                HarmonyTextField(
                    value = state.currentName,
                    onValueChange = { viewModel.onEvent(ConfigServerEvent.OnNameChange(it)) },
                    label = "Server Name",
                    modifier = Modifier.fillMaxWidth(),
                    isEditable = state.canUserConfigure && !state.isUpdating && !state.isDeleting,
                    maxLines = 1
                )


                if (state.updateError != null) {
                    ErrorText(error = state.updateError!!)
                }

                if (state.isUpdating) {
                    CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.weight(1f)) // Pushes delete button down


                // ... (Delete Button and error)
                if(state.canUserConfigure) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(ConfigServerEvent.OnDeleteServerClick) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        enabled = state.canUserConfigure && !state.isUpdating && !state.isDeleting
                    ) {
                        if (state.isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.error, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Server")
                        }
                    }
                    if (state.deleteError != null) {
                        ErrorText(error = state.deleteError!!)
                    }
                }
            }
        }
        // Show general error if serverDetails is null and there *was* an error loading
        else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                ErrorText(error = state.error!!)
            }
        }
    }

    // Delete Confirmation Dialog (keep as is)
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ConfigServerEvent.OnCancelDeleteServer) },
            title = { Text("Delete Server?") },
            // ... rest of the dialog
            text = { Text("Are you sure you want to delete '${state.serverDetails?.name ?: "this server"}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(ConfigServerEvent.OnConfirmDeleteServer) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ConfigServerEvent.OnCancelDeleteServer) }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun ServerProfileImagePicker(
    currentImageUrl: String?,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    Box(contentAlignment = Alignment.Center) {
        // Display selected image or current image or placeholder
        val imageModifier = Modifier
            .size(100.dp)
            .clip(CircleShape)

        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected Server Profile",
                modifier = imageModifier,
                contentScale = ContentScale.Crop
            )
        } else if (!currentImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = currentImageUrl,
                contentDescription = "Current Server Profile",
                modifier = imageModifier,
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder if no image
            Box(
                modifier = imageModifier.background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Server Image Placeholder",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Edit Button Overlay
        Button(
            onClick = { launcher.launch("image/*") },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(36.dp)
                .offset(x = 10.dp, y = 10.dp), // Adjust offset as needed
            contentPadding = PaddingValues(0.dp),
            enabled = enabled
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Change Server Image",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
package com.example.harmony.presentation.main.create_server

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.example.harmony.R
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.core.components.HarmonyTextField
import com.example.harmony.core.theme.HarmonyTheme

sealed class ServerCreationScreen(val route: String, val label: String) {
    object ServerName: ServerCreationScreen("server/creation/name", "Name")
    object ServerInvite: ServerCreationScreen("server/creation/invite", "Invite")
}

fun ShareTextToOtherApps(textToShare: String, context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null) // Title for the chooser
    context.startActivity(shareIntent, null)
}

@Composable
fun ServerCreationNav(
    navController: NavHostController,
    viewModel: CreateServerViewModel // Added parameter
) {
    NavHost(
        navController = navController,
        startDestination = ServerCreationScreen.ServerName.route,
        modifier = Modifier.fillMaxWidth()
    ) {
        composable(ServerCreationScreen.ServerName.route) {
            // Pass ViewModel down
            ServerCreationName(navController = navController, viewModel = viewModel)
        }
        composable(ServerCreationScreen.ServerInvite.route) {
            // Pass ViewModel down
            ServerCreationInvite(nestedNavController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun ServerCreationInvite(nestedNavController: NavHostController, viewModel: CreateServerViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsState()

    if (state.value.createdServerInviteLink == null) return

    val url = state.value.createdServerInviteLink!!

    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        // skip button
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    viewModel.onEvent(CreateServerEvent.InviteDismissed)
                },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.server_creation_invite_doneButton),
                    style = TextStyle(
                        fontSize = 14.sp
                    )
                )
            }
        }

        // title
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.server_creation_invite_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // description
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.server_creation_invite_description),
            style = TextStyle(
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            ),
            overflow = TextOverflow.Visible
        )

        Spacer(modifier = Modifier.height(8.dp))

        // url
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HarmonyTextField(
                value = url,
                label = "Invite Link",
                onValueChange = {},
                maxLines = 1,
                isEditable = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // occupy remaining space
                    .padding(end = 8.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(url))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            )

        }

        Spacer(modifier = Modifier.height(6.dp))
        // share link to other apps
        HarmonyButton(
            text = stringResource(R.string.server_creation_invite_sharelinkButton),
            onClick = {
                ShareTextToOtherApps(
                    textToShare = url,
                    context = context
                )
            }
        )
    }
}

@Composable
fun ImagePickerFromGallery(selectedImageUri: Uri?, setSelectedImageUri: (Uri?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        setSelectedImageUri(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                launcher.launch("image/*")
            },
            shape = CircleShape,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier.size(80.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            if (selectedImageUri == null) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = ""
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(selectedImageUri)
                            .build()
                    ),
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun ServerCreationName(navController: NavHostController, viewModel: CreateServerViewModel) {
    val state = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.value.errorMessage != null) {
        Toast.makeText(
            context,
            state.value.errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    // header
    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        // title
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.server_creation_servername_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        // description
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.server_creation_servername_description),
            style = TextStyle(
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            ),
            overflow = TextOverflow.Visible
        )

        // server image
        ImagePickerFromGallery(
            setSelectedImageUri = { uri ->
                viewModel.onEvent(CreateServerEvent.ImageSelected(uri))
            },
            selectedImageUri = state.value.selectedImageUri,
        )

        // server name
        HarmonyTextField(
            value = state.value.serverName,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            isEditable = !state.value.isLoading,
            onValueChange = { newText ->
                viewModel.onEvent(CreateServerEvent.NameChanged(newText))
            },
            label = stringResource(R.string.server_creation_servername_label),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // create
        HarmonyButton(
            text = stringResource(R.string.server_creation_createbutton),
            onClick = {
//                navController.navigate(ServerCreationScreen.ServerInvite.route)
                viewModel.onEvent(CreateServerEvent.CreateServerClicked)
            },
            isLoading = state.value.isLoading
        )
    }
}

@Composable
fun CreateServerScreen(
    mainNavController: NavHostController // Passed from the main NavGraph call
) {
    val viewModel: CreateServerViewModel = hiltViewModel()
    // This NavController is for the nested navigation (Name <-> Invite)
    val nestedNavController = rememberNavController()

    LaunchedEffect(key1 = viewModel.navigationEvent) {
        viewModel.navigationEvent.collect { command ->
            when (command) {
                is NavigationCommand.NavigateTo -> {
                    // Use the nested controller for navigation *within* this flow
                    nestedNavController.navigate(command.route)
                }
                is NavigationCommand.NavigateBack -> {
                    // Use the main controller to exit the *entire* ServerCreation flow
                    mainNavController.popBackStack()
                }
            }
        }
    }

    // Pass the nested NavController to the NavHost managing Name/Invite screens
    Column (
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ServerCreationNav(
            navController = nestedNavController,
            viewModel = viewModel // Pass the viewModel down
        )
    }
}

//@Preview
//@Composable
//fun ServerCreationPreview() {
//    HarmonyTheme {
//        ServerCreation(rememberNavController())
//    }
//}
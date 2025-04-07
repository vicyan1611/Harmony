package com.example.harmony.composes.server

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.harmony.composes.TextBox
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.example.harmony.composes.ShareText

sealed class ServerCreationScreen(val route: String, val label: String) {
    object ServerName: ServerCreationScreen("server/creation/name", "Name")
    object ServerInvite: ServerCreationScreen("server/creation/invite", "Invite")
}

@Composable
fun ServerCreationNav(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = ServerCreationScreen.ServerName.route,
        modifier = Modifier.fillMaxWidth()
    ) {
        composable(ServerCreationScreen.ServerName.route) {
            ServerCreationName(navController)
        }
        composable(ServerCreationScreen.ServerInvite.route) {
            ServerCreationInvite(navController)
        }
    }
}

@Composable
fun ServerCreationInvite(navController: NavHostController, url: String = "http://hahahaha.com") {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val activity = context as? ComponentActivity
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
                    activity?.finish()
                },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.White
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
                textAlign = TextAlign.Center,
                color = Color.LightGray
            ),
            overflow = TextOverflow.Visible
        )

        Spacer(modifier = Modifier.height(8.dp))

        // url
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    clipboardManager.setText(AnnotatedString(url))
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextBox(
                text = url,
                maxLines = 1,
                textStyle = TextStyle(
                    color = Color.LightGray
                ),
                editable = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // occupy remaining space
                    .padding(end = 8.dp)
            )
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "",
                modifier = Modifier.padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        // share link to other apps
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            onClick = {
                ShareText(
                    textToShare = url,
                    context = context
                )
            },
            contentPadding = PaddingValues(12.dp),
            colors = ButtonColors(
                containerColor = Color(0xFF4F1FCC),
                contentColor = Color.White,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.server_creation_invite_sharelinkButton),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun ImagePickerFromGallery(setSelectedImageUri: (Uri?) -> Unit, selectedImageUri: Uri?) {
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
                color = Color.White,
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
fun ServerCreationName(navController: NavHostController) {
    var serverName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
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
                color = Color.LightGray
            ),
            overflow = TextOverflow.Visible
        )

        // server image
        ImagePickerFromGallery(
            setSelectedImageUri = { uri ->
                selectedImageUri = uri
            },
            selectedImageUri = selectedImageUri
        )

        // server name
        TextBox(
            text = serverName,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            editable = true,
            onValueChange = { newText ->
                serverName = newText
            },
            label = stringResource(R.string.server_creation_servername_label)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // create button
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            onClick = {
                navController.navigate(ServerCreationScreen.ServerInvite.route)
            },
            contentPadding = PaddingValues(12.dp),
            colors = ButtonColors(
                containerColor = Color(0xFF4F1FCC),
                contentColor = Color.White,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.server_creation_createbutton),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun ServerCreation(navController: NavHostController = rememberNavController()) {
    ServerCreationNav(
        navController = navController
    )
}
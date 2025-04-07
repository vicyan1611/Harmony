package com.example.harmony.composes.channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.harmony.R
import com.example.harmony.composes.RoundedAvatar
import com.example.harmony.composes.RoundedButton
import com.example.harmony.composes.TextBox
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class ChannelConfigScreens(val route: String, val label: String) {
    object General: ChannelConfigScreens("channel-config/", "General") // top level
    object Settings: ChannelConfigScreens("channel-config/settings", "Settings")
}

val seederParticipants = listOf<String>(
    "https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2V8ZW58MHx8MHx8fDA%3D",
    "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1zwhySGCEBxRRFYIcQgvOLOpRGqrT3d7Qng&s",
    "https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2V8ZW58MHx8MHx8fDA%3D",
    "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1zwhySGCEBxRRFYIcQgvOLOpRGqrT3d7Qng&s",
    "https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2V8ZW58MHx8MHx8fDA%3D",
    "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1zwhySGCEBxRRFYIcQgvOLOpRGqrT3d7Qng&s",
    "https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2V8ZW58MHx8MHx8fDA%3D",
    "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1zwhySGCEBxRRFYIcQgvOLOpRGqrT3d7Qng&s",
    "https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2V8ZW58MHx8MHx8fDA%3D",
    "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1zwhySGCEBxRRFYIcQgvOLOpRGqrT3d7Qng&s",
    "https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aW1hZ2V8ZW58MHx8MHx8fDA%3D",
    "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1zwhySGCEBxRRFYIcQgvOLOpRGqrT3d7Qng&s",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelConfigMenus(modifier: Modifier = Modifier, channelName: String = "", channelDescription: String = "", onDismissRequest: () -> Unit) {
    ModalBottomSheet (
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        ChannelConfigNavigation(
            channelName = channelName,
            channelDescription = channelDescription,
            onDismissRequest = onDismissRequest
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ChannelConfigNavigation(channelName: String = "", onDismissRequest: () -> Unit = {}, channelDescription: String = "") {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ChannelConfigScreens.General.route
    ) {
        composable(ChannelConfigScreens.General.route) {
            ChannelConfigGeneralScreen(
                modifier = Modifier.fillMaxSize(),
                channelName = channelName,
                navController = navController
            )
        }
        composable(ChannelConfigScreens.Settings.route) {
            ChannelSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onDismissRequest = onDismissRequest,
                channelName = channelName,
                channelDescription = channelDescription,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ChannelConfigGeneralScreen(modifier: Modifier = Modifier, channelName: String = "", navController: NavHostController) {
    Column (
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // channel name
        Row (
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = channelName,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }

        // buttons
        Row (
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RoundedButton (
                    size = 40.dp,
                    onClick = {

                    },
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = ""
                    )
                }

                // button's label
                Text(
                    text = stringResource(R.string.channel_config_btnNotifications_label),
                    style = TextStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp
                    )
                )
            }

            Column (
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RoundedButton (
                    size = 40.dp,
                    onClick = {
                        navController.navigate(ChannelConfigScreens.Settings.route)
                    },
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = ""
                    )
                }

                // button's label
                Text(
                    text = stringResource(R.string.channel_config_btnSettings_label),
                    style = TextStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // title: Member
        Text(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            text = stringResource(R.string.channel_config_memberList_title),
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )

        // list of members
        Row (
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            LazyColumn (
                modifier = Modifier.fillMaxWidth()
            ) {
                var cnt = 0
                item {
                    Card (
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        seederParticipants.forEachIndexed { idx, url ->
                            Row (
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RoundedAvatar(
                                    size = 40.dp,
                                    avatarImageUrl = url
                                )

                                Text(
                                    text = "name name neeee",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }

                            if (idx < seederParticipants.size - 1) {
                                HorizontalDivider(
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun ChannelSettingsScreen(modifier: Modifier = Modifier, onDismissRequest: () -> Unit = {}, channelName: String = "", channelDescription: String = "", channelPermission: List<Uuid> = listOf(), navController: NavHostController) {
    var cnName by remember { mutableStateOf(channelName) }
    var cnDescription by remember { mutableStateOf(channelDescription) }
//    ModalBottomSheet(
//        onDismissRequest = onDismissRequest,
//        modifier = modifier
//            .fillMaxWidth(),
//        containerColor = MaterialTheme.colorScheme.secondary,
//        contentColor = MaterialTheme.colorScheme.onSecondary,
//        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    ) {
//
//    }

    Column {
        // header (pinned to top): Cancel - Title - Save
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            // button go back
            Button(
                onClick = {
                    navController.popBackStack()
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.width(50.dp),
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContentColor = Color.LightGray,
                    disabledContainerColor = Color.LightGray
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = ""
                )
            }


            Text(
                text = stringResource(R.string.channel_config_btnSettings_label),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )

            // button save
            Button(
                onClick = {},
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContentColor = Color.LightGray,
                    disabledContainerColor = Color.LightGray
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.channel_setting_btnSave),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp),
            userScrollEnabled = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // channel name
            item {
                TextBox(
                    text = cnName,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { newText ->
                        cnName = newText
                    },
                    label = stringResource(R.string.channel_setting_title_cnName),
                    maxLines = 1,
                    editable = true,
                )

            }

            // channel description
            item {
                TextBox(
                    text = cnDescription,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { newText ->
                        cnDescription = newText
                    },
                    label = stringResource(R.string.channel_setting_title_cnDescription),
                    minLines = 6,
                    maxLines = 6,
                    editable = true,
                    showCharsCounter = true,
                    maxChars = 1024
                )
            }

            // delete channel
            item {
                TextButton (
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                    contentPadding = PaddingValues(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Red,
                        disabledContentColor = Color.LightGray,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "",
                            tint = Color.Red
                        )

                        Text(
                            text = stringResource(R.string.channel_setting_title_cnDelete),
                            style = TextStyle(
                                color = Color.Red,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
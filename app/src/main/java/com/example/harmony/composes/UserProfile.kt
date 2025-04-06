package com.example.harmony.composes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.harmony.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileLayout(displayedName: String = "", username: String = "", bio: String = "", modifier: Modifier, onDismissRequest: () -> Unit, headerContent: @Composable RowScope.() -> Unit, bodyContent: @Composable RowScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .fillMaxWidth(),
//            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.2f), // set max height when fully expand, but it does not work ??
        // sheetState = rememberModalBottomSheetState(true), // skip partially expand
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // interactive buttons on top
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()){
                headerContent()
            }

            // user info: avatar, name, username, bio
            Row (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RoundedAvatar(size = 64.dp, char = displayedName.getOrElse(0) { ' ' }.uppercaseChar())
                    Text(
                        text = displayedName,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = username,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Box(modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Green), contentAlignment = Alignment.Center) {
                            Text(
                                text = "#",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // body
            Row (
                modifier = Modifier.fillMaxWidth()
            ) {
                bodyContent()
            }
        }

    }
}

@Composable
fun RoundedContainer(modifier: Modifier = Modifier, size: Dp = 80.dp, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun RoundedButton(modifier: Modifier = Modifier, size: Dp = 40.dp, onClick: () -> Unit, containerColor: Color = MaterialTheme.colorScheme.secondary, contentColor: Color = MaterialTheme.colorScheme.onSecondary, content: @Composable RowScope.() -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = Color.LightGray,
            disabledContainerColor = Color.LightGray
        ),
        shape = CircleShape,
        modifier = Modifier.size(size),
        contentPadding = PaddingValues(2.dp),
        content = content
    )
}

@Composable
fun RoundedAvatar(modifier: Modifier = Modifier, size: Dp = 80.dp, char: Char = ' ') {
    RoundedContainer(modifier = modifier, size = size) {
        Text(
            text = char.toString(),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 24.sp
            )
        )
    }
}

@Composable
fun OtherUserProfile(displayedName: String = "", username: String = "", isFriend: Boolean = false, bio: String = "", modifier: Modifier, onDismissRequest: () -> Unit) {
    UserProfileLayout(
        displayedName = displayedName,
        username = username,
        bio = bio,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        headerContent = {
            if (isFriend) {
                val unfriendDialogState = remember { mutableStateOf(false) }
                RoundedButton(
                    size = 32.dp,
                    onClick = {
                        unfriendDialogState.value = true;
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_friend_added),
                        contentDescription = "Friend added",
                    )
                }
                UnfriendDialog(unfriendDialogState, displayedName)
            }
        },
        bodyContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 2.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    RoundedButton(
                        onClick = {},
                        size = 48.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubble,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            contentDescription = stringResource(R.string.view_profile_send_message_button),
//                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.view_profile_send_message_button),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    RoundedButton(
                        onClick = {},
                        size = 48.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            contentDescription = stringResource(R.string.view_profile_audio_call_button)
                        )
                    }
                    Text(
                        text = stringResource(R.string.view_profile_audio_call_button),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    RoundedButton(
                        onClick = {},
                        size = 48.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoCall,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            contentDescription = stringResource(R.string.view_profile_video_call_button)
                        )
                    }
                    Text(
                        text = stringResource(R.string.view_profile_video_call_button),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    )
                }

                if (!isFriend) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        RoundedButton(
                            onClick = {},
                            size = 48.dp
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                contentDescription = stringResource(R.string.view_profile_add_friend_button)
                            )
                        }
                        Text(
                            text = stringResource(R.string.view_profile_add_friend_button),
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnfriendDialog(state: MutableState<Boolean>, displayedName: String = "") {
    if (!state.value) return;
    BasicAlertDialog(
        onDismissRequest = {
            state.value = false
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier,
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(
                        vertical = 16.dp,
                        horizontal = 16.dp
                    )
            ) {
                Text(
                    text = "${stringResource(R.string.delete_friend_prompt_header)} \'$displayedName\'",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = stringResource(R.string.delete_friend_prompt_description),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                )
                Column (
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TextButton(
                        onClick = {},
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            disabledContentColor = Color.DarkGray,
                            disabledContainerColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
//                                    .graphicsLayer {
//                                        scaleX = 0.95f
//                                        scaleY = 0.95f
//                                    }
                    ) {
                        Text(
                            text = stringResource(R.string.delete_friend_prompt_btnDel),
                            style = TextStyle(
//                            fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    TextButton(
                        onClick = {},
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White,
                            disabledContentColor = Color.LightGray,
                            disabledContainerColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.delete_friend_prompt_btnDismiss),
                            style = TextStyle(
//                            fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

        }
    }
}
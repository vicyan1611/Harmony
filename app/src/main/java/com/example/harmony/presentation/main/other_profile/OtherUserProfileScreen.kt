package com.example.harmony.presentation.main.other_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.harmony.R
import com.example.harmony.core.components.RoundedButton
import com.example.harmony.core.components.UserProfileLayout

@Composable
fun OtherUserProfile(displayedName: String = "", username: String = "", isFriend: Boolean = false, bio: String = "", modifier: Modifier, avatarUrl: String = "", onDismissRequest: () -> Unit, headerContent: @Composable RowScope.() -> Unit = {}, bodyContent: @Composable RowScope.() -> Unit = {}) {
    UserProfileLayout(
        displayedName = displayedName,
        username = username,
        bio = bio,
        modifier = modifier,
        avatarUrl = avatarUrl,
        onDismissRequest = onDismissRequest,
        headerContent = {
            headerContent()
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
            bodyContent()
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
                            color = MaterialTheme.colorScheme.onSurface,
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
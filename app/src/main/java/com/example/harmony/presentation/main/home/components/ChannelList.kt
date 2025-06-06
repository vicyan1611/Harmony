// harmony/presentation/main/home/components/ChannelList.kt
package com.example.harmony.presentation.main.home.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.harmony.R
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.domain.model.Channel
import com.example.harmony.domain.model.ChannelType
import com.example.harmony.domain.model.User
import com.example.harmony.presentation.main.home.HomeEvent
import com.example.harmony.presentation.main.home.HomeViewModel

@Composable
fun ChannelList(
    serverName: String?,
    channels: List<Channel>,
    currentUser: User?, // Needed for user panel at the bottom
    onVoiceChannelClick: (Channel) -> Unit,
    onTextChannelClick: (Channel) -> Unit,
    onAddChannelClick: () -> Unit,
    onUserSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHost: Boolean = true,
    onAvatarClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val listBgColor = MaterialTheme.colorScheme.surface // Slightly lighter than server list
    Column(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 240.dp) // Min width for channel list
            .background(listBgColor)
    ) {
        // Server Name Header (if a server is selected)
        if (serverName != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Standard header height
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = serverName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isHost) {
                    IconButton(
                        onClick = { viewModel.onEvent(HomeEvent.OnNavigateToConfigServer) },
                        // horizontally end
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            // You could show a "Direct Messages" header here if needed
            Spacer(modifier = Modifier.height(56.dp)) // Keep height consistent
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

        // Channel List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val textChannels = channels.filter { it.type == ChannelType.TEXT }
            val voiceChannels = channels.filter { it.type == ChannelType.VOICE }

            if (textChannels.isNotEmpty()) {
                item {
                    ChannelCategoryHeader(
                        name = stringResource(R.string.channel_list_display_title_textchannel),
                        onAddClick = { /* TODO: Differentiate add text/voice? */ onAddChannelClick() },
                        canAddChannel = isHost
                    )
                }
                items(textChannels, key = { "text-${it.id}" }) { channel ->
                    ChannelItem(
                        channel = channel,
                        isSelected = false, // TODO: Implement selection state if needed
                        onClick = { onTextChannelClick(channel) } // Use text handler
                    )
                }
            }
            if (voiceChannels.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp)) // Space between categories
                    ChannelCategoryHeader(
                        name = stringResource(R.string.channel_list_display_title_voicechannel), // Add this string resource
                        onAddClick = { /* TODO: Differentiate add text/voice? */ onAddChannelClick() }, // Allow adding voice channels?
                        canAddChannel = isHost
                    )
                }
                items(voiceChannels, key = { "voice-${it.id}" }) { channel ->
                    ChannelItem(
                        channel = channel,
                        isSelected = false, // TODO: Implement selection state if needed
                        onClick = { onVoiceChannelClick(channel) } // Use voice handler
                    )
                }
            }
        }



        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        // User Panel at the Bottom
        UserPanel(
            user = currentUser,
            onSettingsClick = onUserSettingsClick,
            onAvatarClick = onAvatarClick
        )
    }
}

@Composable
private fun ChannelCategoryHeader(
    name: String,
    onAddClick: () -> Unit,
    canAddChannel: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (canAddChannel) {
            IconButton(onClick = onAddClick, modifier = Modifier.size(18.dp)) {
                Icon (
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Channel",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun ChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemBgColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(itemBgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Choose icon based on channel type
        val icon = when (channel.type) {
            ChannelType.TEXT -> Icons.Default.Tag
            ChannelType.VOICE -> Icons.AutoMirrored.Filled.VolumeUp
        }
        Icon(
            imageVector = icon,
            contentDescription = if (channel.type == ChannelType.TEXT) "Text Channel" else "Voice Channel",
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = channel.name,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun UserPanel(
    user: User?,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp) // Standard height
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) // Slightly different bg
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 8.dp).clickable { onAvatarClick() }
        ) {
            // User Avatar
            RoundedAvatar( // Use your RoundedAvatar
                size = 32.dp,
                avatarImageUrl = user?.photoUrl ?: "",
                char = user?.displayName?.firstOrNull() ?: '?'
            )
            Spacer(Modifier.width(8.dp))
            // Username
            Text(
                text = user?.displayName ?: "Unknown User",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Optional: Add user status indicator (# tag, etc.)
        }

        // Action Icons (Mic, Headset, Settings)
        Row {
            // TODO: Add Mic/Headset icons later if needed
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Log out",
                    tint = Color.Red
                )
            }
        }
    }
}
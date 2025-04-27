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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag // For text channels
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.harmony.R
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.core.components.RoundedButton
import com.example.harmony.domain.model.Channel
import com.example.harmony.domain.model.User

@Composable
fun ChannelList(
    serverName: String?,
    channels: List<Channel>,
    currentUser: User?, // Needed for user panel at the bottom
    onChannelClick: (Channel) -> Unit,
    onAddChannelClick: () -> Unit, // TODO: Implement later
    onUserSettingsClick: () -> Unit, // TODO: Implement later
    modifier: Modifier = Modifier,
    isHost: Boolean = true
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
            item {
                ChannelCategoryHeader(
                    name = stringResource(R.string.channel_list_display_title_textchannel),
                    // Pass the lambda here
                    onAddClick = onAddChannelClick,
                    canAddChannel = isHost
                )
            }

            items(channels, key = { it.id }) { channel ->
                ChannelItem(
                    channel = channel,
                    isSelected = false,
                    onClick = { onChannelClick(channel) }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        // User Panel at the Bottom
        UserPanel(
            user = currentUser,
            onSettingsClick = onUserSettingsClick
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
        // Use Tag icon for text channels (replace if you have voice channels later)
        Icon(
            imageVector = Icons.Default.Tag,
            contentDescription = "Text Channel",
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
        // Optional: Add icons for unread messages, mentions, etc. here
    }
}

@Composable
fun UserPanel(
    user: User?,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
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
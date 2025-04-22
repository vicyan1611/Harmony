package com.example.harmony.composes.voice


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteFriendsBottomSheet(
    channelName: String = "ML + Mobile",
    onDismiss: () -> Unit,
    onInvite: (String) -> Unit
) {
    val friends = listOf(
        Friend("user1", "file:///android_asset/cat.jpg"),
        Friend("user2", "file:///android_asset/cat.jpg"),
        Friend("user3", "file:///android_asset/cat.jpg"),
        Friend("user4", "file:///android_asset/cat.jpg"),
        Friend("user5", "file:///android_asset/cat.jpg"),
        Friend("user6", "file:///android_asset/cat.jpg"),
        Friend("user7", "file:///android_asset/cat.jpg"),
        Friend("user9.", "file:///android_asset/cat.jpg")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1F22),
        dragHandle = { Box(modifier = Modifier.padding(vertical = 12.dp)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Title
            Text(
                text = "Invite a friend",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Share options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareOption(
                    icon = Icons.Default.Share,
                    label = "Share Invite",
                    onClick = { /* Handle share */ }
                )

                ShareOption(
                    icon = Icons.Default.Link,
                    label = "Copy Link",
                    onClick = { /* Handle copy link */ }
                )

                ShareOption(
                    icon = Icons.Default.Message,
                    label = "Messages",
                    tint = Color.Green,
                    onClick = { /* Handle messages */ }
                )

                ShareOption(
                    icon = Icons.Default.Email,
                    label = "Email",
                    tint = Color(0xFF64B5F6),
                    onClick = { /* Handle email */ }
                )
            }

            Divider(
                color = Color(0xFF3F4147),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Search bar
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Invite friends to $channelName", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            )

            // Expiration notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your invite link expires in 7 days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Edit invite link",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF00AEFF),
                    modifier = Modifier.clickable { /* Handle edit link */ }
                )
            }

            // Friends list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(friends) { friend ->
                    FriendItem(
                        friend = friend,
                        onInvite = { onInvite(friend.name) }
                    )

                    Divider(
                        color = Color(0xFF3F4147),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ShareOption(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF36393F)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    onInvite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(friend.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Name
        Text(
            text = friend.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // Invite button
        OutlinedButton(
            onClick = onInvite,
            modifier = Modifier.width(100.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF36393F),
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFF3F4147))
        ) {
            Text("Invite")
        }
    }
}

data class Friend(
    val name: String,
    val avatarUrl: String
)



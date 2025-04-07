package com.example.harmony.composes.voice

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChannelBottomSheet(
    participants: List<VoiceParticipant>,
    onDismiss: () -> Unit,
    onJoinVoice: () -> Unit
) {

    var hasJoined by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(true) }
    var currentParticipants by remember { mutableStateOf(participants) }


    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1F22),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2B2D31))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text(
                        text = "voice 1",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = Color.White
                    )
                }
            }

            // People count
            Text(
                text = "${participants.size} People in Voice",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2B2D31))
            ) {
                LazyColumn {
                    items(participants) { participant ->
                        VoiceParticipantItem(participant)
                        if (participant != participants.last()) {
                            Divider(color = Color(0xFF3F4147), thickness = 0.5.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom controls

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute/Unmute button
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) Color.Red else Color(0xFF2ECC71),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Join/Leave voice button
                Button(
                    onClick = {
                        if (!hasJoined) {
                            val meParticipant = VoiceParticipant(
                                name = "Me",
                                avatarUrl = "file:///android_asset/avatar.jpeg",
                                isMuted = isMuted
                            )
                            currentParticipants = currentParticipants + meParticipant
                        } else {
                            currentParticipants = currentParticipants.filter { it.name != "Me" }
                        }
                        hasJoined = !hasJoined
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasJoined) Color.Red else Color(0xFF2ECC71)
                    )
                ) {
                    Text(
                        text = if (hasJoined) "Leave Voice" else "Join Voice",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                // Invite Friend button
                IconButton(
                    onClick = {  /* handle invite friend */},
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF36393F))
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Invite Friend",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceParticipantItem(participant: VoiceParticipant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(participant.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name
        Text(
            text = participant.name,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // Mute icon if user is muted
        if (participant.isMuted) {
            Icon(
                imageVector = Icons.Default.MicOff,
                contentDescription = "Muted",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Cách sử dụng
@Composable
fun YourScreen() {
    var showVoiceChannel by remember { mutableStateOf(false) }

    Button(onClick = { showVoiceChannel = true }) {
        Text("Show Voice Channel")
    }

    if (showVoiceChannel) {
        VoiceChannelBottomSheet(
            participants = voiceParticipants,
            onDismiss = { showVoiceChannel = false },
            onJoinVoice = {

            }
        )
    }
}



// Model class
data class VoiceParticipant(
    val name: String,
    val avatarUrl: String,
    val isMuted: Boolean = false
)

// Sample data
val voiceParticipants = listOf(
    VoiceParticipant("User B", "file:///android_asset/cat.jpg"),
    VoiceParticipant("User C", "file:///android_asset/cat.jpg"),
    VoiceParticipant("User 🦆", "file:///android_asset/cat.jpg", true),
    VoiceParticipant("User A", "file:///android_asset/cat.jpg"),
    VoiceParticipant("User CA", "file:///android_asset/cat.jpg") ,
    VoiceParticipant("User BA", "file:///android_asset/cat.jpg")
)

@Preview
@Composable
fun VoiceChannelBottomSheetPreview() {
    MaterialTheme{
        YourScreen()
    }
}
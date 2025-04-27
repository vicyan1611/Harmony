package com.example.harmony.presentation.main.home.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.core.components.RoundedButton
import com.example.harmony.domain.model.Server

@Composable
fun ServerListSidebar(
    servers: List<Server>,
    selectedServerId: String?,
    onServerClick: (Server) -> Unit,
    onAddServerClick: () -> Unit,
    isLoading: Boolean, // To show placeholders or loading indicator
    modifier: Modifier = Modifier
) {
    val sidebarColor = MaterialTheme.colorScheme.background // Or choose a darker color
    Toast.makeText(LocalContext.current, servers.size.toString() ?: "Loading", Toast.LENGTH_SHORT).show()
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(72.dp) // Discord-like width
            .background(sidebarColor)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder for Direct Messages button
        RoundedButton(
            onClick = {},
            modifier = Modifier.padding(4.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Message,
                contentDescription = "Direct Messages",
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(modifier = Modifier.width(40.dp).padding(bottom = 8.dp), color = MaterialTheme.colorScheme.onSurface)

        // Server List
        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                items(servers.size) { // Show shimmer placeholders
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        // Add shimmer effect here if desired
                    )
                }
            } else {
                items(servers, key = { it.id }) { server ->
                    RoundedButton(
                        roundedShape =
                            if (server.id == selectedServerId)
                                RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                            else
                                CircleShape
                        ,
                        onClick = { onServerClick(server) },
                    ) {
                        RoundedAvatar(
                            modifier = Modifier.fillMaxSize(),
                            avatarImageUrl = server.profileUrl,
                            char =
                                    if (server.name.isBlank())
                                        ' '
                                    else
                                        server.name[0],
                        )
                    }
                }
            }
        }

        RoundedButton(
            onClick = onAddServerClick,
            modifier = Modifier.padding(4.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = Icons.Filled.PlusOne,
                contentDescription = "Add new server",
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}
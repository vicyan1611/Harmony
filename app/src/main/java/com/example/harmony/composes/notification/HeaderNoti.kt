package com.example.harmony.composes.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// header of notification
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderNotification() {
    var showBottomSheet by remember { mutableStateOf(false) }
    var checkedRole by remember { mutableStateOf(true) }
    var checkMention by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2E3136))
            .padding(16.dp)
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        IconButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = Color.White
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1E1F22)
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    NotificationSettingItem(
                        icon = Icons.Default.Person,
                        title = "Show Role Mentions",
                        checked = checkedRole,
                        onCheckedChange = { checkedRole = !checkedRole }
                    )

                    NotificationSettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Show Indirect Mentions",
                        subtitle = "@here and @everyone",
                        checked = checkMention,
                        onCheckedChange = {
                            checkMention = !checkMention
                        }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFF42464D)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Text(
                            text = "Notification Settings",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF5865F2)
            )
        )
    }
}
package com.example.harmony.composes.Setting


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val settingsItems = remember { getSettingsItems() }
    val filteredItems = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            settingsItems
        } else {
            settingsItems.map { section ->
                section.copy(
                    items = section.items.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.subtitle?.contains(searchQuery, ignoreCase = true) == true
                    }
                )
            }.filter { it.items.isNotEmpty() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.Gray,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Settings list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            filteredItems.forEach { section ->
                // Section header
                if (section.title.isNotEmpty()) {
                    item {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                }

                // Section items
                items(section.items) { item ->
                    SettingItem(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        onClick = item.onClick
                    )

                    Divider(
                        color = Color(0xFF3F4147),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }

            // Billing Settings section at the bottom
            item {
                Text(
                    text = "Billing Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 16.dp)
        )

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Arrow icon
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open",
            tint = Color.Gray
        )
    }
}

// Data classes for settings
data class SettingsSection(
    val title: String,
    val items: List<SettingItemData>
)

data class SettingItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val onClick: () -> Unit
)

// Sample data
fun getSettingsItems(): List<SettingsSection> {
    return listOf(
        SettingsSection(
            title = "Account Settings",
            items = listOf(
                SettingItemData(
                    icon = Icons.Default.Star,
                    title = "Get Nitro",
                    subtitle = "Subscribe to unlock enhanced features",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.Person,
                    title = "Account",
                    subtitle = "Privacy & security settings",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.Group,
                    title = "Content & Social",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.Lock,
                    title = "Data & Privacy",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.People,
                    title = "Family Center",
                    onClick = { /* Handle click */ }
                )
            )
        ),
        SettingsSection(
            title = "App Settings",
            items = listOf(
                SettingItemData(
                    icon = Icons.Default.Apps,
                    title = "Authorized Apps",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.Devices,
                    title = "Devices",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.Link,
                    title = "Connections",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.ContentCut,
                    title = "Clips",
                    onClick = { /* Handle click */ }
                ),
                SettingItemData(
                    icon = Icons.Default.QrCode,
                    title = "Scan QR Code",
                    onClick = { /* Handle click */ }
                )
            )
        )
    )
}

@Preview
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(onBackClick = {})
    }
}

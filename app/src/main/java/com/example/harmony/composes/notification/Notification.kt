package com.example.harmony.composes.notification

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

data class NotificationItem(
    val channelName: String,
    val message: String,
    val timestamp: String,
    val avatarUrl: String,
    val isMention: Boolean
)

// display each notification
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscordNotification(
    channelName: String,
    message: String,
    timestamp: String,
    avatarUrl: String,
    isMention: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    var showDeleteOptions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick).pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showDeleteOptions = true }
                )
            }
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                //change url image here
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Channel Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = channelName,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Visible,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = timestamp,
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                if (isMention) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(
                                color = Color.Gray,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (isMention) message else "You have new message in $channelName",
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showDeleteOptions) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteOptions = false },
            containerColor = Color(0xFF2E3136)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DeleteOption(
                    icon = Icons.Default.Delete,
                    text = "Remove Notification",
                    onClick = {
                        onDelete()
                        Log.d("Debug From Delete", "Delete")
                        showDeleteOptions = false
                    }
                )

                DeleteOption(
                    icon = Icons.Default.Feedback,
                    text = "Give Feedback on this Notification",
                    onClick = {
                        // Handle feedback action
                        showDeleteOptions = false
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

}

@Composable
fun DeleteOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}






// section header for notification groups
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Color.Gray,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

// main notification screen
@Composable
fun NotificationScreen() {
    val notificationItems = remember {
        listOf(
            NotificationItem(
                channelName = "User1 mentioned you in ML + Mobile - announcement:",
                message = "các bạn làm nhớ chưa sẵn cái gọi string từ R.string... nha\ntạo string trong string xml rồi xài hàm stringResource(R.string....) để truy xuất\n@ketamean @khoile8407 @Vinh Phạm",
                timestamp = "14h",
                avatarUrl = "file:///android_asset/cat.jpg",
                isMention = true
            ),
            NotificationItem(
                channelName = "User2 mentioned you in SAB in HCMUS - Tha thiết tìm bạn nam ở ghép (liên càng tốt ạ):",
                message = "Địa chỉ: Trần Hưng Đạo, P7, Q5 (Gần đoạn Trần Hưng Đạo x Nguyễn Tri Phương x An Bình)\n\nPhòng ở lầu 3 giá 3.500k/phòng/tháng (chưa chia đầu người)\nĐiện, nước giá nhà nước.\nRác 100k/phòng/tháng\nXe 300k/xe\nPhòng gồm 1 phòng khách, 1 gác, 1 phòn...",
                timestamp = "15h",
                avatarUrl = "https://example.com/avatar2.jpg",
                isMention = true
            ),
            NotificationItem(
                channelName = "SAB in HCMUS",
                message = "",
                timestamp = "1d",
                avatarUrl = "https://example.com/avatar3.jpg",
                isMention = false
            ),
            NotificationItem(
                channelName = "User1 mentioned you in SAB in HCMUS - 🍌 | seeding:",
                message = "@Sabies Khởi mới được biết là bên Pima đang có chương trình học này và nó hoàn",
                timestamp = "2d",
                avatarUrl = "file:///android_asset/cat.jpg",
                isMention = true
            ),
            NotificationItem(
                channelName = "User3 ko toi mentioned you in SAB in HCMUS - 🍌 | seeding:",
                message = "@Sabies Khởi mới được biết là bên Pima đang có chương trình học này và nó hoàn",
                timestamp = "2d",
                avatarUrl = "file:///android_asset/cat.jpg",
                isMention = true
            ),
            NotificationItem(
                channelName = "User3 mentioned you in SAB in HCMUS - 🍌 | seeding:",
                message = "@Sabies Khởi mới được biết là bên Pima đang có chương trình học này và nó hoàn",
                timestamp = "2d",
                avatarUrl = "file:///android_asset/cat.jpg",
                isMention = true
            )
        )
    }

    var selectedTab by remember { mutableStateOf(1) } // Default to Notifications tab
    var notifications by remember {mutableStateOf (notificationItems)}

    Scaffold(
        bottomBar = { FooterNavigation(selectedTab, onTabSelected = {
            selectedTab = it
            Log.d("Debug From On tab", "tab $it")
        }) }
    ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = Color(0xFF36393F) // Discord dark theme background
            ) {
                Column {
                    HeaderNotification()

                    LazyColumn {
                            item { SectionHeader("Recent Activity") }

                            items(notifications) { item ->
                                DiscordNotification(
                                    channelName = item.channelName,
                                    message = item.message,
                                    timestamp = item.timestamp,
                                    avatarUrl = item.avatarUrl,
                                    isMention = item.isMention,
                                    onClick = {},
                                    onDelete = {
                                        Log.d("Debug From Delete call back", "Delete")
                                        notifications = notifications.filter { it != item }
                                    }
                                )

                                Divider(
                                    modifier = Modifier.padding(start = 60.dp),
                                    color = Color(0xFF42464D)
                                )
                            }
                        }
                }
            }
        }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    MaterialTheme {
        NotificationScreen()
    }
}

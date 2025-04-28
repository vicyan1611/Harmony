// harmony/presentation/main/chat/messages/MessageSettings.kt
package com.example.harmony.presentation.main.chat.messages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ForwardToInbox // Keep if needed
import androidx.compose.material.icons.automirrored.filled.Reply // Keep if needed
import androidx.compose.material.icons.filled.Edit // Keep if needed
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState // Import SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState // Import rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope // Import rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch // Import launch

// Define the standard emojis and their indices
val standardEmojis = listOf("👍", "❤\uFE0F", "\uD83D\uDE02", "\uD83C\uDF89", "\uD83C\uDF88", "\uD83E\uDD73") // 0 to 5

// Emoji list for the picker (can be expanded)
val reactionEmojiList = listOf(
    "❤️", "👍", "👎", "😂", "😮", "😢", "😠", "🤔",
    "👏", "🙏", "🔥", "⭐", "💯", "🚀", "✅", "❌",
    "❓", "❗", "➕", "➖", "🔗", "🗑️", "✏️", "➡️",
    "⬅️", "⬆️", "⬇️", "🏠", "⚙️", "🔔", "🔍", "👤",
    "💬", "📍", "🗺️", "➕", "➖", "▶️", "⏸️", "⏹️",
    "✉️", "💡", "📸", "🎵", "🎮", "🛒", "☕", "📅", "⏰"
)

sealed class MessageSettingScreens(val route: String, val label: String) {
    object General: MessageSettingScreens("message-setting/", "General")
    object ChooseEmoji: MessageSettingScreens("message-setting/choose-emoji", "Emoji")
}

// Removed NavHost for simplicity now. Will integrate reaction directly.
// If navigation is needed later for Edit/Reply, it can be re-added.

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MessageSetting(
    messageId: String, // Pass the message ID
    onDismissRequest: () -> Unit,
    onEditMsg: () -> Unit, // Keep for future use
    onReactSelected: (emojiIndex: Int) -> Unit, // Callback for reaction selection
    sheetState: SheetState // Pass the SheetState for programmatic control
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState, // Use the passed state
        onDismissRequest = onDismissRequest
    ) {
        // Directly show the general settings content
        MessageGeneralSetting(
            // navController = navController, // Remove NavController for now
            onEditMsg = onEditMsg,
            onDismissRequest = onDismissRequest,
            onReactSelected = { emojiIndex ->
                onReactSelected(emojiIndex) // Pass the selected index up
                scope.launch { // Close the sheet after selection
                    sheetState.hide()
                    onDismissRequest() // Ensure dismiss logic is called
                }
            }
        )
    }
}

@Composable
fun MessageGeneralSetting(
    // navController: NavHostController, // Removed for now
    onEditMsg: () -> Unit,
    onDismissRequest: () -> Unit,
    onReactSelected: (emojiIndex: Int) -> Unit // Changed parameter name
) {
    // Use the standardEmojis list defined above
    LazyColumn (
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 16.dp) // Add vertical padding
    ) {
        item {
            // Row for standard emoji reactions
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                standardEmojis.forEachIndexed { index, emoji -> // Use forEachIndexed
                    Button(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        onClick = { onReactSelected(index) }, // Pass the index
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(
                            text = emoji,
                            style = TextStyle(fontSize = 20.sp)
                        )
                    }
                }

                // "More Emojis" button - Placeholder for now, could open a picker later
                Button(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    onClick = { /* TODO: Implement full emoji picker */ },
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEmotions,
                        contentDescription = "More Emojis"
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

            // Other actions (Edit, Reply, etc.) - Kept for potential future use
            Button(
                onClick = onEditMsg,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {

            }
            // ... other buttons (Reply, Forward) ...
        }
    }
}

// MessageReactChooseEmoji can be removed for now or kept for future expansion
// If kept, it needs to be modified to call onReactSelected with the correct index
// and be navigated to from MessageGeneralSetting.
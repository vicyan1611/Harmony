package com.example.harmony.presentation.main.my_profile

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.harmony.core.components.RoundedButton
import com.example.harmony.core.components.UserProfileLayout

@Composable
fun MyProfile(displayedName: String = "", username: String = "", bio: String = "", avatarUrl: String = "", modifier: Modifier, onDismissRequest: () -> Unit, headerContent: @Composable RowScope.() -> Unit = {}, bodyContent: @Composable RowScope.() -> Unit = {},
              onSettingsClick: () -> Unit
) {
    UserProfileLayout(
        displayedName = displayedName,
        username = username,
        bio = bio,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        avatarUrl = avatarUrl,
        headerContent = {
            headerContent()
            RoundedButton(
                containerColor = MaterialTheme.colorScheme.primary,
                size = 32.dp,
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        bodyContent = {
            bodyContent()
        }
    )
}
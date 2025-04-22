package com.example.harmony.composes.profile

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.harmony.composes.RoundedButton

@Composable
fun MyProfile(
    displayedName: String = "",
    username: String = "",
    bio: String = "",
    avatarUrl: String = "",
    modifier: Modifier,
    onDismissRequest: () -> Unit,
    headerContent: @Composable RowScope.() -> Unit = {},
    bodyContent: @Composable RowScope.() -> Unit = {}
) {
    UserProfileLayout(
        displayedName = displayedName,
        username = username,
        bio = bio,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        avatarUrl = avatarUrl,
        headerContent = {
            bodyContent()
            RoundedButton(
                size = 32.dp,
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        bodyContent = {}
    )
}
package com.example.harmony.presentation.main.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harmony.core.components.ErrorText
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.core.components.HarmonyTextField
import com.example.harmony.domain.model.ChannelType

@Composable
fun AddChannelSheetContent(
    modifier: Modifier = Modifier,
    channelName: String,
    channelDescription: String,
    selectedChannelType: ChannelType,
    isLoading: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeSelected: (ChannelType) -> Unit,
    onCreateClick: () -> Unit,
    onDismissRequest: () -> Unit // To handle sheet dismissal if needed
) {
    val sheetTitle = when (selectedChannelType) {
        ChannelType.TEXT -> "Create Text Channel"
        ChannelType.VOICE -> "Create Voice Channel"
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = sheetTitle, // Dynamic title
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Channel Type Selection ---
        Text("CHANNEL TYPE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChannelTypeSelector(
                type = ChannelType.TEXT,
                icon = Icons.Default.Tag, // Or Icons.Default.Chat
                label = "Text",
                isSelected = selectedChannelType == ChannelType.TEXT,
                onClick = { onTypeSelected(ChannelType.TEXT) },
                modifier = Modifier.weight(1f)
            )
            ChannelTypeSelector(
                type = ChannelType.VOICE,
                icon = Icons.Default.Audiotrack,
                label = "Voice",
                isSelected = selectedChannelType == ChannelType.VOICE,
                onClick = { onTypeSelected(ChannelType.VOICE) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // --- End Channel Type Selection ---

        HarmonyTextField(
            value = channelName,
            onValueChange = onNameChange,
            label = "Channel Name",
            placeholder = if (selectedChannelType == ChannelType.TEXT) "new-text-channel" else "General Voice",
            maxLines = 1,
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth(),
            isEditable = !isLoading,
            showCharactersCount = false,
            maxNChars = 50
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Description field (optional for voice channels)
        if (selectedChannelType == ChannelType.TEXT) {
            HarmonyTextField(
                value = channelDescription,
                onValueChange = onDescriptionChange,
                label = "Channel Description (Optional)",
                placeholder = "What is this channel about?",
                maxLines = 5,
                singleLine = false,
                imeAction = ImeAction.Done,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp), // Reduced height
                isEditable = !isLoading,
                showCharactersCount = true,
                maxNChars = 200
            )
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp)) // Maintain spacing
        }


        if (error != null) {
            ErrorText(error = error, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        HarmonyButton(
            text = "Create Channel",
            onClick = onCreateClick,
            isLoading = isLoading,
            enabled = channelName.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ChannelTypeSelector(
    type: ChannelType,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val selectedBorderColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val unselectedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Surface( // Using Surface for better click effect and background
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) selectedBorderColor else unselectedBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        color = if (isSelected) selectedColor else unselectedColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label)
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}
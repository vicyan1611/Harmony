package com.example.harmony.presentation.main.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harmony.core.components.ErrorText
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.core.components.HarmonyTextField

@Composable
fun AddChannelSheetContent(
    modifier: Modifier = Modifier,
    channelName: String,
    channelDescription: String,
    isLoading: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onDismissRequest: () -> Unit // To handle sheet dismissal if needed
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Text Channel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp // Slightly smaller for bottom sheet
        )
        Spacer(modifier = Modifier.height(16.dp))

        HarmonyTextField(
            value = channelName,
            onValueChange = onNameChange,
            label = "Channel Name",
            placeholder = "new-channel",
            maxLines = 1,
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth(),
            isEditable = !isLoading,
            showCharactersCount = true,
            maxNChars = 50 // Max 50 chars
        )
        Spacer(modifier = Modifier.height(16.dp))

        HarmonyTextField(
            value = channelDescription,
            onValueChange = onDescriptionChange,
            label = "Channel Description (Optional)",
            placeholder = "What is this channel about?",
            maxLines = 4, // Allow multi-line
            singleLine = false,
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), // Give description field some height
            isEditable = !isLoading,
            showCharactersCount = true,
            maxNChars = 200 // Max 200 chars
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            ErrorText(error = error, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        HarmonyButton(
            text = "Create Channel",
            onClick = onCreateClick,
            isLoading = isLoading,
            enabled = channelName.isNotBlank() && !isLoading, // Enable only if name is not blank
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Optional: Add a cancel button
        // TextButton(onClick = onDismissRequest) { Text("Cancel") }
    }
}
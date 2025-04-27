// harmony/presentation/main/profile/edit/EditProfileScreen.kt
package com.example.harmony.presentation.main.profile.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use auto-mirrored icon
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens // Icon for Theme
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Keep this import if needed elsewhere, but not for placeholder/error
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// Import rememberAsyncImagePainter from coil3.compose if not already done
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
// Correct import for placeholder/error with resource IDs
import coil3.request.error
import coil3.request.placeholder
import com.example.harmony.R // Ensure R class is imported correctly
import com.example.harmony.core.components.ErrorText
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.core.components.HarmonyTextField
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.domain.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        viewModel.onEvent(EditProfileEvent.OnAvatarUriChange(uri))
    }

    LaunchedEffect(state.selectedAvatarUri) {
        if (state.selectedAvatarUri == null) {
            selectedImageUri = null
        }
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            onNavigateBack()
        }
    }

    var languageMenuExpanded by remember { mutableStateOf(false) }

    val currentLanguage = state.selectedLanguage ?: state.initialLanguage ?: AppLanguage.ENGLISH
    val currentTheme = state.selectedTheme ?: state.initialTheme ?: AppTheme.LIGHT

    val hasChanges = (state.username != state.initialUsername && state.username.isNotBlank()) ||
            (state.selectedAvatarUri != null) ||
            (state.selectedLanguage != state.initialLanguage && state.selectedLanguage != null) ||
            (state.selectedTheme != state.initialTheme && state.selectedTheme != null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_action)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Avatar Section ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(id = R.string.select_avatar_action)
                    ) { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageToDisplay = selectedImageUri ?: state.currentAvatarUrl
                val placeholderResId = R.drawable.user // Define resource ID
                val errorResId = placeholderResId // Use same or different error drawable ID

                if (imageToDisplay != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(imageToDisplay)
                                .crossfade(true)
                                // Pass Resource ID directly
                                .placeholder(placeholderResId)
                                .error(errorResId)
                                .build()
                        ),
                        contentDescription = stringResource(id = R.string.profile_avatar_description),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display the placeholder directly if no image is selected/available
                    Image(
                        painter = painterResource(id = placeholderResId), // Load placeholder painter here
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 0.6f
                    )
                }
                // Camera icon overlay
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .padding(7.dp)
                        .size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }


            Spacer(modifier = Modifier.height(32.dp))

            // --- Username Section ---
            SectionTitle(text = stringResource(R.string.profile_information_title))
            Spacer(modifier = Modifier.height(12.dp))
            HarmonyTextField(
                value = state.username,
                onValueChange = { viewModel.onEvent(EditProfileEvent.OnUsernameChange(it)) },
                label = stringResource(id = R.string.username_label),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                imeAction = ImeAction.Done,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(28.dp))

            // --- Settings Section ---
            SectionTitle(text = stringResource(R.string.app_settings_title))
            Spacer(modifier = Modifier.height(12.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.outlinedCardColors(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // --- Language Setting ---
                    SettingRow(
                        icon = Icons.Default.Language,
                        title = stringResource(id = R.string.app_language_label),
                        contentDescription = stringResource(id = R.string.language_label)
                    ) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { languageMenuExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentLanguage.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false }
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.displayName) },
                                        onClick = {
                                            viewModel.onEvent(EditProfileEvent.ChangeLanguage(lang))
                                            languageMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))


                    // --- Theme Setting ---
                    SettingRow(
                        icon = Icons.Filled.ColorLens,
                        title = stringResource(id = R.string.theme_label),
                        contentDescription = stringResource(id = R.string.theme_settings_title),
                    )


                    Row(
                        Modifier
                            .selectableGroup()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AppTheme.values().forEach { theme ->
                            Row(
                                Modifier
                                    .selectable(
                                        selected = (currentTheme == theme),
                                        onClick = { viewModel.onEvent(EditProfileEvent.ChangeTheme(theme)) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (currentTheme == theme),
                                    onClick = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            // --- Error Message ---
            if (state.error != null) {
                val errorMessage = when (state.error) {
                    "error_username_empty" -> stringResource(id = R.string.error_username_empty)
                    "error_no_changes" -> stringResource(id = R.string.error_no_changes)
                    "User not logged in" -> stringResource(R.string.error_user_not_logged_in)
                    else -> state.error!!
                }
                ErrorText(
                    error = errorMessage,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }


            // --- Save Button ---
            HarmonyButton(
                text = stringResource(id = R.string.save_changes),
                onClick = { viewModel.onEvent(EditProfileEvent.OnSaveChangesClick) },
                isLoading = state.isLoading,
                enabled = hasChanges && !state.isLoading,
                modifier = Modifier.fillMaxWidth(0.9f)
            )


            Spacer(modifier = Modifier.height(20.dp))

        }
    }
}


// Helper composable for section titles - Adjusted Styling
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}


// Helper composable for consistent setting rows - Adjusted Styling
@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        if (trailingContent != null) {
            Spacer(Modifier.width(8.dp))
            trailingContent()
        }
    }
}
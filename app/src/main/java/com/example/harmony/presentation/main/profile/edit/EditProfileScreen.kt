package com.example.harmony.presentation.main.profile.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // Import stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
// import coil3.request.crossfade // Bỏ import này nếu không dùng coil3 hoặc đã có sẵn
import com.example.harmony.R // Import R class
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
    val currentTheme = state.selectedTheme ?: state.initialTheme ?: AppTheme.LIGHT //Sửa fallback thành SYSTEM_DEFAULT cho theme

    val hasChanges = (state.username != state.initialUsername && state.username.isNotBlank()) ||
            (state.selectedAvatarUri != null) ||
            (state.selectedLanguage != state.initialLanguage && state.selectedLanguage != null) ||
            (state.selectedTheme != state.initialTheme && state.selectedTheme != null)

    Scaffold(
        topBar = {
            TopAppBar(
                // Sử dụng stringResource
                title = { Text(stringResource(id = R.string.edit_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            // Sử dụng stringResource
                            contentDescription = stringResource(id = R.string.back_action)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageToDisplay = selectedImageUri ?: state.currentAvatarUrl

                if (imageToDisplay != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(imageToDisplay)
                                .crossfade(true)
                                .build()
                        ),
                        // Sử dụng stringResource
                        contentDescription = stringResource(id = R.string.profile_avatar_description),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        // Sử dụng stringResource
                        contentDescription = stringResource(id = R.string.select_avatar_action),
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null, // Decorative, không cần dịch
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                        .padding(4.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            HarmonyTextField(
                value = state.username,
                onValueChange = { viewModel.onEvent(EditProfileEvent.OnUsernameChange(it)) },
                // Sử dụng stringResource
                label = stringResource(id = R.string.username_label),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        // Sử dụng stringResource (có thể dùng lại key label)
                        contentDescription = stringResource(id = R.string.username_label)
                    )
                },
                imeAction = ImeAction.Done,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- Language Setting ---
            // Sử dụng stringResource
            Text(stringResource(id = R.string.language_settings_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Language,
                        // Sử dụng stringResource
                        contentDescription = stringResource(id = R.string.language_label),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    // Sử dụng stringResource
                    Text(stringResource(id = R.string.app_language_label))
                }
                Box {
                    TextButton(onClick = { languageMenuExpanded = true }) {
                        Text(currentLanguage.displayName)
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
            // ------------------------

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- Theme Setting ---
            // Sử dụng stringResource
            Text(stringResource(id = R.string.theme_settings_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column(Modifier.selectableGroup()) {
                AppTheme.values().forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { viewModel.onEvent(EditProfileEvent.ChangeTheme(theme)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentTheme == theme),
                            onClick = { viewModel.onEvent(EditProfileEvent.ChangeTheme(theme)) }
                        )
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            // --------------------

            Spacer(modifier = Modifier.height(24.dp))

            // Sử dụng ErrorText component (giả sử nó đã xử lý string bên trong hoặc nhận String)
            if (state.error != null) {
                // Nếu ErrorText nhận String, bạn có thể truyền stringResource nếu lỗi là mã lỗi
                // Hoặc truyền state.error trực tiếp nếu nó đã là thông báo lỗi hoàn chỉnh
                // Ví dụ xử lý mã lỗi:
                val errorMessage = when (state.error) {
                    "error_username_empty" -> stringResource(id = R.string.error_username_empty)
                    "error_no_changes" -> stringResource(id = R.string.error_no_changes)
                    // Thêm các mã lỗi khác nếu cần
                    else -> state.error!! // Hiển thị lỗi gốc nếu không phải mã lỗi đã biết
                }
                ErrorText(error = errorMessage)
                Spacer(modifier = Modifier.height(16.dp))
            }


            HarmonyButton(
                // Sử dụng stringResource
                text = stringResource(id = R.string.save_changes),
                onClick = { viewModel.onEvent(EditProfileEvent.OnSaveChangesClick) },
                isLoading = state.isLoading,
                enabled = hasChanges && !state.isLoading,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}
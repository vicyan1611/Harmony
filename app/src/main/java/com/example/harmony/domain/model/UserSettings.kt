package com.example.harmony.domain.model

// Định nghĩa Enum cho các ngôn ngữ hỗ trợ
enum class AppLanguage(val displayName: String, val localeTag: String) { // Thêm displayName để hiển thị thân thiện hơn
    ENGLISH("English", "en"),
    VIETNAMESE("Tiếng Việt", "vi");
    // Thêm ngôn ngữ khác nếu cần

    companion object {
        // Hàm tiện ích để lấy enum từ tên hiển thị hoặc tên enum
        fun fromString(name: String?): AppLanguage {
            return values().find { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: ENGLISH // Mặc định là English
        }
        fun fromLocaleTag(tag: String?): AppLanguage {
            return values().find { it.localeTag.equals(tag, ignoreCase = true) } ?: ENGLISH
        }
        // Function to get default system language if needed
        fun getDefault(): AppLanguage {
            val defaultLocale = java.util.Locale.getDefault().language
            return fromLocaleTag(defaultLocale) ?: ENGLISH
        }
    }
}

// Ensure AppTheme is defined
enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark");
    // Add SYSTEM_DEFAULT if needed: SYSTEM_DEFAULT("System Default")
    companion object {
        fun fromString(name: String?): AppTheme {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: LIGHT // Default to Light
        }
    }
}

data class UserSettings(
    val enableNotifications: Boolean = true, // Keep or remove as needed
    val language: AppLanguage = AppLanguage.ENGLISH, // Default language
    val theme: AppTheme = AppTheme.LIGHT, // Default theme
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Ensure constructor provides defaults matching the fields
    constructor() : this(true, AppLanguage.ENGLISH, AppTheme.LIGHT, System.currentTimeMillis())
}
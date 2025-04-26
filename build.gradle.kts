// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
//    kotlin("kapt") version "2.1.10"
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
    id("com.google.dagger.hilt.android") version "2.56.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

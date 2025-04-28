package com.example.harmony.di

import android.content.Context
import com.example.harmony.data.rtc.AgoraRtcManager
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.PresenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RtcModule {

    @Provides
    @Singleton
    fun provideAgoraRtcManager(
        @ApplicationContext context: Context,
        authRepository: AuthRepository, // Hilt provides this from RepositoryModule
        presenceRepository: PresenceRepository // Add PresenceRepository as a parameter
    ): AgoraRtcManager {
        // Hilt will now automatically inject AuthRepository and PresenceRepository
        val manager = AgoraRtcManager(context, authRepository, presenceRepository)
        manager.initialize()
        return manager
    }
}
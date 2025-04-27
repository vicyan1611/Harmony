package com.example.harmony.di

import com.example.harmony.data.repository.AuthRepositoryImpl
import com.example.harmony.data.repository.DirectMessageRepositoryImpl
import com.example.harmony.data.repository.MessageRepositoryImpl
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.DirectMessageRepository
import com.example.harmony.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindDirectMessageRepositoryImpl(
        directMessageRepositoryImpl: DirectMessageRepositoryImpl
    ): DirectMessageRepository
}
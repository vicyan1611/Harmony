package com.example.harmony.di

import com.example.harmony.data.repository.AuthRepositoryImpl
import com.example.harmony.data.repository.DirectMessageRepositoryImpl
import com.example.harmony.data.repository.ChannelRepositoryImpl
import com.example.harmony.data.repository.ServerRepositoryImpl
import com.example.harmony.data.repository.UserRepositoryImpl
import com.example.harmony.data.repository.MessageRepositoryImpl
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.DirectMessageRepository
import com.example.harmony.domain.repository.ChannelRepository
import com.example.harmony.domain.repository.ServerRepository
import com.example.harmony.domain.repository.UserRepository
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
    abstract fun bindServerRepository(
        serverRepositoryImpl: ServerRepositoryImpl
    ): ServerRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindChannelRepository(
        channelRepositoryImpl: ChannelRepositoryImpl
    ): ChannelRepository

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
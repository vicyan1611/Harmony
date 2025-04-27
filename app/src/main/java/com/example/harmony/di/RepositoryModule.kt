package com.example.harmony.di

import com.example.harmony.data.repository.AuthRepositoryImpl
import com.example.harmony.data.repository.ServerRepositoryImpl
import com.example.harmony.data.repository.StorageRepositoryImpl
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ServerRepository
import com.example.harmony.domain.repository.StorageRepository
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
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository
}
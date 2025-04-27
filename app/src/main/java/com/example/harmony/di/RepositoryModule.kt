// harmony/di/RepositoryModule.kt - CORRECTED IMPORT
package com.example.harmony.di

// Correct the import for UserRepository
import com.example.harmony.data.repository.AuthRepositoryImpl
import com.example.harmony.data.repository.UserRepositoryImpl
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.UserRepository
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
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl // Provide the implementation class
    ): UserRepository // Return the interface

}
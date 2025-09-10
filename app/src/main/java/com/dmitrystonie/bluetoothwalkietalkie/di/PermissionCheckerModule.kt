package com.dmitrystonie.bluetoothwalkietalkie.di

import android.content.Context
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionChecker
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object PermissionCheckerModule {

    @Provides
    @Singleton
    fun providePermissionChecker(
        @ApplicationContext context: Context
    ): PermissionCheckerImpl = PermissionCheckerImpl(
        context = context, mainDispatcher = Dispatchers.Main.immediate
    )
}
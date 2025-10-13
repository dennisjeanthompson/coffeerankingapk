package com.example.coffeerankingapk.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // TODO: Add repository providers when implementing real data layer
    // @Provides
    // @Singleton
    // fun provideCafeRepository(): CafeRepository {
    //     return CafeRepositoryImpl()
    // }
    
    // TODO: Add Firebase providers
    // @Provides
    // @Singleton
    // fun provideFirebaseAuth(): FirebaseAuth {
    //     return FirebaseAuth.getInstance()
    // }
    
    // @Provides
    // @Singleton
    // fun provideFirestore(): FirebaseFirestore {
    //     return FirebaseFirestore.getInstance()
    // }
    
    // TODO: Add Google Maps providers
    // @Provides
    // @Singleton
    // fun provideLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
    //     return LocationServices.getFusedLocationProviderClient(context)
    // }
}
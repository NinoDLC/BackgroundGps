package fr.delcey.backgroundgps.data;

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.delcey.backgroundgps.domain.location.GpsLocationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSingletonBindModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: GpsLocationRepositoryImpl): GpsLocationRepository
}
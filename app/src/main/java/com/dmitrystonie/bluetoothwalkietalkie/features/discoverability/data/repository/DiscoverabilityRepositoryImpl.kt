package com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.data.repository

import com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.data.datasourse.DiscoverabilityDatasource
import com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.domain.repository.DiscoverabilityRepository
import javax.inject.Inject

class DiscoverabilityRepositoryImpl @Inject constructor(private val datasource: DiscoverabilityDatasource):
    DiscoverabilityRepository {
    override fun getDiscoverability(): Boolean = datasource.getDiscoverability()

    override fun enableDiscoverability() {
        if(!datasource.getDiscoverability()){
            datasource.enableDiscoverability()
        }
    }

    override fun disableDiscoverability() {
//        TODO("Not yet implemented")
    }
}
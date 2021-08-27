package com.timothy.coffee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.timothy.coffee.data.LocationDataSource
import com.timothy.coffee.data.CafeDataSource
import javax.inject.Inject

//為了要把datamodel帶進viewmodel，只好實作factory
class ViewModelFactory @Inject constructor(
    private val locationDataSource: LocationDataSource,
    private val cafeDataSource: CafeDataSource
):ViewModelProvider.Factory {

    //如果是要provide MainViewModel的話，就把datamodel帶進去並回傳
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(locationDataSource,cafeDataSource) as T
        else
            throw IllegalArgumentException("Unknown ViewModel class")
    }
}
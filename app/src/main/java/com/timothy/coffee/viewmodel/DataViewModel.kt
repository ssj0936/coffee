package com.timothy.coffee.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.timothy.coffee.model.DataModel
import com.timothy.coffee.util.AbsentLiveData

class DataViewModel(application: Application) :AndroidViewModel(application){

    private val mAppContext:Context = application.applicationContext
    private val dataModel = DataModel()

    private lateinit var loc :MutableLiveData<List<Double>>


    fun fetchLocation(): LiveData<List<Double>>{
        return dataModel.getLocation(mAppContext) ?: AbsentLiveData.create<List<Double>>()
    }
}
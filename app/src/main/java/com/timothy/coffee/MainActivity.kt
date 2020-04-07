package com.timothy.coffee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.timothy.coffee.databinding.ActivityMainBinding
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.util.Util
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    private lateinit var mMainViewModel:MainViewModel

    @Inject
    lateinit var mViewModelFactory:ViewModelFactory
    private lateinit var binding : ActivityMainBinding

    private val adapter:CafeAdapter = CafeAdapter(listOf())

    val TAG = "[coffee] MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        mMainViewModel = ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)

        Util.isLocationPermissionGet(this@MainActivity)

        binding.fetchBtn.setOnClickListener {
            mMainViewModel.getCafeList(this@MainActivity)
        }

        binding.recyclerViewCafeList.layoutManager=LinearLayoutManager(this)
        binding.recyclerViewCafeList.adapter = adapter

        mMainViewModel.cafeList.observe(this, Observer {
            Log.d(TAG,"cafe list Changed")
            binding.recyclerViewCafeList.swapAdapter(CafeAdapter(it),false)
        })
    }
}

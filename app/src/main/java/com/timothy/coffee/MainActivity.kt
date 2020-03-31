package com.timothy.coffee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.databinding.ActivityMainBinding
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.util.Util
import com.timothy.coffee.viewmodel.DataViewModel
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
//import io.reactivex.rxjava3.core.ObservableSource
//import io.reactivex.rxjava3.disposables.Disposable
//import io.reactivex.rxjava3.functions.Function
//import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var mViewModel:DataViewModel
    private lateinit var binding : ActivityMainBinding

    private val adapter:CafeAdapter = CafeAdapter(listOf())
    private var count = 0

    val TAG = "[coffee] MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        mViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)

        binding.fetchBtn.setOnClickListener {
            mViewModel.getCafeList(this@MainActivity)
        }

        binding.recyclerViewCafeList.layoutManager=LinearLayoutManager(this)
        binding.recyclerViewCafeList.adapter = adapter
        binding.recyclerViewCafeList.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        mViewModel.cafeList.observe(this, Observer {
            Log.d(TAG,"cafe list Changed")
            binding.recyclerViewCafeList.swapAdapter(CafeAdapter(it),false)
        })
    }
}

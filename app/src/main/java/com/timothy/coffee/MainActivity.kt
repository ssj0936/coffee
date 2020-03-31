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
            Log.d(TAG,"click")
//            mViewModel.fetchLocation(++count)
            mViewModel.getLocationObservable()
                .observeOn(Schedulers.newThread())
                .flatMap(object :Function<LonAndLat, ObservableSource<Locationiq>>{
                    override fun apply(t: LonAndLat): ObservableSource<Locationiq> {
                        Log.d(TAG,"first flatmap : ${Thread.currentThread().name}")

                        Log.d(TAG,"longitude:${t.longitude},latitude:${t.latitude}")
                        return mViewModel.getLocationiqObservable(t.latitude!!,t.longitude!!)
                    }
                },false)
                .flatMap {
                    Log.d(TAG,"second flatmap : ${Thread.currentThread().name}")

                    mViewModel.getCafenomadObservable(it.address!!.state!!)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object:io.reactivex.Observer<List<Cafenomad>>{
                    override fun onComplete() {
                            Log.d(TAG,"subscribe onComplete: ${Thread.currentThread().name}")
                            Toast.makeText(this@MainActivity, "download OK", Toast.LENGTH_SHORT).show();
                        }

                        override fun onSubscribe(d: Disposable?) {
                        }

                        override fun onNext(t: List<Cafenomad>) {
                            Log.d(TAG,"subscribe onNext: ${Thread.currentThread().name}")

                            Log.d(TAG,"cafe list Changed")
                            binding.recyclerViewCafeList.swapAdapter(CafeAdapter(t),false)
                        }

                        override fun onError(e: Throwable?) {
                            Log.e(TAG, "Exception: "+Log.getStackTraceString(e))
                            Toast.makeText(this@MainActivity,e.toString(), Toast.LENGTH_SHORT).show()
                        }
                })
        }

//        mViewModel.loc.observe(this, Observer<LonAndLat> {loc ->
//            Log.d(TAG,"Changed")
//            if(loc != null) {
//                Log.d(TAG,"loc != null")
//                if (loc.latitude != null && loc.longitude != null) {
//                    binding.loc.text = "longitude:${loc.longitude},latitude:${loc.latitude}"
//                }
//            }else{
//                Log.d(TAG,"loc == null")
//            }
//        })
        binding.recyclerViewCafeList.layoutManager=LinearLayoutManager(this)
        binding.recyclerViewCafeList.adapter = adapter
        binding.recyclerViewCafeList.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

//        mViewModel.cafeList.observe(this, Observer {
//            Log.d(TAG,"cafe list Changed")
//            binding.recyclerViewCafeList.swapAdapter(CafeAdapter(it),false)
//        })
    }
}

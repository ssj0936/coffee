package com.timothy.coffee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.timothy.coffee.databinding.ActivityMainBinding
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.viewmodel.DataViewModel

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
            mViewModel.fetchLocation(++count)
        }

        mViewModel.loc.observe(this, Observer<LonAndLat> {loc ->
            Log.d(TAG,"Changed")
            if(loc != null) {
                Log.d(TAG,"loc != null")
                if (loc.latitude != null && loc.longitude != null) {
                    binding.loc.text = "longitude:${loc.longitude},latitude:${loc.latitude}"
                }
            }else{
                Log.d(TAG,"loc == null")
            }
        })
        binding.recyclerViewCafeList.layoutManager=LinearLayoutManager(this)
        binding.recyclerViewCafeList.adapter = adapter
        binding.recyclerViewCafeList.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        mViewModel.cafeList.observe(this, Observer {
            Log.d(TAG,"cafe list Changed")
            binding.recyclerViewCafeList.swapAdapter(CafeAdapter(it),false)
        })
    }
}

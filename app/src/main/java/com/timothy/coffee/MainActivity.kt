package com.timothy.coffee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
//import android.arch.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModelProviders
import com.timothy.coffee.databinding.ActivityMainBinding
import com.timothy.coffee.model.DataModel
import com.timothy.coffee.viewmodel.DataViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mViewModel:DataViewModel
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        mViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)


        binding.fetchBtn.setOnClickListener {
            mViewModel.fetchLocation()
        }
    }
}

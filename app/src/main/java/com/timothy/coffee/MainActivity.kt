package com.timothy.coffee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
//import android.arch.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModelProviders
import com.timothy.coffee.model.DataModel
import com.timothy.coffee.viewmodel.DataViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetch_btn.setOnClickListener {
            mViewModel.fetchLocation()
        }
    }
}

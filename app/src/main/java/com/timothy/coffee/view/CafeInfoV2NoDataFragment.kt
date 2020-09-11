package com.timothy.coffee.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.timothy.coffee.R

class CafeInfoV2NoDataFragment:Fragment(){

    companion object {
        @JvmStatic
        private lateinit var INSTANCE:CafeInfoV2NoDataFragment
        fun getInstance():CafeInfoV2NoDataFragment{
            if(!::INSTANCE.isInitialized){
                INSTANCE = CafeInfoV2NoDataFragment()
            }
            return INSTANCE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cafe_info_v2_nodata,container,false)
    }
}
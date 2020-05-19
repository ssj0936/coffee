package com.timothy.coffee.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.timothy.coffee.R
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.databinding.FragmentCafeInfoBinding
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
class CafeInfoFragment: Fragment() {

    lateinit var binding:FragmentCafeInfoBinding
    private lateinit var mMainViewModel: MainViewModel
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel = activity?.run {
            ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCafeInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner = this

    }

    companion object {
        @JvmStatic
        private lateinit var INSTANCE:CafeInfoFragment
        fun getInstance():CafeInfoFragment{
            if(!::INSTANCE.isInitialized){
                INSTANCE = CafeInfoFragment()
            }
            return INSTANCE
        }
    }
}

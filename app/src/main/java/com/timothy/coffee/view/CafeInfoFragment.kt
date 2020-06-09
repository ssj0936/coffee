package com.timothy.coffee.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import com.timothy.coffee.R
import com.timothy.coffee.databinding.FragmentCafeInfoBinding
import com.timothy.coffee.util.Utils
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_cafe_list.*
import javax.inject.Inject


class CafeInfoFragment: Fragment(),CafeBaseFragment ,View.OnClickListener{
    lateinit var binding:FragmentCafeInfoBinding
    private lateinit var mMainViewModel: MainViewModel
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

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

        binding.btnNavigate.setOnClickListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val anchorOffset = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_anchor_offset)
        view.setPadding(0, 0, 0, anchorOffset)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner = this

    }

    override fun onClick(v: View?) {
        val intent = Utils.getGoogleMapDirectionIntent(
            mMainViewModel.loc.value!!.latitude,
            mMainViewModel.loc.value!!.longitude,
            mMainViewModel.chosenCafe.value!!.name)
        startActivity(intent)
    }

    override fun setNestScrollingEnable(enable:Boolean){
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            binding.nestedScrollView.isSmoothScrollingEnabled = enable
        }
    }


}

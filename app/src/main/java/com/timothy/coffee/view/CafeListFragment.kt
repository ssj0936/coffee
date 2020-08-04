package com.timothy.coffee.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.timothy.coffee.R
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.databinding.FragmentCafeListBinding
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.ui.CafeListDecoration
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_cafe_list.*
import javax.inject.Inject

class CafeListFragment:Fragment(),CafeBaseFragment,CafeAdapter.OnCafeAdapterClickListener{
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private var adapter:CafeAdapter = CafeAdapter(listOf(),this)
    lateinit var binding:FragmentCafeListBinding
    private lateinit var mMainViewModel: MainViewModel

    companion object{
        private lateinit var INSTANCE: CafeListFragment
        fun getInstance():CafeListFragment{
            if(!::INSTANCE.isInitialized)
                INSTANCE =CafeListFragment()
            return INSTANCE
        }
    }
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel = activity?.run {
            ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCafeListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewCafeList.adapter = adapter
        recyclerViewCafeList.addItemDecoration(
            CafeListDecoration(requireContext(),
                LinearLayoutManager.VERTICAL,
                isDrawLastDivider = false,
                isDrawFirstDivider = false)
        )

        val anchorOffset = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_anchor_offset)
        view.setPadding(0, 0, 0, anchorOffset)

        mMainViewModel.cafeListDisplay.observe(viewLifecycleOwner,
            Observer<List<CafenomadDisplay>>{
                adapter = CafeAdapter(it,this)
                recyclerViewCafeList.swapAdapter(
                    adapter,
                    false)
                recyclerViewCafeList.visibility = View.VISIBLE
            })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner = this
    }

    override fun onItemClick(cafe: CafenomadDisplay) {
        mMainViewModel.chosenCafe.value = cafe
        mMainViewModel.lastMove.isClickList = true
        mMainViewModel.lastMove.isClickMap = false
    }

    override fun setNestScrollingEnable(enable:Boolean){
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            recyclerViewCafeList.isNestedScrollingEnabled = enable
        }
    }
}
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
import com.timothy.coffee.R
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.util.Utils
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_cafe_list.*
import timber.log.Timber
import javax.inject.Inject

class CafeListFragment:Fragment(),CafeBaseFragment,CafeAdapter.OnCafeAdapterClickListener{
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private var adapter:CafeAdapter = CafeAdapter(listOf(),this)
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
        return inflater.inflate(R.layout.fragment_cafe_list,container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewCafeList.adapter = adapter

        val anchorOffset = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_anchor_offset)
        view.setPadding(0, 0, 0, anchorOffset)

        mMainViewModel.cafeList.observe(viewLifecycleOwner,
            Observer<List<Cafenomad>>{
                adapter = CafeAdapter(it,this)
                recyclerViewCafeList.swapAdapter(
                    adapter,
                    false)
                recyclerViewCafeList.visibility = View.VISIBLE
            })
    }

    override fun onItemClick(cafe: Cafenomad) {
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
package com.timothy.coffee.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.timothy.coffee.R
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.databinding.CafelistFragmentBinding
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.util.Util
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class CafeListFragment:Fragment(),CafeAdapter.OnCafeAdapterClickListener{
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    lateinit var binding:CafelistFragmentBinding
    private var adapter:CafeAdapter = CafeAdapter(listOf(),this)
    private val compositeDisposable = CompositeDisposable()
    private lateinit var mMainViewModel: MainViewModel

    companion object{
        private lateinit var INSTANCE: CafeListFragment
        fun getInstance():CafeListFragment{
            if(!::INSTANCE.isInitialized)
                INSTANCE =CafeListFragment()
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Util.isLocationPermissionGet(context!!)
        mMainViewModel.getCafeList(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CafelistFragmentBinding.inflate(inflater,container,false)

        binding.recyclerViewCafeList.layoutManager= LinearLayoutManager(context!!)
        binding.recyclerViewCafeList.adapter = adapter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.add(
            mMainViewModel.getCafeList(context!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    adapter = CafeAdapter(it,this)
                    binding.recyclerViewCafeList.swapAdapter(
                        adapter,
                        false)
                },{error-> Timber.d(error)})
        )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onItemClick(cafe: Cafenomad) {
        mMainViewModel.chosenCafe.value = cafe
        Timber.d(cafe.name)
    }

}
package com.timothy.coffee

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
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

class CafeListFragment :Fragment(){
    private lateinit var mMainViewModel: MainViewModel

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    lateinit var binding:CafelistFragmentBinding
    private val adapter:CafeAdapter = CafeAdapter(listOf(),null)
    private val compositeDisposable = CompositeDisposable()

    companion object{
        const val TAG:String = "CafeListFragment"
        val NEW_INSTANCE:CafeListFragment = CafeListFragment()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mMainViewModel = ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
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
                    binding.recyclerViewCafeList.swapAdapter(
                        CafeAdapter(it,mMainViewModel.loc.value),
                        false)
                },{error-> Timber.d(error)})
        )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}
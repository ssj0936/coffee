package com.timothy.coffee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.timothy.coffee.databinding.ActivityMainBinding
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.util.Util
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    private lateinit var mMainViewModel:MainViewModel

    @Inject
    lateinit var mViewModelFactory:ViewModelFactory
    private lateinit var binding : ActivityMainBinding
    private val adapter:CafeAdapter = CafeAdapter(listOf(),null)
    private val compositeDisposable = CompositeDisposable()

    val TAG = "[coffee] MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        mMainViewModel = ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        Util.isLocationPermissionGet(this@MainActivity)

        mMainViewModel.getCafeList(this@MainActivity)

        binding.recyclerViewCafeList.layoutManager=LinearLayoutManager(this)
        binding.recyclerViewCafeList.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.add(
            mMainViewModel.getCafeList(this@MainActivity)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    binding.recyclerViewCafeList.swapAdapter(
                        CafeAdapter(it,mMainViewModel.loc.value),
                        false)
                },{error->Timber.d(error)})
        )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}

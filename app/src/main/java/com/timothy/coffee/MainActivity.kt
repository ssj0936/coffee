package com.timothy.coffee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasAndroidInjector {

    private lateinit var mMainViewModel:MainViewModel

    @Inject
    lateinit var dispatchingAndroidInjector:DispatchingAndroidInjector<Any>

    @Inject
    lateinit var mViewModelFactory:ViewModelFactory
//    private lateinit var binding : ActivityMainBinding
    private val adapter:CafeAdapter = CafeAdapter(listOf(),null)
    private val compositeDisposable = CompositeDisposable()

    val TAG = "[coffee] MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity);

        val tag = CafeListFragment.TAG

        //單純產生一個fragment instance 並以fragmentManager加入
        if(supportFragmentManager.findFragmentByTag(tag) == null) {
            Timber.d("fragment == NULL, create a new fragment instance")
            val fragment: CafeListFragment = CafeListFragment.NEW_INSTANCE
            supportFragmentManager.beginTransaction()
                .add(R.id.container,fragment,tag)
                .commit()
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    //    override fun onStart() {
//        super.onStart()
//        compositeDisposable.add(
//            mMainViewModel.getCafeList(this@MainActivity)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    binding.recyclerViewCafeList.swapAdapter(
//                        CafeAdapter(it,mMainViewModel.loc.value),
//                        false)
//                },{error->Timber.d(error)})
//        )
//    }
//
//    override fun onStop() {
//        super.onStop()
//        compositeDisposable.clear()
//    }
}

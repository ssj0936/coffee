package com.timothy.coffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayoutMediator
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.databinding.MainActivityBinding
import com.timothy.coffee.util.reduceDragSensitivity
import com.timothy.coffee.ui.CafeViewPager2Adapter
import com.timothy.coffee.ui.CafeViewPagerAdapter
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector:DispatchingAndroidInjector<Any>

    private lateinit var mMainViewModel: MainViewModel
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private val mPageNum = 2
    private lateinit var cafeAdapter: CafeViewPagerAdapter
    private lateinit var cafeAdapter2: CafeViewPager2Adapter
    private lateinit var binding:MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("Activity onCreate")
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.main_activity)

        mMainViewModel = ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        //viewpager
        cafeAdapter = CafeViewPagerAdapter(supportFragmentManager)
        //viewpager2
//        cafeAdapter2 = CafeViewPager2Adapter(
//            this,
//            mPageNum
//        )
        viewpager.adapter = cafeAdapter
//        viewpager.reduceDragSensitivity()
        val behavior = AnchorBottomSheetBehavior.from(viewpager)

        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner=this
//        TabLayoutMediator(indicator,viewpager){tab,position ->
//            Timber.d(""+tab.text)
//            Timber.d(""+position)
//        }.attach()

        mMainViewModel.chosenCafe.observe (this,
            Observer<Cafenomad> {
                //unhidden second page
//                if(cafeAdapter2.isInfoPageHide.value!!){
//                    cafeAdapter2.setHideInfoPage(false)
//                }
                if(cafeAdapter.isInfoPageHide.value!!){
                    cafeAdapter.setHideInfoPage(false)
                }

                //nav to second page
                viewpager.currentItem = mPageNum-1

                if(behavior.state!=AnchorBottomSheetBehavior.STATE_ANCHORED)
                    behavior.state = AnchorBottomSheetBehavior.STATE_ANCHORED
            })
    }

    override fun onBackPressed() {
        if(viewpager.currentItem == 0) {
            viewpager.currentItem = -1
            super.onBackPressed()
        }
        else
            viewpager.currentItem = viewpager.currentItem - 1;

    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}

package com.timothy.coffee

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayoutMediator
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.databinding.MainActivityBinding
import com.timothy.coffee.util.reduceDragSensitivity
import com.timothy.coffee.ui.CafeViewPager2Adapter
import com.timothy.coffee.ui.CafeViewPagerAdapter
import com.timothy.coffee.util.Utils
import com.timothy.coffee.view.MapFragment
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector:DispatchingAndroidInjector<Any>
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var mMainViewModel: MainViewModel
    private lateinit var cafeAdapter: CafeViewPagerAdapter
//    private lateinit var cafeAdapter2: CafeViewPager2Adapter
    private lateinit var binding:MainActivityBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var behavior: AnchorBottomSheetBehavior<View>

    private val mPageNum = 2
    private val RESULT_PERMISSION_LOCATION = 0
    private val RESULT_CODE_WIFI = 1
    private val RESULT_MANUAL_ENABLE = 2

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("Activity onCreate")
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        //data binding
        binding = DataBindingUtil.setContentView(this,R.layout.main_activity)

        //viewmodel setup
        mMainViewModel = ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner=this

        //viewpager
        cafeAdapter = CafeViewPagerAdapter(supportFragmentManager)
        binding.viewpager.adapter = cafeAdapter
        behavior = AnchorBottomSheetBehavior.from(viewpager)

        //when user clicked item of cafe list
        mMainViewModel.chosenCafe.observe (this,
            Observer<Cafenomad> {
                if(cafeAdapter.isInfoPageHide.value!!){
                    cafeAdapter.setHideInfoPage(false)
                }

                //nav to second page
                binding.viewpager.currentItem = mPageNum-1

                if(behavior.state!=AnchorBottomSheetBehavior.STATE_ANCHORED)
                    behavior.state = AnchorBottomSheetBehavior.STATE_ANCHORED
            }
        )

        //check network
        if(!Utils.isNetworkAvailable(this))
            showNetworkConnectDialog()

        //check permission
        if(!isPermissionGranted()){
            Timber.d("need to request")
            permissionRequest()
        }
    }

    private fun initMap(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, MapFragment.getInstance(), MapFragment.TAG)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.d("onRequestPermissionsResult")
        when(requestCode){
            RESULT_PERMISSION_LOCATION->{
                if(isPermissionGranted()){
                    requestCafe()
                }else{
                    showManualPermissionSettingsDialog()
                }
            }
            else ->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            RESULT_CODE_WIFI-> {
                if (Utils.isNetworkAvailable(this)) {
                    Timber.d("isNetworkAvailable")
                    if (isPermissionGranted()) {
                        Timber.d("isPermissionGranted")
                        requestCafe()
                    } else {
                        Timber.d("!!isPermissionGranted")
                        permissionRequest()
                    }
                }else{
                    showNetworkConnectDialog()
                }
            }
            RESULT_PERMISSION_LOCATION->{

            }

            RESULT_MANUAL_ENABLE->{
                if(isPermissionGranted()){
                    requestCafe()
                }else{
                    showManualPermissionSettingsDialog()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        if(viewpager.currentItem == 0) {
            when(behavior.state){
                AnchorBottomSheetBehavior.STATE_ANCHORED ->
                    behavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
                AnchorBottomSheetBehavior.STATE_COLLAPSED -> {
                    viewpager.currentItem = -1
                    super.onBackPressed()
                }
            }
        }
        else if(viewpager.currentItem == 1) {
            if(mMainViewModel.lastMove.isClickList)
                viewpager.currentItem = viewpager.currentItem - 1
            else if (mMainViewModel.lastMove.isClickMap){
                when(behavior.state){
                    AnchorBottomSheetBehavior.STATE_ANCHORED ->
                        behavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
                    AnchorBottomSheetBehavior.STATE_COLLAPSED -> {
                        viewpager.currentItem = viewpager.currentItem - 1
                        behavior.state = AnchorBottomSheetBehavior.STATE_ANCHORED
                    }
                }

            }

        }
    }

    private fun showNetworkConnectDialog(){
        AlertDialog.Builder(this)
            .setMessage(R.string.dialog_network_require_message)
            .setPositiveButton(R.string.dialog_network_require_pos_btn
            ) { _, _ ->
                val wifiIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivityForResult(wifiIntent,RESULT_CODE_WIFI)
            }
            .setCancelable(false)
            .create().show()
    }

    private fun showManualPermissionSettingsDialog(){
        AlertDialog.Builder(this)
            .setMessage(R.string.dialog_permission_require_message)
            .setPositiveButton("Permission setting"
            ) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, RESULT_MANUAL_ENABLE)

            }
            .setNegativeButton("Finish"
            ) { _, _ ->finish()}
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onStart() {
        super.onStart()

        //get Cafe info when network available && permission granted
        if(Utils.isNetworkAvailable(this) && isPermissionGranted()) {
            initMap()
            requestCafe()
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    private fun requestCafe(){
        compositeDisposable.add(
            mMainViewModel.getCafeList(this)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    //play animation first time
                    if(mMainViewModel.cafeList.value == null) {
                        val anim = AnimationUtils.loadAnimation(this, R.anim.translate)
                        anim.interpolator = OvershootInterpolator()
                        viewpager.startAnimation(anim)
                    }

                    mMainViewModel.cafeList.value = it
                },{error-> Timber.d(error)})
        )
    }

    private fun isPermissionGranted():Boolean{
        val permissionMissingList = arrayListOf<String>()

        for(p in Utils.needPermissions){
            if(ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
                permissionMissingList.add(p)
        }

        Timber.d("isPermissionGranted:${permissionMissingList.size == 0}")

        return permissionMissingList.size == 0
    }

    private fun permissionRequest(){
        ActivityCompat.requestPermissions(this, Utils.needPermissions, RESULT_PERMISSION_LOCATION)
    }
}

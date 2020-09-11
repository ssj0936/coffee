package com.timothy.coffee

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.databinding.FragmentMainBinding
import com.timothy.coffee.ui.CafeViewPagerAdapterV2
import com.timothy.coffee.util.Utils
import com.timothy.coffee.view.CafeInfoV2Fragment
import com.timothy.coffee.view.MapFragment
import com.timothy.coffee.view.SortDialogFragment
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import java.util.stream.IntStream
import javax.inject.Inject

val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

class MainFragment: Fragment()
    , View.OnClickListener
{
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var mMainViewModel: MainViewModel
//    private lateinit var cafeAdapter: CafeViewPagerAdapter
    private lateinit var cafeAdapter: CafeViewPagerAdapterV2
    private lateinit var binding: FragmentMainBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var behavior: AnchorBottomSheetBehavior<View>

    companion object{
        const val RESULT_PERMISSION_LOCATION = 0
        const val RESULT_CODE_WIFI = 1
        const val RESULT_MANUAL_ENABLE = 2

        const val TAG = "MainFragment"
        private lateinit var INSTANCE:MainFragment
        fun getInstance():MainFragment{
            if(!::INSTANCE.isInitialized){
                INSTANCE = MainFragment()
            }
            return INSTANCE
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        //viewpager
//        cafeAdapter = CafeViewPagerAdapter(requireActivity().supportFragmentManager)
        cafeAdapter = CafeViewPagerAdapterV2(requireActivity().supportFragmentManager)
        binding.viewpager?.apply {
            adapter = cafeAdapter
            pageMargin = 10.toPx
            addOnPageChangeListener(mPageChangeListener)
        }
        behavior = AnchorBottomSheetBehavior.from(viewpager)

        //when user clicked item of cafe list
        mMainViewModel.chosenCafe.observe (viewLifecycleOwner,
            Observer<CafenomadDisplay> {
//                Timber.d("chosen: $it")
//                //nav to second page
//                binding.viewpager.currentItem = mPageNum-1

                //nav to chosen cafe position or index0
                if(it == null){
                    binding.viewpager.currentItem = 0
                }else {
                    val findIndex: Int = IntStream.range(0, cafeAdapter.cafeListCurrent.size)
                        .filter { i -> cafeAdapter.cafeListCurrent[i].cafenomad.id == it.cafenomad.id }
                        .findFirst().orElse(0)

                    binding.viewpager.currentItem = findIndex
                }

                //scroll to top
                (cafeAdapter.getItem(binding.viewpager.currentItem) as? CafeInfoV2Fragment)?.scrollToTop()

            }
        )

        mMainViewModel.cafeListDisplay.observe(viewLifecycleOwner,
            Observer<List<CafenomadDisplay>>{
                //"no data" fragment no need to be able to drag
                behavior.allowUserDragging = it.isNotEmpty()
                cafeAdapter.setCardList(it)
        })

        mMainViewModel.sortType.observe(viewLifecycleOwner,
            Observer<String>{
                mMainViewModel.setCafeViaSortType(it,requireContext())
        })

        // setting button
        binding.settingBtn.setOnClickListener(this)
        binding.sortBtn.setOnClickListener(this)

        //check network
        if(!Utils.isNetworkAvailable(requireContext()))
            showNetworkConnectDialog()

        //check permission
        if(!isPermissionGranted()){
            Timber.d("need to request")
            permissionRequest()
        }

        //get Cafe info when network available && permission granted
        if(Utils.isNetworkAvailable(requireContext()) && isPermissionGranted()) {
            requestCafe()
        }
        initMap()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //viewmodel setup
        mMainViewModel = activity?.run {
            ViewModelProviders.of(this, mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner=this

    }

    private fun initMap(){
        activity?.run {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, MapFragment.getInstance(), MapFragment.TAG)
                .commit()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            RESULT_PERMISSION_LOCATION->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    requestCafe()

                    requireActivity().supportFragmentManager
                        .findFragmentByTag(MapFragment.TAG)
                        ?.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
                if (Utils.isNetworkAvailable(requireContext())) {
                    if (isPermissionGranted()) {
                        requestCafe()
                    } else {
                        permissionRequest()
                    }
                }else{
                    showNetworkConnectDialog()
                }
            }
            RESULT_PERMISSION_LOCATION->{
                if(isPermissionGranted()){
                    requestCafe()
                    //TODO propagation permission granted event to MapFragment
                }else{
                    permissionRequest()
                }
            }

            RESULT_MANUAL_ENABLE->{
                if(isPermissionGranted()){
                    requestCafe()
                    //TODO propagation permission granted event to MapFragment
                }else{
                    showManualPermissionSettingsDialog()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //return false for event propagation
    fun onBackPressed():Boolean {
//        if(viewpager.currentItem == 0) {
//            when(behavior.state){
//                AnchorBottomSheetBehavior.STATE_ANCHORED -> {
//                    behavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
//                    return true
//                }
//                AnchorBottomSheetBehavior.STATE_COLLAPSED -> {
//                    viewpager.currentItem = -1
//                    return false
//                }
//            }
//        }
//        else if(viewpager.currentItem == 1) {
//            if(mMainViewModel.lastMove.isClickList) {
//                viewpager.currentItem = viewpager.currentItem - 1
//            }
//            else if (mMainViewModel.lastMove.isClickMap){
//                when(behavior.state){
//                    AnchorBottomSheetBehavior.STATE_ANCHORED ->
//                        behavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
//                    AnchorBottomSheetBehavior.STATE_COLLAPSED -> {
//                        viewpager.currentItem = viewpager.currentItem - 1
//                        behavior.state = AnchorBottomSheetBehavior.STATE_ANCHORED
//                    }
//                }
//            }
//            return true
//        }
        return false
    }

    private fun showNetworkConnectDialog(){
        AlertDialog.Builder(requireContext())
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
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.dialog_permission_require_message)
            .setPositiveButton("Permission setting"
            ) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivityForResult(intent, RESULT_MANUAL_ENABLE)

            }
            .setNegativeButton("Finish"
            ) { _, _ ->requireActivity().finish()}
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onStart() {
        super.onStart()
        //get Cafe info when network available && permission granted
        if(compositeDisposable.size() == 0 && (Utils.isNetworkAvailable(requireContext()) && isPermissionGranted())) {
            requestCafe(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if(compositeDisposable.size() != 0)
            compositeDisposable.clear()
    }

    private fun requestCafe() = requestCafe(false)

    @SuppressLint("CheckResult")
    private fun requestCafe(force:Boolean){
        compositeDisposable.add(
            queryCafeList(force)
        )
    }

    @SuppressLint("ResourceType")
    private fun queryCafeList(force:Boolean):Disposable{
        return mMainViewModel.getCafeList(requireContext(),force)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                //play animation first time
                if(mMainViewModel.cafeListAll.value == null) {
                    val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.translate)
                    anim.interpolator = OvershootInterpolator()
                    viewpager.startAnimation(anim)
                }
                mMainViewModel.initialLocalCafeData(it, requireContext())

            },{error-> Timber.e(error)})
    }

    private fun isPermissionGranted():Boolean{
        val permissionMissingList = arrayListOf<String>()

        for(p in Utils.needPermissions){
            if(ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED)
                permissionMissingList.add(p)
        }

//        Timber.d("isPermissionGranted:${permissionMissingList.size == 0}")

        return permissionMissingList.size == 0
    }

    private fun permissionRequest(){
        requestPermissions(Utils.needPermissions, RESULT_PERMISSION_LOCATION)
    }

    private fun showFilterDialog(){
        requireActivity().supportFragmentManager?.let{
            SortDialogFragment().show(it,"")
        }
    }

    override fun onClick(v: View) {
        when(v){
            setting_btn ->{
                (requireActivity() as? MainActivity)?.switchToSettingPreference()
            }
            sort_btn->{
                showFilterDialog()
            }
        }
    }

    private val mPageChangeListener = object :ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {}

        //position是你當前選中的頁面的Position（位置編號）(從A滑動到B，就是B的position)
        override fun onPageSelected(position: Int) {
            this@MainFragment.onPageSelected(position)
        }
    }

    fun onPageSelected(position: Int){
        mMainViewModel.chosenCafe.value = cafeAdapter.cafeListCurrent[position].copy()
    }
}

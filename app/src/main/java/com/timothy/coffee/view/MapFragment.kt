package com.timothy.coffee.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.timothy.coffee.MainFragment
import com.timothy.coffee.R
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.databinding.FragmentMapBinding
import com.timothy.coffee.util.Utils
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_map.*
import timber.log.Timber
import java.util.stream.IntStream
import javax.inject.Inject


class MapFragment : Fragment(),OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraIdleListener,
    View.OnClickListener
{

    @Inject
    lateinit var mViewModelFactory:ViewModelFactory
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mMap:GoogleMap
    private lateinit var binding: FragmentMapBinding
    private val Z_INDEX_NORMAL = 0f
    private val Z_INDEX_CURRENT = 2f

    private val markerList = mutableListOf<Marker>()

    companion object {
        @JvmStatic
        private lateinit var INSTANCE:MapFragment
        fun getInstance():MapFragment{
            if(!::INSTANCE.isInitialized) INSTANCE = MapFragment()
            return INSTANCE
        }
        val TAG = "MapFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel = activity?.run{
            ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner=this

        mapView?.run {
            onCreate(savedInstanceState)
            onResume()
            getMapAsync(this@MapFragment)
        }

        binding.researchThisAreaBtn.setOnClickListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
//        Timber.d("onMapReady")
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        enableMyLocation()

        mMainViewModel.screenCenterLoc.observe(viewLifecycleOwner,
            Observer<LatLng>{
                moveCamera()
                mMainViewModel.screenCenterLoc.removeObservers(viewLifecycleOwner)
            })

        mMainViewModel.cafeListDisplay.observe(viewLifecycleOwner,
            Observer<List<CafenomadDisplay>> { cafes ->
                mMap.let{
                    //remove all markers first
                    mMap.clear()
                    addMarkers(cafes)

                    //only move camera when scrolling too far from group of marker
//                    if(!isThereMarkerInMap())
                        moveCameraToShowAllMarkers()
                }
            })

        mMainViewModel.chosenCafe.observe(viewLifecycleOwner,
            Observer<CafenomadDisplay> {
//                moveCameraTo(it)
                updateMarkersIcon()
            })

        mMap.setOnCameraIdleListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        mMainViewModel.chosenCafe.value = marker.tag as CafenomadDisplay
        mMainViewModel.lastMove.isClickList = false
        mMainViewModel.lastMove.isClickMap = true

        //return true for not moving camera after marker clicking
        return true
    }

    private fun updateMarkersIcon(){
        IntStream.range(0,markerList.size).forEach {index ->
            val item = markerList[index]
            val cafeIndexDisplay = 1+index

            when {
                (item.tag as CafenomadDisplay).cafenomad.id == mMainViewModel.chosenCafe.value?.cafenomad?.id ->
                    item.apply {
                        setIcon(getBitmapMapPin(cafeIndexDisplay, R.drawable.ic_location_pin_selected))
                        zIndex = Z_INDEX_CURRENT
                    }
                (item.tag as CafenomadDisplay).isFavorite ->
                    item.apply {
                        setIcon(getBitmapMapPin(cafeIndexDisplay, R.drawable.ic_location_pin_favorite))
                        zIndex = Z_INDEX_NORMAL
                    }
                else ->
                    item.apply {
                        setIcon(getBitmapMapPin(cafeIndexDisplay, R.drawable.ic_location_pin))
                        zIndex = Z_INDEX_NORMAL
                    }
            }
        }
    }

    private fun addMarkers(cafes:List<CafenomadDisplay>){
        markerList.clear()

        IntStream.range(0,cafes.size).forEach { cafeIndex ->
            val cafe = cafes[cafeIndex]
            val cafeIndexDisplay = 1+cafeIndex
            mMap.run {
                val marker = addMarker(MarkerOptions()
                    .position(LatLng(cafe.cafenomad.latitude.toDouble(),cafe.cafenomad.longitude.toDouble()))
                    .icon(
                        when {
                            cafe.cafenomad.id == mMainViewModel.chosenCafe.value?.cafenomad?.id ->
                                getBitmapMapPin(cafeIndexDisplay, R.drawable.ic_location_pin_selected)
                            cafe.isFavorite ->
                                getBitmapMapPin(cafeIndexDisplay, R.drawable.ic_location_pin_favorite)
                            else ->
                                getBitmapMapPin(cafeIndexDisplay, R.drawable.ic_location_pin)
                        }
                    )
                    .title(cafe.cafenomad.name)
                    .draggable(false)
                    .zIndex(
                        when {
                            cafe.cafenomad.id == mMainViewModel.chosenCafe.value?.cafenomad?.id -> Z_INDEX_CURRENT
                            else -> Z_INDEX_NORMAL
                        }
                    )
                )

                marker.tag = cafe
                markerList.add(marker)
            }
        }
    }

    private fun getBitmapMapPin(number:Int, resId:Int): BitmapDescriptor?{
        val drawable = ContextCompat.getDrawable(requireContext(),resId)
        return drawable?.run{
            setBounds(0,0,intrinsicWidth,intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth,intrinsicHeight,Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            val mPaint = Paint()
            draw(canvas)

            val text = number.toString()
            mPaint.apply {
                strokeWidth = 3f
                textSize = 40f
                color = resources.getColor(R.color.white,null)
            }
            val bounds = Rect()
            mPaint.getTextBounds(text, 0, text.length, bounds)
            val fontMetrics: Paint.FontMetricsInt = mPaint.fontMetricsInt
            val baseline: Int =
                (bitmap.height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top
            canvas.drawText(text,(bitmap.width/2).toFloat() - bounds.width() / 2 - 4, (baseline - 5).toFloat(), mPaint);
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun moveCamera(){
        mMainViewModel.screenCenterLoc.value?.let {
            val cameraPosition = CameraPosition.builder()
                .target(LatLng(it.latitude,it.longitude))
                .zoom(
                    kotlin.run {
                    val outValue = TypedValue()
                    resources.getValue(R.dimen.google_map_zoom_level,outValue,true)
                        outValue.float
                })
                .build()

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun moveCameraTo(cafe:CafenomadDisplay) {
        moveCameraTo(LatLng(cafe.cafenomad.latitude.toDouble(),cafe.cafenomad.longitude.toDouble()))
    }

    private fun moveCameraTo(latlng:LatLng){
        mMap?.run{
            val cameraPosition = CameraPosition.builder()
                .target(latlng)
                .zoom(
                    kotlin.run {
                        val outValue = TypedValue()
                        resources.getValue(R.dimen.google_map_zoom_level,outValue,true)
                        outValue.float
                    })
                .build()

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    //extend function of LatLngBounds.Builder
    //to create a bound for all markers.
    private fun LatLngBounds.Builder.createBoundsForAllMarkers(markers: MutableList<Marker>):LatLngBounds? {
        if(markers.isEmpty()) return null

        markers.stream().forEach {
            include(it.position)
        }

        return build()
    }

    private fun moveCameraToShowAllMarkers(){
        val builder = LatLngBounds.Builder()
        val bounds = builder.createBoundsForAllMarkers(markerList) ?: return  //Updated bounds.

//        Timber.d("bounds:$bounds")
//
//        markerList.stream().forEach {
//            (it.tag as CafenomadDisplay).run{
//                Timber.d("name:${this.cafenomad.name}/lat:${this.cafenomad.latitude}/lng:${this.cafenomad.longitude}")
//            }
//        }

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels -
                resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)*2
        val padding = (width * 0.1).toInt()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

        mMap.animateCamera(cu, object :GoogleMap.CancelableCallback{
            //Invoked if the animation goes to completion without interruption.
            override fun onFinish() {
                mMap.cameraPosition.target?.let {
                    if(it != mMainViewModel.screenCenterLoc.value) {
                        mMainViewModel.screenCenterLoc.value = it
                        mMainViewModel.isReSearchable.value = false
                    }
                }
            }

            //Invoked if the animation is interrupted by calling stopAnimation() or starting a new camera movement.
            override fun onCancel() {
                mMap.cameraPosition.target?.let {
                    if(it != mMainViewModel.screenCenterLoc.value) {
                        mMainViewModel.screenCenterLoc.value = it
                        mMainViewModel.isReSearchable.value = false
                    }
                }
            }
        })
    }

    private fun enableMyLocation(){
        activity?.let {
            //permission granted
            if(ContextCompat.checkSelfPermission(it, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            }
        }
    }

    private fun isThereMarkerInMap():Boolean{
        val currentViewBound = mMap.projection.visibleRegion.latLngBounds
        return markerList.stream().anyMatch{ marker -> currentViewBound.contains(marker.position)}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            MainFragment.RESULT_PERMISSION_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
        }
    }

    override fun onCameraIdle() {
        val currentCenterCoordinate = mMap.cameraPosition.target

        mMainViewModel.screenCenterLoc.value?.let {
            if(Utils.distance(currentCenterCoordinate.latitude,it.latitude
                    ,currentCenterCoordinate.longitude,it.longitude) >1000){
                mMainViewModel.isReSearchable.value = true
            }
        }

//        Timber.d("The camera has stopped moving.")
    }

    @SuppressLint("CheckResult")
    override fun onClick(v: View?) {
        mMainViewModel.isReSearchable.value = false
        mMainViewModel.screenCenterLoc.value = mMap.cameraPosition.target
        mMainViewModel.isLoading.value = true

        mMainViewModel.screenCenterLoc.value?.let { latlon ->
            mMainViewModel.getCafeListFromLocation(requireContext(),latlon,false)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({
                    //update cafe list
                    mMainViewModel.initialLocalCafeData(it, requireContext())

                    mMainViewModel.isLoading.postValue(false)
                },{error->
                    Timber.e("ReFetch data error: $error")
                    mMainViewModel.isLoading.postValue(false)
                })
        }
    }
}

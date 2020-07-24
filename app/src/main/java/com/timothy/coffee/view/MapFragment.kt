package com.timothy.coffee.view

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_map.*
import timber.log.Timber
import javax.inject.Inject


class MapFragment : Fragment(),OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    @Inject
    lateinit var mViewModelFactory:ViewModelFactory
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mMap:GoogleMap
    private val isMapInitialed:Boolean
        get() = mMap!=null

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.run {
            onCreate(savedInstanceState)
            onResume()
            getMapAsync(this@MapFragment)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
//        Timber.d("onMapReady")
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        enableMyLocation()

        mMainViewModel.loc.observe(viewLifecycleOwner,
            Observer<LonAndLat>{
                moveCamera()
                mMainViewModel.loc.removeObservers(viewLifecycleOwner)
            })

        mMainViewModel.cafeList.observe(viewLifecycleOwner,
            Observer<List<CafenomadDisplay>> { cafes ->
                mMap?.let{
                    //remove all markers first
                    mMap.clear()
                    addMarkers(cafes)
                }
            })

        mMainViewModel.chosenCafe.observe(viewLifecycleOwner,
            Observer<CafenomadDisplay> {
                moveCameraTo(it)
                updateMarkersIcon()
            })
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        mMainViewModel.chosenCafe.value = marker.tag as CafenomadDisplay
        mMainViewModel.lastMove.isClickList = false
        mMainViewModel.lastMove.isClickMap = true

        return false
    }

    private fun updateMarkersIcon(){
        markerList.stream().forEach {
            when {
                (it.tag as CafenomadDisplay).cafenomad.id == mMainViewModel.chosenCafe.value?.cafenomad?.id ->
                    it.setIcon(getBitmapMapPin(R.drawable.ic_place_selected))
                (it.tag as CafenomadDisplay).isFavorite ->
                    it.setIcon(getBitmapMapPin(R.drawable.ic_place_favorite))
                else ->
                    it.setIcon(getBitmapMapPin(R.drawable.ic_place))
            }
        }
    }

    private fun addMarkers(cafes:List<CafenomadDisplay>){
        markerList.clear()

        cafes.stream().forEach { cafe ->run{
            mMap?.run {
                val marker = addMarker(MarkerOptions()
                    .position(LatLng(cafe.cafenomad.latitude.toDouble(),cafe.cafenomad.longitude.toDouble()))
                    .icon(
                        when {
                            cafe.cafenomad.id == mMainViewModel.chosenCafe.value?.cafenomad?.id ->
                                getBitmapMapPin(R.drawable.ic_place_selected)
                            cafe.isFavorite ->
                                getBitmapMapPin(R.drawable.ic_place_favorite)
                            else ->
                                getBitmapMapPin(R.drawable.ic_place)
                        }
                    )
                    .title(cafe.cafenomad.name)
                    .draggable(false)
                )

                marker.tag = cafe
                markerList.add(marker)
            }
        }}
    }

    private fun getBitmapMapPin(resId:Int): BitmapDescriptor?{
        val drawable = ContextCompat.getDrawable(requireContext(),resId)
        return drawable?.run{
            setBounds(0,0,intrinsicWidth,intrinsicHeight)

            val bitmap = Bitmap.createBitmap(intrinsicWidth,intrinsicHeight,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            draw(canvas)

            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun moveCamera(){
        mMainViewModel.loc.value?.let {
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
}

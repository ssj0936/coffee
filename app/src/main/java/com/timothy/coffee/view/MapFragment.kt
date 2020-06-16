package com.timothy.coffee.view

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.timothy.coffee.R
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_map.*
import javax.inject.Inject


class MapFragment : Fragment(),OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    @Inject
    lateinit var mViewModelFactory:ViewModelFactory
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mMap:GoogleMap
    private val isMapInitialed:Boolean
        get() = mMap!=null

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
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        enableMyLocation()

        mMainViewModel.loc.observe(this,
            Observer<LonAndLat>{
                moveCamera()
            })

        mMainViewModel.cafeList.observe(this,
            Observer<List<Cafenomad>> { cafes ->
                mMap?.let{

                    //remove all markers first
                    mMap.clear()
                    addMarkers(cafes)
                }
            })
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        mMainViewModel.chosenCafe.value = marker.tag as Cafenomad
        return false
    }

    private fun addMarker(cafe:Cafenomad){
        mMap?.run {
            addMarker(MarkerOptions()
                .position(LatLng(cafe.latitude.toDouble(),cafe.longitude.toDouble()))
                .title(cafe.name)
                .draggable(false)
            ).tag = cafe
        }
    }

    private fun addMarkers(cafes:List<Cafenomad>){
        cafes.stream().forEach { cafe -> addMarker(cafe)}
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

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
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


}

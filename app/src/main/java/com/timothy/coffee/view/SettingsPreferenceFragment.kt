package com.timothy.coffee.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.timothy.coffee.R
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class SettingsPreferenceFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object{
        @JvmStatic
        val TAG = "SettingsPreferenceFragment"
        private lateinit var INSTANCE:SettingsPreferenceFragment
        fun getInstance():SettingsPreferenceFragment{
            if(!::INSTANCE.isInitialized){
                INSTANCE = SettingsPreferenceFragment()
            }
            return INSTANCE
        }
    }
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mRefetchButton: Preference

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //viewmodel setup
        mMainViewModel = activity?.run {
            ViewModelProvider(this, mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    //in order to
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState) as LinearLayout

        val btn = LinearLayout.inflate(requireContext(),R.layout.about_section_preference_layout,null)
        btn.setOnClickListener {
            requireActivity().supportFragmentManager.let {
                AboutFragment.getInstance().show(it,AboutFragment.TAG)
            }
        }

        v.addView(btn)

        return v
    }
    @SuppressLint("ShowToast")
    private val refetchCallback = object :MainViewModel.RefetchCallback{

        override fun onRefetchSuccess() {
            Snackbar.make(
                requireView(),
                R.string.snackbar_refetch_done,
                Snackbar.LENGTH_LONG
            ).setAction("Action", null).show()
        }

        override fun onRefetchFail() {
            Snackbar.make(
                requireView(),
                R.string.snackbar_refetch_fail,
                Snackbar.LENGTH_LONG
            ).setAction("Action", null).show()
        }
    }
    @SuppressLint("ResourceType")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference,rootKey)

        mRefetchButton = findPreference(getString(R.string.preference_key_refetch_data))!!
        mRefetchButton.setOnPreferenceClickListener {
            mMainViewModel.onRefetchData(refetchCallback)
            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key){
            getString(R.string.preference_key_max_cafe_return_number)->{
                mMainViewModel.onDisplayNumberChange()
            }
        }
    }
}
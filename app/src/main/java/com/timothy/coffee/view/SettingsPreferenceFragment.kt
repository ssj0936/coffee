package com.timothy.coffee.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.timothy.coffee.R
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
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
            ViewModelProviders.of(this, mViewModelFactory).get(MainViewModel::class.java)
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

    @SuppressLint("ResourceType")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference,rootKey)

//        mRangeSeekbar = this.findPreference(getString(R.string.preference_key_search_range))!!

        mRefetchButton = findPreference(getString(R.string.preference_key_refetch_data))!!
        mRefetchButton.setOnPreferenceClickListener {
            if(mMainViewModel.isLoading.value == true) return@setOnPreferenceClickListener true

            mMainViewModel.isLoading.value = true

            mMainViewModel.refetchCafeData(requireContext())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({
                    Snackbar.make(
                        requireView(),
                        R.string.snackbar_refetch_done,
                        Snackbar.LENGTH_LONG
                    ).setAction("Action", null).show()

                    //update cafe list
                    mMainViewModel.initialLocalCafeData(it, requireContext())

                    mMainViewModel.isLoading.postValue(false)
                },{error->
                    Timber.e("ReFetch data error: $error")
                    Snackbar.make(
                        requireView(),
                        R.string.snackbar_refetch_fail,
                        Snackbar.LENGTH_LONG
                    ).setAction("Action", null).show()
                    mMainViewModel.isLoading.postValue(false)
                })

                true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key){
            getString(R.string.preference_key_max_cafe_return_number)->{
                mMainViewModel.updateDisplayCafeData(requireContext())
            }
        }
    }

}
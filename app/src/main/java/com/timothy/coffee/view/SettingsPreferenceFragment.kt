package com.timothy.coffee.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.google.android.material.snackbar.Snackbar
import com.timothy.coffee.R
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import java.util.stream.Collectors
import javax.inject.Inject


class SettingsPreferenceFragment: PreferenceFragmentCompat() {
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

    private lateinit var mRangeSeekbar: SeekBarPreference
    private lateinit var mRefetchButton: Preference
    private var isRefetching = false;

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


    @SuppressLint("ResourceType")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference,rootKey)

        mRangeSeekbar = this.findPreference(getString(R.string.preference_key_search_range))!!

        mRefetchButton = findPreference(getString(R.string.preference_key_refetch_data))!!
        mRefetchButton.setOnPreferenceClickListener {
            if(mMainViewModel.isDataFetching) return@setOnPreferenceClickListener true

            mMainViewModel.isDataFetching = true

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
                    mMainViewModel.updateLocalCafeData(it, requireContext())

                    mMainViewModel.isDataFetching = false
                },{error->
                    Timber.e(error)
                    Snackbar.make(
                        requireView(),
                        R.string.snackbar_refetch_fail,
                        Snackbar.LENGTH_LONG
                    ).setAction("Action", null).show()

                    mMainViewModel.isDataFetching = false
                })

                true
        }
    }

}
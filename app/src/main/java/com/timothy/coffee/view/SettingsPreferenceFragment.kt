package com.timothy.coffee.view

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.timothy.coffee.R
import timber.log.Timber


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

    lateinit var mRangeSeekbar: SeekBarPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference,rootKey)

        mRangeSeekbar = this.findPreference(getString(R.string.preference_key_search_range))!!
    }

}
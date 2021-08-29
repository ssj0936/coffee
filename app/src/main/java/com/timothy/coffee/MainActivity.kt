package com.timothy.coffee

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.timothy.coffee.util.Utils.Companion.resetFilter
import com.timothy.coffee.view.SettingsPreferenceFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class MainActivity: AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector:DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
    private var currentTopFragmentTag = ""

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //reset filter
        resetFilter(this)

        switchToMainFragment()

        supportActionBar?.setShowHideAnimationEnabled(true)
    }

    private fun switchFragment(tag:String){
        //show action bar only for setting preference
        supportActionBar?.let {
            if(tag == SettingsPreferenceFragment.TAG)
                it.show()
            else
                it.hide()
        }

        //switch fragment
        supportFragmentManager.beginTransaction().apply{

            setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)

            //hide current fragment
            getTopVisibleFragment()?.run {
                hide(this)
            }

            supportFragmentManager.findFragmentByTag(tag).also{
                if(it != null)
                    show(it)
                else{
                    add(R.id.container,
                        when(tag){
                            MainFragment.TAG -> MainFragment()
                            SettingsPreferenceFragment.TAG -> SettingsPreferenceFragment()
                            else-> throw IllegalArgumentException("no Fragment with tag:$tag exist.")
                        },
                        tag
                    )
                }
            }
            currentTopFragmentTag = tag
            commit()
        }
    }

    fun switchToSettingPreference(){
        switchFragment(SettingsPreferenceFragment.TAG)
    }

    private fun switchToMainFragment(){
        switchFragment(MainFragment.TAG)
    }

    private fun getTopVisibleFragment():Fragment?{
        return supportFragmentManager.findFragmentByTag(currentTopFragmentTag)
    }

    override fun onBackPressed() {
        when(val currentVisibleFragment = getTopVisibleFragment()){
            is MainFragment->{
                if(!currentVisibleFragment.onBackPressed()) super.onBackPressed()
            }

            is SettingsPreferenceFragment->{
                switchFragment(MainFragment.TAG)
            }

            else -> super.onBackPressed()
        }
    }
}
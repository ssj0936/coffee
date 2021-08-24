package com.timothy.coffee

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.timothy.coffee.util.Utils.Companion.resetFilter
import com.timothy.coffee.view.FilterDialogFragment
import com.timothy.coffee.view.SettingsPreferenceFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MainActivity: AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector:DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
    private val mFragmentList:List<Fragment> = listOf(
        MainFragment.getInstance(),
        SettingsPreferenceFragment.getInstance()
    )

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

    private fun switchFragment(fragment:Fragment, tag:String){
        //show action bar only for setting preference
        supportActionBar?.let {
            if(fragment is SettingsPreferenceFragment)
                it.show()
            else
                it.hide()
        }

        //switch fragment
        supportFragmentManager.beginTransaction().run{

            setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)

            //hide current fragment
            getTopVisibleFragment()?.let{
                hide(it)
            }

            //add or show new fragment
            if(fragment.isAdded) {
                show(fragment)
            }else {
                add(R.id.container, fragment, tag)
            }
            commit()
        }
    }

    fun switchToSettingPreference(){
        switchFragment(SettingsPreferenceFragment.getInstance(),SettingsPreferenceFragment.TAG)
    }

    private fun switchToMainFragment(){
        switchFragment(MainFragment.getInstance(), MainFragment.TAG)
    }

    private fun getTopVisibleFragment():Fragment?{
        return mFragmentList.stream().filter { it.isVisible }.findAny().orElse(null)
    }

    override fun onBackPressed() {
        val currentVisibleFragment = getTopVisibleFragment()

        currentVisibleFragment?.let{
            when(it){
                is MainFragment->{
                    if(!it.onBackPressed()) super.onBackPressed()
                }

                is SettingsPreferenceFragment->{
                    switchFragment(MainFragment.getInstance(), MainFragment.TAG)
                }

                else -> super.onBackPressed()
            }
        }
    }
}
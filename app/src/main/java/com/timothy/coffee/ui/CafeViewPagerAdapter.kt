package com.timothy.coffee.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.MutableLiveData
import com.timothy.coffee.view.CafeInfoFragment
import com.timothy.coffee.view.CafeListFragment

class CafeViewPagerAdapter constructor(
    fm: FragmentManager
): FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var isInfoPageHide: MutableLiveData<Boolean> = MutableLiveData(true)

    val list = listOf(CafeListFragment.getInstance(),CafeInfoFragment.getInstance())

    override fun getItem(position: Int): Fragment = list[position]
    override fun getCount(): Int = if(isInfoPageHide.value!!) list.size-1 else list.size

    fun setHideInfoPage(isHide:Boolean){
        isInfoPageHide.value = isHide
//        notifyItemRangeChanged(1,1)
        notifyDataSetChanged()
    }
}
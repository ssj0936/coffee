package com.timothy.coffee.ui

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.view.CafeInfoV2Fragment
import com.timothy.coffee.view.CafeInfoV2NoDataFragment

@SuppressLint("WrongConstant")
class CafeViewPagerAdapterV2 constructor(
    fm: FragmentManager
): FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragmentList = mutableListOf<Fragment>()
    var cafeListCurrent = listOf<CafenomadDisplay>()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int = fragmentList.size

    fun setCardList(list:List<CafenomadDisplay>){
        fragmentList.clear()

        //different fragments be added to adapter when list is empty or not
        if(list.isEmpty()){
            fragmentList.add(CafeInfoV2NoDataFragment.getInstance())
        }else {
            cafeListCurrent = list.map { item -> item.copy() }
            for ((index, cafe) in cafeListCurrent.withIndex()) {
                fragmentList.add(CafeInfoV2Fragment.newInstance(index))
            }
        }

        notifyDataSetChanged()
    }

    //ref:
    // https://www.jianshu.com/p/266861496508

    //Called when the host view is attempting to determine if an item's position has changed.
    //Returns POSITION_UNCHANGED if the position of the given item has not changed
    //or POSITION_NONE if the item is no longer present in the adapter.
    // The default implementation assumes that items will never change position and always returns POSITION_UNCHANGED.

    override fun getItemPosition(fragment: Any): Int {
        return POSITION_NONE
    }

}
package com.timothy.coffee.ui

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.timothy.coffee.R
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.view.CafeInfoV2Fragment

class CafeViewPagerAdapterV2 constructor(
    fm: FragmentManager
): FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var count = 0

    override fun getItem(position: Int): Fragment {
        return if(count >0) CafeInfoV2Fragment.newInstance(position) else Fragment(R.layout.fragment_cafe_info_v2_nodata)
    }

    override fun getCount(): Int = if(count==0) 1 else count

    fun setCardList(list:List<CafenomadDisplay>){
        count = list.size
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

    override fun saveState(): Parcelable? {
        return null
    }
}
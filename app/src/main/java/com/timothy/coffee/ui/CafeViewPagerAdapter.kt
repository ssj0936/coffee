package com.timothy.coffee.ui

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.MutableLiveData
import com.timothy.coffee.view.CafeBaseFragment
import com.timothy.coffee.view.CafeInfoFragment
import com.timothy.coffee.view.CafeListFragment
import timber.log.Timber

class CafeViewPagerAdapter constructor(
    fm: FragmentManager
): FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var isInfoPageHide: MutableLiveData<Boolean> = MutableLiveData(true)

    val list:List<Fragment> = listOf(CafeListFragment.getInstance(),CafeInfoFragment.getInstance())

    override fun getItem(position: Int): Fragment {
        return list[position]
    }
    override fun getCount(): Int = if(isInfoPageHide.value!!) list.size-1 else list.size

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)

        obj?.let {
            for(index in list.indices){
                val shouldNestedScroll = (position==index)
                val fragment = list[position]
                if(fragment is CafeBaseFragment){
                    fragment.setNestScrollingEnable(shouldNestedScroll)
                }
            }
            container.requestLayout()
        }

    }

    fun setHideInfoPage(isHide:Boolean){
        isInfoPageHide.value = isHide
        notifyDataSetChanged()
    }
}
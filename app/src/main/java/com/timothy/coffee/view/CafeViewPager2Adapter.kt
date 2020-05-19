package com.timothy.coffee.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.timothy.coffee.viewmodel.MainViewModel

class CafeViewPager2Adapter(
    fragmentActivity:FragmentActivity,
    private val mPageNum:Int,
    private val mMainViewModel: MainViewModel
): FragmentStateAdapter(fragmentActivity) {
    var isInfoPageHide:MutableLiveData<Boolean> = MutableLiveData(true)

    override fun getItemCount(): Int = if(isInfoPageHide.value!!) mPageNum-1 else mPageNum

    override fun createFragment(position: Int): Fragment =
        when(position){
            0 -> CafeListFragment.getInstance()
            else -> CafeInfoFragment.getInstance()
        }

    fun setHideInfoPage(isHide:Boolean){
        isInfoPageHide.value = isHide
        notifyItemRangeChanged(1,1)
        notifyDataSetChanged()
    }
}
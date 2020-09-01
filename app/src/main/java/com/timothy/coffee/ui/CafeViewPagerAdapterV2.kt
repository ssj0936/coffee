package com.timothy.coffee.ui

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.view.CafeInfoV2Fragment
import com.timothy.coffee.view.CafeInfoV2Fragment.Companion.ARGUMENT_KEY
import timber.log.Timber

@SuppressLint("WrongConstant")
class CafeViewPagerAdapterV2 constructor(
    fm: FragmentManager
): FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var fragmentList = mutableListOf<CafeInfoV2Fragment>()
    var cafeListCurrent = listOf<CafenomadDisplay>()
    var cafeListLast = listOf<CafenomadDisplay>()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int = fragmentList.size

//    fun addCardFragment(fragment: CafeInfoV2Fragment) {
//        fragmentList.add(fragment)
//    }

//    fun setCardFragment(list:List<CafeInfoV2Fragment>){
//        fragmentList = list
//    }

    fun setCardList(list:List<CafenomadDisplay>){
        cafeListCurrent = list.map { item -> item.copy() }
        fragmentList.clear()

        for((index, cafe) in cafeListCurrent.withIndex()){
//            Timber.d("$index:${cafe.cafenomad.name} / ${cafe.isFavorite}")
            fragmentList.add(CafeInfoV2Fragment.newInstance(index))
        }
//        cafeList.forEach {
//            fragmentList.add(CafeInfoV2Fragment.newInstance(it))
//        }

        notifyDataSetChanged()
    }

//    override fun getItemPosition(fragment: Any): Int {
//        val index = (fragment as CafeInfoV2Fragment).arguments?.getInt(ARGUMENT_KEY)
//
//
//        return POSITION_NONE
//    }

    //    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
//        super.setPrimaryItem(container, position, obj)
//
//        obj?.let {
//            for(index in fragmentList.indices){
//                val shouldNestedScroll = (position==index)
//                val fragment = fragmentList[position]
//                if(fragment is CafeBaseFragment){
//                    fragment.setNestScrollingEnable(shouldNestedScroll)
//                }
//            }
//            container.requestLayout()
//        }
//
//    }
}
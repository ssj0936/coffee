package com.timothy.coffee.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timothy.coffee.R
import timber.log.Timber

class CafeListDecoration(
    mContext: Context,
    orientation:Int
): RecyclerView.ItemDecoration() {

    private val HORIZONTAL_LIST: Int = LinearLayoutManager.HORIZONTAL
    private val VERTICAL_LIST: Int = LinearLayoutManager.VERTICAL

    private var mOrientation:Int
    private var mDivider: Drawable
    private var isDrawFirstDivider: Boolean = true
    private var isDrawLastDivider: Boolean = true

    constructor(mContext: Context,
                orientation:Int,
                isDrawFirstDivider:Boolean,
                isDrawLastDivider:Boolean):this(mContext,orientation){
        this.isDrawFirstDivider = isDrawFirstDivider
        this.isDrawLastDivider = isDrawLastDivider
    }

    init {
        mOrientation =
            if(orientation == VERTICAL_LIST || orientation == HORIZONTAL_LIST) orientation
            else throw IllegalArgumentException("invalid orientation")

        mDivider = ContextCompat.getDrawable(mContext, R.drawable.divider)
            ?: throw IllegalArgumentException("error divider resource")
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        when(mOrientation){
            //basically unreachable state
            HORIZONTAL_LIST ->{}

            VERTICAL_LIST -> drawHorizontal(c,parent)

        }
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView){
        val left = parent.paddingLeft
        val right = parent.run { width - paddingRight }
        val childCount = parent.childCount
//        Timber.d("childCount:$childCount")
        for(index in (if(isDrawFirstDivider)0 else 1) until childCount){
            val child = parent.getChildAt(index)
            val param = child.layoutParams as RecyclerView.LayoutParams

            val top = child.top - param.topMargin
            val bottom = top + mDivider.intrinsicHeight

            mDivider.setBounds(left,top, right, bottom)
            mDivider.draw(c)
        }

    }

    //畫完分隔線的偏移量
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        when(mOrientation){
            //basically unreachable state
            HORIZONTAL_LIST->{}

            VERTICAL_LIST->{
                outRect.set(0,0,0,mDivider.intrinsicHeight)
//                outRect.bottom = mDivider.intrinsicHeight
            }
        }
    }
}
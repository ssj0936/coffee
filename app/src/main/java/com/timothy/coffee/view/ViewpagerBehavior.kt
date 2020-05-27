package com.timothy.coffee.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout

class ViewpagerBehavior: CoordinatorLayout.Behavior<View> {
    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is TextView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val delta:Float = if(dependency.height+dependency.translationY < 0) 0F else dependency.height+dependency.translationY
        child.y = delta
        return true
    }
}
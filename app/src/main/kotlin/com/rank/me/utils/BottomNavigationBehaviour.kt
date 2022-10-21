package com.rank.me.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomnavigation.BottomNavigationView


class BottomNavigationBehavior(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<BottomNavigationView?>(context, attrs) {
    fun onStartNestedScroll(
        axes: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
         coordinatorLayout: CoordinatorLayout,
         child: BottomNavigationView,
         target: View,
        dx: Int,
        dy: Int,
         consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        child.translationY = 0.0f.coerceAtLeast(child.height.toFloat().coerceAtMost(child.translationY + dy))
    }
}

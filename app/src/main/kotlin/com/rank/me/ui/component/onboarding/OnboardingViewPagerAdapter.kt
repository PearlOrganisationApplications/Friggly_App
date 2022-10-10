package com.rank.me.ui.component.onboarding

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rank.me.R


class OnboardingViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val context: Context
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.title_on_boarding_first_slide),
                context.resources.getString(R.string.description_on_boarding_first_slide)
            )
            1 -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.title_on_boarding_second_slide),
                context.resources.getString(R.string.description_on_boarding_second_slide)
            )
            3 -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.title_on_boarding_third_slide),
                context.resources.getString(R.string.description_on_boarding_third_slide)
            )
            else -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.title_on_boarding_fourth_slide),
                context.resources.getString(R.string.description_on_boarding_fourth_slide)
            )
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}

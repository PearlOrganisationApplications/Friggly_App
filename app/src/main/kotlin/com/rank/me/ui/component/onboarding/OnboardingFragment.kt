package com.rank.me.ui.component.onboarding

import android.os.Bundle
import android.view.View
import com.rank.me.databinding.FragmentOnboardingBinding
import com.rank.me.ui.base.BaseFragment

class OnboardingFragment :
    BaseFragment<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {
    private lateinit var title: String
    private lateinit var description: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title =
                requireArguments().getString(ARG_PARAM1)!!
            description =
                requireArguments().getString(ARG_PARAM2)!!
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textOnboardingTitle.text = title
        binding.textOnboardingDescription.text = description
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(
            title: String,
            description: String,
        ): OnboardingFragment {
            val fragment =
                OnboardingFragment()
            val args = Bundle()
            args.putString(
                ARG_PARAM1,
                title
            )
            args.putString(
                ARG_PARAM2,
                description
            )
            fragment.arguments = args
            return fragment
        }
    }
}

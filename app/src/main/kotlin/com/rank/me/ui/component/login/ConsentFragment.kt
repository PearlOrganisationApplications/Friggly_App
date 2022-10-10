package com.rank.me.ui.component.login

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import com.rank.me.R
import com.rank.me.databinding.FragmentConsentBinding
import com.rank.me.ui.base.BaseFragment
import kotlinx.android.synthetic.main.lyt_consent_fifth.*

class ConsentFragment :
    BaseFragment<FragmentConsentBinding>(FragmentConsentBinding::inflate) {
    private lateinit var title: String
    private lateinit var description: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = requireArguments().getString(ARG_PARAM1)!!
            description = requireArguments().getString(ARG_PARAM2)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.agreeButton.setOnClickListener {
            (activity as LoginActivity).doLogin()
        }
        binding.consentFirst.toggleLyt.setOnClickListener {
            if (binding.consentFirst.hiddenView.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    binding.consentFirst.baseCardview,
                    AutoTransition()
                )
                binding.consentFirst.hiddenView.visibility = View.GONE
                binding.consentFirst.arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_down)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.consentFirst.baseCardview,
                    AutoTransition()
                )
                binding.consentFirst.hiddenView.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_up)
            }
        }
        binding.consentSecond.toggleLyt.setOnClickListener {
            if (binding.consentSecond.hiddenView.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    binding.consentSecond.baseCardview,
                    AutoTransition()
                )
                binding.consentSecond.hiddenView.visibility = View.GONE
                binding.consentSecond.arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_down)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.consentSecond.baseCardview,
                    AutoTransition()
                )
                binding.consentSecond.hiddenView.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_up)
            }
        }
        binding.consentThird.toggleLyt.setOnClickListener {
            if (binding.consentThird.hiddenView.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    binding.consentThird.baseCardview,
                    AutoTransition()
                )
                binding.consentThird.hiddenView.visibility = View.GONE
                binding.consentThird.arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_down)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.consentThird.baseCardview,
                    AutoTransition()
                )
                binding.consentThird.hiddenView.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_up)
            }
        }
        binding.consentFourth.toggleLyt.setOnClickListener {
            if (binding.consentFourth.hiddenView.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    binding.consentFourth.baseCardview,
                    AutoTransition()
                )
                binding.consentFourth.hiddenView.visibility = View.GONE
                binding.consentFourth.arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_down)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.consentFourth.baseCardview,
                    AutoTransition()
                )
                binding.consentFourth.hiddenView.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_up)
            }
        }
        binding.consentFifth.toggleLyt.setOnClickListener {
            if (binding.consentFifth.hiddenView.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    binding.consentFifth.baseCardview,
                    AutoTransition()
                )
                binding.consentFifth.hiddenView.visibility = View.GONE
                binding.consentFifth.arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_down)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.consentFifth.baseCardview,
                    AutoTransition()
                )
                binding.consentFifth.hiddenView.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.mtrl_ic_arrow_drop_up)
            }
        }

    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(
        ): ConsentFragment {
            return ConsentFragment()
        }
    }
}

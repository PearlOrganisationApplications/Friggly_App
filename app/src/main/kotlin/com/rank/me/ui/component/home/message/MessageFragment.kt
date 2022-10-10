package com.rank.me.ui.component.home.message

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.rank.me.R
import com.rank.me.databinding.FragmentMsgBinding
import com.rank.me.ui.base.BaseFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_OBJECT = "object"

class MessageFragment : BaseFragment<FragmentMsgBinding>(FragmentMsgBinding::inflate) {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var messageSubAdapter: MessageSubAdapter
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageSubAdapter = MessageSubAdapter(this)
        binding.pager.adapter = messageSubAdapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Inbox"
                    tab.icon = resources.getDrawable(R.drawable.ic_baseline_inbox_24)
                }
                1 -> {
                    tab.text = "Highlight"
                    tab.icon = resources.getDrawable(R.drawable.ic_message_highlight_24)
                }
                3 -> {
                    tab.text = "Promotion"
                    tab.icon = resources.getDrawable(R.drawable.ic_promotion_24)
                }
                else -> {
                    tab.text = "Spam"
                    tab.icon = resources.getDrawable(R.drawable.ic_baseline_block_24)
                }
            }
        }.attach()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment msg.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

package com.rank.me.ui.component.home.message
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rank.me.ui.component.home.message.highlight.MessageHighlighFragment
import com.rank.me.ui.component.home.message.inbox.MessageInboxFragment
import com.rank.me.ui.component.home.message.promotion.MessagePromotionFragment
import com.rank.me.ui.component.home.message.spam.MessageSpamFragment

class MessageSubAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val ARG_OBJECT = "object"
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        // Inbox / highlights / promtions / spam
        var fragment : Fragment = MessageInboxFragment()
        when (position) {
            1 -> {
                fragment = MessageHighlighFragment()
            }
            2 -> {
                fragment = MessagePromotionFragment()
            }
            3 -> {
                fragment = MessageSpamFragment()
            }
        }
        fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT, position)
        }
        return fragment
    }
}

package com.rank.me.ui.component.home.call

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.rank.me.BuildConfig
import com.rank.me.R
import com.rank.me.databinding.FragmentCallBinding
import com.rank.me.dialer.activities.DialpadActivity
import com.rank.me.dialer.activities.SettingsActivity
import com.rank.me.dialer.adapters.ViewPagerAdapter
import com.rank.me.dialer.dialogs.ChangeSortingDialog
import com.rank.me.dialer.extensions.config
import com.rank.me.dialer.extensions.launchCreateNewContactIntent
import com.rank.me.dialer.fragments.FavoritesFragment
import com.rank.me.dialer.fragments.MyViewPagerFragment
import com.rank.me.dialer.helpers.OPEN_DIAL_PAD_AT_LAUNCH
import com.rank.me.dialer.helpers.RecentsHelper
import com.rank.me.dialer.helpers.tabsList
import com.rank.me.message.activities.NewConversationActivity
import com.rank.me.ui.component.home.HomeActivity
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.SimpleContact
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.main_holder
import kotlinx.android.synthetic.main.activity_message_main.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_recents.*
import me.grantland.widget.AutofitHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_OBJECT = "object"

/**
 * A simple [Fragment] subclass.
 * Use the [call.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallFragment : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private var param1: String? = null
    private var param2: String? = null

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val sharedViewModel: CallViewModel by activityViewModels()
    private var binding: FragmentCallBinding? = null

    private var isSearchOpen = false
    private var launchedDialer = false
    private var mSearchMenuItem: MenuItem? = null
    private var storedShowTabs: Int? = 0
    private var searchQuery = ""
    var cachedContacts = ArrayList<SimpleContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        binding?.apply {
            // Specify the fragment as the lifecycle owner
            lifecycleOwner = viewLifecycleOwner

            // Assign the view model to a property in the binding class
            viewModel = sharedViewModel

            // Assign the fragment
            callFragment = this@CallFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentCallBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchedDialer = savedInstanceState?.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH) ?: false
//        setupOptionsMenu()
//        refreshMenuItems()
        if (requireContext().isDefaultDialer()) {
            checkContactPermissions()

            if (!requireContext().config.wasOverlaySnackbarConfirmed && !Settings.canDrawOverlays(requireContext())) {
                val snackbar = Snackbar.make(main_holder, R.string.allow_displaying_over_other_apps, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok) {
                    requireContext().config.wasOverlaySnackbarConfirmed = true
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }

                snackbar.setBackgroundTint(requireContext().getProperBackgroundColor().darkenColor())
                snackbar.setTextColor(requireContext().getProperTextColor())
                snackbar.setActionTextColor(requireContext().getProperTextColor())
                snackbar.show()
            }

            (activity as HomeActivity).handleNotificationPermission { granted ->
                if (!granted) {
                    (activity as HomeActivity).toast(R.string.no_post_notifications_permissions)
                }
            }
        } else {
            (activity as HomeActivity).launchSetDefaultDialerIntent()
        }
        SimpleContact.sorting = requireContext().config.sorting
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
            CallFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun openThread() {
        requireActivity().hideKeyboard()
        val intent = Intent(requireActivity(), NewConversationActivity::class.java)
        startActivity(intent)
    }

    private fun refreshMenuItems() {
        val currentFragment = getCurrentFragment()
        (activity as HomeActivity).main_toolbar.menu.apply {
            findItem(R.id.clear_call_history).isVisible = currentFragment == recents_fragment
            findItem(R.id.sort).isVisible = currentFragment != recents_fragment
            findItem(R.id.create_new_contact).isVisible = currentFragment == contacts_fragment
        }
    }

    private fun setupOptionsMenu() {
        setupSearch((activity as HomeActivity).main_toolbar.menu)
        (activity as HomeActivity).main_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.clear_call_history -> clearCallHistory()
                R.id.create_new_contact -> (activity as HomeActivity).launchCreateNewContactIntent()
                R.id.sort -> showSortingDialog(showCustomSorting = getCurrentFragment() is FavoritesFragment)
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun checkContactPermissions() {
        (activity as HomeActivity).handlePermission(PERMISSION_READ_CONTACTS) {
            initFragments()
        }
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchMenuItem = menu.findItem(R.id.search)
        (mSearchMenuItem!!.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isSubmitButtonEnabled = false
            queryHint = getString(R.string.search)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (isSearchOpen) {
                        searchQuery = newText
                        getCurrentFragment()?.onSearchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                isSearchOpen = true
                main_dialpad_button.beGone()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                if (isSearchOpen) {
                    getCurrentFragment()?.onSearchClosed()
                }

                isSearchOpen = false
                main_dialpad_button.beVisible()
                return true
            }
        })
    }

    private fun clearCallHistory() {
        activity?.let {
            ConfirmationDialog(it, "", R.string.clear_history_confirmation) {
                RecentsHelper(it).removeAllRecentCalls(activity as HomeActivity) {
                    it.runOnUiThread {
                        recents_fragment?.refreshItems()
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun checkShortcuts() {
        val appIconColor = activity?.config?.appIconColor
        if (isNougatMR1Plus() && activity?.config?.lastHandledShortcutColor != appIconColor) {
            val launchDialpad = appIconColor?.let { getLaunchDialpadShortcut(it) }

            try {
                activity?.shortcutManager?.dynamicShortcuts = listOf(launchDialpad)
                if (appIconColor != null) {
                    this.activity?.config?.lastHandledShortcutColor = appIconColor
                }
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getLaunchDialpadShortcut(appIconColor: Int): ShortcutInfo {
        val newEvent = getString(R.string.dialpad)
        val drawable = resources.getDrawable(R.drawable.shortcut_dialpad)
        (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_dialpad_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(context, DialpadActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        return ShortcutInfo.Builder(context, "launch_dialpad")
            .setShortLabel(newEvent)
            .setLongLabel(newEvent)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
    }

    private fun setupTabColors() {
        val activeView = main_tabs_holder.getTabAt(view_pager.currentItem)?.customView
        context?.updateBottomTabItemColors(activeView, true)

        getInactiveTabIndexes(view_pager.currentItem).forEach { index ->
            val inactiveView = main_tabs_holder.getTabAt(index)?.customView
            activity?.updateBottomTabItemColors(inactiveView, false)
        }

        val bottomBarColor = context?.getBottomNavigationBackgroundColor()
        if (bottomBarColor != null) {
            main_tabs_holder.setBackgroundColor(bottomBarColor)
            (activity as HomeActivity).updateNavigationBarColor(bottomBarColor)
        }
    }

    private fun getInactiveTabIndexes(activeIndex: Int) = (0 until tabsList.size).filter { it != activeIndex }

    private fun initFragments() {
        view_pager.offscreenPageLimit = 2
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                main_tabs_holder.getTabAt(position)?.select()
                getAllFragments().forEach {
                    it?.finishActMode()
                }
                refreshMenuItems()
            }
        })

        // selecting the proper tab sometimes glitches, add an extra selector to make sure we have it right
        main_tabs_holder.onGlobalLayout {
            Handler().postDelayed({
                var wantedTab = getDefaultTab()

                // open the Recents tab if we got here by clicking a missed call notification
                if (activity != null) {
                    if (requireActivity().intent.action == Intent.ACTION_VIEW && requireActivity().config.showTabs and TAB_CALL_HISTORY > 0) {
                        wantedTab = main_tabs_holder.tabCount - 1

                        ensureBackgroundThread {
                            clearMissedCalls()
                        }
                    }
                }
                main_tabs_holder.getTabAt(wantedTab)?.select()
                refreshMenuItems()
            }, 100L)
        }

        main_dialpad_button.setOnClickListener {
            launchDialpad()
        }

        view_pager.onGlobalLayout {
            refreshMenuItems()
        }

        if (activity?.config?.openDialPadAtLaunch == true && !launchedDialer) {
            launchDialpad()
            launchedDialer = true
        }
    }

    private fun setupTabs() {
        view_pager.adapter = null
        main_tabs_holder.removeAllTabs()
        tabsList.forEachIndexed { index, value ->
            if ((activity?.config?.showTabs?.and(value) ?: 0) != 0) {
                main_tabs_holder.newTab().setCustomView(R.layout.bottom_tablayout_item).apply {
                    customView?.findViewById<ImageView>(R.id.tab_item_icon)?.setImageDrawable(getTabIcon(index))
                    customView?.findViewById<TextView>(R.id.tab_item_label)?.text = getTabLabel(index)
                    AutofitHelper.create(customView?.findViewById(R.id.tab_item_label))
                    main_tabs_holder.addTab(this)
                }
            }
        }

        main_tabs_holder.onTabSelectionChanged(
            tabUnselectedAction = {
                activity?.updateBottomTabItemColors(it.customView, false)
            },
            tabSelectedAction = {
                closeSearch()
                view_pager.currentItem = it.position
                activity?.updateBottomTabItemColors(it.customView, true)
            }
        )

        main_tabs_holder.beGoneIf(main_tabs_holder.tabCount == 1)
        storedShowTabs = activity?.config?.showTabs
    }

    private fun getTabIcon(position: Int): Drawable {
        val drawableId = when (position) {
            0 -> R.drawable.ic_person_vector
            1 -> R.drawable.ic_star_vector
            else -> R.drawable.ic_clock_vector
        }
        return resources.getColoredDrawableWithColor(drawableId, requireActivity().getProperTextColor())
    }

    private fun getTabLabel(position: Int): String {
        val stringId = when (position) {
            0 -> R.string.contacts_tab
            1 -> R.string.favorites_tab
            else -> R.string.call_history_tab
        }

        return resources.getString(stringId)
    }

    private fun refreshItems(openLastTab: Boolean = false) {
        if (activity?.isDestroyed != false || activity?.isFinishing != false) {
            return
        }

        if (activity != null) {

            if (view_pager.adapter == null) {
                view_pager.adapter = ViewPagerAdapter(activity as HomeActivity)
                view_pager.currentItem = if (openLastTab) requireActivity().config.lastUsedViewPagerPage else getDefaultTab()
                view_pager.onGlobalLayout {
                    refreshFragments()
                }
            } else {
                refreshFragments()
            }
        }
    }

    private fun launchDialpad() {
        Intent(context, DialpadActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun refreshFragments() {
        contacts_fragment?.refreshItems()
        favorites_fragment?.refreshItems()
        recents_fragment?.refreshItems()
    }

    private fun getAllFragments(): ArrayList<MyViewPagerFragment?> {
        val showTabs = activity?.config?.showTabs
        val fragments = arrayListOf<MyViewPagerFragment?>()

        if (showTabs != null) {
            if (showTabs and TAB_CONTACTS > 0) {
                fragments.add(contacts_fragment)
            }

            if (showTabs and TAB_FAVORITES > 0) {
                fragments.add(favorites_fragment)
            }

            if (showTabs and TAB_CALL_HISTORY > 0) {
                fragments.add(recents_fragment)
            }

        }
        return fragments
    }

    private fun getCurrentFragment(): MyViewPagerFragment? = getAllFragments().getOrNull(view_pager.currentItem)

    private fun getDefaultTab(): Int {
        if (activity != null) {
            val showTabsMask = requireActivity().config.showTabs
            return when (requireActivity().config.defaultTab) {
                TAB_LAST_USED -> if (requireActivity().config.lastUsedViewPagerPage < main_tabs_holder.tabCount) requireActivity().config.lastUsedViewPagerPage else 0
                TAB_CONTACTS -> 0
                TAB_FAVORITES -> if (showTabsMask and TAB_CONTACTS > 0) 1 else 0
                else -> {
                    if (showTabsMask and TAB_CALL_HISTORY > 0) {
                        if (showTabsMask and TAB_CONTACTS > 0) {
                            if (showTabsMask and TAB_FAVORITES > 0) {
                                2
                            } else {
                                1
                            }
                        } else {
                            if (showTabsMask and TAB_FAVORITES > 0) {
                                1
                            } else {
                                0
                            }
                        }
                    } else {
                        0
                    }
                }
            }
        } else {
            return 0
        }
    }

    @SuppressLint("MissingPermission")
    private fun clearMissedCalls() {
        try {
            // notification cancellation triggers MissedCallNotifier.clearMissedCalls() which, in turn,
            // should update the database and reset the cached missed call count in MissedCallNotifier.java
            // https://android.googlesource.com/platform/packages/services/Telecomm/+/master/src/com/android/server/telecom/ui/MissedCallNotifierImpl.java#170
            activity?.telecomManager?.cancelMissedCallsNotification()
        } catch (ignored: Exception) {
        }
    }

    private fun launchSettings() {
        closeSearch()
        activity?.hideKeyboard()
        startActivity(Intent(context, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        closeSearch()
        val licenses = LICENSE_GLIDE or LICENSE_INDICATOR_FAST_SCROLL or LICENSE_AUTOFITTEXTVIEW

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title, R.string.faq_1_text),
            FAQItem(R.string.faq_9_title_commons, R.string.faq_9_text_commons)
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
            faqItems.add(FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
        }

        (activity as HomeActivity).startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun showSortingDialog(showCustomSorting: Boolean) {
        ChangeSortingDialog(activity as HomeActivity, showCustomSorting) {
            favorites_fragment?.refreshItems {
                if (isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(searchQuery)
                }
            }

            contacts_fragment?.refreshItems {
                if (isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(searchQuery)
                }
            }
        }
    }

    private fun closeSearch() {
        if (isSearchOpen) {
            getAllFragments().forEach {
                it?.onSearchQueryChanged("")
            }
            mSearchMenuItem?.collapseActionView()
        }
    }

    fun cacheContacts(contacts: List<SimpleContact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (e: Exception) {
        }
    }
}

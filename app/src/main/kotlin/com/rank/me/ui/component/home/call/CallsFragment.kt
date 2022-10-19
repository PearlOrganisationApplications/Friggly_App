package com.rank.me.ui.component.home.call

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.rank.me.BuildConfig
import com.rank.me.R
import com.rank.me.databinding.FragmentCallsBinding
import com.rank.me.dialer.activities.DialpadActivity
import com.rank.me.dialer.activities.SettingsActivity
import com.rank.me.dialer.adapters.RecentCallsAdapter
import com.rank.me.dialer.dialogs.ChangeSortingDialog
import com.rank.me.dialer.helpers.OPEN_DIAL_PAD_AT_LAUNCH
import com.rank.me.dialer.helpers.RecentsHelper
import com.rank.me.dialer.interfaces.RefreshItemsListener
import com.rank.me.dialer.models.RecentCall
import com.rank.me.extensions.config
import com.rank.me.extensions.launchCreateNewContactIntent
import com.rank.me.ui.base.SimpleActivity
import com.rank.me.ui.component.home.HomeActivity
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.CallConfirmationDialog
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.SimpleContact

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

class CallsFragment : Fragment(), RefreshItemsListener {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private var param1: String? = null
    private var param2: String? = null
    private val sharedViewModel: CallViewModel by activityViewModels()
    private var searchQuery = ""

    private var launchedDialer = false
    private var isSearchOpen = false

    private var allRecentCalls = ArrayList<RecentCall>()
    private lateinit var binding: FragmentCallsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentCallsBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            // Specify the fragment as the lifecycle owner
            lifecycleOwner = viewLifecycleOwner
            // Assign the view model to a property in the binding class
            viewModel = sharedViewModel
            // Assign the fragment
            callFragment = this@CallsFragment
        }
        launchedDialer = savedInstanceState?.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH) ?: false
        if (requireActivity().isDefaultDialer()) {
            checkContactPermissions()

            //TODO snackbar for allowing display over apps setting not working..... Maybe fix/change to dialogue later
//            if (!requireActivity().config.wasOverlaySnackbarConfirmed && !Settings.canDrawOverlays(requireActivity())) {
//                val snackbar = Snackbar.make(binding.root, R.string.allow_displaying_over_other_apps, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok) {
//                    requireActivity().config.wasOverlaySnackbarConfirmed = true
//                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
//                }
//
//                snackbar.setBackgroundTint(requireActivity().getProperBackgroundColor().darkenColor())
//                snackbar.setTextColor(requireActivity().getProperTextColor())
//                snackbar.setActionTextColor(requireActivity().getProperTextColor())
//                snackbar.show()
//            }

            (requireActivity() as HomeActivity).handleNotificationPermission { granted ->
                if (!granted) {
                    requireActivity().toast(R.string.no_post_notifications_permissions)
                }
            }
        } else {
            (requireActivity() as HomeActivity).launchSetDefaultDialerIntent()
        }
        SimpleContact.sorting = requireActivity().config.sorting

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                Log.e("TAG", "onMenuItemSelected: $menuItem")
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.search -> {
                        Toast.makeText(context, "Menu call search", Toast.LENGTH_SHORT).show()
                        // clearCompletedTasks()
                        true
                    }
                    R.id.sort -> {
                        showSortingDialog()
                        Toast.makeText(context, "Menu call sort", Toast.LENGTH_SHORT).show()
                        // loadTasks(true)
                        true
                    }
                    R.id.create_new_contact -> {
                        (activity as HomeActivity).launchCreateNewContactIntent()
                        Toast.makeText(context, "create_new_contact", Toast.LENGTH_SHORT).show()
                        // loadTasks(true)
                        true
                    }
                    R.id.clear_call_history -> {
                        clearCallHistory()
                        Toast.makeText(context, "clear_call_history", Toast.LENGTH_SHORT).show()
                        // loadTasks(true)
                        true
                    }
                    R.id.settings -> {
                        requireActivity().hideKeyboard()
                        startActivity(Intent(requireContext(), SettingsActivity::class.java))
                        Toast.makeText(context, "Menu call settings", Toast.LENGTH_SHORT).show()
                        // loadTasks(true)
                        true
                    }
                    R.id.about -> {
                        launchAbout()
                        Toast.makeText(context, "Menu call about", Toast.LENGTH_SHORT).show()
                        // loadTasks(true)
                        true
                    }
                    else -> false
                }
                // Handle the menu selection
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OPEN_DIAL_PAD_AT_LAUNCH, launchedDialer)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshItems()
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        val privateCursor = context?.getMyContactsCursor(false, true)
        val groupSubsequentCalls = context?.config?.groupSubsequentCalls ?: false
        context?.let {
            RecentsHelper(it).getRecentCalls(groupSubsequentCalls) { recents ->
                SimpleContactsHelper(it).getAvailableContacts(false) { contacts ->
                    val privateContacts = MyContactsContentProvider.getSimpleContacts(it, privateCursor)

                    recents.filter { it.phoneNumber == it.name }.forEach { recent ->
                        var wasNameFilled = false
                        if (privateContacts.isNotEmpty()) {
                            val privateContact = privateContacts.firstOrNull { it.doesContainPhoneNumber(recent.phoneNumber) }
                            if (privateContact != null) {
                                recent.name = privateContact.name
                                wasNameFilled = true
                            }
                        }

                        if (!wasNameFilled) {
                            val contact = contacts.firstOrNull { it.phoneNumbers.first().normalizedNumber == recent.phoneNumber }
                            if (contact != null) {
                                recent.name = contact.name
                            }
                        }
                    }

                    allRecentCalls = recents
                    activity?.runOnUiThread {
                        gotRecents(recents)
                    }
                }
            }
        }
    }

    private fun gotRecents(recents: ArrayList<RecentCall>) {
        if (recents.isEmpty()) {
            binding.recentsPlaceholder.beVisible()
            if (context?.hasPermission(PERMISSION_READ_CALL_LOG) == true) binding.recentsPlaceholder.beGone() else requestCallLogPermission()
            binding.recentsList.beGone()
        } else {
            binding.run {
                recentsPlaceholder.beGone()
                recentsPlaceholder.beGone()
            }
            binding.recentsList.beVisible()

            val currAdapter = binding.recentsList.adapter
            if (currAdapter == null) {
                RecentCallsAdapter(activity as SimpleActivity, recents, binding.recentsList, this, true) {
                    val recentCall = it as RecentCall
                    if (context?.config?.showCallConfirmation == true) {
                        CallConfirmationDialog(activity as SimpleActivity, recentCall.name) {
                            (activity as HomeActivity).launchCallIntent(recentCall.phoneNumber)
                        }
                    } else {
                        (activity as HomeActivity).launchCallIntent(recentCall.phoneNumber)
                    }
                }.apply {
                    binding.recentsList.adapter = this
                }

                if (context?.areSystemAnimationsEnabled == true) {
                    binding.recentsList.scheduleLayoutAnimation()
                }
            } else {
                (currAdapter as RecentCallsAdapter).updateItems(recents)
            }
        }
    }

    private fun requestCallLogPermission() {
        (activity as HomeActivity).handlePermission(PERMISSION_READ_CALL_LOG) {
            if (it) {
                binding.recentsPlaceholder.text = context?.getString(R.string.no_previous_calls)
                binding.recentsPlaceholder.beGone()

                val groupSubsequentCalls = context?.config?.groupSubsequentCalls ?: false
                context?.let { it1 ->
                    RecentsHelper(it1).getRecentCalls(groupSubsequentCalls) { recents ->
                        activity?.runOnUiThread {
                            gotRecents(recents)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // we dont really care about the result, the app can work without being the default Dialer too
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkContactPermissions()
        }
    }

    private fun checkContactPermissions() {
        (requireActivity() as HomeActivity).handlePermission(PERMISSION_READ_CONTACTS) {
            refreshItems()
        }
    }

    override fun onResume() {
        super.onResume()
        val properPrimaryColor = (requireActivity() as HomeActivity).getProperPrimaryColor()
        val dialpadIcon = resources.getColoredDrawableWithColor(R.drawable.ic_dialpad_vector, properPrimaryColor.getContrastColor())
        binding.mainDialpadButton.setImageDrawable(dialpadIcon)
        binding.mainDialpadButton.setOnClickListener {
            Intent(context, DialpadActivity::class.java).apply {
                startActivity(this)
            }
        }
        refreshItems()
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(activity as BaseSimpleActivity, false) {
            refreshItems {
                if (isSearchOpen) {
                    onSearchQueryChanged(searchQuery)
                }
            }
        }
    }

    fun onSearchClosed() {
        binding.recentsPlaceholder.beVisibleIf(allRecentCalls.isEmpty())
        (binding.recentsList.adapter as? RecentCallsAdapter)?.updateItems(allRecentCalls)
    }

    fun onSearchQueryChanged(text: String) {
        val recentCalls = allRecentCalls.filter {
            it.name.contains(text, true) || it.doesContainPhoneNumber(text)
        }.sortedByDescending {
            it.name.startsWith(text, true)
        }.toMutableList() as ArrayList<RecentCall>

        binding.recentsPlaceholder.beVisibleIf(recentCalls.isEmpty())
        (binding.recentsList.adapter as? RecentCallsAdapter)?.updateItems(recentCalls, text)
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

    private fun closeSearch() {
        if (isSearchOpen) {
//            getAllFragments().forEach {
//                it?.onSearchQueryChanged("")
//            }
//            mSearchMenuItem?.collapseActionView()
        }
    }

    private fun clearCallHistory() {
        if(activity!=null)
        ConfirmationDialog(requireActivity(), "", R.string.clear_history_confirmation) {
            RecentsHelper(requireActivity()).removeAllRecentCalls(activity as HomeActivity) {
                requireActivity().runOnUiThread {
                    refreshItems()
                }
            }
        }
    }

}

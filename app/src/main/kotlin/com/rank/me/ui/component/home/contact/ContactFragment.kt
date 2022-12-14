package com.rank.me.ui.component.home.contact

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
import com.pearltools.commons.activities.BaseSimpleActivity
import com.pearltools.commons.adapters.MyRecyclerViewAdapter
import com.pearltools.commons.dialogs.ConfirmationDialog
import com.pearltools.commons.extensions.*
import com.pearltools.commons.helpers.*
import com.pearltools.commons.models.FAQItem
import com.pearltools.commons.models.SimpleContact
import com.rank.me.BuildConfig
import com.rank.me.R
import com.rank.me.databinding.FragmentContactsBinding
import com.rank.me.dialer.activities.SettingsActivity
import com.rank.me.dialer.adapters.ContactsAdapter
import com.rank.me.dialer.dialogs.ChangeSortingDialog
import com.rank.me.dialer.helpers.OPEN_DIAL_PAD_AT_LAUNCH
import com.rank.me.dialer.helpers.RecentsHelper
import com.rank.me.dialer.interfaces.RefreshItemsListener
import com.rank.me.extensions.launchCreateNewContactIntent
import com.rank.me.extensions.startContactDetailsIntent
import com.rank.me.ui.base.SimpleActivity
import com.rank.me.ui.component.home.HomeActivity
import com.rank.me.ui.component.home.HomeViewModel
import com.rank.me.ui.component.home.call.refresh_count
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_OBJECT = "object"

class ContactFragment : Fragment(), RefreshItemsListener {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private var param1: String? = null
    private var param2: String? = null

    private var searchQuery = ""
    private var launchedDialer = false
    private var isSearchOpen = false
    private var allContacts = ArrayList<SimpleContact>()

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val sharedViewModel: HomeViewModel by activityViewModels()
    private lateinit var binding: FragmentContactsBinding

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
        val fragmentBinding = FragmentContactsBinding.inflate(inflater, container, false)
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
            contactFragment = this@ContactFragment
        }

        launchedDialer = savedInstanceState?.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH) ?: false
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
        setupFragment()
        refreshItems()

        if(activity!=null) {
            setupColors(requireActivity().getProperTextColor(), requireActivity().getProperPrimaryColor())
        }
    }

    private fun setupColors(textColor: Int, properPrimaryColor: Int) {
        (binding.fragmentList.adapter as? MyRecyclerViewAdapter)?.updateTextColor(textColor)
        binding.fragmentPlaceholder.setTextColor(textColor)
        binding.fragmentPlaceholder2.setTextColor(properPrimaryColor)

        binding.letterFastscroller.textColor = textColor.getColorStateList()
        binding.letterFastscroller.pressedTextColor = properPrimaryColor
        binding.letterFastscrollerThumb.setupWithFastScroller(binding.letterFastscroller)
        binding.letterFastscrollerThumb.textColor = properPrimaryColor.getContrastColor()
        binding.letterFastscrollerThumb.thumbColor = properPrimaryColor.getColorStateList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OPEN_DIAL_PAD_AT_LAUNCH, launchedDialer)
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
            ContactFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun setupFragment() {
        val placeholderResId = if (context?.hasPermission(PERMISSION_READ_CONTACTS) == true) {
            R.string.no_contacts_found
        } else {
            R.string.could_not_access_contacts
        }

        binding.fragmentPlaceholder.text = context?.getString(placeholderResId) ?: "Error"

        val placeholderActionResId = if (context?.hasPermission(PERMISSION_READ_CONTACTS) == true) {
            R.string.create_new_contact
        } else {
            R.string.request_access
        }

        binding.fragmentPlaceholder2.apply {
            text = context.getString(placeholderActionResId)
            underlineText()
            setOnClickListener {
                if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
                    (activity as HomeActivity).launchCreateNewContactIntent()
                } else {
                    requestReadContactsPermission()
                }
            }
        }
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        val privateCursor = context?.getMyContactsCursor(false, true)
        SimpleContactsHelper(requireContext()).getAvailableContacts(false) { contacts ->
            allContacts = contacts

            val privateContacts = MyContactsContentProvider.getSimpleContacts(requireContext(), privateCursor)
            if (privateContacts.isNotEmpty()) {
                allContacts.addAll(privateContacts)
                allContacts.sort()
            }

            Log.e("TAG", "refreshItems in contacts 1 : ${contacts.size}", )
            Log.e("TAG", "refreshItems in contacts 2 : ${privateContacts.size}", )
            (activity as HomeActivity).cacheContacts(allContacts)

            activity?.runOnUiThread {
                gotContacts(contacts)
                callback?.invoke()
            }
        }
    }

    private fun gotContacts(contacts: ArrayList<SimpleContact>) {
        setupLetterFastscroller(contacts)
        if (contacts.isEmpty()) {
            binding.fragmentPlaceholder.beVisible()
            binding.fragmentPlaceholder2.beVisible()
            binding.fragmentList.beGone()
        } else {
            binding.fragmentPlaceholder.beGone()
            binding.fragmentPlaceholder2.beGone()
            binding.fragmentList.beVisible()

            val currAdapter = binding.fragmentList.adapter
            if (currAdapter == null) {
                ContactsAdapter(activity as SimpleActivity, contacts, binding.fragmentList, this) {
                    val contact = it as SimpleContact
                    activity?.startContactDetailsIntent(contact)
                }.apply {
                    binding.fragmentList.adapter = this
                }

                if (requireContext().areSystemAnimationsEnabled) {
                    binding.fragmentList.scheduleLayoutAnimation()
                }
            } else {
                (currAdapter as ContactsAdapter).updateItems(contacts)
            }
        }
    }

    private fun setupLetterFastscroller(contacts: ArrayList<SimpleContact>) {
        binding.letterFastscroller.setupWithRecyclerView(binding.fragmentList, { position ->
            try {
                val name = contacts[position].name
                val character = if (name.isNotEmpty()) name.substring(0, 1) else ""
                FastScrollItemIndicator.Text(character.toUpperCase(Locale.getDefault()).normalizeString())
            } catch (e: Exception) {
                FastScrollItemIndicator.Text("")
            }
        })
    }

    fun onSearchClosed() {
       binding.fragmentPlaceholder.beVisibleIf(allContacts.isEmpty())
        (binding.fragmentList.adapter as? ContactsAdapter)?.updateItems(allContacts)
        setupLetterFastscroller(allContacts)
    }

    fun onSearchQueryChanged(text: String) {
        val contacts = allContacts.filter {
            it.doesContainPhoneNumber(text) ||
                it.name.contains(text, true) ||
                it.name.normalizeString().contains(text, true) ||
                it.name.contains(text.normalizeString(), true)
        }.sortedByDescending {
            it.name.startsWith(text, true)
        }.toMutableList() as ArrayList<SimpleContact>

        binding.fragmentPlaceholder.beVisibleIf(contacts.isEmpty())
        (binding.fragmentList.adapter as? ContactsAdapter)?.updateItems(contacts, text)
        setupLetterFastscroller(contacts)
    }

    private fun requestReadContactsPermission() {
        (activity as HomeActivity).handlePermission(PERMISSION_READ_CONTACTS) {
            if (it) {
               binding.fragmentPlaceholder.text = requireContext().getString(R.string.no_contacts_found)
               binding.fragmentPlaceholder2.text = requireContext().getString(R.string.create_new_contact)
                SimpleContactsHelper(requireContext()).getAvailableContacts(false) { contacts ->
                    activity?.runOnUiThread {
                        gotContacts(contacts)
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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

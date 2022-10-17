package com.rank.me.ui.component.home.call

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rank.me.R
import com.rank.me.databinding.FragmentRecentsBinding
import com.rank.me.dialer.activities.DialpadActivity
import com.rank.me.dialer.adapters.RecentCallsAdapter
import com.rank.me.dialer.extensions.config
import com.rank.me.dialer.helpers.OPEN_DIAL_PAD_AT_LAUNCH
import com.rank.me.dialer.helpers.RecentsHelper
import com.rank.me.dialer.interfaces.RefreshItemsListener
import com.rank.me.dialer.models.RecentCall
import com.rank.me.ui.base.SimpleActivity
import com.rank.me.ui.component.home.HomeActivity
import com.simplemobiletools.commons.dialogs.CallConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.SimpleContact
import kotlinx.android.synthetic.main.fragment_recents.*
import kotlinx.android.synthetic.main.fragment_recents.view.*

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

class RecentsFragment : Fragment(), RefreshItemsListener {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private var param1: String? = null
    private var param2: String? = null
    private val sharedViewModel: CallViewModel by activityViewModels()

    private var launchedDialer = false

    private var allRecentCalls = ArrayList<RecentCall>()
    private lateinit var binding: FragmentRecentsBinding

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
        val fragmentBinding = FragmentRecentsBinding.inflate(inflater, container, false)
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
            callFragment = this@RecentsFragment
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


}

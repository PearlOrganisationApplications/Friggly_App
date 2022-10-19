package com.rank.me.ui.component.home.message.inbox

import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rank.me.R
import com.rank.me.databinding.InboxFragmentBinding
import com.rank.me.extensions.*
import com.rank.me.message.activities.NewConversationActivity
import com.rank.me.message.activities.ThreadActivity
import com.rank.me.message.adapters.ConversationsAdapter
import com.rank.me.message.helpers.MessagesExporter
import com.rank.me.message.helpers.THREAD_ID
import com.rank.me.message.helpers.THREAD_TITLE
import com.rank.me.message.models.Conversation
import com.rank.me.ui.component.home.HomeActivity
import com.rank.me.ui.component.home.HomeViewModel
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MessageInboxFragment : Fragment(){
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_OBJECT = "object"

    private var binding: InboxFragmentBinding? = null

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val sharedViewModel: HomeViewModel by activityViewModels()

    private val MAKE_DEFAULT_APP_REQUEST = 1
    private val PICK_IMPORT_SOURCE_INTENT = 11
    private val PICK_EXPORT_FILE_INTENT = 21

    private var storedTextColor = 0
    private var storedFontSize = 0
    private var bus: EventBus? = null
    private val smsExporter by lazy { MessagesExporter(requireActivity()) }

    private fun storeStateVariables() {
        storedTextColor = (activity as HomeActivity).getProperTextColor()
        storedFontSize = requireActivity().config.fontSize
    }

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
    ): View? {
        val fragmentBinding = InboxFragmentBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
//            textView.text = getInt(ARG_OBJECT).toString()
        }
        binding?.apply {
            // Specify the fragment as the lifecycle owner
            lifecycleOwner = viewLifecycleOwner

            // Assign the view model to a property in the binding class
            viewModel = sharedViewModel

            // Assign the fragment
            inboxFragment = this@MessageInboxFragment
        }
        if (isQPlus()) {
            val roleManager = requireActivity().getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    askPermissions()
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    startActivityForResult(intent, MAKE_DEFAULT_APP_REQUEST)
                }
            } else {
                requireActivity().toast(R.string.unknown_error_occurred)
                requireActivity().finish()
            }
        } else {
            if (Telephony.Sms.getDefaultSmsPackage(requireActivity()) == requireActivity().packageName) {
                askPermissions()
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, requireActivity().packageName)
                startActivityForResult(intent, MAKE_DEFAULT_APP_REQUEST)
            }
        }
        requireActivity().clearAllMessagesIfNeeded()
        handlePassCodeFeature();
    }

    private fun handlePassCodeFeature() {
        binding?.later?.setOnClickListener {
            binding!!.passCodeLyt.visibility = View.GONE

        }
    }

    // while SEND_SMS and READ_SMS permissions are mandatory, READ_CONTACTS is optional. If we don't have it, we just won't be able to show the contact name in some cases
    private fun askPermissions() {
        (activity as HomeActivity).handlePermission(PERMISSION_READ_SMS) {
            if (it) {
                (activity as HomeActivity).handlePermission(PERMISSION_SEND_SMS) {
                    if (it) {
                        (activity as HomeActivity).handlePermission(PERMISSION_READ_CONTACTS) {
                            (activity as HomeActivity).handleNotificationPermission { granted ->
                                if (!granted) {
                                    requireActivity().toast(R.string.no_post_notifications_permissions)
                                }
                            }

                            initMessenger()
                            bus = EventBus.getDefault()
                            try {
                                bus!!.register(this)
                            } catch (e: Exception) {
                            }
                        }
                    } else {
                        requireActivity().finish()
                    }
                }
            } else {
                requireActivity().finish()
            }
        }
    }

    private fun initMessenger() {
        storeStateVariables()
        getCachedConversations()

        binding?.noConversationsPlaceholder2?.setOnClickListener {
            launchNewConversation()
        }
    }

    private fun getCachedConversations() {
        ensureBackgroundThread {
            val conversations = try {
                requireActivity().conversationsDB.getAll().toMutableList() as ArrayList<Conversation>
            } catch (e: Exception) {
                ArrayList()
            }

            requireActivity().updateUnreadCountBadge(conversations)
            requireActivity().runOnUiThread {
                setupConversations(conversations)
                getNewConversations(conversations)
            }
        }
    }

    private fun getNewConversations(cachedConversations: ArrayList<Conversation>) {
        val privateCursor = requireActivity().getMyContactsCursor(false, true)
        ensureBackgroundThread {
            val privateContacts = MyContactsContentProvider.getSimpleContacts(requireActivity(), privateCursor)
            val conversations = requireActivity().getConversations(privateContacts = privateContacts)

            requireActivity().runOnUiThread {
                setupConversations(conversations)
            }

            conversations.forEach { clonedConversation ->
                if (!cachedConversations.map { it.threadId }.contains(clonedConversation.threadId)) {
                    requireActivity().conversationsDB.insertOrUpdate(clonedConversation)
                    cachedConversations.add(clonedConversation)
                }
            }

            cachedConversations.forEach { cachedConversation ->
                if (!conversations.map { it.threadId }.contains(cachedConversation.threadId)) {
                    requireActivity().conversationsDB.deleteThreadId(cachedConversation.threadId)
                }
            }

            cachedConversations.forEach { cachedConversation ->
                val conv = conversations.firstOrNull { it.threadId == cachedConversation.threadId && it.toString() != cachedConversation.toString() }
                if (conv != null) {
                    requireActivity().conversationsDB.insertOrUpdate(conv)
                }
            }

            if (requireActivity().config.appRunCount == 1) {
                conversations.map { it.threadId }.forEach { threadId ->
                    val messages = requireActivity().getMessages(threadId, false)
                    messages.chunked(30).forEach { currentMessages ->
                        requireActivity().messagesDB.insertMessages(*currentMessages.toTypedArray())
                    }
                }
            }
        }
    }

    private fun setupConversations(conversations: ArrayList<Conversation>) {
        val hasConversations = conversations.isNotEmpty()
        val sortedConversations = conversations.sortedWith(
            compareByDescending<Conversation> { requireActivity().config.pinnedConversations.contains(it.threadId.toString()) }
                .thenByDescending { it.date }
        ).toMutableList() as ArrayList<Conversation>

        binding?.conversationsFastscroller?.beVisibleIf(hasConversations)
        binding?.noConversationsPlaceholder?.beGoneIf(hasConversations)
        binding?.noConversationsPlaceholder2?.beGoneIf(hasConversations)

        if (!hasConversations && requireActivity().config.appRunCount == 1) {
            binding?.noConversationsPlaceholder?.text = getString(R.string.loading_messages)
            binding?.noConversationsPlaceholder2?.beGone()
        }


        val currAdapter = binding?.conversationsList?.adapter
        if (currAdapter == null) {
            requireActivity().hideKeyboard()
            ConversationsAdapter((activity as HomeActivity), sortedConversations, binding!!.conversationsList) {

                val intent = Intent(requireContext(), ThreadActivity::class.java)
                    .putExtra(THREAD_ID, (it as Conversation).threadId)
                    .putExtra(THREAD_TITLE, it.title)
                startActivity(intent)
            }.apply {
                binding?.conversationsList?.adapter = this
            }

            if (requireActivity().areSystemAnimationsEnabled) {
                binding?.conversationsList?.scheduleLayoutAnimation()
            }
        } else {
            try {
                (currAdapter as ConversationsAdapter).updateConversations(sortedConversations)
                if (currAdapter.conversations.isEmpty()) {
                    binding?.conversationsFastscroller?.beGone()
                    binding?.noConversationsPlaceholder?.text = getString(R.string.no_conversations_found)
                    binding?.noConversationsPlaceholder?.beVisible()
                    binding?.noConversationsPlaceholder2?.beVisible()
                }
            } catch (ignored: Exception) {
            }
        }
    }

    private fun launchNewConversation() {
        requireActivity().hideKeyboard()
        val intent = Intent(requireContext(), NewConversationActivity::class.java)
        startActivity(intent)
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
            MessageInboxFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

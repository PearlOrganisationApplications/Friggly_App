package com.rank.me.ui.component.home.message.inbox

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rank.me.BuildConfig
import com.rank.me.R
import com.rank.me.databinding.InboxFragmentBinding
import com.rank.me.message.activities.*
import com.rank.me.message.adapters.ConversationsAdapter
import com.rank.me.message.dialogs.ExportMessagesDialog
import com.rank.me.message.dialogs.ImportMessagesDialog
import com.rank.me.message.extensions.*
import com.rank.me.message.helpers.EXPORT_MIME_TYPE
import com.rank.me.message.helpers.MessagesExporter
import com.rank.me.message.helpers.THREAD_ID
import com.rank.me.message.helpers.THREAD_TITLE
import com.rank.me.message.models.Conversation
import com.rank.me.message.models.Events
import com.rank.me.ui.component.home.HomeActivity
import com.rank.me.ui.component.home.HomeViewModel
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.Release
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

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
        checkWhatsNewDialog()
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

    @SuppressLint("NewApi")
    private fun checkShortcut() {
        val appIconColor = requireActivity().config.appIconColor
        if (isNougatMR1Plus() && requireActivity().config.lastHandledShortcutColor != appIconColor) {
            val newConversation = getCreateNewContactShortcut(appIconColor)

            val manager = requireActivity().getSystemService(ShortcutManager::class.java)
            try {
                manager.dynamicShortcuts = Arrays.asList(newConversation)
                requireActivity().config.lastHandledShortcutColor = appIconColor
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getCreateNewContactShortcut(appIconColor: Int): ShortcutInfo {
        val newEvent = getString(R.string.new_conversation)
        val drawable = resources.getDrawable(R.drawable.shortcut_plus)
        (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_plus_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(requireContext(), NewConversationActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        return ShortcutInfo.Builder(requireContext(), "new_conversation")
            .setShortLabel(newEvent)
            .setLongLabel(newEvent)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
    }

    private fun launchSearch() {
        requireActivity().hideKeyboard()
        startActivity(Intent(requireContext(), SearchActivity::class.java))
    }

    private fun launchSettings() {
        requireActivity().hideKeyboard()
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val licenses = LICENSE_EVENT_BUS or LICENSE_SMS_MMS or LICENSE_INDICATOR_FAST_SCROLL

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_2_title, R.string.faq_2_text),
            FAQItem(R.string.faq_9_title_commons, R.string.faq_9_text_commons)
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
            faqItems.add(FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
        }

        (activity as HomeActivity).startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun tryToExportMessages() {
        if (isQPlus()) {
            ExportMessagesDialog(activity as SimpleActivity, requireActivity().config.lastExportPath, true) { file ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = EXPORT_MIME_TYPE
                    putExtra(Intent.EXTRA_TITLE, file.name)
                    addCategory(Intent.CATEGORY_OPENABLE)

                    try {
                        startActivityForResult(this, PICK_EXPORT_FILE_INTENT)
                    } catch (e: ActivityNotFoundException) {
                        requireActivity().toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        requireActivity().showErrorToast(e)
                    }
                }
            }
        } else {
            (activity as HomeActivity).handlePermission(PERMISSION_WRITE_STORAGE) {
                if (it) {
                    ExportMessagesDialog(activity as SimpleActivity, requireActivity().config.lastExportPath, false) { file ->
                        (activity as HomeActivity).getFileOutputStream(file.toFileDirItem(requireContext()), true) { outStream ->
                            exportMessagesTo(outStream)
                        }
                    }
                }
            }
        }
    }

    private fun exportMessagesTo(outputStream: OutputStream?) {
        requireActivity().toast(R.string.exporting)
        ensureBackgroundThread {
            smsExporter.exportMessages(outputStream) {
                val toastId = when (it) {
                    MessagesExporter.ExportResult.EXPORT_OK -> R.string.exporting_successful
                    else -> R.string.exporting_failed
                }

                requireActivity().toast(toastId)
            }
        }
    }

    private fun tryImportMessages() {
        if (isQPlus()) {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = EXPORT_MIME_TYPE

                try {
                    startActivityForResult(this, PICK_IMPORT_SOURCE_INTENT)
                } catch (e: ActivityNotFoundException) {
                    requireActivity().toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    requireActivity().showErrorToast(e)
                }
            }
        } else {
            (activity as HomeActivity).handlePermission(PERMISSION_READ_STORAGE) {
                if (it) {
                    importEvents()
                }
            }
        }
    }

    private fun importEvents() {
        FilePickerDialog(activity as BaseSimpleActivity) {
            showImportEventsDialog(it)
        }
    }

    private fun showImportEventsDialog(path: String) {
        ImportMessagesDialog(activity as SimpleActivity, path)
    }

    private fun tryImportMessagesFromFile(uri: Uri) {
        when (uri.scheme) {
            "file" -> showImportEventsDialog(uri.path!!)
            "content" -> {
                val tempFile = (activity as HomeActivity).getTempFile("messages", "backup.json")
                if (tempFile == null) {
                    requireActivity().toast(R.string.unknown_error_occurred)
                    return
                }

                try {
                    val inputStream = requireActivity().contentResolver.openInputStream(uri)
                    val out = FileOutputStream(tempFile)
                    inputStream!!.copyTo(out)
                    showImportEventsDialog(tempFile.absolutePath)
                } catch (e: Exception) {
                    requireActivity().showErrorToast(e)
                }
            }
            else -> requireActivity().toast(R.string.invalid_file_format)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshMessages(event: Events.RefreshMessages) {
        initMessenger()
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(48, R.string.release_48))
            (activity as HomeActivity).checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
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

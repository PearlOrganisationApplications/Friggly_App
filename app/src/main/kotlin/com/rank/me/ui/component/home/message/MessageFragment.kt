package com.rank.me.ui.component.home.message

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.rank.me.R
import com.rank.me.databinding.FragmentMsgBinding
import com.rank.me.extensions.config
import com.rank.me.extensions.conversationsDB
import com.rank.me.message.activities.NewConversationActivity
import com.rank.me.message.activities.SettingsActivity
import com.rank.me.message.dialogs.ExportMessagesDialog
import com.rank.me.message.dialogs.ImportMessagesDialog
import com.rank.me.message.helpers.EXPORT_MIME_TYPE
import com.rank.me.message.helpers.MessagesExporter
import com.rank.me.message.models.Conversation
import com.rank.me.ui.base.SimpleActivity
import com.rank.me.ui.component.home.HomeActivity
import com.rank.me.ui.component.home.HomeViewModel
import com.rank.me.ui.component.home.message.highlight.MessageHighlighFragment
import com.rank.me.ui.component.home.message.inbox.MessageInboxFragment
import com.rank.me.ui.component.home.message.promotion.MessagePromotionFragment
import com.rank.me.ui.component.home.message.spam.MessageSpamFragment
import com.rank.me.utils.livedatapermission.PermissionManager
import com.rank.me.utils.livedatapermission.model.PermissionResult
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.Release
import org.greenrobot.eventbus.EventBus
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_OBJECT = "object"

class MessageFragment : Fragment() , PermissionManager.PermissionObserver {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var messageSubAdapter: MessageSubAdapter
    private var param1: String? = null
    private var param2: String? = null
    private val smsExporter by lazy { MessagesExporter(requireContext()) }

    private val MAKE_DEFAULT_APP_REQUEST = 1
    private val PICK_IMPORT_SOURCE_INTENT = 11
    private val PICK_EXPORT_FILE_INTENT = 21

    private var storedTextColor = 0
    private var storedFontSize = 0
    private var bus: EventBus? = null

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val sharedViewModel: HomeViewModel by activityViewModels()
    private lateinit var binding: FragmentMsgBinding

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
        val fragmentBinding = FragmentMsgBinding.inflate(inflater, container, false)
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
            messageFragment = this@MessageFragment
        }
        //TODO shift this Permission request to somewhere else maybe
//        PermissionManager.requestPermissions(
//            this,
//            4,
//            Manifest.permission.READ_SMS
//        )
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

        binding.messageFab.setOnClickListener {
            openThread()
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.import_messages -> {
                        tryImportMessages()
                        //TODO shift this Permission request to somewhere else maybe
                        Toast.makeText(context, "Menu msg import", Toast.LENGTH_SHORT ).show()
                        // clearCompletedTasks()
                        true
                    }
                    R.id.export_messages -> {
                        tryToExportMessages()
                        Toast.makeText(context, "Menu msg export", Toast.LENGTH_SHORT ).show()
                        // loadTasks(true)
                        true
                    }
                    R.id.settings -> {
                        requireActivity().hideKeyboard()
                        startActivity(Intent(requireContext(), SettingsActivity::class.java))
                        Toast.makeText(context, "Menu msg settings", Toast.LENGTH_SHORT ).show()
                        // loadTasks(true)
                        true
                    }
                    R.id.about -> {
                        //TODO about actiivty should be made already
                        Toast.makeText(context, "Menu msg about", Toast.LENGTH_SHORT ).show()
                        // loadTasks(true)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    class MessageSubAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val ARG_OBJECT = "object"
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            val fragment : Fragment
            when (position) {
                0 -> {
                    fragment = MessageInboxFragment()
                }
                1 -> {
                    fragment = MessageHighlighFragment()
                }
                2 -> {
                    fragment = MessagePromotionFragment()
                }
                3 -> {
                    fragment = MessageSpamFragment()
                }
                else -> {
                    fragment = MessageInboxFragment()
                }
            }
            fragment.arguments = Bundle().apply {
                putInt(ARG_OBJECT, position)
            }
            return fragment
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
        FilePickerDialog(activity as HomeActivity) {
            showImportEventsDialog(it)
        }
    }

    private fun showImportEventsDialog(path: String) {
        ImportMessagesDialog((activity as HomeActivity), path)
    }

    private fun tryImportMessagesFromFile(uri: Uri) {
        when (uri.scheme) {
            "file" -> showImportEventsDialog(uri.path!!)
            "content" -> {
                val tempFile = (activity as HomeActivity).getTempFile("messages", "backup.json")
                if (tempFile == null) {
                    (activity as HomeActivity).toast(R.string.unknown_error_occurred)
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

    private fun tryToExportMessages() {
        if (isQPlus()) {
            ExportMessagesDialog((activity as SimpleActivity), (activity as HomeActivity).config.lastExportPath, true) { file ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = EXPORT_MIME_TYPE
                    putExtra(Intent.EXTRA_TITLE, file.name)
                    addCategory(Intent.CATEGORY_OPENABLE)

                    try {
                        startActivityForResult(this, PICK_EXPORT_FILE_INTENT)
                    } catch (e: ActivityNotFoundException) {
                        (activity as HomeActivity).toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        (activity as HomeActivity).showErrorToast(e)
                    }
                }
            }
        } else {
            (activity as HomeActivity).handlePermission(PERMISSION_WRITE_STORAGE) {
                if (it) {
                    ExportMessagesDialog((activity as SimpleActivity), (activity as HomeActivity).config.lastExportPath, false) { file ->
                        (activity as HomeActivity).getFileOutputStream(file.toFileDirItem((activity as HomeActivity)), true) { outStream ->
                            exportMessagesTo(outStream)
                        }
                    }
                }
            }
        }
    }

    private fun exportMessagesTo(outputStream: OutputStream?) {
        (activity as HomeActivity).toast(R.string.exporting)
        ensureBackgroundThread {
            smsExporter.exportMessages(outputStream) {
                val toastId = when (it) {
                    MessagesExporter.ExportResult.EXPORT_OK -> R.string.exporting_successful
                    else -> R.string.exporting_failed
                }
                (activity as HomeActivity).toast(toastId)
            }
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
            MessageFragment().apply {
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

    override fun setupObserver(permissionResultLiveData: LiveData<PermissionResult>) {
        permissionResultLiveData.observe(this) {
            when (it) {
                is PermissionResult.PermissionGranted -> {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                }
                is PermissionResult.PermissionDenied -> {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                is PermissionResult.ShowRational -> when (it.requestCode) {
                    1 -> {
                        PermissionManager.requestPermissions(
                            this,
                            1,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                    2 -> {
                        PermissionManager.requestPermissions(
                            this,
                            2,
                            Manifest.permission.READ_CONTACTS
                        )
                    }
                    3 -> {
                        PermissionManager.requestPermissions(
                            this,
                            3,
                            Manifest.permission.CAMERA
                        )
                    }
                    4 -> {
                        PermissionManager.requestPermissions(
                            this,
                            4,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CAMERA
                        )
                    }
                }
                is PermissionResult.PermissionDeniedPermanently -> {
                    Toast.makeText(requireContext(), "Denied permanently", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}

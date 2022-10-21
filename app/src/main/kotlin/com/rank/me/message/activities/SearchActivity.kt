package com.rank.me.message.activities

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.pearltools.commons.extensions.*
import com.pearltools.commons.helpers.MyContactsContentProvider
import com.pearltools.commons.helpers.SimpleContactsHelper
import com.pearltools.commons.helpers.ensureBackgroundThread
import com.pearltools.commons.models.SimpleContact
import com.rank.me.R
import com.rank.me.databinding.ActivitySearchBinding
import com.rank.me.dialer.adapters.ContactsAdapter
import com.rank.me.dialer.interfaces.RefreshItemsListener
import com.rank.me.extensions.conversationsDB
import com.rank.me.extensions.messagesDB
import com.rank.me.extensions.startContactDetailsIntent
import com.rank.me.message.models.Conversation
import com.rank.me.message.models.Message
import com.rank.me.message.models.SearchResult
import com.rank.me.ui.base.SimpleActivity

class SearchActivity : SimpleActivity(), RefreshItemsListener {
    private var mIsSearchOpen = true
    private var mLastSearchedText = ""
    private lateinit var binding: ActivitySearchBinding

    private lateinit var  currAdapter : ContactsAdapter

    private var allContacts = ArrayList<SimpleContact>()
    private var searchedContacts: ArrayList<SimpleContact> = ArrayList()

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        updateTextColors(binding.searchHolder)
        binding.searchPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
        binding.searchPlaceholder2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
        setupSearch(binding.searchView)
        refreshItems()
        binding.imageView.setOnClickListener {
            supportFinishAfterTransition()
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("supportFinishAfterTransition()"))
    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    private fun setupSearch(searchView: SearchView) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.requestFocus()
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (mIsSearchOpen) {
                        mLastSearchedText = newText
                        if (newText.isNotEmpty()) {
                            textChanged(newText)
                            onSearchQueryChanged(newText)
                        } else {
                            binding.searchPlaceholder.beVisible()
                            binding.messageList.beGone()
                            binding.contactList.beGone()
                        }
                    }
                    return true
                }
            })
        }
    }

    fun onSearchQueryChanged(text: String) {
            searchedContacts.clear()
            searchedContacts = allContacts.filter {
                it.doesContainPhoneNumber(text) || it.name.contains(text, true) || it.name.normalizeString()
                    .contains(text, true) || it.name.contains(text.normalizeString(), true)
            }.sortedByDescending {
                it.name.startsWith(text, true)
            }.toMutableList() as java.util.ArrayList<SimpleContact>
            gotContacts(searchedContacts)
    }

    private fun textChanged(text: String) {
        binding.searchPlaceholder2.beGoneIf(true)
        ensureBackgroundThread {
            val searchQuery = "%$text%"
            val messages = messagesDB.getMessagesWithText(searchQuery)
            val conversations = conversationsDB.getConversationsWithText(searchQuery)
            if (text == mLastSearchedText) {
                showSearchResults(messages, conversations)
            }
        }
    }

    private fun showSearchResults(messages: List<Message>, conversations: List<Conversation>) {
        val searchResults = ArrayList<SearchResult>()
        conversations.forEach { conversation ->
            val date = conversation.date.formatDateOrTime(this, true, true)
            val searchResult = SearchResult(-1, conversation.title, conversation.phoneNumber, date, conversation.threadId, conversation.photoUri)
            searchResults.add(searchResult)
        }

        messages.sortedByDescending { it.id }.forEach { message ->
            var recipient = message.senderName
            if (recipient.isEmpty() && message.participants.isNotEmpty()) {
                val participantNames = message.participants.map { it.name }
                recipient = TextUtils.join(", ", participantNames)
            }

            val date = message.date.formatDateOrTime(this, true, true)
            val searchResult = SearchResult(message.id, recipient, message.body, date, message.threadId, message.senderPhotoUri)
            searchResults.add(searchResult)
        }
        runOnUiThread {
            //TODO HIDE binding.searchPlaceholder.beGoneIf(true)
            // BUT HIDE based on both results ie message and contact results together...
            binding.messageList.beVisibleIf(searchResults.isNotEmpty())
        }
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        val privateCursor = this@SearchActivity.getMyContactsCursor(false, true)
        SimpleContactsHelper(this@SearchActivity).getAvailableContacts(false) { contacts ->
            allContacts = contacts
            val privateContacts = MyContactsContentProvider.getSimpleContacts(this@SearchActivity, privateCursor)
            if (privateContacts.isNotEmpty()) {
                allContacts.addAll(privateContacts)
                allContacts.sort()
            }
            currAdapter = ContactsAdapter(this@SearchActivity, searchedContacts, binding.messageList, this) {
                val contact = it as SimpleContact
                startContactDetailsIntent(contact)
            }.apply {
                binding.contactList.adapter = this
            }
            if (areSystemAnimationsEnabled) {
                binding.contactList.scheduleLayoutAnimation()
            }
        }
    }

    private fun gotContacts(contacts: ArrayList<SimpleContact>) {
        if (contacts.isEmpty()) {
            Toast.makeText(this@SearchActivity, "empty list", Toast.LENGTH_SHORT).show()
            //TODO show user no contact found somehow
        } else {
            Toast.makeText(this@SearchActivity, "updated items", Toast.LENGTH_SHORT).show()
                currAdapter.updateItems(contacts)
            runOnUiThread {
                binding.contactList.beVisibleIf(contacts.isNotEmpty())
            }
        }
    }
}

package com.rank.me.ui.component.home.message.inbox

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.rank.me.databinding.InboxFragmentBinding
import com.rank.me.ui.base.BaseFragment
import com.rank.me.utils.livedatapermission.PermissionManager
import com.rank.me.utils.livedatapermission.model.PermissionResult

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MessageInboxFragment : BaseFragment<InboxFragmentBinding>(InboxFragmentBinding::inflate) , PermissionManager.PermissionObserver {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_OBJECT = "object"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
//            textView.text = getInt(ARG_OBJECT).toString()
        }
        PermissionManager.requestPermissions(
            this,
            4,
            Manifest.permission.READ_SMS
        )
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

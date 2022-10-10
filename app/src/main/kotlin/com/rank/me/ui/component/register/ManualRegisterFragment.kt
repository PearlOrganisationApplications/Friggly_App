package com.rank.me.ui.component.register

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.rank.me.R
import com.rank.me.databinding.FragmentManualregBinding
import com.rank.me.ui.base.BaseFragment
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.CoilAdapter

class ManualRegisterFragment :
    BaseFragment<FragmentManualregBinding>(FragmentManualregBinding::inflate) {
    private lateinit var imageAdapter: ImageAdapter
    var path = ArrayList<Uri>()
    private lateinit var title: String
    private lateinit var description: String
    private val sharedViewModel: RegisterViewModel by activityViewModels()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                path = it.data?.getParcelableArrayListExtra(FishBun.INTENT_PATH) ?: arrayListOf()
                imageAdapter.changePath(path)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title =
                requireArguments().getString(ARG_PARAM1)!!
            description =
                requireArguments().getString(ARG_PARAM2)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            title =
                requireArguments().getString(ARG_PARAM1)!!
            description =
                requireArguments().getString(ARG_PARAM2)!!
        }
        imageAdapter = ImageAdapter(requireActivity(), ImageController(binding.imgMain), path)
        binding.recyclerview.adapter = imageAdapter
        binding.nextButton.setOnClickListener {
            val selectedOption: Int = binding.genderRg.checkedRadioButtonId
            // Assigning id of the checked radio button
            val radioButton = view.findViewById(selectedOption) as RadioButton
            doManualRegister(
                binding.firstName.text.toString(),
                binding.lastName.text.toString(),
                binding.email.text.toString(),
                binding.age.text.toString(),
                radioButton.text.toString(),
            )
        }
        binding.btnAddImagesUseCallback.setOnClickListener {
            FishBun.with(this@ManualRegisterFragment)
                .setImageAdapter(CoilAdapter())
                .setMaxCount(6)
                .setActionBarColor(
                    resources.getColor(R.color.colorPrimary),
                    resources.getColor(R.color.colorPrimaryDark)
                )
                .setSelectedImages(path)
                .hasCameraInPickerPage(true)
                .startAlbumWithActivityResultCallback(startForResult)
        }
    }

    /**
     * set the entries and do register.
     */
    fun doManualRegister(
        firstName: String,
        lastName: String,
        email: String,
        age: String,
        gender: String
    ) {
        sharedViewModel.setRegisterData(firstName, lastName, email, age, gender)
        (activity as RegisterActivity).startRegister()
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(
        ): ManualRegisterFragment {
            return ManualRegisterFragment()
        }
    }
}

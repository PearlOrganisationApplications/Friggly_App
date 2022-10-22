package com.rank.me.ui.component.onboarding

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import com.rank.me.R
import com.rank.me.data.local.LocalData
import com.rank.me.ui.component.login.LoginActivity
import com.rank.me.utils.livedatapermission.PermissionManager
import com.rank.me.utils.livedatapermission.model.PermissionResult
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

class PermissionsDialog : BlurDialogFragment(), PermissionManager.PermissionObserver {

    override fun onCreateView(): Int {
        return R.layout.fragment_permissions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            val button = dialog.findViewById<TextView>(R.id.continue_permissions)
            button.setOnClickListener {
                PermissionManager.requestPermissions(
                    this,
                    4,
                    Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS
                )
            }
        }
    }

    override fun setupObserver(permissionResultLiveData: LiveData<PermissionResult>) {
        permissionResultLiveData.observe(this) {
            when (it) {
                is PermissionResult.PermissionGranted -> {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                    finishOnBoarding()
                }
                is PermissionResult.PermissionDenied -> {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                    finishOnBoarding()
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
                    finishOnBoarding()
                }
            }
        }
    }

    private fun finishOnBoarding() {
        LocalData(requireContext()).setFirstTime(false)
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }

}

abstract class BlurDialogFragment : DialogFragment() {
    private val BLUR_ID = 514214
    private val DEFAULT_BLUR_RADIUS = 3f
    private val DEFAULT_BLUR_LIVE = false

    /**
     * return your layout id
     * @return Int
     */
    abstract fun onCreateView(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return blurInjector(inflater.inflate(onCreateView(), container, false))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // this part is important to make injected background blur
        val blurView = view.findViewById<BlurView>(BLUR_ID)
        blurView.setupWith(
            requireActivity().window.decorView as ViewGroup,
            RenderScriptBlur(context)
        )
            .setFrameClearDrawable(requireActivity().window.decorView.background)
            .setOverlayColor(resources.getColor(R.color.colorGreyAlpha_30))
            .setBlurRadius(blurRadius())
    }

    /**
     * generate blur background and include content into that
     * @param container View?
     * @return ViewGroup?
     */
    private fun blurInjector(container: View?): ViewGroup {
        val blurContainer = BlurView(requireContext())
        blurContainer.id = BLUR_ID
        blurContainer.addView(container)
        return blurContainer
    }

    /**
     * return blur radius to make your background blur as you can!
     * value must be  (0 < value < 25)
     * @return Float
     */
    fun blurRadius(): Float = DEFAULT_BLUR_RADIUS

    /**
     * if you have live background that means something that is moving while you are showing this Dialog Fragment
     * then you should return {@link Boolean#true} otherwise {@link Boolean#false}
     * @return Boolean
     */
    fun liveBackground(): Boolean = DEFAULT_BLUR_LIVE

}

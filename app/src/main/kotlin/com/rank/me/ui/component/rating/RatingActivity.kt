package com.rank.me.ui.component.rating

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.pearltools.commons.activities.CustomizationActivity
import com.pearltools.commons.activities.ManageBlockedNumbersActivity
import com.pearltools.commons.dialogs.ConfirmationDialog
import com.pearltools.commons.extensions.baseConfig
import com.pearltools.commons.extensions.launchViewIntent
import com.pearltools.commons.helpers.APP_ICON_IDS
import com.pearltools.commons.helpers.APP_LAUNCHER_NAME
import com.pearltools.reviewratings.BarLabels
import com.rank.me.R
import com.rank.me.databinding.ActivityRatingBinding
import com.rank.me.ui.base.SimpleActivity
import java.util.*
import kotlin.math.abs


class RatingActivity : SimpleActivity(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var binding: ActivityRatingBinding

    private  val  PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private  val   PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private  val  ALPHA_ANIMATIONS_DURATION              = 200;

    private var mIsTheTitleVisible          = false;
    private var mIsTheTitleContainerVisible = true;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.main_toolbar))
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        binding.mainAppbar.addOnOffsetChangedListener(this)
        startAlphaAnimation(binding.settings, 0, View.INVISIBLE)
        startAlphaAnimation(binding.edit, 0, View.INVISIBLE)
        startAlphaAnimation(binding.title, 0, View.INVISIBLE)
        binding.handlers = Handlers(this@RatingActivity)
        handleRatingSection()
    }

    private fun handleRatingSection() {
        // Have to use default color scheme from module cuz java - kotlin  issues
        val minValue = 30

        val raters = intArrayOf(
            minValue + Random().nextInt(100 - minValue + 1),
            minValue + Random().nextInt(100 - minValue + 1),
            minValue + Random().nextInt(100 - minValue + 1),
            minValue + Random().nextInt(100 - minValue + 1),
            minValue + Random().nextInt(100 - minValue + 1)
        )

        binding.ratingLyt.ratingReviews.createRatingBars(100, BarLabels.STYPE1, raters);
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        val maxScroll = appBarLayout!!.totalScrollRange
        val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()
        handleAlphaOnTitle(percentage)
        handleToolbarTitleVisibility(percentage)
        handleScrollViewMargin(percentage)
    }

    private fun handleScrollViewMargin(percentage: Float) {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, (1.7 * percentage * 100).toInt(), 0, 0);
        binding.mainLyt.layoutParams = params
    }

    private fun handleToolbarTitleVisibility(percentage: Float) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!mIsTheTitleVisible) {
                startAlphaAnimation(binding.settings, ALPHA_ANIMATIONS_DURATION.toLong(), View.VISIBLE)
                startAlphaAnimation(binding.edit, ALPHA_ANIMATIONS_DURATION.toLong(), View.VISIBLE)
                startAlphaAnimation(binding.title, ALPHA_ANIMATIONS_DURATION.toLong(), View.VISIBLE)
                mIsTheTitleVisible = true
            }
        } else {
            if (mIsTheTitleVisible) {
                startAlphaAnimation(binding.settings, ALPHA_ANIMATIONS_DURATION.toLong(), View.INVISIBLE)
                startAlphaAnimation(binding.edit, ALPHA_ANIMATIONS_DURATION.toLong(), View.INVISIBLE)
                startAlphaAnimation(binding.title, ALPHA_ANIMATIONS_DURATION.toLong(), View.INVISIBLE)
                mIsTheTitleVisible = false
            }
        }
    }

    private fun handleAlphaOnTitle(percentage: Float) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(binding.mainLinearlayoutTitle, ALPHA_ANIMATIONS_DURATION.toLong(), View.INVISIBLE)
                mIsTheTitleContainerVisible = false
            }
        } else {
            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(binding.mainLinearlayoutTitle, ALPHA_ANIMATIONS_DURATION.toLong(), View.VISIBLE)
                mIsTheTitleContainerVisible = true
            }
        }
    }

    fun startAlphaAnimation(v: View, duration: Long, visibility: Int) {
        val alphaAnimation = if (visibility == View.VISIBLE) AlphaAnimation(0f, 1f) else AlphaAnimation(1f, 0f)
        alphaAnimation.duration = duration
        alphaAnimation.fillAfter = true
        v.startAnimation(alphaAnimation)
    }

    class Handlers(private val activity: RatingActivity) {
        fun onRatingsClicked(view: View) {
            activity.startActivity(Intent(activity, RatingActivity::class.java))
        }
        fun onClickUpgrade(view: View) {
            when (view.id) {
                R.id.blocking -> activity.startActivity(Intent(activity, ManageBlockedNumbersActivity::class.java))
                R.id.inbox_clean -> Toast.makeText(activity, "todo: make clean activity", Toast.LENGTH_SHORT).show()
                R.id.who_viewed -> Toast.makeText(activity, "todo: make who viewed activity", Toast.LENGTH_SHORT).show()
                R.id.notifications -> Toast.makeText(activity, "todo: make notifications activity", Toast.LENGTH_SHORT).show()
                R.id.change_theme -> startCustomizationActivity()
            }
        }

            fun startCustomizationActivity() {
                if (activity.packageName.contains("emknarmoc".reversed(), true)) {
                    if (activity.baseConfig.appRunCount > 100) {
                        val label = "You are using a fake version of the app. For your own safety download the original one from www.pearlorganisation.com. Thanks"
                        ConfirmationDialog(activity, label, positive = R.string.ok, negative = 0) {
                            activity.launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
                        }
                        return
                    }
                }

                Intent(activity, CustomizationActivity::class.java).apply {
                    putExtra(APP_ICON_IDS, activity.getAppIconIDs())
                    putExtra(APP_LAUNCHER_NAME, activity.getAppLauncherName())
                    activity.startActivity(this)
            }
        }
    }
}

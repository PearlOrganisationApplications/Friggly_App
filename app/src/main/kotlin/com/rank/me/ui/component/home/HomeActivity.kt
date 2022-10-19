package com.rank.me.ui.component.home

import android.os.Bundle
import android.os.Handler
import android.view.ContextThemeWrapper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rank.me.R
import com.rank.me.databinding.ActivityHomeBinding
import com.rank.me.ui.base.SimpleActivity
import com.simplemobiletools.commons.models.SimpleContact

class HomeActivity : SimpleActivity() {
    private lateinit var navController:NavController
    private lateinit var sharedViewModel: HomeViewModel
    var cachedContacts = ArrayList<SimpleContact>()
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_home)
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController=navHostFragment.navController
        val bottomNavigationView=findViewById<BottomNavigationView>(R.id.navigation_bar)
        setupWithNavController(bottomNavigationView,navController)
        setSupportActionBar(binding.toolbar.getToolBar)
        binding.toolbar.searchView.setFactory { TextView(ContextThemeWrapper(this@HomeActivity, R.style.ToolbarTextStyle), null, 0) }
        changeSearchText()
        binding.toolbar.searchView.setOnClickListener { showSearch() }
    }

    private fun showSearch() {
//        TODO("Not yet implemented")
    }

    private fun changeSearchText() {
        binding.toolbar.searchView.setText(resources.getString(R.string.app_name))
        Handler().postDelayed({ setTextSecond() }, 4000)
    }
    private fun setTextSecond() {
        val inAnim = AnimationUtils.loadAnimation(this,
            android.R.anim.slide_in_left)
        val outAnim = AnimationUtils.loadAnimation(this,
            android.R.anim.slide_out_right)
        inAnim.duration = 700
        outAnim.duration = 700
        binding.toolbar.searchView.inAnimation = inAnim
        binding.toolbar.searchView.outAnimation = outAnim

        binding.toolbar.searchView.setText("Search for calls, sms, people and more...")
    }

    fun cacheContacts(contacts: List<SimpleContact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (e: Exception) {
        }
    }

}

package com.rank.me.ui.component.home

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pearltools.commons.models.SimpleContact
import com.rank.me.ui.component.profile.ProfileActivity
import com.rank.me.R
import com.rank.me.data.local.LocalData
import com.rank.me.databinding.ActivityHomeBinding
import com.rank.me.message.activities.SearchActivity
import com.rank.me.ui.base.SimpleActivity
import android.util.Pair as UtilPair

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
        setSupportActionBar(binding.getToolBar)
        changeSearchText()
        binding.searchView.setOnClickListener { showSearch() }
        binding.imageView.setOnClickListener{ showProfile() }
    }

    private fun showProfile() {
        Log.e("TAG", "showProfile: ${LocalData(this@HomeActivity).getUser().data.toString()}", )
        val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showSearch() {
        val intent = Intent(this@HomeActivity, SearchActivity::class.java).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val options = ActivityOptions.makeSceneTransitionAnimation(this,
            UtilPair.create(binding.searchView, "agreedName1"),
            UtilPair.create(binding.imageView, "agreedName2"))
        startActivity(intent, options.toBundle())
        binding.searchView.text = resources.getString(R.string.app_name)
    }

    override fun onResume() {
        super.onResume()
        changeSearchText()
    }

    private fun changeSearchText() {
        binding.searchView.text = resources.getString(R.string.app_name)
        Handler().postDelayed({ setTextSecond() }, 3000)
    }

    private fun setTextSecond() {
        val inAnim = AnimationUtils.loadAnimation(this,
            android.R.anim.slide_in_left)
        val outAnim = AnimationUtils.loadAnimation(this,
            android.R.anim.slide_out_right)
        inAnim.duration = 700
        outAnim.duration = 700
        binding.searchView.text = "Search for calls, sms, people and more..."
    }

    fun cacheContacts(contacts: List<SimpleContact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (e: Exception) {
        }
    }

}

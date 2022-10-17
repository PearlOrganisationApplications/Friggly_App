package com.rank.me.ui.component.home

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.MenuProvider
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

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                Log.e("TAG", "onMenuItemSelected: $menuItem")
                    // Handle the menu selection
                    return when (menuItem.itemId) {
                        R.id.search -> {
                            Toast.makeText(this@HomeActivity, "Menu call search", Toast.LENGTH_SHORT ).show()
                            // clearCompletedTasks()
                            true
                        }
                        R.id.sort -> {
                            Toast.makeText(this@HomeActivity, "Menu call sort", Toast.LENGTH_SHORT ).show()
                            // loadTasks(true)
                            true
                        }
                        R.id.create_new_contact -> {
                            Toast.makeText(this@HomeActivity, "create_new_contact", Toast.LENGTH_SHORT ).show()
                            // loadTasks(true)
                            true
                        }
                        R.id.clear_call_history -> {
                            Toast.makeText(this@HomeActivity, "clear_call_history", Toast.LENGTH_SHORT ).show()
                            // loadTasks(true)
                            true
                        }
                        R.id.settings -> {
                            Toast.makeText(this@HomeActivity, "Menu call settings", Toast.LENGTH_SHORT ).show()
                            // loadTasks(true)
                            true
                        }
                        R.id.about -> {
                            Toast.makeText(this@HomeActivity, "Menu call about", Toast.LENGTH_SHORT ).show()
                            // loadTasks(true)
                            true
                        }
                        else -> false
                    }
                // Handle the menu selection
                return true
            }
        })

    }

    fun cacheContacts(contacts: List<SimpleContact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (e: Exception) {
        }
    }

}

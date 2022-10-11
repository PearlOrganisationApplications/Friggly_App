package com.rank.me.ui.component.home

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rank.me.R
import com.rank.me.ui.base.SimpleActivity
import com.rank.me.ui.component.home.message.OrderViewModel

class HomeActivity : SimpleActivity() {
    private lateinit var navController:NavController
    private lateinit var sharedViewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController=navHostFragment.navController
        val bottomNavigationView=findViewById<BottomNavigationView>(R.id.navigation_bar)
        setupWithNavController(bottomNavigationView,navController)

    }
}

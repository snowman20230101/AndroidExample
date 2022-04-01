package com.windy.libralive.ui

import com.windy.libralive.databinding.ActivityMainBinding;

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.windy.libralive.R
import com.windy.libralive.base.BaseActivity

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val topLevelDestinationIds = setOf<Int>(
            R.id.navigation_home,
            R.id.navigation_shop,
            R.id.navigation_msg,
            R.id.navigation_me
        )

        supportActionBar?.hide()

        val appBarConfiguration = AppBarConfiguration(topLevelDestinationIds)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }
}
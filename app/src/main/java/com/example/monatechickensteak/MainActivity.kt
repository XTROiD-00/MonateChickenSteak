package com.example.monatechickensteak

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.monatechickensteak.data.CartManager
import com.example.monatechickensteak.data.SessionManager
import com.example.monatechickensteak.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionManager.init(this)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Handle bottom navigation explicitly so guest users cannot enter a fake profile.
        binding.bottomNav.setOnItemSelectedListener { item ->
            val destination = when (item.itemId) {
                R.id.profileFragment -> if (SessionManager.isLoggedIn()) R.id.profileFragment else R.id.registerFragment
                R.id.ordersFragment -> if (SessionManager.isLoggedIn()) R.id.ordersFragment else R.id.loginFragment
                R.id.cartFragment -> R.id.cartFragment
                R.id.menuFragment -> R.id.menuFragment
                else -> R.id.homeFragment
            }
            val currentId = navController.currentDestination?.id
            if (currentId != destination && navController.graph.findNode(destination) != null) {
                // Treat bottom tabs as top-level destinations. Remove nested destinations
                // (including Menu) from the active stack before switching tabs.
                val options = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(false)
                    .setPopUpTo(R.id.homeFragment, false)
                    .build()
                navController.navigate(destination, null, options)
            }
            true
        }

        // Hide the bottom bar on the login and register screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hide = destination.id == R.id.loginFragment || destination.id == R.id.registerFragment
            binding.bottomNav.visibility = if (hide) View.GONE else View.VISIBLE
            // Keep the selected tab in sync when navigation is triggered from a screen.
            val selected = when (destination.id) {
                R.id.menuFragment -> R.id.menuFragment
                R.id.cartFragment -> R.id.cartFragment
                R.id.ordersFragment -> R.id.ordersFragment
                R.id.profileFragment -> R.id.profileFragment
                else -> R.id.homeFragment
            }
            if (!hide && binding.bottomNav.selectedItemId != selected) binding.bottomNav.menu.findItem(selected)?.isChecked = true
        }

        // Red badge on the Cart tab showing how many items are in the cart
        CartManager.items.observe(this) {
            val count = CartManager.itemCount()
            val badge = binding.bottomNav.getOrCreateBadge(R.id.cartFragment)
            badge.isVisible = count > 0
            badge.number = count
        }
    }
}
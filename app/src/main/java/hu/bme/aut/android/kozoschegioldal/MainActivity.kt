package hu.bme.aut.android.kozoschegioldal

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import hu.bme.aut.android.kozoschegioldal.databinding.ActivityMainBinding
import hu.bme.aut.android.kozoschegioldal.fragment.HomeFragmentDirections
import hu.bme.aut.android.kozoschegioldal.fragment.LoginFragmentDirections
import hu.bme.aut.android.kozoschegioldal.helper.observeOnce
import hu.bme.aut.android.kozoschegioldal.service.NotificationFirebaseMessagingService
import hu.bme.aut.android.kozoschegioldal.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationFirebaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment), binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_create_post -> {
                    findNavController(binding.navHostFragment.id).navigate(HomeFragmentDirections.actionLoggedInFragmentToCreatePostFragment())
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_chat -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    findNavController(binding.navHostFragment.id).navigate(HomeFragmentDirections.actionHomeFragmentToChatListFragment(authViewModel.getOwnUserLiveData().value!!))
                    true
                }
                R.id.nav_logout -> {
                    authViewModel.logout()
                    true
                } else -> {
                    false
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.toolbar.visibility = View.GONE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                R.id.createPostFragment, R.id.chatListFragment, R.id.chatMessageListFragment, R.id.createGroupFragment -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
        }

        authViewModel.getOwnUserLiveData().observeOnce(this, { user ->
            if (user != null) {
                val header = binding.navView.getHeaderView(0)
                header.findViewById<TextView>(R.id.tvProfileName).text = user.displayName
            }
        })

        authViewModel.getLoggedOutLiveData().observe(this, { loggedOut ->
            if (loggedOut) {
                findNavController(binding.navHostFragment.id).navigate(NavGraphDirections.actionGlobalLoginFragment())
            }
        })
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(binding.navHostFragment.id).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
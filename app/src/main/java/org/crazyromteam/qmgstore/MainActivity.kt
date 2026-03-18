package org.crazyromteam.qmgstore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.crazyromteam.qmgstore.ui.home.HomeFragment
import org.crazyromteam.qmgstore.ui.hotPics.HotPicsFragment
import org.crazyromteam.qmgstore.ui.menu.MenuFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // Cache fragments to prevent unnecessary re-creations and re-renders on tab switch.
    // This improves performance by maintaining fragment state and avoiding repeated layout inflations.
    private lateinit var homeFragment: Fragment
    private lateinit var hotPicsFragment: Fragment
    private lateinit var menuFragment: Fragment

    private lateinit var activeFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.tabs_bottomnav)

        if (savedInstanceState == null) {
            // First launch: create and add fragments
            homeFragment = HomeFragment()
            hotPicsFragment = HotPicsFragment()
            menuFragment = MenuFragment()

            supportFragmentManager.beginTransaction().apply {
                add(R.id.nav_host_fragment, menuFragment, "menu").hide(menuFragment)
                add(R.id.nav_host_fragment, hotPicsFragment, "hotPics").hide(hotPicsFragment)
                add(R.id.nav_host_fragment, homeFragment, "home")
                commit()
            }
            activeFragment = homeFragment
        } else {
            // Restored from configuration change: find existing fragments
            homeFragment = supportFragmentManager.findFragmentByTag("home") ?: HomeFragment()
            hotPicsFragment = supportFragmentManager.findFragmentByTag("hotPics") ?: HotPicsFragment()
            menuFragment = supportFragmentManager.findFragmentByTag("menu") ?: MenuFragment()

            // Assume the system restored the active state, but set activeFragment to the currently visible one
            activeFragment = if (!hotPicsFragment.isHidden) {
                hotPicsFragment
            } else if (!menuFragment.isHidden) {
                menuFragment
            } else {
                homeFragment
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    switchFragment(homeFragment)
                    true
                }

                R.id.navigation_hot_pics -> {
                    switchFragment(hotPicsFragment)
                    true
                }

                R.id.navigation_menu -> {
                    switchFragment(menuFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit()
            activeFragment = fragment
        }
    }
}

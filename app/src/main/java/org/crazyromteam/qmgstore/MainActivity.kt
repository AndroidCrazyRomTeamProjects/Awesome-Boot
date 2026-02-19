package org.crazyromteam.qmgstore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.crazyromteam.qmgstore.ui.home.HomeFragment
import org.crazyromteam.qmgstore.ui.hotPics.HotPicsFragment
import org.crazyromteam.qmgstore.ui.menu.MenuFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.tabs_bottomnav)

        // Load default fragment
        replaceFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.navigation_hot_pics -> {
                    replaceFragment(HotPicsFragment())
                    true
                }

                R.id.navigation_menu -> {
                    replaceFragment(MenuFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}

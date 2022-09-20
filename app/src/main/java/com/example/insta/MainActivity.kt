package com.example.insta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.insta.fragments.HomeFragment
import com.example.insta.fragments.NotificationsFragment
import com.example.insta.fragments.ProfileFragment
import com.example.insta.fragments.SearchFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    moveToFragment(HomeFragment())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.Search -> {
                    moveToFragment(SearchFragment())
                    return@OnNavigationItemSelectedListener true
                }

                R.id.notification -> {
                    moveToFragment(NotificationsFragment())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.Profile -> {
                    moveToFragment(ProfileFragment())
                    return@OnNavigationItemSelectedListener true
                }
            }


            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fabBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
        }

        val navView: BottomNavigationView = findViewById(R.id.bottom_nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        moveToFragment(HomeFragment())


    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }

}
package com.icode.rentcar

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*
import com.icode.rentcar.ui.AddVehicleFragment
import com.icode.rentcar.ui.TrackCarsFragment

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.nav_add_car -> AddVehicleFragment()
            R.id.nav_track_cars -> TrackCarsFragment()
            R.id.nav_rent_car -> null // Display `RentCarFragment`
            R.id.nav_find_car_lot -> null // Display `FindCarLotFragment`
            R.id.nav_all_business -> null // Display `BusinessFragment`
            R.id.nav_my_business -> null // Display `BusinessFragment` filtered by user
            else -> null
        }

        fragment?.let { replaceFragment(it) }
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .commit()
    }
}

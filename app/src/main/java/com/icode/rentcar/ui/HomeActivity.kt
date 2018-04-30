package com.icode.rentcar.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.icode.rentcar.R
import com.icode.rentcar.getUserType
import com.icode.rentcar.isUserAdmin
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*

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
    showHome()
  }

  private fun showHome() {
    val itemId = if (isUserAdmin(getUserType())) R.id.nav_all_business else R.id.nav_search_car
    renderFragment(itemId)
  }

  override fun onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START)
    } else {
      super.onBackPressed()
    }
  }

  private fun renderFragment(itemId: Int) {
    val (fragment, title) = when (itemId) {
      R.id.nav_add_car -> AddVehicleFragment() to "Agregar Vehiculo"
      R.id.nav_track_cars -> TrackCarsFragment() to "Monitorear Vehiculos"
      R.id.nav_search_car -> SearchVehicleFragment() to "Buscar Vehiculo"
      R.id.nav_find_car_lot -> null to "Ver Parqueos"
      R.id.nav_all_business -> ReservationsFragment() to "Reservaciones"
      R.id.nav_my_business -> ReservationsFragment() to "Mis Reservaciones"
      else -> null to null
    }

    if (fragment is Fragment) {
      replaceFragment(fragment)
    }

    if (title is String) {
      supportActionBar?.title = title
    }
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    renderFragment(item.itemId)
    drawerLayout.closeDrawer(GravityCompat.START)

    return true
  }

  private fun replaceFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.formContainer, fragment)
        .commit()
  }
}

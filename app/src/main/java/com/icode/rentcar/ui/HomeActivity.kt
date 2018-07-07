package com.icode.rentcar.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.icode.rentcar.*
import com.icode.rentcar.models.Reservation
import com.icode.rentcar.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import kotlinx.android.synthetic.main.toolbar.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
  private val db = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()

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
    updateUserInfo()
  }

  private fun updateHeaderViewUserInfo() {
    auth.currentUser?.let { user ->
      val headerView = navView.getHeaderView(0)

      with(headerView) {
        userNameText.text = user.displayName ?: "Anónimo"
        userEmailText.text = user.email ?: "Desconocido"
        Picasso.get().load(user.photoUrl).into(userProfileImage)
      }

      navView.menu.findItem(R.id.adminGroup).setVisible(isUserAdmin(getUserType()))
    }
  }

  private fun updateUserInfo() {
    auth.currentUser?.let { user ->
      val uid = user.uid

      db.collection("users")
          .document(uid)
          .get()
          .addOnCompleteListener {
            var userType = USER_TYPE_CLIENT
            var userName = ""

            if (it.isSuccessful && it.result.exists()) {
              userType = it.result.toObject(User::class.java)?.type ?: USER_TYPE_CLIENT
            } else {
              userName = user.displayName ?: "Anónimo"
              val email = user.email ?: "Desconocido"
              val photoUrl = user.photoUrl.toString()

              db.collection("users")
                  .document(uid)
                  .set(
                      User(
                          uid = uid,
                          name = userName,
                          email = email,
                          photoUrl = photoUrl,
                          type = userType
                      ),
                      SetOptions.merge()
                  )
            }

            setPreferences {
              putInt("user_type", userType)
              putString("user_id", uid)
              putString("user_name", userName)
            }

            showHome(userType)
            updateHeaderViewUserInfo()
          }
    }
  }

  private fun showHome(userType: Int) {
    val itemId = if (isUserAdmin(userType)) R.id.nav_all_business else R.id.nav_search_car
    renderFragment(itemId)
  }

  override fun onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START)
    } else {
      AlertDialog.Builder(this)
          .setTitle("Rent Car")
          .setMessage("Desea salir de la aplicación?")
          .setPositiveButton("Si") { dialog, _ ->
            super.onBackPressed()
          }.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
          }.show()
    }
  }

  private fun renderFragment(itemId: Int) {
    val (fragment, title) = when (itemId) {
      R.id.nav_add_car -> AddVehicleFragment() to "Agregar Vehiculo"
      R.id.nav_track_cars -> TrackCarsFragment.getInstance(TrackCarsFragment.LOCATION_TYPE_CARS) to "GPS de los Vehiculos"
      R.id.nav_search_car -> SearchVehicleFragment() to "Buscar Vehiculo"
      R.id.nav_find_car_lot -> TrackCarsFragment.getInstance(TrackCarsFragment.LOCATION_TYPE_LOTS) to "Parqueos Disponibles"
      R.id.nav_all_business -> ReservationsFragment.getInstance(Role.ADMIN) to "Reservaciones"
      R.id.nav_my_business -> ReservationsFragment.getInstance(Role.CLIENT) to "Mis Reservaciones"
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
    if (item.itemId == R.id.nav_log_out) {
      FirebaseAuth.getInstance().signOut()
      startActivity(Intent(this, LoginActivity::class.java))
    }

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

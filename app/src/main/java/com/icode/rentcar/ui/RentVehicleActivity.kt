package com.icode.rentcar.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.icode.rentcar.models.Vehicle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_rent_vehicle.*
import android.view.MenuItem
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.icode.rentcar.*
import com.icode.rentcar.models.Reservation

class RentVehicleActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rent_vehicle)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val vehicle = intent.getParcelableExtra<Vehicle>("vehicle")

    with(vehicle) {
      Picasso.get().load(imageUrl).into(vehicleImage)
      vehicleDescription.text = getDescription()
    }

    reserveButton.setOnClickListener {
      mainContainer.visibility = View.GONE
      progressBar.visibility = View.VISIBLE

      val reservation = Reservation(
          vehicleId = vehicle.id,
          imageUrl = vehicle.imageUrl,
          dealerId = vehicle.dealerId,
          make = vehicle.make,
          color = vehicle.color,
          year = vehicle.year,
          userId = getUserId(),
          userName = getUserName()
      )

      db.collection("reservations")
          .add(reservation)
          .addOnCompleteListener {
            if (it.isSuccessful) {
              toast("Su reservación fue exitosa")
              updateVehicleField(reservation.vehicleId, "status", "true") {
                updateReservationField(it.result.id, "id", it.result.id) {
                  finish()
                }
              }
            } else {
              toast("La reservación no fue guardada, por favor intente mas tarde")
            }
          }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> onBackPressed().run { true }
      else -> super.onOptionsItemSelected(item)
    }
  }

  companion object {
    fun getIntent(context: Context, vehicle: Vehicle): Intent {
      return Intent(context, RentVehicleActivity::class.java).apply {
        putExtra("vehicle", vehicle)
      }
    }
  }
}

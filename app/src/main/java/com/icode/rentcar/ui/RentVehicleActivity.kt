package com.icode.rentcar.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
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
  val totalMapping = mutableMapOf<String, Int>()
  var total = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rent_vehicle)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val vehicle = intent.getParcelableExtra<Vehicle>("vehicle")

    with(vehicle) {
      Picasso.get().load(imageUrl).into(vehicleImage)
      vehicleDescription.text = getDescription()
    }

    daysToRentField.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {}

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        s?.let {
          totalMapping["days"] = vehicle.price * s.toString().toInt()
        }

        updateTotal()
      }
    })

    listOf(rimsCheckBox, hidCheckBox, musicCheckBox).forEach {
      it.setOnCheckedChangeListener { buttonView, isChecked ->
        val id = buttonView.id

        if (isChecked) {
          val amount = when (buttonView.id) {
            rimsCheckBox.id -> 300
            musicCheckBox.id -> 500
            hidCheckBox.id -> 200
            else -> 0
          }

          totalMapping[id.toString()] = amount
        } else {
          totalMapping[id.toString()] = 0
        }

        updateTotal()
      }
    }

    reserveButton.setOnClickListener {
      if (daysToRentField.text.isNotEmpty()) {
        AlertDialog.Builder(this)
            .setTitle("Reservación")
            .setMessage("Desea hacer esta reservación?")
            .setPositiveButton("Si") { dialog, _ ->
              val reservation = Reservation(
                  vehicleId = vehicle.id,
                  imageUrl = vehicle.imageUrl,
                  dealerId = vehicle.dealerId,
                  make = vehicle.make,
                  color = vehicle.color,
                  year = vehicle.year,
                  userId = getUserId(),
                  userName = getUserName(),
                  total = total
              )

              db.collection("reservations")
                  .add(reservation)
                  .addOnCompleteListener {
                    if (it.isSuccessful) {
                      toast("Su reservación fue exitosa")
                      val reservationId = it.result.id

                      updateVehicleField(reservation.vehicleId, "status", "true") {
                        updateReservationField(reservationId, "id", reservationId) {
                          finish()
                        }
                      }
                    } else {
                      toast("La reservación no fue guardada, por favor intente mas tarde")
                    }
                  }
            }
            .setNegativeButton("No") { dialog, _ ->
              dialog.dismiss()
            }.show()
      } else {
        toast("Debe registrar dias por los que rentará el carro")
      }
    }
  }

  private fun updateTotal() {
    total = totalMapping.map { it.value }.sum()
    reservationTotal.text = "Total: $$total"
  }

  override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
    android.R.id.home -> onBackPressed().run { true }
    else -> super.onOptionsItemSelected(item)
  }

  companion object {
    fun getIntent(context: Context, vehicle: Vehicle) = Intent(context, RentVehicleActivity::class.java).apply {
      putExtra("vehicle", vehicle)
    }
  }
}

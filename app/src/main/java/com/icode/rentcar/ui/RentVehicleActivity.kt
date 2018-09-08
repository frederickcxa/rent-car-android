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
import java.util.*
import android.app.DatePickerDialog
import android.widget.AdapterView
import java.text.SimpleDateFormat


class RentVehicleActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()
  val totalMapping = mutableMapOf<String, Long>()
  private var total: Long = 0
  private val myCalendar = Calendar.getInstance()

  var date: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
    // TODO Auto-generated method stub
    myCalendar.set(Calendar.YEAR, year)
    myCalendar.set(Calendar.MONTH, monthOfYear)
    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    updateLabel()
  }

  private fun updateLabel() {
    pickDateText.text = SimpleDateFormat("dd/mm/yyyy").format(Date(myCalendar.timeInMillis))
  }

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
          if (s.isNotEmpty()) {
            totalMapping["days"] = vehicle.price * s.toString().toLong()
          }
        }

        updateTotal()
      }
    })

    val rimsUrl = "https://www.tirebuyer.com/medias/sys_master/h09/h34/9171220758558.jpg"
    val hidUrl = "https://sc02.alicdn.com/kf/HTB1zVyDdk.HL1JjSZFlq6yiRFXak/Hotsale-35W-55W-HID-Xenon-Kit-55W.jpg_350x350.jpg"

    Picasso.get().load(rimsUrl).into(rimsImage)
    Picasso.get().load(hidUrl).into(hidImage)

    listOf(rimsCheckBox, hidCheckBox, musicCheckBox).forEach {
      it.setOnCheckedChangeListener { buttonView, isChecked ->
        val id = buttonView.id

        if (id == musicCheckBox.id) {
          if (isChecked) {
            totalMapping[id.toString()] = 500
          } else {
            totalMapping[id.toString()] = 0
          }
        }


        when (id) {
          R.id.hidCheckBox -> {
            val isVisible = if (isChecked) View.VISIBLE else View.GONE
            hidSpinner.visibility = isVisible
            hidImage.visibility = isVisible

            if (!isChecked) {
              totalMapping[id.toString()] = 0
            }
          }
          R.id.rimsCheckBox -> {
            val isVisible = if (isChecked) View.VISIBLE else View.GONE
            rimsSpinner.visibility = isVisible
            rimsImage.visibility = isVisible

            if (!isChecked) {
              totalMapping[id.toString()] = 0
            }
          }
        }

        updateTotal()
      }
    }

    pickerButton.setOnClickListener {
      DatePickerDialog(this, date, myCalendar
          .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
          myCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    reserveButton.setOnClickListener {
      if (daysToRentField.text.isNotEmpty() && pickDateText.text.toString() != "No ha seleccionado una fecha") {
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
                  total = total,
                  pickUpDate = pickDateText.text.toString()
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
        toast("Debe registrar dias por los que rentará el carro y desde cuando")
      }
    }

    hidSpinner.adapter = getSpinnerAdapter(HIDS.keys.toList())
    rimsSpinner.adapter = getSpinnerAdapter(RIMS.keys.toList())

    listOf(hidSpinner, rimsSpinner).forEach {
      it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          if (it.id == R.id.rimsSpinner) {
            RIMS[rimsSpinner.selectedItem.toString()]?.let { (url, price) ->
              Picasso.get().load(url).into(rimsImage)
              totalMapping[rimsCheckBox.id.toString()] = price.toLong()
              updateTotal()
            }
          } else if (it.id == R.id.hidSpinner) {
            HIDS[hidSpinner.selectedItem.toString()]?.let { (url, price) ->
              Picasso.get().load(url).into(hidImage)
              totalMapping[hidCheckBox.id.toString()] = price.toLong()
              updateTotal()
            }
          }
        }
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

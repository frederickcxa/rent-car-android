package com.icode.rentcar.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.icode.rentcar.*
import com.icode.rentcar.models.Vehicle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_filter_cars.view.*
import kotlinx.android.synthetic.main.item_vehicle.view.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_search_vehicle.*

private const val TAG = "SearchVehicleFragment"

class SearchVehicleFragment : Fragment() {
  private val db = FirebaseFirestore.getInstance()
  private val vehicles = arrayListOf<Vehicle>()
  private lateinit var vehicleAdapter: VehicleAdapter
  private lateinit var filterDialog: View
  private lateinit var safeContext: Context

  override fun onAttach(context: Context) {
    super.onAttach(context)
    safeContext = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
      inflater.inflate(R.layout.fragment_search_vehicle, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    vehicleAdapter = VehicleAdapter { showCarDetail(it) }
    filterDialog = layoutInflater.inflate(R.layout.dialog_filter_cars, null)
    val dialog = AlertDialog.Builder(view.context)
        .setView(filterDialog)
        .create()

    with(filterDialog) {
      yearSpinner.adapter = getSpinnerAdapter(YEARS)
      colorSpinner.adapter = getSpinnerAdapter(COLORS)
      makeSpinner.adapter = getSpinnerAdapter(MAKES.keys.toList())

      modelSpinner.adapter = getSpinnerAdapter(listOf("Seleciona una marca primero"))
      modelSpinner.isEnabled = false

      makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          MAKES[makeSpinner.selectedItem.toString()]?.let {
            modelSpinner.isEnabled = position > 0
            modelSpinner.adapter = getSpinnerAdapter(it.toList())
            modelSpinner.setSelection(0)
          }

          if (position == 0) modelSpinner.adapter = getSpinnerAdapter(listOf("Seleciona una marca primero"))
        }
      }

      applyFiltersButton.setOnClickListener {
        filterVehicles()
        dialog.dismiss()
      }
    }

    with(filterVehiclesButton) {
      isEnabled = false
      setOnClickListener {
        dialog.show()
      }
    }

    with(vehiclesRecyclerView) {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = vehicleAdapter
    }
  }

  override fun onResume() {
    super.onResume()
    loadData()
  }

  private fun loadData() {
    showView(progressBar)
    db.collection("vehicles")
        .whereEqualTo("status", "false")
        .get()
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            vehicles.clear()
            task.result.map { it.toObject(Vehicle::class.java) }.also {
              vehicles.addAll(it)
              vehicleAdapter.render(vehicles)
              showView(emptyView, vehicles.isEmpty())
              filterVehiclesButton.isEnabled = vehicles.isNotEmpty()
            }
          } else {
            Log.d(TAG, "Error getting documents: ", task.exception)
          }

          showView(progressBar, false)
        }
  }

  private fun showCarDetail(vehicle: Vehicle) {
    val intent = RentVehicleActivity.getIntent(safeContext, vehicle)

    startActivity(intent)
  }

  private fun filterVehicles() {
    with(filterDialog) {
      val color = colorSpinner.selectedItem.toString()
      val year = yearSpinner.selectedItem.toString()
      val make = makeSpinner.selectedItem.toString()

      val filteredVehicles = vehicles.filter {
        val colorMatches = if (color != DEFAULT_OPTION) color == it.color else true
        val yearMatches = if (year != DEFAULT_OPTION) year == it.year else true
        val makeMatches = if (make != DEFAULT_OPTION) make == it.make else true

        colorMatches && yearMatches && makeMatches
      }

      vehicleAdapter.render(filteredVehicles)
      showView(emptyView, filteredVehicles.isEmpty())
    }
  }

  inner class VehicleAdapter(val onItemClick: (Vehicle) -> Unit) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {
    private var data: List<Vehicle> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)

      return VehicleViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
      holder.bind(data[position])
    }

    fun render(newData: List<Vehicle>) {
      data = newData
      notifyDataSetChanged()
    }

    inner class VehicleViewHolder(item: View) : RecyclerView.ViewHolder(item) {
      private val image = item.vehicleImage
      private val description = item.reservationDescription

      fun bind(item: Vehicle) {
        with(item) {
          itemView.setOnClickListener { onItemClick(this) }
          Picasso.get().load(imageUrl).into(image)
          description.text = getDescription()
        }
      }
    }
  }
}

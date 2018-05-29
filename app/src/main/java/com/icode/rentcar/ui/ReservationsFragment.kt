package com.icode.rentcar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.icode.rentcar.*
import com.icode.rentcar.models.Reservation
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_reservations.*
import kotlinx.android.synthetic.main.item_reservation.view.*
import java.io.Serializable

private const val TAG = "ReservationsFragment"

enum class Role : Serializable {
  ADMIN, CLIENT
}

class ReservationsFragment : Fragment() {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var reservationAdapter: ReservationAdapter
  private lateinit var listenerRegistration: ListenerRegistration
  private lateinit var role: Role

  companion object {
    fun getInstance(role: Role): ReservationsFragment {
      return ReservationsFragment().apply { arguments = Bundle().apply { putSerializable("role", role) } }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    role = arguments?.getSerializable("role") as? Role ?: Role.CLIENT
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
      inflater.inflate(R.layout.fragment_reservations, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    reservationAdapter = ReservationAdapter().apply {
      if (isUserAdmin(view.context.getUserType()) && role == Role.ADMIN) {
        onClick = { reservationId, vehicleId ->
          AlertDialog.Builder(view.context)
              .setTitle("Despachar Reservación")
              .setMessage("Desea despachar esta reservacióm?")
              .setPositiveButton("Si") { dialog, _ ->
                dialog.dismiss()
                updateReservationField(reservationId, "status", "S")
                updateVehicleField(vehicleId, "status", "false")
              }
              .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
              }.show()
        }
      }
    }

    emptyView.text = if (role == Role.ADMIN) "No hay reservaciones pendientes" else "No tienes reservaciones pendientes"

    with(reservationsRecycler) {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = reservationAdapter
    }
  }

  override fun onResume() {
    super.onResume()
    loadData()
  }

  private fun loadData() {
    showView(progressBar)
    listenerRegistration = db.collection("reservations").run {
      context?.let {
        if (isUserAdmin(it.getUserType())) {
          this
        } else {
          whereEqualTo("userId", it.getUserId())
        }
      } ?: this
    }.whereEqualTo("status", "P").addSnapshotListener { values, error ->
      if (error != null) return@addSnapshotListener

      values?.map { it.toObject(Reservation::class.java) }?.also { reservations ->
        reservationAdapter.render(reservations)
        showView(emptyView, reservations.isEmpty())
      }

      showView(progressBar, false)
    }
  }

  override fun onStop() {
    super.onStop()
    listenerRegistration.remove()
  }

  inner class ReservationAdapter(var onClick: (id: String, vehicleId: String) -> Unit = { _, _ -> }) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {
    private var data: List<Reservation> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
      val item = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)

      return ReservationViewHolder(item)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
      holder.bind(data[position])
    }

    fun render(newData: List<Reservation>) {
      data = newData
      notifyDataSetChanged()
    }

    inner class ReservationViewHolder(item: View) : RecyclerView.ViewHolder(item) {
      private val image = item.vehicleImage
      private val description = item.reservationDescription

      fun bind(reservation: Reservation) {
        with(reservation) {
          Picasso.get().load(imageUrl).into(image)
          description.text = getDescription()
          itemView.setOnClickListener { onClick(id, vehicleId) }
        }
      }
    }
  }
}

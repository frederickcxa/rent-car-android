package com.icode.rentcar.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.icode.rentcar.*
import com.icode.rentcar.models.Reservation
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_reservations.*
import kotlinx.android.synthetic.main.item_reservation.view.*

private const val TAG = "ReservationsFragment"

class ReservationsFragment : Fragment() {
  private val db = FirebaseFirestore.getInstance()
  lateinit var reservationAdapter: ReservationAdapter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
      inflater.inflate(R.layout.fragment_reservations, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    reservationAdapter = ReservationAdapter()

    with(reservationsRecycler) {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = reservationAdapter
    }

    loadData()
  }

  private fun loadData() {
    showView(progressBar)
    db.collection("reservations").run {
      context?.let {
        if (isUserAdmin(it.getUserType())) this else whereEqualTo("userId", it.getUserId())
      } ?: this
    }
        .get()
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            task.result.map { it.toObject(Reservation::class.java) }.also { reservations ->
              reservationAdapter.render(reservations)
              showView(emptyView, reservations.isEmpty())
            }
          } else {
            Log.d(TAG, "Error getting documents: ", task.exception)
          }

          showView(progressBar, false)
        }
  }

  inner class ReservationAdapter : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {
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

    inner class ReservationViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
      private val image = item.vehicleImage
      private val description = item.reservationDescription

      fun bind(reservation: Reservation) {
        with(reservation) {
          Picasso.get().load(imageUrl).into(image)
          description.text = getDescription()
        }
      }
    }
  }
}

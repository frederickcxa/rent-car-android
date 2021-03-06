package com.icode.rentcar.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*

import com.icode.rentcar.R
import com.icode.rentcar.models.MapPosition
import com.google.android.gms.maps.model.LatLngBounds


private const val TAG = "TrackCarsFragment"
private const val PARAM_LOCATION_TYPE = "PARAM_LOCATION_TYPE"

class TrackCarsFragment : Fragment(), OnMapReadyCallback {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var googleMap: GoogleMap
  private lateinit var registration: ListenerRegistration

  companion object {
    const val LOCATION_TYPE_CARS = 1
    const val LOCATION_TYPE_LOTS = 2

    fun getInstance(locationToDisplay: Int) = TrackCarsFragment().apply {
      arguments = Bundle().apply { putInt(PARAM_LOCATION_TYPE, locationToDisplay) }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View =
      inflater.inflate(R.layout.fragment_track_cars, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
  }

  override fun onMapReady(map: GoogleMap) {
    googleMap = map
    getPositions()
  }

  private fun getPositions() {
    val locationType = arguments?.get(PARAM_LOCATION_TYPE) ?: LOCATION_TYPE_CARS
    val collection = if (locationType == LOCATION_TYPE_CARS) "carPositions" else "lotsPositions"

    val query = db.collection(collection)
    registration = query.addSnapshotListener { value, error ->
      if (error != null) return@addSnapshotListener

      googleMap.clear()

      value?.map { it.toObject(MapPosition::class.java) }?.map { mapPosition ->
        val position = LatLng(mapPosition.latitude.toDouble(), mapPosition.longitude.toDouble())
        val markerOptions = MarkerOptions().apply {
          position(position)
          title(mapPosition.title)
        }

        val marker = googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(position))


        marker
      }.also { markers ->
        markers?.let {
          val builder = LatLngBounds.Builder()

          for (marker in it) {
            builder.include(marker.position)
          }

          val cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 200)

          googleMap.moveCamera(cu)
        }
      }
    }
  }

  override fun onDetach() {
    super.onDetach()
    registration.remove()
  }
}

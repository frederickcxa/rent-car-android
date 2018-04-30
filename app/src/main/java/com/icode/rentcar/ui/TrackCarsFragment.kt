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

import com.icode.rentcar.R

class TrackCarsFragment : Fragment(), OnMapReadyCallback {

  private lateinit var googleMap: GoogleMap

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View =
      inflater.inflate(R.layout.fragment_track_cars, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)
  }

  override fun onMapReady(googleMap: GoogleMap) {
    with(googleMap) {

      val sydney = LatLng(-34.0, 151.0)
      addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
      moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
  }
}

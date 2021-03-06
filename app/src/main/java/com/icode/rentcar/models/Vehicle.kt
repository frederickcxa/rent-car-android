package com.icode.rentcar.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Vehicle(
    var id: String = "",
    var make: String = "",
    var color: String = "",
    var year: String = "",
    var imageUrl: String = "",
    var dealerId: String = "",
    var status: String = "false",
    var price: Int = 0
) : Parcelable {
  fun getDescription() = "$make $year $color $${price}- $id"
}

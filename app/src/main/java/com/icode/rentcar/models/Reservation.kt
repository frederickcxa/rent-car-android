package com.icode.rentcar.models

data class Reservation(
    var id: String = "",
    var vehicleId: String = "",
    var make: String = "",
    var color: String = "",
    var year: String = "",
    var imageUrl: String = "",
    var dealerId: String = "",
    var userId: String = "",
    var userName: String = ""
) {
  fun getDescription() = "$userName - $make $year $color"

}
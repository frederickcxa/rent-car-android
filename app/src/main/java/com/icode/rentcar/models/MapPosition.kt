package com.icode.rentcar.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MapPosition(
    var id: String = "",
    var longitude: Float = 0.0F,
    var latitude: Float = 0.0F,
    var title: String = "",
    var icon: Int? = null
) : Parcelable

package com.icode.rentcar

const val USER_TYPE_ADMIN = 1
const val USER_TYPE_CLIENT = 2

const val TAG = "AddVehicleFragment"
const val DEFAULT_OPTION = ""
val COLORS = listOf(DEFAULT_OPTION).plus(
        listOf(
                "Negro",
                "Amarillo",
                "Azul",
                "Rojo",
                "Gris",
                "Blanco",
                "Mamey"
        ).sorted()
)

val MAKES = listOf(DEFAULT_OPTION).plus(
        listOf(
                "Toyota",
                "Honda",
                "Mazda",
                "Nissan",
                "Chevrolet"
        ).sorted()
)

val YEARS = arrayListOf(DEFAULT_OPTION).plus((1990..2018).map(Int::toString)).toList()

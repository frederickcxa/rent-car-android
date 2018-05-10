package com.icode.rentcar


// User Types
const val USER_TYPE_ADMIN = 1
const val USER_TYPE_CLIENT = 2

const val DEFAULT_OPTION = "Selecciona un"
val COLORS = listOf("$DEFAULT_OPTION color").plus(
    sortedSetOf(
        "Negro",
        "Amarillo",
        "Azul",
        "Rojo",
        "Gris",
        "Blanco",
        "Mamey"
    )
)

val MAKES = mapOf("${DEFAULT_OPTION}a marca" to sortedSetOf("")).plus(
    sortedMapOf(
        "Toyota" to sortedSetOf("Corolla", "Camry"),
        "Honda" to sortedSetOf("Civic", "Accord"),
        "Mazda" to sortedSetOf("Demio"),
        "Nissan" to sortedSetOf("March", "Murano"),
        "Chevrolet" to sortedSetOf("Spark")
    )
)

val YEARS = arrayListOf("$DEFAULT_OPTION año").plus((1990..2018)
    .map(Int::toString))
    .toList()

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

val RIMS = mapOf(
    "Standard" to Pair("https://www.tirebuyer.com/medias/sys_master/h09/h34/9171220758558.jpg", 300),
    "Full" to Pair("https://www.tirebuyer.com/medias/sys_master/h09/h34/9171220758558.jpg", 400)
)

val HIDS = mapOf(
    "Standard" to Pair("https://sc02.alicdn.com/kf/HTB1zVyDdk.HL1JjSZFlq6yiRFXak/Hotsale-35W-55W-HID-Xenon-Kit-55W.jpg_350x350.jpg", 100),
    "Full" to Pair("https://sc02.alicdn.com/kf/HTB1zVyDdk.HL1JjSZFlq6yiRFXak/Hotsale-35W-55W-HID-Xenon-Kit-55W.jpg_350x350.jpg", 200)
)
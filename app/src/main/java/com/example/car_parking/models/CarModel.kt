package com.example.car_parking.models

data class CarModel(
    val carId: String = "",
    val userId: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val licensePlate: String = "",
    val color: String = "",
    val fuelType: String = "",
    val photoUrl: String = "",
    val notes: String = "",
    val driverName: String = "",
    val phoneNumber: String = "",
    val isParked: Boolean = false,
    val slotId: String = "",
    val entryDateTime: Long = 0L,
    val createdAt: Long = 0L
)

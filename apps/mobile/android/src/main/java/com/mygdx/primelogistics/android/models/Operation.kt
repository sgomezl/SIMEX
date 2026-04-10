package com.mygdx.primelogistics.android.models

data class Operation {
    val id: Int,
    val originPortId: String,
    val destinationPortId: String,
    val totalCost: Double,
    val etd: Date,
    val eta: Date,
    val IncortermId: Int,
    val pkgNumber: Int,
    val totalWeight: Double
}


package com.mygdx.primelogistics.android.models

import java.time.LocalDateTime

data class Operation (
    val id: Int,
    val originPort: String,
    val destinationPort: String,
    val totalCost: Double,
    val etd: LocalDateTime,
    val eta: LocalDateTime,
    val incotermId: Int,
    val pkgQuantity: Int,
    val totalWeight: Double
)

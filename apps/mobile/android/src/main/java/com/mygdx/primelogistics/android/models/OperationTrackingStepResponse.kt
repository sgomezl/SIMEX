package com.mygdx.primelogistics.android.models

data class OperationTrackingStepResponse(
    val message: String?,
    val operationId: Int,
    val trackingFlowId: Int?,
    val currentTrackingStepId: Int?,
    val currentTrackingStepName: String?,
    val currentTrackingStepOrder: Int?,
    val currentTrackingStepUiPercent: Int?,
    val currentTrackingStepArrivedAt: String?
)

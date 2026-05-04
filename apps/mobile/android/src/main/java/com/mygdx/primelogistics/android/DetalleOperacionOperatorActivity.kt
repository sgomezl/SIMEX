package com.mygdx.primelogistics.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mygdx.primelogistics.R
import com.mygdx.primelogistics.android.api.RetrofitClient
import com.mygdx.primelogistics.android.models.AdvanceOperationTrackingStepRequest
import com.mygdx.primelogistics.android.models.Operation
import com.mygdx.primelogistics.android.utils.HomeNavigator
import com.mygdx.primelogistics.android.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalleOperacionOperatorActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var tvOperationReference: TextView
    private lateinit var tvOrigen: TextView
    private lateinit var tvDestination: TextView
    private lateinit var tvIncoterm: TextView
    private lateinit var tvEstado: TextView
    private lateinit var etDescripcion: EditText
    private lateinit var btnAvanzarEstado: Button

    private var currentOperation: Operation? = null
    private var operationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_operacion_operator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivityOperacionOperator)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        RetrofitClient.init { sessionManager.getAccessToken() }

        bindViews()
        bindActions()
        loadOperationFromIntent()
    }

    private fun bindViews() {
        tvOperationReference = findViewById(R.id.tvOperationReference)
        tvOrigen = findViewById(R.id.tvOrigen)
        tvDestination = findViewById(R.id.tvDestination)
        tvIncoterm = findViewById(R.id.tvIncoterm)
        tvEstado = findViewById(R.id.tvEstado)
        etDescripcion = findViewById(R.id.tvDescripcionn)
        btnAvanzarEstado = findViewById(R.id.btnAvanzarEstado)
    }

    private fun bindActions() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { HomeNavigator.navigateToHome(this) }
        findViewById<ImageButton>(R.id.btnUser).setOnClickListener {
            startActivity(Intent(this, UsuarioActivity::class.java))
        }
        findViewById<Button>(R.id.btnVolve).setOnClickListener { finish() }

        btnAvanzarEstado.setOnClickListener {
            if (operationId <= 0) {
                Toast.makeText(this, "No se pudo identificar la operacion.", Toast.LENGTH_SHORT).show()
            } else {
                advanceTrackingStep()
            }
        }
    }

    private fun loadOperationFromIntent() {
        operationId = intent.getIntExtra(EXTRA_OPERATION_ID, -1)

        val hasFullPayload = intent.hasExtra(EXTRA_ORDER_REFERENCE)
        if (hasFullPayload) {
            currentOperation = Operation(
                id = operationId,
                orderReference = intent.getStringExtra(EXTRA_ORDER_REFERENCE).orEmpty(),
                originPortName = intent.getStringExtra(EXTRA_ORIGIN).orEmpty(),
                destinationPortName = intent.getStringExtra(EXTRA_DESTINATION).orEmpty(),
                totalCost = 0.0,
                etd = "",
                eta = "",
                incotermCode = intent.getStringExtra(EXTRA_INCOTERM_CODE).orEmpty(),
                piecesNumber = null,
                kilograms = 0.0,
                statusName = intent.getStringExtra(EXTRA_STATUS_NAME),
                trackingFlowId = intent.getIntExtra(EXTRA_TRACKING_FLOW_ID, -1).takeIf { it > 0 },
                trackingFlowName = null,
                currentTrackingFlowStepId = intent.getIntExtra(EXTRA_TRACKING_STEP_ID, -1).takeIf { it > 0 },
                currentTrackingStepName = intent.getStringExtra(EXTRA_TRACKING_STEP_NAME),
                currentTrackingStepOrder = intent.getIntExtra(EXTRA_TRACKING_STEP_ORDER, -1).takeIf { it > 0 },
                currentTrackingStepUiPercent = intent.getIntExtra(EXTRA_TRACKING_STEP_UI_PERCENT, -1).takeIf { it >= 0 },
                currentTrackingStepArrivedAt = intent.getStringExtra(EXTRA_TRACKING_STEP_ARRIVED_AT)
            )
            renderOperation(currentOperation)
        } else {
            fetchOperation()
        }
    }

    private fun fetchOperation() {
        if (operationId <= 0) {
            Toast.makeText(this, "No se recibio ninguna operacion.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getUserOperations()

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful -> {
                            val operation = response.body()?.firstOrNull { it.id == operationId }
                            if (operation == null) {
                                Toast.makeText(
                                    this@DetalleOperacionOperatorActivity,
                                    "No se encontro la operacion seleccionada.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                currentOperation = operation
                                renderOperation(operation)
                            }
                        }

                        response.code() == 401 -> {
                            Toast.makeText(
                                this@DetalleOperacionOperatorActivity,
                                "Tu sesion ha expirado.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        else -> {
                            Toast.makeText(
                                this@DetalleOperacionOperatorActivity,
                                "No se pudo cargar la operacion.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DetalleOperacionOperatorActivity,
                        "Sin conexion: verificar internet o estado del servidor.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun renderOperation(operation: Operation?) {
        if (operation == null) {
            return
        }

        tvOperationReference.text = operation.orderReference.ifBlank { "Sin referencia" }
        tvOrigen.text = operation.originPortName.ifBlank { "Sin origen" }
        tvDestination.text = operation.destinationPortName.ifBlank { "Sin destino" }
        tvIncoterm.text = operation.incotermCode.ifBlank { "Sin incoterm" }
        tvEstado.text = resolveStateLabel(operation)
        restoreAdvanceButton()
    }

    private fun resolveStateLabel(operation: Operation): String {
        val stepName = operation.currentTrackingStepName?.trim().orEmpty()
        if (stepName.isNotBlank()) {
            return stepName
        }

        val statusName = operation.statusName?.trim().orEmpty()
        if (statusName.isNotBlank()) {
            return statusName
        }

        return "Sin tracking"
    }

    private fun advanceTrackingStep() {
        setAdvanceLoading(true)

        val observations = etDescripcion.text?.toString()?.trim()?.takeIf { it.isNotBlank() }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.advanceOperationTrackingStep(
                    operationId = operationId,
                    request = AdvanceOperationTrackingStepRequest(observations = observations)
                )

                withContext(Dispatchers.Main) {
                    setAdvanceLoading(false)

                    when {
                        response.isSuccessful -> {
                            val body = response.body()
                            currentOperation = currentOperation?.copy(
                                trackingFlowId = body?.trackingFlowId ?: currentOperation?.trackingFlowId,
                                currentTrackingFlowStepId = body?.currentTrackingStepId
                                    ?: currentOperation?.currentTrackingFlowStepId,
                                currentTrackingStepName = body?.currentTrackingStepName
                                    ?: currentOperation?.currentTrackingStepName,
                                currentTrackingStepOrder = body?.currentTrackingStepOrder
                                    ?: currentOperation?.currentTrackingStepOrder,
                                currentTrackingStepUiPercent = body?.currentTrackingStepUiPercent
                                    ?: currentOperation?.currentTrackingStepUiPercent,
                                currentTrackingStepArrivedAt = body?.currentTrackingStepArrivedAt
                                    ?: currentOperation?.currentTrackingStepArrivedAt
                            )
                            renderOperation(currentOperation)
                            etDescripcion.text?.clear()
                            setResult(RESULT_OK)
                            Toast.makeText(
                                this@DetalleOperacionOperatorActivity,
                                body?.message ?: "Tracking avanzado correctamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        response.code() == 400 -> {
                            val message = extractServerMessage(response.errorBody()?.string())
                                ?: "No se pudo avanzar el tracking."
                            if (message.contains("ultimo paso", ignoreCase = true)) {
                                btnAvanzarEstado.isEnabled = false
                                btnAvanzarEstado.text = "TRACKING COMPLETADO"
                            }
                            Toast.makeText(this@DetalleOperacionOperatorActivity, message, Toast.LENGTH_SHORT).show()
                        }

                        response.code() == 401 -> {
                            Toast.makeText(
                                this@DetalleOperacionOperatorActivity,
                                "Tu sesion ha expirado.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        response.code() == 404 -> {
                            Toast.makeText(
                                this@DetalleOperacionOperatorActivity,
                                "No se encontro la operacion.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this@DetalleOperacionOperatorActivity,
                                "No se pudo avanzar el tracking.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    setAdvanceLoading(false)
                    Toast.makeText(
                        this@DetalleOperacionOperatorActivity,
                        "Sin conexion: verificar internet o estado del servidor.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setAdvanceLoading(isLoading: Boolean) {
        btnAvanzarEstado.isEnabled = !isLoading
        etDescripcion.isEnabled = !isLoading
        btnAvanzarEstado.text = if (isLoading) "ACTUALIZANDO..." else "AVANZAR ESTADO"
    }

    private fun restoreAdvanceButton() {
        if (!::btnAvanzarEstado.isInitialized) {
            return
        }

        btnAvanzarEstado.isEnabled = true
        btnAvanzarEstado.text = "AVANZAR ESTADO"
    }

    private fun extractServerMessage(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) {
            return null
        }

        val marker = "\"message\":\""
        val start = rawBody.indexOf(marker)
        if (start == -1) {
            return null
        }

        val from = start + marker.length
        val end = rawBody.indexOf('"', from)
        if (end == -1) {
            return null
        }

        return rawBody.substring(from, end)
            .replace("\\u00f3", "ó")
            .replace("\\u00e1", "á")
            .replace("\\u00e9", "é")
            .replace("\\u00ed", "í")
            .replace("\\u00fa", "ú")
            .replace("\\u00f1", "ñ")
    }

    companion object {
        private const val EXTRA_OPERATION_ID = "extra_operation_id"
        private const val EXTRA_ORDER_REFERENCE = "extra_order_reference"
        private const val EXTRA_ORIGIN = "extra_origin"
        private const val EXTRA_DESTINATION = "extra_destination"
        private const val EXTRA_INCOTERM_CODE = "extra_incoterm_code"
        private const val EXTRA_STATUS_NAME = "extra_status_name"
        private const val EXTRA_TRACKING_FLOW_ID = "extra_tracking_flow_id"
        private const val EXTRA_TRACKING_STEP_ID = "extra_tracking_step_id"
        private const val EXTRA_TRACKING_STEP_NAME = "extra_tracking_step_name"
        private const val EXTRA_TRACKING_STEP_ORDER = "extra_tracking_step_order"
        private const val EXTRA_TRACKING_STEP_UI_PERCENT = "extra_tracking_step_ui_percent"
        private const val EXTRA_TRACKING_STEP_ARRIVED_AT = "extra_tracking_step_arrived_at"

        fun createIntent(context: Context, operation: Operation): Intent {
            return Intent(context, DetalleOperacionOperatorActivity::class.java).apply {
                putExtra(EXTRA_OPERATION_ID, operation.id)
                putExtra(EXTRA_ORDER_REFERENCE, operation.orderReference)
                putExtra(EXTRA_ORIGIN, operation.originPortName)
                putExtra(EXTRA_DESTINATION, operation.destinationPortName)
                putExtra(EXTRA_INCOTERM_CODE, operation.incotermCode)
                putExtra(EXTRA_STATUS_NAME, operation.statusName)
                putExtra(EXTRA_TRACKING_FLOW_ID, operation.trackingFlowId ?: -1)
                putExtra(EXTRA_TRACKING_STEP_ID, operation.currentTrackingFlowStepId ?: -1)
                putExtra(EXTRA_TRACKING_STEP_NAME, operation.currentTrackingStepName)
                putExtra(EXTRA_TRACKING_STEP_ORDER, operation.currentTrackingStepOrder ?: -1)
                putExtra(EXTRA_TRACKING_STEP_UI_PERCENT, operation.currentTrackingStepUiPercent ?: -1)
                putExtra(EXTRA_TRACKING_STEP_ARRIVED_AT, operation.currentTrackingStepArrivedAt)
            }
        }
    }
}

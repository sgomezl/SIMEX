package com.mygdx.primelogistics.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.mygdx.primelogistics.R
import com.mygdx.primelogistics.android.models.Operation
import com.mygdx.primelogistics.android.utils.CountryCoordinateResolver
import com.mygdx.primelogistics.android.utils.MaritimeRoutePlanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalleOperacionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var tvOperationReference: TextView
    private lateinit var tvOrigin: TextView
    private lateinit var tvDestination: TextView

    private var orderReference: String = ""
    private var originName: String = ""
    private var destinationName: String = ""
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_operacion)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivityOperacion)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvOperationReference = findViewById(R.id.tvOperationReference)
        tvOrigin = findViewById(R.id.tvOrigin)
        tvDestination = findViewById(R.id.tvDestination)

        orderReference = intent.getStringExtra(EXTRA_ORDER_REFERENCE).orEmpty().ifBlank { "Sin referencia" }
        originName = intent.getStringExtra(EXTRA_ORIGIN).orEmpty().ifBlank { "Origen desconocido" }
        destinationName = intent.getStringExtra(EXTRA_DESTINATION).orEmpty().ifBlank { "Destino desconocido" }

        tvOperationReference.text = orderReference
        tvOrigin.text = originName
        tvDestination.text = destinationName

        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnUser).setOnClickListener {
            startActivity(Intent(this, UsuarioActivity::class.java))
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        renderApproximateRoute()
    }

    private fun renderApproximateRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            val originLocation = CountryCoordinateResolver.resolveLocation(this@DetalleOperacionActivity, originName)
            val destinationLocation = CountryCoordinateResolver.resolveLocation(this@DetalleOperacionActivity, destinationName)

            withContext(Dispatchers.Main) {
                tvOrigin.text = originLocation?.displayName ?: originName
                tvDestination.text = destinationLocation?.displayName ?: destinationName
                drawRoute(originLocation, destinationLocation)
            }
        }
    }

    private fun drawRoute(
        originLocation: com.mygdx.primelogistics.android.utils.MaritimeLocation?,
        destinationLocation: com.mygdx.primelogistics.android.utils.MaritimeLocation?
    ) {
        val map = googleMap ?: return
        map.clear()

        val originLatLng = originLocation?.latLng
        val destinationLatLng = destinationLocation?.latLng

        if (originLatLng == null && destinationLatLng == null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 1.4f))
            Toast.makeText(
                this,
                "No se pudieron resolver las coordenadas del origen y destino.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (originLatLng != null) {
            map.addMarker(
                MarkerOptions()
                    .position(originLatLng)
                    .title(originLocation?.displayName ?: originName)
            )
        }

        if (destinationLatLng != null) {
            map.addMarker(
                MarkerOptions()
                    .position(destinationLatLng)
                    .title(destinationLocation?.displayName ?: destinationName)
            )
        }

        if (originLatLng != null && destinationLatLng != null && originLatLng != destinationLatLng) {
            val routePoints = if (originLocation != null && destinationLocation != null) {
                MaritimeRoutePlanner.buildRoute(originLocation, destinationLocation)
            } else {
                listOf(originLatLng, destinationLatLng)
            }

            map.addPolyline(
                PolylineOptions()
                    .addAll(routePoints)
                    .geodesic(false)
                    .width(8f)
                    .color(ContextCompat.getColor(this, R.color.destacado))
            )

            val boundsBuilder = LatLngBounds.builder()
            routePoints.forEach(boundsBuilder::include)
            val bounds = boundsBuilder.build()

            map.setOnMapLoadedCallback {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            }
            return
        }

        val fallbackPoint = originLatLng ?: destinationLatLng ?: LatLng(20.0, 0.0)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(fallbackPoint, 4.5f))
    }

    companion object {
        private const val EXTRA_ORDER_REFERENCE = "extra_order_reference"
        private const val EXTRA_ORIGIN = "extra_origin"
        private const val EXTRA_DESTINATION = "extra_destination"

        fun createIntent(context: Context, operation: Operation): Intent {
            return Intent(context, DetalleOperacionActivity::class.java).apply {
                putExtra(EXTRA_ORDER_REFERENCE, operation.orderReference)
                putExtra(EXTRA_ORIGIN, operation.originPortName)
                putExtra(EXTRA_DESTINATION, operation.destinationPortName)
            }
        }
    }
}

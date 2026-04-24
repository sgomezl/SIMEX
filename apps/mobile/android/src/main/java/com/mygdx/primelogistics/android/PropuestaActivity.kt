package com.mygdx.primelogistics.android

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mygdx.primelogistics.R

class PropuestaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_propuesta)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPropuesta)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnRechazar).setOnClickListener {
            showRejectDialog()
        }
    }

    private fun showRejectDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rechazo_propuesta, null)
        val reasonInput = dialogView.findViewById<EditText>(R.id.etMotivoRechazo)
        val confirmCheck = dialogView.findViewById<CheckBox>(R.id.cbConfirmReject)
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirmReject)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        confirmCheck.setOnCheckedChangeListener { _, isChecked ->
            confirmButton.isEnabled = isChecked
        }

        confirmButton.setOnClickListener {
            val rejectReason = reasonInput.text.toString().trim()

            if (rejectReason.isEmpty()) {
                reasonInput.error = "Escribe el motivo del rechazo"
                return@setOnClickListener
            }

            Toast.makeText(this, "Propuesta rechazada", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.82f).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}

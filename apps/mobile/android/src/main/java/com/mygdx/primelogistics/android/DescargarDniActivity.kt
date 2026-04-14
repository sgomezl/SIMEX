package com.mygdx.primelogistics.android

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mygdx.primelogistics.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Socket

class DescargarDniActivity : AppCompatActivity() {
    private lateinit var btnHomeUsuario: ImageButton
    private lateinit var btnDescargarDNI: Button
    private lateinit var btnVolver: Button
    private lateinit var tvUserName: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var tvArchivoDni: TextView
    private var identificationCardPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_descargar_dni)

        identificationCardPath = intent.getStringExtra("identification_card_path") ?: ""
        val userName = intent.getStringExtra("user_name") ?: "Usuario"
        val companyName = intent.getStringExtra("company_name") ?: "Sin empresa"

        defineComponents()
        tvUserName.text = userName
        tvCompanyName.text = companyName
        tvArchivoDni.text = identificationCardPath.ifBlank {
            "No hay documento disponible"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivityDescargarDni)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnHomeUsuario.setOnClickListener {
            volverAUsuario()
        }

        btnVolver.setOnClickListener {
            volverAUsuario()
        }

        btnDescargarDNI.setOnClickListener {
            if (identificationCardPath.isBlank()) {
                tvArchivoDni.text = "No hay documento disponible"
                return@setOnClickListener
            }

            tvArchivoDni.text = "Descargando documento..."

            lifecycleScope.launch {
                val downloadedFile = downloadFileFromServer(identificationCardPath)

                tvArchivoDni.text = if (downloadedFile != null) {
                    "Documento descargado en: ${downloadedFile.name}"
                } else {
                    "Error al descargar el documento"
                }
            }
        }
    }

    private fun defineComponents() {
        btnHomeUsuario = findViewById(R.id.btnHomeUsuario)
        btnDescargarDNI = findViewById(R.id.btnDescargarDNI)
        btnVolver = findViewById(R.id.btnVolver)
        tvUserName = findViewById(R.id.tvUserName)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvArchivoDni = findViewById(R.id.tvArchivoDni)
    }

    private fun volverAUsuario() {
        startActivity(Intent(this, UsuarioActivity::class.java))
        finish()
    }

    private suspend fun downloadFileFromServer(storedFileName: String): File? {
        return withContext(Dispatchers.IO) {
            Socket("10.0.2.2", 5000).use { socket ->
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())

                output.writeUTF("DOWNLOAD")
                output.writeUTF(storedFileName)
                output.flush()

                val exists = input.readBoolean()
                if (!exists) {
                    return@withContext null
                }

                val size = input.readLong()
                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
                val targetFile = File(downloadsDir, storedFileName)

                FileOutputStream(targetFile).use { fos ->
                    var remaining = size
                    while (remaining > 0) {
                        val b = input.read()
                        if (b == -1) {
                            throw IllegalStateException("Stream ended early during download")
                        }
                        fos.write(b)
                        remaining--
                    }
                    fos.flush()
                }

                targetFile
            }
        }
    }
}

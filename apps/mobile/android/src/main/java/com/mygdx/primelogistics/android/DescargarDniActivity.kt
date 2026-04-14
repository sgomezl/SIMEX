package com.mygdx.primelogistics.android

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Socket

class DescargarDniActivity : AppCompatActivity() {
    private lateinit var btnHomeUsuario: ImageButton
    private lateinit var btnDescargarDNI: Button
    private lateinit var btnReuploadDNI: Button
    private lateinit var btnVolver: Button
    private lateinit var tvUserName: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var tvArchivoDni: TextView
    private lateinit var ivDniPreview: ImageView
    private var currentUser: Int = -1
    private var currentUserName: String = ""
    private var currentCompanyName: String = ""
    private var identificationCardPath: String = ""
    private var previewBytes: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_descargar_dni)

        currentUser = intent.getIntExtra("user_id", -1)
        identificationCardPath = intent.getStringExtra("identification_card_path") ?: ""
        currentUserName = intent.getStringExtra("user_name") ?: "Usuario"
        currentCompanyName = intent.getStringExtra("company_name") ?: "Sin empresa"

        defineComponents()
        tvUserName.text = currentUserName
        tvCompanyName.text = currentCompanyName
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

        btnReuploadDNI.setOnClickListener {
            volverASubirDni()
        }

        if (identificationCardPath.isNotBlank()) {
            cargarVistaPrevia()
        }

        btnDescargarDNI.setOnClickListener {
            if (identificationCardPath.isBlank()) {
                tvArchivoDni.text = "No hay documento disponible"
                return@setOnClickListener
            }

            tvArchivoDni.text = "Descargando documento..."

            lifecycleScope.launch {
                val downloadedFile = saveDownloadedFile(
                    identificationCardPath,
                    previewBytes ?: fetchFileBytesFromServer(identificationCardPath)
                )

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
        btnReuploadDNI = findViewById(R.id.btnReuploadDNI)
        btnVolver = findViewById(R.id.btnVolver)
        tvUserName = findViewById(R.id.tvUserName)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvArchivoDni = findViewById(R.id.tvArchivoDni)
        ivDniPreview = findViewById(R.id.ivDniPreview)
    }

    private fun volverAUsuario() {
        startActivity(Intent(this, UsuarioActivity::class.java))
        finish()
    }

    private fun volverASubirDni() {
        val intent = Intent(this, SubirDniActivity::class.java)
        intent.putExtra("user_id", currentUser)
        intent.putExtra("user_name", currentUserName)
        intent.putExtra("company_name", currentCompanyName)
        startActivity(intent)
        finish()
    }

    private fun cargarVistaPrevia() {
        tvArchivoDni.text = "Cargando vista previa..."

        lifecycleScope.launch {
            val fileBytes = fetchFileBytesFromServer(identificationCardPath)
            previewBytes = fileBytes

            if (fileBytes == null) {
                tvArchivoDni.text = "No se pudo cargar la vista previa"
                return@launch
            }

            val bitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.size)
            if (bitmap != null) {
                ivDniPreview.setImageBitmap(bitmap)
                ivDniPreview.visibility = ImageView.VISIBLE
                tvArchivoDni.text = identificationCardPath
            } else {
                tvArchivoDni.text = "Vista previa no disponible"
            }
        }
    }

    private suspend fun fetchFileBytesFromServer(storedFileName: String): ByteArray? {
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
                val buffer = ByteArrayOutputStream()

                var remaining = size
                while (remaining > 0) {
                    val b = input.read()
                    if (b == -1) {
                        throw IllegalStateException("Stream ended early during download")
                    }
                    buffer.write(b)
                    remaining--
                }

                buffer.toByteArray()
            }
        }
    }

    private suspend fun saveDownloadedFile(storedFileName: String, fileBytes: ByteArray?): File? {
        if (fileBytes == null) {
            return null
        }

        return withContext(Dispatchers.IO) {
            val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
            val targetFile = File(downloadsDir, storedFileName)

            FileOutputStream(targetFile).use { fos ->
                fos.write(fileBytes)
                fos.flush()
            }

            targetFile
        }
    }
}

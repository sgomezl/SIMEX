package com.mygdx.primelogistics.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mygdx.primelogistics.R
import com.mygdx.primelogistics.android.api.RetrofitClient
import com.mygdx.primelogistics.android.models.UpdateIdentificationCardPathRequest
import com.mygdx.primelogistics.android.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class SubirDniActivity : AppCompatActivity() {
    private lateinit var btnHomeUsuario: ImageButton
    private lateinit var btnVolver: Button
    private lateinit var btnSubirDNI: ImageButton
    private lateinit var tvUserName: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var tvSelectedFile: TextView
    private var selectedUri: Uri? = null
    private var currentUser: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_subir_dni)
        currentUser = intent.getIntExtra("user_id", -1)
        val userName = intent.getStringExtra("user_name") ?: "Usuario"
        val companyName = intent.getStringExtra("company_name") ?: "Sin empresa"
        defineComponents()
        tvUserName.text = userName
        tvCompanyName.text = companyName

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivitySubirDni)) { v, insets ->
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

        btnSubirDNI.setOnClickListener {
            pickDocument.launch(arrayOf("image/*"))
        }
    }

    private fun defineComponents() {
        btnHomeUsuario = findViewById(R.id.btnHomeUsuario)
        btnVolver = findViewById(R.id.btnVolve)
        btnSubirDNI = findViewById(R.id.btnSubirDNI)
        tvUserName = findViewById(R.id.tvUserName)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvSelectedFile = findViewById(R.id.tvNombreArchivo)

    }

    private fun volverAUsuario() {
        startActivity(Intent(this, UsuarioActivity::class.java))
        finish()
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                fileName = it.getString(nameIndex)
            }
        }

        return fileName
    }

    private val pickDocument = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            tvSelectedFile.text = "Subiendo ${getFileName(uri) ?: "archivo"}..."

            if (currentUser == -1) {
                tvSelectedFile.text = "Usuario invalido"
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                val savedFileName = uploadFileToServer(currentUser, uri)

                if (savedFileName != null) {
                    val updated = updateIdentificationCardPath(savedFileName)

                    if (updated) {
                        tvSelectedFile.text = "Archivo subido correctamente"
                        volverAUsuario()
                    } else {
                        tvSelectedFile.text = "Archivo subido, pero no se pudo guardar la ruta"
                    }
                } else {
                    tvSelectedFile.text = "Error al subir archivo"
                }
            }

        }
    }

    private suspend fun uploadFileToServer(userId: Int, uri: Uri): String? {
        val fileName = getFileName(uri) ?: return null
        val fileBytes = readFileBytes(uri) ?: return null

        return withContext(Dispatchers.IO) {
            Socket("10.0.2.2", 5000).use { socket ->
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())

                output.writeUTF("UPLOAD")
                output.writeInt(userId)
                output.writeUTF(fileName)
                output.writeLong(fileBytes.size.toLong())
                output.write(fileBytes)
                output.flush()

                val response = input.readUTF()
                val savedFileName = input.readUTF()

                if (response == "OK") savedFileName else null
            }
        }
    }

    private fun readFileBytes(uri: Uri): ByteArray? {
        return contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        }
    }

    private suspend fun updateIdentificationCardPath(savedFileName: String): Boolean {
        val token = SessionManager(this).getAccessToken() ?: return false

        val response = RetrofitClient.api.updateIdentificationCardPath(
            authorization = "Bearer $token",
            request = UpdateIdentificationCardPathRequest(savedFileName)
        )

        return response.isSuccessful
    }


}

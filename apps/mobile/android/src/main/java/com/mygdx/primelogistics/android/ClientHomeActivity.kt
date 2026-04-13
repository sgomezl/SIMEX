package com.mygdx.primelogistics.android

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mygdx.primelogistics.R
import com.mygdx.primelogistics.android.models.Operation
import com.mygdx.primelogistics.android.adapters.OpAdapter
import com.mygdx.primelogistics.android.adapters.PropAdapter
import com.mygdx.primelogistics.android.utils.SessionManager

class ClientHomeActivity  : AppCompatActivity() {
    val sessionManager = SessionManager(this)
    private lateinit var txtUserName: TextView
    //private lateinit var recyclerProposals: RecyclerView
    private lateinit var recyclerRecent: RecyclerView
    //private lateinit var adapterProposals: PropAdapter
    private lateinit var adapterRecent: OpAdapter
    private val operations: MutableList<Operation> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_client_home)
        txtUserName = findViewById(R.id.txtUserName)
        txtUserName.text = sessionManager.fetchUserName()

        //recyclerProposals = findViewById(R.id.rvProposals)
        //recyclerProposals.layoutManager = LinearLayoutManager(this)

        recyclerRecent = findViewById(R.id.rvRecent)
        recyclerRecent.layoutManager = LinearLayoutManager(this)

        //adapterProposals = PropAdapter()
        //recyclerProposals.adapter = adapterProposals

        adapterRecent = OpAdapter()
        recyclerRecent.adapter = adapterRecent

        //refrescarLista()


    }
    /*
    private fun refrescarLista() {
        tareas.clear()
        tareas.addAll(datos.listaTareas.filter { it.listaUsuarios.contains(user.Id) })
        adapter.notifyDataSetChanged()
    }
    */
}

package com.mygdx.primelogistics.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mygdx.primelogistics.R
import com.mygdx.primelogistics.android.models.Operation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class opAdapter {
    private val operations: MutableList<Operation>,
    private val onCardClick: (Operation) -> Unit) : RecyclerView.Adapter<TaskAdapter.ViewHolder>()
    {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val opIdentification: TextView = view.findViewById(R.id.op_id)
            val opOrigin: TextView = view.findViewById(R.id.txt_origin)
            val opDestination: TextView = view.findViewById(R.id.txt_destination)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val vista =
                LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
            return ViewHolder(vista)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tarea = tareas[position]
            val fechaInicioInput = LocalDateTime.parse(
                tarea.FechaInicio,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            )
            val fechaFinalInput = LocalDateTime.parse(
                tarea.FechaFinal,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            )
            val fechaInicio = fechaInicioInput.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
            val fechaFinal = fechaFinalInput.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )

            val context = holder.itemView.context

            holder.titulo.text = tarea.Titulo
            holder.estado.text = txtEstado
            holder.inicio.text = fechaInicio
            holder.fin.text = fechaFinal
            holder.itemView.setOnClickListener { onCardClick(tarea) }
        }

        override fun getItemCount(): Int = operations.size
    }
}

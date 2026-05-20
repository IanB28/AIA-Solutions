
package data.firebase.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aia_solutions.R
import data.firebase.model.Turno
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class TurnoAdapter(
    private var lista: List<Turno>
) : RecyclerView.Adapter<TurnoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNumero: TextView  = view.findViewById(R.id.txtNumeroTurno)
        val txtIniciales: TextView = view.findViewById(R.id.txtIniciales)
        val txtNombre: TextView  = view.findViewById(R.id.txtNombreCliente)
        val txtEstado: TextView  = view.findViewById(R.id.txtEstadoTurno)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_turno, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val turno = lista[position]
        val db    = FirebaseFirestore.getInstance()

        holder.txtNumero.text = "#${turno.number}"
        holder.txtEstado.text = "En espera · posición ${position + 1}"

        // Buscar nombre del cliente
        if (turno.userId.isNotEmpty()) {
            db.collection("users").document(turno.userId).get()
                .addOnSuccessListener { userDoc ->
                    val nombre   = userDoc.getString("name") ?: "Cliente"
                    val iniciales = nombre.take(2).uppercase()
                    holder.txtNombre.text   = nombre
                    holder.txtIniciales.text = iniciales
                }
        } else {
            holder.txtNombre.text    = "Cliente #${turno.number}"
            holder.txtIniciales.text = "#${turno.number}"
        }
    }

    fun actualizarLista(nuevaLista: List<Turno>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
package data.firebase.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aia_solutions.R

import data.firebase.model.Business

class BusinessAdapter(
    private var lista: List<Business>,
    private val onClick: (Business) -> Unit
) : RecyclerView.Adapter<BusinessAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nombre: TextView      = view.findViewById(R.id.txtNombre)
        val description: TextView = view.findViewById(R.id.txtDescription)
        val status: TextView      = view.findViewById(R.id.txtStatus)
        val category: TextView    = view.findViewById(R.id.txtCategory)
        val icon: ImageView = view.findViewById(R.id.imgIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_business, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val business = lista[position]
        val ctx = holder.itemView.context

        holder.nombre.text      = business.name
        holder.description.text = business.description
        holder.category.text    = business.category


        if (business.isActive) {
            holder.status.text = "Activo"
            holder.status.setBackgroundResource(R.drawable.bg_badge_active)
            holder.status.setTextColor(ContextCompat.getColor(ctx, R.color.green_text))
        } else {
            holder.status.text = "Inactivo"
            holder.status.setBackgroundResource(R.drawable.bg_badge_inactive)
            holder.status.setTextColor(ContextCompat.getColor(ctx, R.color.red_text))
        }

        // Ícono según categoría
        val iconRes = when (business.category.lowercase()) {
            "salud"       -> R.drawable.ic_health
            "restaurante" -> R.drawable.ic_restaurant
            "tienda"      -> R.drawable.ic_store
            "servicios"   -> R.drawable.ic_service
            else          -> R.drawable.ic_business
        }
        holder.icon.setImageResource(iconRes)

        holder.itemView.setOnClickListener { onClick(business) }
    }

    fun actualizarLista(nuevaLista: List<Business>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
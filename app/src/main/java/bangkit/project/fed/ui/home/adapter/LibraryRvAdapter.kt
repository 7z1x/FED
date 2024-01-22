package bangkit.project.fed.ui.home.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import bangkit.project.fed.R
import bangkit.project.fed.data.EggData
import bangkit.project.fed.data.api.FirestoreHelper
import com.bumptech.glide.Glide

class LibraryRvAdapter(private val context: Context, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<LibraryRvAdapter.ViewHolder>() {

    private val listEgg = mutableListOf<EggData>()

    interface OnItemClickListener {
        fun onItemClick(data: EggData)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val label: TextView = itemView.findViewById(R.id.label)
        val date: TextView = itemView.findViewById(R.id.date)
        val menu: ImageView = itemView.findViewById(R.id.menu)
    }

    fun submitList(newList: List<EggData>) {
        listEgg.clear()
        listEgg.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardlibrary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listEgg[position]
        holder.apply {
            Glide.with(context)
                .load(data.imageURL)
                .into(image)

            label.text = data.label
            date.text = data.timestamp?.toDate().toString()
            menu.setOnClickListener {
                showPopupMenu(itemView, data, position)
            }

            itemView.setOnClickListener {
                itemClickListener.onItemClick(data)
            }
        }
    }

    private fun showPopupMenu(view: View, data: EggData, position: Int) {
        val popupMenu = PopupMenu(view.context, view, Gravity.END)
        popupMenu.inflate(R.menu.librarymenu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    // Tambahkan logika untuk mengedit data jika diperlukan
                    true
                }
                R.id.menu_delete -> {
                    val firestore = FirestoreHelper()
                    firestore.deleteData(data.label, data.userId, data.timestamp) {
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Success Delete Data", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun getItemCount(): Int {
        return listEgg.size
    }
}
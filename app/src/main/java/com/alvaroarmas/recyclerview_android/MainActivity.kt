package com.alvaroarmas.recyclerview_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dataset = mutableListOf<Device>(Device("A1:B2:C3:D4:E5:F6", "AirPods"),
                                        Device("A2:B3:C4:D5:E6:F7", "Xiaomi"),
                                        Device("A3:B4:C5:D6:E7:F8", "DELL"),
                                        Device("A4:B5:C6:D7:E8:F9", "Lenovo"),
                                        Device("A5:B6:C7:D8:E9:F0", "Wireless Mouse"))
        val customAdapter = CustomAdapter(dataset) { position ->
            AlertDialog.Builder(this).setTitle("Device details").setMessage("Name: ${dataset.get(position).name}\nMac: ${dataset.get(position).mac}").show()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView?.setLayoutManager(lm)
        recyclerView?.setAdapter(customAdapter)

        val button = findViewById<Button>(R.id.button2)
        button.setOnClickListener {
            runOnUiThread {
                dataset.add(Device(generateMac(), generateName()))
                recyclerView.adapter?.notifyItemInserted(dataset.size - 1)

                }
            }


    }
}
fun generateMac(): String {
    return List(6) {
        (0..255).random().toString(16).padStart(2, '0')
    }.joinToString(":").uppercase()
}

fun generateName(): String {
    return "Device${(Math.random() * 2000).toInt()}"
}
class Device(val mac: String, val name: String)
{

}
class CustomAdapter(private val dataSet: MutableList<Device>, private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textView)
        }
    }
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = "${dataSet[position].name}\n\t\t${dataSet[position].mac}"

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
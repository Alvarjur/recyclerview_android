package com.alvaroarmas.recyclerview_android

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.bluetooth.BluetoothManager
import androidx.annotation.RequiresPermission
import kotlin.text.clear


class MainActivity : AppCompatActivity(), BLEconnDialog.BLEConnectionCallback {
    companion object {
        var dataset = mutableListOf<BluetoothDevice>()
        lateinit var bleDialog: BLEconnDialog
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestBluetoothPermissionAndUpdate()


        val customAdapter = CustomAdapter(dataset) { position ->
            showBLEDialog(dataset[position])

            //AlertDialog.Builder(this).setTitle("Device details").setMessage("Name: $position.").show()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView?.setLayoutManager(lm)
        recyclerView?.setAdapter(customAdapter)

        val button = findViewById<Button>(R.id.button2)
        button.setOnClickListener {
            runOnUiThread {
                updatePairedDevices()


                }
            }



    }

    override fun onConnectionSuccess(gatt: BluetoothGatt) {
        runOnUiThread {
            Toast.makeText(this, "Connectat amb èxit!", Toast.LENGTH_SHORT).show()
            // Aquí pots fer operacions amb el gatt connectat
            // Per exemple: llegir/escribre característiques
        }
    }

    override fun onConnectionFailed(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Error de connexió: $error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onConnectionCancelled() {
        runOnUiThread {
            Toast.makeText(this, "Connexió cancel·lada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReceivedImage(file: File) {
        runOnUiThread {
            val filename = file.name
            Toast.makeText(this, "Imatge rebuda: $filename", Toast.LENGTH_SHORT).show()
        }
    }

    // DIALOG : cridar aquesta funció per mostrar-lo
////////////////////////////////////////////////
    private fun showBLEDialog(device: BluetoothDevice) {
        bleDialog = BLEconnDialog(this, device, this)
        bleDialog?.apply {
            setCancelable(false)
            setOnCancelListener {
                onConnectionCancelled()
            }
            show()
        }
    }


    private val REQUEST_CODE_BLUETOOTH = 100 // es pot posar un nombre aleatori no emprat en cap altre lloc

    private fun requestBluetoothPermissionAndUpdate() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requereix BLUETOOTH_CONNECT
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            // Versions anteriors
            Manifest.permission.BLUETOOTH
        }

        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED) {

            // Demanar el permís
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                REQUEST_CODE_BLUETOOTH
            )
        } else {
            // Permís ja concedit - llegir dispositius
            updatePairedDevices()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
                // Permís concedit - llegir dispositius
                updatePairedDevices()
            } else {
                // Permís denegat
                Toast.makeText(this, "Permís necessari per a llegir Bluetooth",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun updatePairedDevices() {
        // empty list
        dataset.clear()

        // update list
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        for( elem in bluetoothAdapter.bondedDevices.filter { device ->
            // Filtrar per dispositius BLE
            device.type == BluetoothDevice.DEVICE_TYPE_LE ||
                    device.type == BluetoothDevice.DEVICE_TYPE_DUAL ||
                    device.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN
        } ) {
            // afegim element al dataset
            dataset.add( elem )
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
class CustomAdapter(private val dataSet: MutableList<BluetoothDevice>, private val onItemClick: (Int) -> Unit) :
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


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = "${dataSet[position].name}"

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}


package com.example.knockitriderapp.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.util.Locale

class PermissionActivity : AppCompatActivity() {

    val ACCESS_FINE_LOCATION = 1
    val REQUEST_PERMISSION_SETTING = 12
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var nameEmailDialog: Dialog
    lateinit var locationTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationTextView = findViewById(R.id.location_text)

        var sh: SharedPreferences = getSharedPreferences("Address", MODE_PRIVATE)!!
        var address: String = sh.getString("address", "").toString();
        locationTextView.text = address

        ////////////////loading dialog
        nameEmailDialog = Dialog(this)
        nameEmailDialog.setContentView(R.layout.dialog_name_email)
        nameEmailDialog.setCancelable(false)
        nameEmailDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        var name: EditText = nameEmailDialog.findViewById(R.id.name)!!
        var email: EditText = nameEmailDialog.findViewById(R.id.email)!!
        var submitBtn: AppCompatButton = nameEmailDialog.findViewById(R.id.submit_btn)!!
        ////////////////loading dialog

        FirebaseFirestore.getInstance().collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .get()
            .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                val model: RiderModel? = documentSnapshot.toObject(RiderModel::class.java)

                if (model?.name.equals("")) {
                    nameEmailDialog.show()
                } else {

                    if (ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (!model?.drivingLicence.equals("")) {
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        } else {
                            val intent = Intent(
                                this@PermissionActivity,
                                MainActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        ActivityCompat.requestPermissions(
                            this@PermissionActivity,
                            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                            ACCESS_FINE_LOCATION
                        )
                    }

                }
            })


        submitBtn.setOnClickListener {
            if (!name.text.isEmpty()) {
                if (!email.text.toString().isEmpty()) {
                    nameEmailDialog.dismiss()
                    val userData: MutableMap<String, Any?> = HashMap()
                    userData["name"] = name.text.toString()
                    userData["email"] = email.text.toString()

                    FirebaseFirestore.getInstance()
                        .collection("USERS")
                        .document(FirebaseAuth.getInstance().uid.toString())
                        .update(userData)
                        .addOnCompleteListener() { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Profile Updated Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                if (ContextCompat.checkSelfPermission(
                                        applicationContext,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val intent =
                                        Intent(this@PermissionActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    ActivityCompat.requestPermissions(
                                        this@PermissionActivity,
                                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                                        ACCESS_FINE_LOCATION
                                    )
                                }

                            }
                        }

                } else {
                    email.error = "Enter Email"
                    name.setText("")
                }
            } else {
                name.error = "Enter Name"
                name.setText("")
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            for (i in permissions.indices) {
                val per = permissions[i]
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    val showretional = shouldShowRequestPermissionRationale(per!!)
                    if (!showretional) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("App Permission")
                            .setMessage(
                                """
                            For showing product, You nust allow to access location on your device
                            
                            Now follow the below steps
                            
                            Open setting for the bellow button
                            Click on permission
                            Allow access to for location
                            """.trimIndent()
                            )
                            .setPositiveButton(
                                "Open settings"
                            ) { dialogInterface, i ->
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts(
                                    "package",
                                    packageName, null
                                )
                                intent.data = uri
                                startActivityForResult(
                                    intent,
                                    REQUEST_PERMISSION_SETTING
                                )
                            }.create().show()
                    } else {
                        ActivityCompat.requestPermissions(
                            this@PermissionActivity, arrayOf<String>(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), ACCESS_FINE_LOCATION
                        )
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                try {
                                    val geocoder =
                                        Geocoder(this@PermissionActivity, Locale.getDefault())
                                    val addresses = geocoder.getFromLocation(
                                        location.latitude,
                                        location.longitude,
                                        1
                                    )

                                    //////// Update Address
                                    val userData: MutableMap<String, Any?> = HashMap()
                                    userData["city"] = addresses!![0].locality
                                    userData["country"] = addresses!![0].countryName
                                    userData["state"] = addresses!![0].adminArea
                                    userData["pincode"] = addresses!![0].postalCode
                                    userData["address"] = addresses!![0].getAddressLine(0)
                                    userData["latitude"] = addresses!![0].latitude
                                    userData["longitude"] = addresses!![0].longitude

                                    var sharedPreferences: SharedPreferences =
                                        getSharedPreferences("Address", MODE_PRIVATE)
                                    val myEdit = sharedPreferences.edit()
                                    myEdit.putString("address", addresses!![0].getAddressLine(0))
                                    myEdit?.commit()

                                    FirebaseFirestore.getInstance()
                                        .collection("RIDERS")
                                        .document(FirebaseAuth.getInstance().uid.toString())
                                        .update(userData)
                                        .addOnCompleteListener() { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    "Address Updated Successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                if (ContextCompat.checkSelfPermission(
                                                        applicationContext,
                                                        Manifest.permission.ACCESS_FINE_LOCATION
                                                    ) == PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    FirebaseFirestore.getInstance()
                                                        .collection("RIDERS")
                                                        .document(FirebaseAuth.getInstance().uid.toString())
                                                        .get()
                                                        .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                                                            val model: RiderModel? =
                                                                documentSnapshot.toObject(RiderModel::class.java)

                                                            if (!model?.drivingLicence.equals("")) {
                                                                startActivity(
                                                                    Intent(
                                                                        this,
                                                                        DashboardActivity::class.java
                                                                    )
                                                                )
                                                                finish()
                                                            } else {
                                                                val intent = Intent(
                                                                    this@PermissionActivity,
                                                                    MainActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                        })
                                                } else {
                                                    ActivityCompat.requestPermissions(
                                                        this@PermissionActivity,
                                                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                                                        ACCESS_FINE_LOCATION
                                                    )
                                                }

                                            }
                                        }
                                    //////// Update Address

                                    Toast.makeText(
                                        this,
                                        addresses!![0].getAddressLine(0).toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()

        FirebaseFirestore.getInstance().collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .get()
            .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                val model: RiderModel? = documentSnapshot.toObject(RiderModel::class.java)

                if (model?.name.equals("")) {
                    nameEmailDialog.show()

                } else if (model?.city.equals("")) {
                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                try {
                                    val geocoder =
                                        Geocoder(this@PermissionActivity, Locale.getDefault())
                                    val addresses = geocoder.getFromLocation(
                                        location.latitude,
                                        location.longitude,
                                        1
                                    )

                                    //////// Update Address
                                    val userData: MutableMap<String, Any?> = HashMap()
                                    userData["city"] = addresses!![0].locality
                                    userData["country"] = addresses!![0].countryName
                                    userData["state"] = addresses!![0].adminArea
                                    userData["pincode"] = addresses!![0].postalCode
                                    userData["address"] = addresses!![0].getAddressLine(0)
                                    userData["latitude"] = addresses!![0].latitude
                                    userData["longitude"] = addresses!![0].longitude

                                    var sharedPreferences: SharedPreferences =
                                        getSharedPreferences("Address", MODE_PRIVATE)
                                    val myEdit = sharedPreferences.edit()
                                    myEdit.putString("address", addresses!![0].getAddressLine(0))
                                    myEdit?.commit()

                                    FirebaseFirestore.getInstance()
                                        .collection("RIDERS")
                                        .document(FirebaseAuth.getInstance().uid.toString())
                                        .update(userData)
                                        .addOnCompleteListener() { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    "Address Updated Successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                if (ContextCompat.checkSelfPermission(
                                                        applicationContext,
                                                        Manifest.permission.ACCESS_FINE_LOCATION
                                                    ) == PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    FirebaseFirestore.getInstance().collection("RIDERS")
                                                        .document(FirebaseAuth.getInstance().uid.toString())
                                                        .get()
                                                        .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                                                            val model: RiderModel? = documentSnapshot.toObject(RiderModel::class.java)

                                                            if (!model?.drivingLicence.equals("")){
                                                                startActivity(Intent(this, DashboardActivity::class.java))
                                                                finish()
                                                            }else{
                                                                val intent = Intent(
                                                                    this@PermissionActivity,
                                                                    MainActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                        })
                                                } else {
                                                    ActivityCompat.requestPermissions(
                                                        this@PermissionActivity,
                                                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                                                        ACCESS_FINE_LOCATION
                                                    )
                                                }

                                            }
                                        }
                                    //////// Update Address

                                    Toast.makeText(
                                        this,
                                        addresses!![0].getAddressLine(0),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                } else {
//                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        val intent = Intent(this@PermissionActivity, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    }
                }

            })

    }
}
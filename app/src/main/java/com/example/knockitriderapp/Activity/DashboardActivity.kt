package com.example.knockitriderapp.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.knockitriderapp.Fragment.HomeFragment
import com.example.knockitriderapp.Fragment.OderFragment
import com.example.knockitriderapp.Fragment.ProfileFragment
import com.example.knockitriderapp.Fragment.WalletFragment
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.ActivityDashboardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    val ACCESS_FINE_LOCATION = 1
    val HOME_FRAGMENT = 0
    val CATEGORY_FRAGMENT = 1
    val MY_ORDER_FRAGMENT = 2
    val WALLET_FRAGMENT = 3
    val PROFILE_FRAGMENT = 4
    var CurrentFragment = -1

    lateinit var binding: ActivityDashboardBinding
    lateinit var reviewDialog: Dialog
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        val view: View = binding.getRoot()
        setContentView(view)
        setFragment(HomeFragment(), HOME_FRAGMENT)
        window.setStatusBarColor(ContextCompat.getColor(this@DashboardActivity,R.color.primary));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        ////////////////loading dialog
        reviewDialog = Dialog(this)
        reviewDialog.setContentView(R.layout.dialog_under_review)
        reviewDialog.setCancelable(false)
        reviewDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        var okBtn: AppCompatButton = reviewDialog.findViewById(R.id.okBtn)!!
        okBtn.setOnClickListener {
            finish()
        }
        ////////////////loading dialog

        FirebaseFirestore.getInstance().collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .get()
            .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                val model: RiderModel? = documentSnapshot.toObject(RiderModel::class.java)

                if (model?.driverAccount.equals("")){
                    reviewDialog.show()
                }else{
                    reviewDialog.dismiss()
                }
            })

        binding.bottomNavigationView!!.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    setCheckedChancel()
                    invalidateOptionsMenu()
                    setFragment(HomeFragment(), HOME_FRAGMENT)
                    window.setStatusBarColor(ContextCompat.getColor(this@DashboardActivity,R.color.primary));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                }

                R.id.my_oder -> {
                    setCheckedChancel()
                    invalidateOptionsMenu()
                    setFragment(OderFragment(), MY_ORDER_FRAGMENT)
                    item.isChecked = true
                    window.setStatusBarColor(ContextCompat.getColor(this@DashboardActivity,R.color.primary));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                }

                R.id.wallet -> {
                    setCheckedChancel()
                    invalidateOptionsMenu()
                    setFragment(WalletFragment(), WALLET_FRAGMENT)
                    item.isChecked = true
                    window.setStatusBarColor(ContextCompat.getColor(this@DashboardActivity,R.color.primary));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                }

                R.id.profile -> {
                    setCheckedChancel()
                    invalidateOptionsMenu()
                    setFragment(ProfileFragment(), PROFILE_FRAGMENT)
                    item.isChecked = true
                    window.setStatusBarColor(ContextCompat.getColor(this@DashboardActivity,R.color.white));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                }
            }
            true
        }
    }

    private fun setFragment(fragment: Fragment, fragmentNo: Int) {
        if (fragmentNo != CurrentFragment) {
            CurrentFragment = fragmentNo
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.slide_from_right,
                R.anim.slideout_from_left
            )
            fragmentTransaction.replace(binding.frameLayout!!.id, fragment)
            fragmentTransaction.commit()
        }
    }

    private fun setCheckedChancel() {
        binding.bottomNavigationView.getMenu().getItem(0).setChecked(false)
        binding.bottomNavigationView.getMenu().getItem(1).setChecked(false)
        binding.bottomNavigationView.getMenu().getItem(2).setChecked(false)
        binding.bottomNavigationView.getMenu().getItem(3).setChecked(false)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    try {
                        val geocoder =
                            Geocoder(this@DashboardActivity, Locale.getDefault())
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

                        var sharedPreferences: SharedPreferences = getSharedPreferences("Address", MODE_PRIVATE)
                        val myEdit = sharedPreferences.edit()
                        myEdit.putString("address", addresses!![0].getAddressLine(0))
                        myEdit?.commit()

                        FirebaseFirestore.getInstance()
                            .collection("RIDERS")
                            .document(FirebaseAuth.getInstance().uid.toString())
                            .update(userData)
                            .addOnCompleteListener() { task ->
                                if (task.isSuccessful) {

                                    if (ContextCompat.checkSelfPermission(
                                            applicationContext,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {

                                    } else {
                                        ActivityCompat.requestPermissions(
                                            this@DashboardActivity,
                                            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                                            ACCESS_FINE_LOCATION
                                        )
                                    }

                                }
                            }
                        //////// Update Address

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
    }
}
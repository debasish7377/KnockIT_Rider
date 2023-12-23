package com.example.knockitriderapp.Fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.knockitbranchapp.Service.MyServices
import com.example.knockitriderapp.Activity.DashboardActivity
import com.example.knockitriderapp.Database.MyOderDatabase
import com.example.knockitriderapp.Model.BranchModel
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentHomeBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class HomeFragment : Fragment() {

    lateinit var loadingDialog: Dialog

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        ////////////////loading dialog
        loadingDialog = Dialog(context!!)
        loadingDialog.setContentView(R.layout.dialog_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawable(context!!.getDrawable(R.drawable.btn_buy_now))
        loadingDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        ////////////////loading dialog

        MyOderDatabase.loadMyOder(context!!, binding.oderRecyclerview, "Out for delivery")

        FirebaseFirestore.getInstance().collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .get()
            .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                val model: RiderModel? = documentSnapshot.toObject(RiderModel::class.java)

                try {
                    if (model?.profile.equals("")) {
                        Glide.with(context!!).load(R.drawable.avatara).into(binding.profileImage)
                    } else {
                        Glide.with(context!!).load(model?.profile.toString())
                            .into(binding.profileImage)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.name.text = model?.name
                binding.email.text = model?.email
                binding.totalEarning.text = "₹" + model?.totalEarning

            })

        FirebaseFirestore.getInstance()
            .collection("RiderNotification")
            .document(FirebaseAuth.getInstance().uid.toString())
            .addSnapshotListener { value, error ->
                var timeStamp = value?.getLong("timeStamp")
                var storeOwnerName = value?.getString("storeOwnerName").toString()
                var storeOwnerProfile = value?.getString("storeOwnerProfile").toString()
                var storeName = value?.getString("storeName").toString()
                var storeId = value?.getString("storeId").toString()
                var riderId = value?.getString("riderId").toString()
                if (FirebaseAuth.getInstance().uid.toString().equals(riderId)) {
                    binding.storeConnectionBg.visibility = View.VISIBLE
                    binding.storeConnection.storeName.text = storeName
                    binding.storeConnection.storeOwnerName.text = storeOwnerName
                    binding.storeConnection.storeId.text = storeId
                    binding.storeConnection.riderId.text = riderId
                    try {
                        Glide.with(context!!).load(storeOwnerProfile)
                            .placeholder(R.drawable.avatara)
                            .into(binding.storeConnection.storeOwnerProfile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    binding.storeConnectionBg.visibility = View.GONE
                }
            }

        binding.storeConnection.OkBtn.setOnClickListener {
            if (binding.storeConnection.OkBtn.text.equals("Store Connected")) {
                Toast.makeText(context, "Rider already connect with store", Toast.LENGTH_SHORT).show()
//                val builder = AlertDialog.Builder(context)
//                builder.setTitle("Store")
//                builder.setMessage("Your order completed ?")
//
//                builder.setPositiveButton("Yes") { dialog, which ->
//                    loadingDialog.show()
//                    val userData: MutableMap<String, Any?> = HashMap()
//                    userData["connectWithRider"] = ""
//                    FirebaseFirestore.getInstance()
//                        .collection("BRANCHES")
//                        .document(binding.storeConnection.storeId.text.toString())
//                        .update(userData)
//                        .addOnCompleteListener {
//                            binding.storeConnection.storeId.text = ""
//                            val userData: MutableMap<String, Any?> = HashMap()
//                            userData["connectWithStore"] = ""
//                            FirebaseFirestore.getInstance()
//                                .collection("RIDERS")
//                                .document(FirebaseAuth.getInstance().uid.toString())
//                                .update(userData)
//                                .addOnCompleteListener {
//                                    loadingDialog.dismiss()
//                                    Toast.makeText(
//                                        context,
//                                        "Store Disconnected",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    startActivity(Intent(context!!, DashboardActivity::class.java))
//                                }
//                        }
//                }
//
//                builder.setNegativeButton("No") { dialog, which ->
//                }
//
//                builder.show()

            } else if (binding.storeConnection.OkBtn.text.equals("Accept")){
                FirebaseFirestore.getInstance()
                    .collection("BRANCHES")
                    .document(binding.storeConnection.storeId.text.toString())
                    .get()
                    .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                        val model: BranchModel? = documentSnapshot.toObject(BranchModel::class.java)

                        if (!model?.connectWithRider.toString().equals("")) {
                            FirebaseFirestore.getInstance()
                                .collection("RiderNotification")
                                .document(FirebaseAuth.getInstance().uid.toString())
                                .delete()
                            Toast.makeText(context, "Rider already connect with store", Toast.LENGTH_SHORT).show()
                        } else {

                            loadingDialog.show()
                            binding.storeConnection.cancelBtn.visibility = View.GONE
                            binding.storeConnection.OkBtn.text = "Store Connected"

                            val userData: MutableMap<String, Any?> = HashMap()
                            userData["connectWithStore"] = binding.storeConnection.storeId.text
                            FirebaseFirestore.getInstance()
                                .collection("RIDERS")
                                .document(FirebaseAuth.getInstance().uid.toString())
                                .update(userData)
                                .addOnCompleteListener {
                                    val userData: MutableMap<String, Any?> = HashMap()
                                    userData["connectWithRider"] =
                                        binding.storeConnection.riderId.text
                                    FirebaseFirestore.getInstance()
                                        .collection("BRANCHES")
                                        .document(binding.storeConnection.storeId.text.toString())
                                        .update(userData)
                                        .addOnCompleteListener {
                                            loadingDialog.dismiss()
                                            Toast.makeText(
                                                context,
                                                "Store Connected",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            startActivity(
                                                Intent(
                                                    context,
                                                    DashboardActivity::class.java
                                                )
                                            )

                                            FirebaseFirestore.getInstance()
                                                .collection("RiderNotification")
                                                .document(FirebaseAuth.getInstance().uid.toString())
                                                .delete()
                                        }
                                }
                        }
                    })
            }
        }

        binding.storeConnection.cancelBtn.setOnClickListener {
            MyServices.ringtone.stop()
            loadingDialog.show()
            FirebaseFirestore.getInstance()
                .collection("RiderNotification")
                .document(FirebaseAuth.getInstance().uid.toString())
                .delete()
                .addOnCompleteListener {
                    loadingDialog.dismiss()
                    Toast.makeText(context, "Store Rejected by you", Toast.LENGTH_SHORT).show()
                }
        }

        FirebaseFirestore.getInstance()
            .collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .addSnapshotListener { value, error ->
                var connectWithStore = value?.getString("connectWithStore").toString()

                if (!connectWithStore.toString().equals("")) {
                    binding.storeConnectionBg.visibility = View.VISIBLE
                    binding.storeConnection.cancelBtn.visibility = View.GONE
                    binding.storeConnection.OkBtn.text = "Store Connected"
                    FirebaseFirestore.getInstance()
                        .collection("BRANCHES")
                        .document(connectWithStore)
                        .addSnapshotListener { value, error ->
                            var storeOwnerName = value?.getString("name").toString()
                            var storeOwnerProfile = value?.getString("profile").toString()
                            var storeName = value?.getString("storeName").toString()
                            var number = value?.getString("number").toString()
                            var storeId = value?.getString("storeId").toString()
                            binding.storeConnectionBg.visibility = View.VISIBLE
                            binding.storeConnection.storeName.text = storeName
                            binding.storeConnection.storeOwnerName.text = storeOwnerName
                            binding.storeConnection.storeNumber.text = number
                            binding.storeConnection.storeId.text = storeId
                            binding.storeConnection.storeNumber.visibility = View.VISIBLE
                            try {
                                Glide.with(context!!).load(storeOwnerProfile)
                                    .placeholder(R.drawable.avatara)
                                    .into(binding.storeConnection.storeOwnerProfile)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                } else {
                    //binding.storeConnectionBg.visibility = View.GONE
                }
            }

        return binding.root
    }

    override fun onResume() {
        super.onResume()


    }
}
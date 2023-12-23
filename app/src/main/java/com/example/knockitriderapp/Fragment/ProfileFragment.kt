package com.example.knockitriderapp.Fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.knockitriderapp.Activity.RegisterActivity
import com.example.knockitriderapp.Activity.UpdateProfileActivity
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID

class ProfileFragment : Fragment() {

    var selected: String? = null
    lateinit var loadingDialog: Dialog
    lateinit var redeemDialog: Dialog

    lateinit var bankName: TextView
    lateinit var bankNumber: TextView
    lateinit var ifscCode: TextView
    lateinit var bankHolderName: TextView
    lateinit var totalEarning: TextView
    lateinit var redeemBtn: AppCompatButton

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentProfileBinding =
            FragmentProfileBinding.inflate(inflater, container, false)

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

        ////////////////loading dialog
        redeemDialog = Dialog(context!!)
        redeemDialog.setContentView(R.layout.dialog_redeem)
        redeemDialog.setCancelable(true)
        redeemDialog.window?.setBackgroundDrawable(context!!.getDrawable(R.drawable.btn_buy_now))
        redeemDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bankName = redeemDialog.findViewById(R.id.bankName)
        bankNumber = redeemDialog.findViewById(R.id.accountNumber)
        ifscCode = redeemDialog.findViewById(R.id.ifscCode)
        bankHolderName = redeemDialog.findViewById(R.id.bankHolderName)
        totalEarning = redeemDialog.findViewById(R.id.totalAmount)
        redeemBtn = redeemDialog.findViewById(R.id.redeemBtn)
        ////////////////loading dialog

        FirebaseFirestore.getInstance()
            .collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .addSnapshotListener { querySnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
                querySnapshot?.let {
                    val userModel = it.toObject(RiderModel::class.java)

                    try {
                        if (userModel?.profile.equals("")) {
                            Glide.with(context!!).load(R.drawable.avatara)
                                .into(binding.profileImage)
                        } else {
                            Glide.with(context!!).load(userModel?.profile.toString())
                                .placeholder(R.drawable.avatara)
                                .into(binding.profileImage)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    binding.fullName.text = userModel?.name
                    binding.email.text = userModel?.email

                    bankName.text = userModel?.bankName
                    bankNumber.text = userModel?.bankAccountNumber
                    bankHolderName.text = userModel?.bankHolderName
                    ifscCode.text = userModel?.bankIFSCCode
                    totalEarning.text = userModel?.totalEarning.toString()

                }
            }

        binding.updateProfile.setOnClickListener {
            startActivity(Intent(context, UpdateProfileActivity::class.java))
        }
        binding.logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(getContext(), RegisterActivity::class.java))
            getActivity()?.finish()
        }
        binding.dayNight.setOnClickListener {
            showDialog1()
        }
        binding.rateUs.setOnClickListener {
            val intent1 = Intent(Intent.ACTION_VIEW)
            intent1.data = Uri.parse(
                "https://play.google.com/store/apps/details?id=com.example.thugsoffacts"
            )
            intent1.setPackage("com.android.vending")
            startActivity(intent1)
        }

        binding.wallet.setOnClickListener {
            redeemDialog.show()
        }

        redeemBtn.setOnClickListener {
            if (totalEarning.text.toString().toInt() >= 1000) {

                loadingDialog.show()
                redeemDialog.dismiss()

                val randomString = UUID.randomUUID().toString().substring(0, 18)
                val userData3: MutableMap<String, Any?> =
                    HashMap()
                userData3["id"] = randomString
                userData3["title"] = "Redeem"
                userData3["description"] =
                    "Your Redeem Successful. Payment credited within 24hr"
                userData3["payment"] = totalEarning.text.toString() + " Payment Redeem"
                userData3["timeStamp"] = System.currentTimeMillis()
                userData3["read"] = "true"
                FirebaseFirestore.getInstance()
                    .collection("RIDERS")
                    .document(FirebaseAuth.getInstance().uid.toString())
                    .collection("MY_NOTIFICATION")
                    .document(randomString)
                    .set(userData3)
                    .addOnCompleteListener {
                        val userData2: MutableMap<String, Any?> = HashMap()
                        userData2["riderId"] = FirebaseAuth.getInstance().uid.toString()
                        userData2["id"] = randomString
                        userData2["totalAmount"] = totalEarning.text.toString().toInt()
                        userData2["bankName"] = bankName.text.toString()
                        userData2["bankHolderName"] = bankHolderName.text.toString()
                        userData2["ifscCode"] = ifscCode.text.toString()
                        userData2["bankAccountNumber"] = bankNumber.text.toString()
                        userData2["timeStamp"] = System.currentTimeMillis()
                        userData2["payment"] = "false"
                        FirebaseFirestore.getInstance()
                            .collection("RIDER_PAYMENT")
                            .document(randomString)
                            .set(userData2)
                            .addOnCompleteListener {
                                val userData: MutableMap<String, Any?> =
                                    HashMap()
                                userData["totalEarning"] = 0.toInt()
                                FirebaseFirestore.getInstance()
                                    .collection("RIDERS")
                                    .document(FirebaseAuth.getInstance().uid.toString())
                                    .update(userData)
                                    .addOnCompleteListener {

                                        FirebaseFirestore.getInstance()
                                            .collection("RIDERS")
                                            .document(FirebaseAuth.getInstance().uid.toString())
                                            .collection("PAYMENT")
                                            .document(randomString)
                                            .set(userData2)
                                            .addOnCompleteListener {
                                                loadingDialog.dismiss()
                                                Toast.makeText(
                                                    context,
                                                    "Redeem Successful",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }

                                    }

                            }
                    }

            } else {
                Toast.makeText(
                    context,
                    "Minimum Balance ₹1000 to redeem",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun showDialog1() {
        val thems = this.resources.getStringArray(R.array.Them)
        val builder = MaterialAlertDialogBuilder(context!!)
        builder.setTitle("Select them")
        builder.setSingleChoiceItems(R.array.Them, 0,
            DialogInterface.OnClickListener { dialog, i ->
                selected = thems[i]
            })
        builder.setPositiveButton("Ok") { dialog, i ->
            if (selected == null) {
                selected = thems[0]
            }
            when (selected) {
                "System Default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> {}
            }

        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.dismiss() }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
package com.example.knockitbranchapp.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.example.knockitriderapp.Activity.PermissionActivity
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.databinding.FragmentOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.concurrent.TimeUnit

class OtpFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var number: String
    var otpid: String? = null
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentOtpBinding = FragmentOtpBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var sh: SharedPreferences? = context?.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        number = sh?.getString("number", "").toString();
        binding.progressBar.visibility = View.VISIBLE
        initiateotp(binding.progressBar)


        binding.getOtpBtn.setOnClickListener(View.OnClickListener {
            if (binding.otpView.getText().toString().isEmpty()) {
                binding.otpView.setError("Fill in the blank")
                Toast.makeText(
                    context,
                    "Blank Field can not be processed",
                    Toast.LENGTH_LONG
                ).show()
                return@OnClickListener
            } else if (binding.otpView.getText().toString().length !== 6) {
                binding.otpView.setError("Incorrect otp")
                Toast.makeText(context, "Invalid OTP", Toast.LENGTH_LONG).show()
                return@OnClickListener
            } else {
                try {
                    val credential = PhoneAuthProvider.getCredential(
                        otpid!!,
                        binding.otpView.getText().toString()
                    )
                    signInWithPhoneAuthCredential(credential, context!!)
                }catch (e: Exception){
                    e.printStackTrace()
                }

            }
        })

        return binding.root
    }

    private fun initiateotp(progress: ProgressBar) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            number,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            context as Activity,  // Activity (for callback binding)
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    otpid = s
                    progress.visibility = View.GONE
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(phoneAuthCredential, context!!)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }) // OnVerificationStateChangedCallbacks
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, context: Context) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener {

                FirebaseFirestore.getInstance()
                    .collection("RIDERS")
                    .document(FirebaseAuth.getInstance().uid.toString())
                    .addSnapshotListener { querySnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
                        querySnapshot?.let {
                            val userModel = it.toObject(RiderModel::class.java)

                            try {
                                if (!FirebaseAuth.getInstance().uid.toString().equals(userModel?.riderId)) {
                                    val userData: MutableMap<Any, Any?> = HashMap()
                                    userData["name"] = ""
                                    userData["email"] = ""
                                    userData["number"] = number
                                    userData["riderId"] = auth.uid
                                    userData["profile"] = ""
                                    userData["totalEarning"] = 0
                                    userData["drivingLicence"] = ""
                                    userData["drivingLicenceImage_1"] = ""
                                    userData["drivingLicenceImage_2"] = ""
                                    userData["driverAccount"] = ""
                                    userData["connectWithStore"] = ""

                                    userData["bankAccountNumber"] = ""
                                    userData["bankName"] = ""
                                    userData["bankHolderName"] = ""
                                    userData["bankIFSCCode"] = ""

                                    userData["city"] = ""
                                    userData["country"] = ""
                                    userData["state"] = ""
                                    userData["pincode"] = ""
                                    userData["address"] = ""
                                    userData["latitude"] = 0
                                    userData["longitude"] = 0
                                    userData["timeStamp"] = System.currentTimeMillis()

                                    FirebaseFirestore.getInstance().collection("RIDERS")
                                        .document(auth.uid.toString())
                                        .set(userData)
                                        .addOnCompleteListener() { task ->
                                            if (task.isSuccessful) {
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        PermissionActivity::class.java
                                                    )
                                                )

                                                Toast.makeText(
                                                    context,
                                                    "Successfully your profile completed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                var error: String? = task.exception?.message
                                                Toast.makeText(
                                                    context,
                                                    error,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                } else {
                                    startActivity(
                                        Intent(
                                            context,
                                            PermissionActivity::class.java
                                        )
                                    )
                                    requireActivity().finish()
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }

                        }
                    }
            }
    }
}
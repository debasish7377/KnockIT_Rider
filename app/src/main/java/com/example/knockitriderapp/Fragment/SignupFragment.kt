package com.example.knockitbranchapp.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.example.knockitriderapp.Activity.PermissionActivity
import com.example.knockitriderapp.Activity.RegisterActivity
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentSignupBinding = FragmentSignupBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.haveAccount.setOnClickListener {
            setFragment(LoginFragment())
        }

        binding.btnSignup.setOnClickListener(View.OnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if (!binding.edEmail.text.isEmpty()) {
                if (!binding.edName.text.toString().isEmpty()) {
                    if (!binding.edNumber.text.toString().isEmpty() && binding.edNumber.length() == 10) {
                        if (!binding.edPassword.text.toString().isEmpty() && binding.edPassword.length() >= 8) {
                            if (!binding.edConfirmPassword.text.toString()
                                    .isEmpty() && binding.edPassword.text.toString()
                                    .equals(binding.edPassword.text.toString())
                            ) {

                                auth.createUserWithEmailAndPassword(
                                    binding.edEmail.text.toString(),
                                    binding.edPassword.text.toString()
                                )
                                    .addOnCompleteListener() { task ->
                                        if (task.isSuccessful) {
                                            val userData: MutableMap<Any, Any?> = HashMap()
                                            userData["name"] = binding.edName.text.toString()
                                            userData["email"] = binding.edEmail.text.toString()
                                            userData["number"] = binding.edNumber.text.toString()
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

                                            firebaseFirestore.collection("RIDERS")
                                                .document(auth.uid.toString())
                                                .set(userData)
                                                .addOnCompleteListener() { task ->
                                                    if (task.isSuccessful) {
                                                        binding.progressBar.visibility = View.GONE
                                                        startActivity(
                                                            Intent(
                                                                context,
                                                                PermissionActivity::class.java
                                                            )
                                                        )
                                                        requireActivity().finish()
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
                                            var error: String? = task.exception?.message
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT)
                                                .show()
                                        }

                                    }

                            } else {
                                binding.edConfirmPassword.error = "Password does not match"
                                binding.progressBar.visibility = View.GONE
                                binding.edConfirmPassword.setText("")
                            }
                        } else {
                            binding.edPassword.error = "Enter Password"
                            binding.progressBar.visibility = View.GONE
                            binding.edPassword.setText("")
                        }
                    } else {
                        binding.edNumber.error = "Enter Phone Number"
                        binding.progressBar.visibility = View.GONE
                        binding.edNumber.setText("")
                    }
                } else {
                    binding.edName.error = "Enter Name"
                    binding.progressBar.visibility = View.GONE
                    binding.edName.setText("")
                }
            } else {
                binding.edEmail.error = "Enter Email"
                binding.progressBar.visibility = View.GONE
                binding.edEmail.setText("")
            }
        })

        return binding.root
    }

    fun setFragment(fragment: Fragment?) {
        val fragmentTransaction: FragmentTransaction = activity?.supportFragmentManager?.beginTransaction()!!
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slideout_from_left)
        if (fragment != null) {
            fragmentTransaction.replace(RegisterActivity.frameLayout.getId(), fragment)
        }
        fragmentTransaction.commit()
    }
}
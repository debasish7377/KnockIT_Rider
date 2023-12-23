package com.example.knockitbranchapp.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.example.knockitriderapp.Activity.PermissionActivity
import com.example.knockitriderapp.Activity.RegisterActivity
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    lateinit var doNotHaveAccount: TextView
    lateinit var auth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.doNotHaveAccount.setOnClickListener {
            setFragment(SignupFragment())
        }

        binding.phoneLogin.setOnClickListener {
            setFragment(PhoneLoginFragment())
        }

        binding.resetPassword.setOnClickListener {
            setFragment(PasswordResetFragment())
        }
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if (!binding.edEmail.text.isEmpty()) {
                if (!binding.edPassword.text.toString().isEmpty() && binding.edPassword.length() >= 8) {

                    auth.signInWithEmailAndPassword(binding.edEmail.text.toString(),binding.edPassword.text.toString())
                        .addOnCompleteListener() { task ->
                            if (task.isSuccessful) {
                                binding.progressBar.visibility = View.GONE
                                startActivity(Intent(context, PermissionActivity::class.java))
                                requireActivity().finish()
                                Toast.makeText(context, "Login Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                var error : String? = task.exception?.message
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        }
                }else{
                    binding.edPassword.error = "Wrong Password!"
                    binding.progressBar.visibility = View.GONE
                }
            }else{
                binding.edEmail.error = "Enter Email"
                binding.progressBar.visibility = View.GONE
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
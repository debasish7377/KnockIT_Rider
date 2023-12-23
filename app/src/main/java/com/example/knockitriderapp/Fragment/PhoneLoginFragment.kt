package com.example.knockitbranchapp.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.example.knockitriderapp.Activity.RegisterActivity
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentPhoneLoginBinding

class PhoneLoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentPhoneLoginBinding = FragmentPhoneLoginBinding.inflate(inflater, container, false)

        binding.getOtpBtn.setOnClickListener {
            if (!binding.edNumber.text.isEmpty()  && binding.edNumber.length() == 10) {
                val sharedPreferences: SharedPreferences? = context?.getSharedPreferences("MySharedPref",
                    Context.MODE_PRIVATE
                )
                val myEdit = sharedPreferences?.edit()
                myEdit?.putString("number", "+91"+binding.edNumber.text.toString())
                myEdit?.commit()
                setFragment(OtpFragment())
            }else{
                binding.edNumber.error = "Enter Number"
            }
        }

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
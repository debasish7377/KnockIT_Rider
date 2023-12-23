package com.example.knockitriderapp.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.knockitriderapp.Database.MyOderDatabase
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentHomeBinding
import com.example.knockitriderapp.databinding.FragmentOderBinding

class OderFragment : Fragment() {

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentOderBinding = FragmentOderBinding.inflate(inflater, container, false)


        MyOderDatabase.loadMyDeliveryOder(context!!, binding.oderRecyclerview, "Delivered")
        return binding.root
    }
}
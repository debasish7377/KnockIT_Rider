package com.example.knockitriderapp.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.knockitriderapp.Database.NotificationDatabase
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentWalleteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class WalletFragment : Fragment() {

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentWalleteBinding = FragmentWalleteBinding.inflate(inflater, container, false)

        FirebaseFirestore.getInstance()
            .collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .addSnapshotListener { querySnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
                querySnapshot?.let {
                    val userModel = it.toObject(RiderModel::class.java)

                    binding.pendingPayment.text = "Total Payment ₹"+userModel?.totalEarning.toString()

                }
            }

        NotificationDatabase.loadNotification(context!!, binding.notificationRecyclerView)

        return binding.root
    }
}
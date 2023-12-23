package com.example.knockitriderapp.Database

import android.content.Context
import android.text.Editable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.knockitriderapp.Adapter.NotificationAdapter
import com.example.knockitriderapp.Model.NotificationModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class NotificationDatabase {

    companion object {
        fun loadNotification(context: Context, notificationRecyclerView: RecyclerView) {
            var notificationModel: ArrayList<NotificationModel> = ArrayList<NotificationModel>()
            var notificationAdapter = NotificationAdapter(context!!, notificationModel)
            val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            notificationRecyclerView.layoutManager = layoutManager
            notificationRecyclerView.adapter = notificationAdapter

            FirebaseFirestore.getInstance()
                .collection("RIDERS")
                .document(FirebaseAuth.getInstance().uid.toString())
                .collection("MY_NOTIFICATION")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    querySnapshot?.let {
                        notificationModel.clear()
                        for (document in it) {
                            val model = document.toObject(NotificationModel::class.java)
                            notificationModel.add(model)


                        }
                        notificationAdapter.notifyDataSetChanged()
                    }
                }
        }
    }

//            FirebaseFirestore.getInstance()
//                .collection("PRODUCTS")
//                .document(productId)
//                .collection("productSize")
//                .orderBy("timeStamp", Query.Direction.DESCENDING)
//                .get().addOnSuccessListener(OnSuccessListener<QuerySnapshot> { queryDocumentSnapshots ->
//                    for (snapshot in queryDocumentSnapshots) {
//                        val model: SelectQtyModel = snapshot.toObject(SelectQtyModel::class.java)
//                        qtyModel.add(model)
//                    }
//                    qtyAdapter.notifyDataSetChanged()
//                })
}
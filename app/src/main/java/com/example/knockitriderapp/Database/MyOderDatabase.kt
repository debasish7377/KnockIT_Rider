package com.example.knockitriderapp.Database

import android.app.Dialog
import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.knockitriderapp.Adapter.MyOderAdapter
import com.example.knockitriderapp.Adapter.MyOderDeliveredAdapter
import com.example.knockitriderapp.Model.MyOderModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.UUID

class MyOderDatabase {

    companion object {

        fun loadMyDeliveryOder(context: Context, myOrderRecyclerView: RecyclerView, deliveryText: String) {
            var orderModel: ArrayList<MyOderModel> = ArrayList<MyOderModel>()
            var orderAdapter = MyOderDeliveredAdapter(context!!, orderModel)
            val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            myOrderRecyclerView.layoutManager = layoutManager
            myOrderRecyclerView.adapter = orderAdapter
            var orderItems: ArrayList<MyOderModel> = ArrayList<MyOderModel>()

            FirebaseFirestore.getInstance()
                .collection("ORDER")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    querySnapshot?.let {
                        orderItems.clear()
                        for (document in it) {
                            val model = document.toObject(MyOderModel::class.java)
                            orderItems.add(model)

                            orderModel.clear()
                            for (p in orderItems){
                                if (p.riderId.equals(FirebaseAuth.getInstance().uid)){
                                    if (p.delivery.equals(deliveryText)){
                                        orderModel.add(p)
                                    }
                                }
                            }
                            orderAdapter.notifyDataSetChanged()
                        }
                    }
                }
        }

        fun loadMyOder(context: Context, myOrderRecyclerView: RecyclerView, deliveryText: String) {
            var orderModel: ArrayList<MyOderModel> = ArrayList<MyOderModel>()
            var CartAdapter = MyOderAdapter(context!!, orderModel)
            val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            myOrderRecyclerView.layoutManager = layoutManager
            myOrderRecyclerView.adapter = CartAdapter
            var orderItems: ArrayList<MyOderModel> = ArrayList<MyOderModel>()

            FirebaseFirestore.getInstance()
                .collection("ORDER")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    querySnapshot?.let {
                        orderItems.clear()
                        for (document in it) {
                            val model = document.toObject(MyOderModel::class.java)
                            orderItems.add(model)

                            orderModel.clear()
                            for (p in orderItems){
                                if (p.riderId.equals(FirebaseAuth.getInstance().uid)){
                                    if (p.delivery.equals(deliveryText)){
                                        orderModel.add(p)
                                    }
                                }
                            }
                            CartAdapter.notifyDataSetChanged()
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
    }
}
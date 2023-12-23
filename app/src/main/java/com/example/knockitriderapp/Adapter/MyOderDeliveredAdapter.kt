package com.example.knockitriderapp.Adapter

import android.R.attr.name
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.knockitriderapp.Model.BranchModel
import com.example.knockitriderapp.Model.MyOderModel
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.Model.UserModel
import com.example.knockitriderapp.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID


class MyOderDeliveredAdapter(var context: Context, var model: ArrayList<MyOderModel>) :
    RecyclerView.Adapter<MyOderDeliveredAdapter.viewHolder>() {

    lateinit var canceledDialog: Dialog
    lateinit var canceledText: EditText
    lateinit var okBtn: AppCompatButton
    lateinit var loadingDialog: Dialog
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        var view: View = LayoutInflater.from(context).inflate(R.layout.item_my_delivered_oder, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {

        FirebaseFirestore.getInstance()
            .collection("PRODUCTS")
            .document(model[position].productId)
            .get()
            .addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                if (task.isSuccessful) {
                    Glide.with(context).load(task.result.getString("productImage").toString())
                        .into(holder.productImage)
                }
            })

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

        ////////////////canceled dialog
        canceledDialog = Dialog(context)
        canceledDialog.setContentView(R.layout.dialog_oder_canceled)
        canceledDialog.setCancelable(true)
        canceledDialog.window?.setBackgroundDrawable(context.getDrawable(R.drawable.btn_buy_now))
        canceledDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        canceledText = canceledDialog.findViewById(R.id.canceledText)
        okBtn = canceledDialog.findViewById(R.id.okBtn)
        ////////////////canceled dialog

        holder.productTitle.text = model[position].productTitle
        holder.productPrice.text = model[position].productPrice.toString()
        holder.productCuttedPrice.text = model[position].productCuttedPrice.toString()
        holder.yourPrice.text = model[position].price.toString()
        holder.qty_text.text = model[position].qty
        holder.qty_no.text = "qty : " + model[position].qtyNo.toString()
        holder.userUid.text = model[position].uid
        var youSaved: String =
            (model[position].productCuttedPrice.toInt() - model[position].productPrice.toInt()).toString()
        holder.discountedPrice.text = "₹" + youSaved + " Saved"

        if (model[position].delivery.equals("Delivered")) {
            holder.deliveryBtn.text = "Order Successfully Completed"
            holder.canceledBtn.visibility = View.GONE
            holder.deliveryBtn.setOnClickListener {
                Toast.makeText(context, "Order already Delivered", Toast.LENGTH_SHORT).show()
            }
        } else if (model[position].delivery.equals("Canceled")) {
            holder.canceledBtn.text = "Order Canceled"
            holder.deliveryBtn.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return model.size
    }

    class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var productPrice: TextView = itemView.findViewById<TextView?>(R.id.mini_product_price)
        var productCuttedPrice: TextView =
            itemView.findViewById<TextView?>(R.id.mini_product_cutted_price)
        var productTitle: TextView = itemView.findViewById<TextView?>(R.id.mini_product_title)
        var productImage: ImageView = itemView.findViewById<ImageView?>(R.id.mini_product_image)
        var discountedPrice: TextView = itemView.findViewById<TextView?>(R.id.discount_text)
        var qty_text: TextView = itemView.findViewById<TextView?>(R.id.qty_text)
        var qty_no: TextView = itemView.findViewById<TextView?>(R.id.qty_no)
        var yourPrice: TextView = itemView.findViewById<TextView?>(R.id.yourPrice)
        var userUid: TextView = itemView.findViewById<TextView?>(R.id.userUid)

        var canceledBtn: AppCompatButton = itemView.findViewById<AppCompatButton?>(R.id.canceledBtn)
        var deliveryBtn: AppCompatButton = itemView.findViewById<AppCompatButton?>(R.id.deliveryBtn)

    }
}


//            holder.deliveryBtn.setOnClickListener {
//
//                val builder = AlertDialog.Builder(context)
//                builder.setTitle("Order")
//                builder.setMessage("Order delivered ?")
//
//                builder.setPositiveButton("Yes") { dialog, which ->
//
//                    //////Order pending payment
//                    FirebaseFirestore.getInstance().collection("BRANCHES")
//                        .document(model[position].storeId)
//                        .get()
//                        .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
//                            val model: BranchModel? =
//                                documentSnapshot.toObject(BranchModel::class.java)
//
//                            val userData: MutableMap<String, Any?> =
//                                HashMap()
//                            userData["pendingPayment"] = (model?.pendingPayment.toString().toInt() + holder.yourPrice.text.toString().toInt()).toInt()
//                            FirebaseFirestore.getInstance()
//                                .collection("BRANCHES")
//                                .document(FirebaseAuth.getInstance().uid.toString())
//                                .update(userData)
//                                .addOnCompleteListener {
//
//                                }
//
//                        })
//                    //////Order pending payment
//
//                    val userData: MutableMap<String, Any?> =
//                        HashMap()
//                    userData["delivery"] = "Delivered"
//                    userData["deliveredDate"] = System.currentTimeMillis().toString()
//                    FirebaseFirestore.getInstance()
//                        .collection("ORDER")
//                        .document(model[position].id)
//                        .update(userData)
//                        .addOnCompleteListener {
//                            if (it.isSuccessful) {
//                                notifyDataSetChanged()
//                                Toast.makeText(context, "Order Delivered", Toast.LENGTH_SHORT).show()
//                            } else {
//
//                            }
//                        }
//                }
//
//                builder.setNegativeButton("No") { dialog, which ->
//                }
//
//                builder.show()
//            }

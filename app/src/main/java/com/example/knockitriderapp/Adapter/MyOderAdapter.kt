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


class MyOderAdapter(var context: Context, var model: ArrayList<MyOderModel>) :
    RecyclerView.Adapter<MyOderAdapter.viewHolder>() {

    lateinit var canceledDialog: Dialog
    lateinit var canceledText: EditText
    lateinit var okBtn: AppCompatButton
    lateinit var loadingDialog: Dialog
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        var view: View = LayoutInflater.from(context).inflate(R.layout.item_my_oder, parent, false)
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

        holder.userName.text = "Name - "+model[position].name
        holder.userAddress.text = "Address - "+model[position].address
        holder.userNumber.text = "Number - "+model[position].number.toString()
        holder.storeId.text = model[position].storeId.toString()

        FirebaseFirestore.getInstance()
            .collection("BRANCHES")
            .document(model[position].storeId.toString())
            .addSnapshotListener { value, error ->
                var storeOwnerName = value?.getString("name").toString()
                var storeAddress = value?.getString("address").toString()
                var number = value?.getString("number").toString()
                holder.storeAddress.text = "Address - "+storeAddress
                holder.storeNumber.text = "Number - "+number

            }

        holder.canceledBtn.setOnClickListener {
            if (model[position].delivery.equals("Canceled")) {
                Toast.makeText(context, "Order already Canceled", Toast.LENGTH_SHORT).show()
            } else {

                FirebaseFirestore.getInstance().collection("BRANCHES")
                    .document(model[position].storeId)
                    .get()
                    .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                        val model: BranchModel? =
                            documentSnapshot.toObject(BranchModel::class.java)

                        val userData: MutableMap<String, Any?> =
                            HashMap()
                        userData["pendingPayment"] = (model?.pendingPayment.toString()
                            .toInt() - holder.yourPrice.text.toString().toInt()).toInt()
                        FirebaseFirestore.getInstance()
                            .collection("BRANCHES")
                            .document(holder.storeId.text.toString())
                            .update(userData)
                            .addOnCompleteListener {

                            }

                    })

                val randomString = UUID.randomUUID().toString().substring(0, 18)
                val userData1: MutableMap<String, Any?> =
                    HashMap()
                userData1["id"] = randomString
                userData1["title"] = "Order Canceled"
                userData1["description"] =
                    "Your Order " + holder.productTitle.text.toString() + " and price ₹" + holder.yourPrice.text.toString() + " Canceled by you"
                userData1["payment"] = holder.yourPrice.text.toString() + " Payment Canceled"
                userData1["timeStamp"] = System.currentTimeMillis()
                userData1["read"] = "true"

                FirebaseFirestore.getInstance()
                    .collection("BRANCHES")
                    .document(holder.storeId.text.toString())
                    .collection("MY_NOTIFICATION")
                    .document(randomString)
                    .set(userData1)
                    .addOnCompleteListener {

                    }

                canceledDialog.show()
            }
        }

        okBtn.setOnClickListener {
            if (!canceledText.text.toString().equals("")) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Order")
                builder.setMessage("Are you sure to cancel this order ?")

                builder.setPositiveButton("Yes") { dialog, which ->

                    canceledDialog.dismiss()
                    FirebaseFirestore.getInstance()
                        .collection("OrderNotification")
                        .document(FirebaseAuth.getInstance().uid.toString())
                        .delete()
                    val randomString = UUID.randomUUID().toString().substring(0, 18)
                    val userData1: MutableMap<String, Any?> =
                        HashMap()
                    userData1["id"] = randomString
                    userData1["title"] = "Order Canceled"
                    userData1["description"] = canceledText.text.toString()
                    userData1["timeStamp"] = System.currentTimeMillis()
                    userData1["read"] = "true"

                    FirebaseFirestore.getInstance()
                        .collection("USERS")
                        .document(model[position].uid)
                        .collection("MY_NOTIFICATION")
                        .document(randomString)
                        .set(userData1)
                        .addOnCompleteListener {

                        }

                    FirebaseFirestore.getInstance().collection("USERS")
                        .document(model[position].uid)
                        .get()
                        .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                            val model: UserModel? =
                                documentSnapshot.toObject(UserModel::class.java)

                            val userData2: MutableMap<String, Any?> = HashMap()
                            userData2["notificationSize"] =
                                (model?.notificationSize.toString().toInt() + 1).toString()

                            FirebaseFirestore.getInstance()
                                .collection("USERS")
                                .document(holder.userUid.text.toString())
                                .update(userData2)
                                .addOnCompleteListener {
                                    canceledDialog.dismiss()
                                }

                        })

                    val userData: MutableMap<String, Any?> =
                        HashMap()
                    userData["delivery"] = "Canceled"
                    FirebaseFirestore.getInstance()
                        .collection("ORDER")
                        .document(model[position].id)
                        .update(userData)
                        .addOnCompleteListener {
                            notifyDataSetChanged()
                            Toast.makeText(context, "Order Canceled", Toast.LENGTH_SHORT).show()
                        }

                }

                builder.setNegativeButton("No") { dialog, which ->
                }

                builder.show()
            } else {
                Toast.makeText(context, "Enter Details", Toast.LENGTH_SHORT).show()
            }
        }

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

        //////Order Confirmed
        holder.deliveryBtn.setOnClickListener {
            FirebaseFirestore.getInstance().collection("BRANCHES")
                .document(model[position].storeId)
                .get()
                .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                    val model: BranchModel? =
                        documentSnapshot.toObject(BranchModel::class.java)

                    val userData: MutableMap<String, Any?> = HashMap()
                    userData["pendingPayment"] = (model?.pendingPayment.toString().toInt() - holder.yourPrice.text.toString().toInt()).toInt()
                    userData["totalEarning"] = (model?.totalEarning.toString().toInt() + holder.yourPrice.text.toString().toInt()).toInt()

                    FirebaseFirestore.getInstance()
                        .collection("BRANCHES")
                        .document(holder.storeId.text.toString())
                        .update(userData)
                        .addOnCompleteListener {
                            Toast.makeText(context, "Order Successfully Delivered", Toast.LENGTH_SHORT).show()

                        }

                })

            FirebaseFirestore.getInstance().collection("RIDERS")
                .document(FirebaseAuth.getInstance().uid.toString())
                .get()
                .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                    val model: RiderModel? =
                        documentSnapshot.toObject(RiderModel::class.java)

                    val userData3: MutableMap<String, Any?> =
                        HashMap()
                    userData3["totalEarning"] = (model?.totalEarning.toString().toInt() + 80).toInt()
                    FirebaseFirestore.getInstance()
                        .collection("RIDERS")
                        .document(FirebaseAuth.getInstance().uid.toString())
                        .update(userData3)
                        .addOnCompleteListener {

                        }

                })

            val randomString = UUID.randomUUID().toString().substring(0, 18)
            val userData1: MutableMap<String, Any?> =
                HashMap()
            userData1["id"] = randomString
            userData1["title"] = "Order Delivered"
            userData1["description"] =
                "Your Order " + holder.productTitle.text.toString() + " and price ₹" + holder.yourPrice.text.toString() + " Delivered"
            userData1["payment"] = holder.yourPrice.text.toString() + " Payment Added"
            userData1["timeStamp"] = System.currentTimeMillis()
            userData1["read"] = "true"

            FirebaseFirestore.getInstance()
                .collection("BRANCHES")
                .document(model[position].storeId.toString())
                .collection("MY_NOTIFICATION")
                .document(randomString)
                .set(userData1)
                .addOnCompleteListener {

                }
            val userData3: MutableMap<String, Any?> =
                HashMap()
            userData3["id"] = randomString
            userData3["title"] = "Order Delivered"
            userData3["description"] =
                "Your Order " + holder.productTitle.text.toString() + " and price ₹" + holder.yourPrice.text.toString() + " Delivered"
            userData3["payment"] = 80.toString() + " Payment Added"
            userData3["timeStamp"] = System.currentTimeMillis()
            userData3["read"] = "true"
            FirebaseFirestore.getInstance()
                .collection("RIDERS")
                .document(FirebaseAuth.getInstance().uid.toString())
                .collection("MY_NOTIFICATION")
                .document(randomString)
                .set(userData3)
                .addOnCompleteListener {

                }

            val randomString1 = UUID.randomUUID().toString().substring(0, 18)
            val userData12: MutableMap<String, Any?> =
                HashMap()
            userData12["id"] = randomString1
            userData12["title"] = "Order Delivered"
            userData12["description"] = "Your Order " + holder.productTitle.text.toString() + " and price ₹" + holder.productPrice.text.toString() + " Delivered"
            userData12["timeStamp"] = System.currentTimeMillis()
            userData12["read"] = "true"

            FirebaseFirestore.getInstance()
                .collection("USERS")
                .document(model[position].uid)
                .collection("MY_NOTIFICATION")
                .document(randomString1)
                .set(userData12)
                .addOnCompleteListener {

                }

            FirebaseFirestore.getInstance().collection("USERS")
                .document(model[position].uid)
                .get()
                .addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                    val model: UserModel? =
                        documentSnapshot.toObject(UserModel::class.java)

                    val userData2: MutableMap<String, Any?> = HashMap()
                    userData2["notificationSize"] =
                        (model?.notificationSize.toString().toInt() + 1).toString()

                    FirebaseFirestore.getInstance()
                        .collection("USERS")
                        .document(holder.userUid.text.toString())
                        .update(userData2)
                        .addOnCompleteListener {
                            canceledDialog.dismiss()
                        }

                })

            val userData: MutableMap<String, Any?> =
                HashMap()
            userData["delivery"] = "Delivered"
            userData["deliveredDate"] = System.currentTimeMillis().toString()
            FirebaseFirestore.getInstance()
                .collection("ORDER")
                .document(model[position].id)
                .update(userData)
                .addOnCompleteListener {
                    notifyDataSetChanged()
                }
        }
        //////Order Confirmed

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
        var storeId: TextView = itemView.findViewById<TextView?>(R.id.storeId)

        var canceledBtn: AppCompatButton = itemView.findViewById<AppCompatButton?>(R.id.canceledBtn)
        var deliveryBtn: AppCompatButton = itemView.findViewById<AppCompatButton?>(R.id.deliveryBtn)

        var storeAddress: TextView = itemView.findViewById<TextView?>(R.id.shop_address)
        var storeNumber: TextView = itemView.findViewById<TextView?>(R.id.shop_phone)

        var userAddress: TextView = itemView.findViewById<TextView?>(R.id.user_address)
        var userNumber: TextView = itemView.findViewById<TextView?>(R.id.user_phone)
        var userName: TextView = itemView.findViewById<TextView?>(R.id.user_name)

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

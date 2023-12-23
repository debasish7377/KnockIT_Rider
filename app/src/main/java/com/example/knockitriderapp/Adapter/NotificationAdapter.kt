package com.example.knockitriderapp.Adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.knockitriderapp.Model.NotificationModel
import com.example.knockitriderapp.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Date

class NotificationAdapter(var context: Context, var model: List<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.viewHolder>() {

    lateinit var notificationDialog: Dialog
    lateinit var notificationDesc: TextView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        var view: View =
            LayoutInflater.from(context).inflate(R.layout.item_notifications, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {

        holder.title.text = model[position].title
        holder.desc.text = model[position].description
        val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
        val netDate = Date(model[position].timeStamp)
        val date = sdf.format(netDate)
        holder.timing.text = "Valid - " + date
        if (!model[position].payment.toString().equals("")){
            holder.paymentText.visibility = View.VISIBLE
            holder.paymentText.text = model[position].payment.toString()
        }else{
            holder.paymentText.visibility = View.GONE
        }

        ////////////////loading dialog
        notificationDialog = Dialog(context)
        notificationDialog?.setContentView(R.layout.dialog_notification)
        notificationDialog?.setCancelable(true)
        notificationDialog?.window?.setBackgroundDrawable(context.getDrawable(R.drawable.btn_buy_now))
        notificationDialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        notificationDesc = notificationDialog.findViewById(R.id.descriptionText)
        ////////////////loading dialog

        if (model[position].read.equals("true")) {
            holder.notificationBg.setBackgroundTintList(
                context.getResources().getColorStateList(R.color.white)
            );
            holder.notificationBg.backgroundTintList = context.getColorStateList(R.color.white)

        } else {
            holder.notificationBg.setBackgroundTintList(
                context.getResources().getColorStateList(R.color.clickedBg)
            );
            holder.notificationBg.backgroundTintList = context.getColorStateList(R.color.clickedBg)
        }


        holder.itemView.setOnClickListener {
            if (model[position].read.equals("true")){
                notificationDialog.show()
                notificationDesc.text = model[position].description

                val userData: MutableMap<String, Any?> = HashMap()
                userData["read"] = "false"
                FirebaseFirestore.getInstance()
                    .collection("BRANCHES")
                    .document(FirebaseAuth.getInstance().uid.toString())
                    .collection("MY_NOTIFICATION")
                    .document(model[position].id)
                    .update(userData)
                    .addOnCompleteListener {

                    }

            }else{
                notificationDialog.show()
                notificationDesc.text = model[position].description
            }
        }

    }

    override fun getItemCount(): Int {
        return model.size
    }

    class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView.findViewById<TextView?>(R.id.notificationTitle)
        var desc: TextView = itemView.findViewById<TextView?>(R.id.notificationDesc)
        var timing: TextView = itemView.findViewById<TextView?>(R.id.notificationTime)
        var paymentText: TextView = itemView.findViewById<TextView?>(R.id.paymentText)
        var notificationBg: ConstraintLayout =
            itemView.findViewById<ConstraintLayout?>(R.id.notificationBg)
    }
}
package com.example.knockitriderapp.Activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.knockitriderapp.Model.RiderModel
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.ActivityUpdateProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class UpdateProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityUpdateProfileBinding
    lateinit var filePath: Uri
    var updatePhoto: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        val view: View = binding.getRoot()
        setContentView(view)

        FirebaseFirestore.getInstance()
            .collection("RIDERS")
            .document(FirebaseAuth.getInstance().uid.toString())
            .addSnapshotListener { querySnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
                querySnapshot?.let {
                    val userModel = it.toObject(RiderModel::class.java)

                    try {
                        if (userModel?.profile.equals("")) {
                            Glide.with(this).load(R.drawable.avatara)
                                .into(binding.profileImage)
                        } else {
                            Glide.with(this).load(userModel?.profile.toString())
                                .placeholder(R.drawable.avatara)
                                .into(binding.profileImage)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    binding.name.text = Editable.Factory.getInstance().newEditable(userModel?.name.toString())
                    binding.email.text = userModel?.email
                    binding.phone.text = "+91 "+userModel?.number

                }
            }

        binding.profileImage.setOnClickListener {
            Dexter.withContext(this@UpdateProfileActivity)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(
                            Intent.createChooser(intent, "Select Picture"),
                            1
                        )
                    }

                    override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {}
                    override fun onPermissionRationaleShouldBeShown(
                        permissionRequest: PermissionRequest?,
                        permissionToken: PermissionToken
                    ) {
                        permissionToken.continuePermissionRequest()
                    }
                })
                .check()
        }

        binding.updateBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.updateBtn.visibility = View.GONE
            if (!binding.name.text.equals("")) {
                if (updatePhoto) {

                    var reference: StorageReference =
                        FirebaseStorage.getInstance().getReference()
                            .child("profiles").child(
                                FirebaseAuth.getInstance().getUid()
                                    .toString()
                            );
                    reference.putFile(filePath).addOnCompleteListener {
                        reference.downloadUrl.addOnSuccessListener { profileImage ->
                            val userData: MutableMap<String, Any?> =
                                HashMap()
                            userData["name"] =
                                binding.name.text.toString()
                            userData["profile"] = profileImage

                            FirebaseFirestore.getInstance()
                                .collection("RIDERS")
                                .document(FirebaseAuth.getInstance().uid.toString())
                                .update(userData)
                                .addOnCompleteListener {
                                    binding.progressBar.visibility =
                                        View.GONE
                                    binding.updateBtn.visibility = View.VISIBLE
                                    Toast.makeText(
                                        this,
                                        "Profile Updated Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                } else {

                    val userData: MutableMap<String, Any?> =
                        HashMap()
                    userData["name"] =
                        binding.name.text.toString()
                    FirebaseFirestore.getInstance()
                        .collection("RIDERS")
                        .document(FirebaseAuth.getInstance().uid.toString())
                        .update(userData)
                        .addOnCompleteListener {
                            binding.progressBar.visibility =
                                View.GONE
                            binding.updateBtn.visibility = View.VISIBLE
                            Toast.makeText(
                                this,
                                "Profile Updated Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
            }else{
                Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.updateBtn.visibility = View.VISIBLE
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //var bitmapImage: Bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data?.data!!)
            filePath = data?.data!!
            updatePhoto = true
//
//            var byteArrOutputStream = ByteArrayOutputStream()
//            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50 , byteArrOutputStream)
//            var bytesArray: ByteArray = byteArrOutputStream.toByteArray()
//            compressedImage = BitmapFactory.decodeByteArray(bytesArray, 0 , bytesArray.size)
//
//            filePath = MediaStore.Images.Media.insertImage(this.contentResolver, compressedImage,"erg","reg").toUri()
            Glide.with(this).load(filePath).into(binding.profileImage)
        }
    }
}
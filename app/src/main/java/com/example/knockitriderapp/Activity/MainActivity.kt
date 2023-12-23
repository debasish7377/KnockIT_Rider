package com.example.knockitriderapp.Activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var drivingLicenceImage_1: Uri
    lateinit var drivingLicenceImage_2: Uri
    var updateDrivingLicenceImage_1: Boolean = false
    var updateDrivingLicenceImage_2: Boolean = false
    lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.getRoot()
        setContentView(view)

        ////////////////loading dialog
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.dialog_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        ////////////////loading dialog

        binding.createAccount.OkBtn.setOnClickListener {
            binding.createAccount.OkBtn.visibility = View.GONE
            if (updateDrivingLicenceImage_1) {
                if (updateDrivingLicenceImage_2) {
                    if (!binding.createAccount.drivingLicence.text.isEmpty()) {
                        if (!binding.createAccount.accountNumber.text.toString().isEmpty()) {
                            if (!binding.createAccount.accountHolderName.text.toString().isEmpty()) {
                                if (!binding.createAccount.bankName.text.toString().isEmpty()) {
                                    if (!binding.createAccount.ifscCode.text.toString()
                                            .isEmpty()
                                    ) {

                                            loadingDialog.show()
                                            var reference: StorageReference =
                                                FirebaseStorage.getInstance().getReference()
                                                    .child("profiles").child(
                                                        FirebaseAuth.getInstance().getUid()
                                                            .toString()
                                                    );
                                            reference.putFile(drivingLicenceImage_1).addOnCompleteListener {
                                                reference.downloadUrl.addOnSuccessListener { profileImage ->

                                                    var uploadStoreImage: StorageReference =
                                                        FirebaseStorage.getInstance().getReference()
                                                            .child("StoreImage")
                                                            .child(
                                                                FirebaseAuth.getInstance().getUid()
                                                                    .toString()
                                                            );
                                                    uploadStoreImage.putFile(drivingLicenceImage_2)
                                                        .addOnCompleteListener {
                                                            uploadStoreImage.downloadUrl.addOnSuccessListener { storeImage ->

                                                                val userData: MutableMap<String, Any?> =
                                                                    HashMap()
                                                                userData["drivingLicence"] =
                                                                    binding.createAccount.drivingLicence.text.toString()
                                                                userData["drivingLicenceImage_1"] = profileImage
                                                                userData["drivingLicenceImage_2"] = storeImage
                                                                userData["bankAccountNumber"] =
                                                                    binding.createAccount.accountNumber.text.toString()
                                                                userData["bankName"] =
                                                                    binding.createAccount.bankName.text.toString()
                                                                userData["bankHolderName"] = binding.createAccount.accountHolderName.text.toString()
                                                                userData["bankIFSCCode"] = binding.createAccount.ifscCode.text.toString()

                                                                FirebaseFirestore.getInstance()
                                                                    .collection("RIDERS")
                                                                    .document(FirebaseAuth.getInstance().uid.toString())
                                                                    .update(userData)
                                                                    .addOnCompleteListener {

                                                                        loadingDialog.dismiss()
                                                                        Toast.makeText(
                                                                            this,
                                                                            "Your Profile Under Review",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                        startActivity(
                                                                            Intent(
                                                                                this,
                                                                                DashboardActivity::class.java
                                                                            )
                                                                        )
                                                                        finish()
                                                                        binding.createAccount.OkBtn.visibility =
                                                                            View.VISIBLE
                                                                    }

                                                            }
                                                        }

                                                }
                                            }
                                    } else {
                                        binding.createAccount.ifscCode.error = "Select Category"
                                        binding.createAccount.ifscCode.setText("")
                                        binding.createAccount.OkBtn.visibility = View.VISIBLE
                                    }
                                } else {
                                    binding.createAccount.bankName.error = "Address"
                                    binding.createAccount.bankName.setText("")
                                    binding.createAccount.OkBtn.visibility = View.VISIBLE
                                }
                            } else {
                                binding.createAccount.accountHolderName.error = "Enter Store Description"
                                binding.createAccount.accountHolderName.setText("")
                                binding.createAccount.OkBtn.visibility = View.VISIBLE
                            }
                        } else {
                            binding.createAccount.accountNumber.error = "Enter Account Number"
                            binding.createAccount.accountNumber.setText("")
                            binding.createAccount.OkBtn.visibility = View.VISIBLE
                        }
                    } else {
                        binding.createAccount.drivingLicence.error = "Enter Driving licence"
                        binding.createAccount.drivingLicence.setText("")
                        binding.createAccount.OkBtn.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this, "Upload Photo", Toast.LENGTH_SHORT).show()
                    binding.createAccount.OkBtn.visibility = View.VISIBLE
                }

            } else {
                Toast.makeText(this, "Upload photo", Toast.LENGTH_SHORT).show()
                binding.createAccount.OkBtn.visibility = View.VISIBLE
            }
        }

        binding.drivingLicenceImage1.setOnClickListener {
            Dexter.withContext(this@MainActivity)
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

        binding.drivingLicenceImage2.setOnClickListener {
            Dexter.withContext(this@MainActivity)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(
                            Intent.createChooser(intent, "Select Picture"),
                            2
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //var bitmapImage: Bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data?.data!!)
            drivingLicenceImage_1 = data?.data!!
            updateDrivingLicenceImage_1 = true
//
//            var byteArrOutputStream = ByteArrayOutputStream()
//            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50 , byteArrOutputStream)
//            var bytesArray: ByteArray = byteArrOutputStream.toByteArray()
//            compressedImage = BitmapFactory.decodeByteArray(bytesArray, 0 , bytesArray.size)
//
//            filePath = MediaStore.Images.Media.insertImage(this.contentResolver, compressedImage,"erg","reg").toUri()
            Glide.with(this).load(drivingLicenceImage_1).into(binding.drivingLicenceImage1)
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            drivingLicenceImage_2 = data?.data!!
            updateDrivingLicenceImage_2 = true
//            var bitmapImage: Bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data?.data!!)
//            updateStoreImage = true
//            var byteArrOutputStream = ByteArrayOutputStream()
//            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50 , byteArrOutputStream)
//            var bytesArray = byteArrOutputStream.toByteArray()
//            compressedImage = BitmapFactory.decodeByteArray(bytesArray, 0 , bytesArray.size)
//
//            storeImagePath = MediaStore.Images.Media.insertImage(this.contentResolver, compressedImage,"erg","reg").toUri()
            Glide.with(this).load(drivingLicenceImage_2).into(binding.drivingLicenceImage2)
        }
    }
}
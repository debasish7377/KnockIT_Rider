package com.example.knockitbranchapp.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.knockitriderapp.Activity.RegisterActivity
import com.example.knockitriderapp.R
import com.example.knockitriderapp.databinding.FragmentPasswordResetBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class PasswordResetFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentPasswordResetBinding = FragmentPasswordResetBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        binding.resetButton.setOnClickListener(View.OnClickListener {
            binding.emailText.setText("Processing.....")
            binding.emailText.setTextColor(resources.getColor(R.color.black))
            binding.resetButton.setTextColor(resources.getColor(R.color.gray))
            binding.resetButton.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            auth.sendPasswordResetEmail(binding.resetEmail.getText().toString())
                .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "successfully reset link sent to your registered email", Toast.LENGTH_SHORT).show()
                        binding.emailText.setText("successfully reset link sent to your registered email")
                        binding.emailText.setTextColor(resources.getColor(R.color.green))
                        startActivity(Intent(context, RegisterActivity::class.java))
                    } else {
                        val error = task.exception!!.message
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        binding.emailText.setText(error)
                        binding.emailText.setTextColor(resources.getColor(R.color.red))
                    }
                    binding.resetButton.setEnabled(true)
                    binding.resetButton.setTextColor(resources.getColor(R.color.gray))
                    binding.resetButton.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                })
        })

        return binding.root
    }
}
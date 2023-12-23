package com.example.knockitriderapp.Activity

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.knockitbranchapp.Fragment.LoginFragment
import com.example.knockitbranchapp.Fragment.SignupFragment
import com.example.knockitriderapp.R

class RegisterActivity : AppCompatActivity() {

    companion object {
        lateinit var frameLayout: FrameLayout
    }
    var onresetPassword = false
    var signUpFragment = false
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        frameLayout = findViewById(R.id.frame_layout2)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        if (signUpFragment) {
            signUpFragment = false
            setFragment(SignupFragment())
        } else {
            setFragment(LoginFragment())
        }
    }

    fun setFragment(fragment: Fragment?) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slideout_from_left)
        if (fragment != null) {
            fragmentTransaction.replace(RegisterActivity.frameLayout.getId(), fragment)
        }
        fragmentTransaction.commit()
    }
}
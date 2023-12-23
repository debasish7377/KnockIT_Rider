package com.example.knockitriderapp.Activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.ScaleAnimation
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.knockitbranchapp.Service.MyServices
import com.example.knockitriderapp.R
import com.example.knockitriderapp.Service.RunningApp
import com.example.knockitriderapp.databinding.ActivitySplashscreenBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class SplashscreenActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var binding: ActivitySplashscreenBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashscreenBinding.inflate(layoutInflater)
        val view: View = binding.getRoot()
        setContentView(view)

//        Intent(applicationContext, RunningApp::class.java).also {
//            it.action = MyServices.Actions.START.toString()
//            startService(it)
//        }

        var zoom = ScaleAnimation(0f, 1f, 1f, 1f)
        zoom.setDuration(1000)

// Start the animation like this
        binding.imageView3.startAnimation(zoom)
        Intent(applicationContext, RunningApp::class.java).also {
            it.action = MyServices.Actions.START.toString()
            startForegroundService(it)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val time: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (firebaseAuth.currentUser == null) {
                        startActivity(Intent(this@SplashscreenActivity, RegisterActivity::class.java))
                    } else {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                                    return@OnCompleteListener
                                }

                                val token = task.result
                                val sharedPreferences =
                                    getSharedPreferences("MySharedPref", MODE_PRIVATE)

                                val myEdit = sharedPreferences.edit()

                                myEdit.putString("token", token)

                                myEdit.commit()
                                //Toast.makeText(this@SplashscreenActivity, token.toString(), Toast.LENGTH_SHORT).show()

                                FirebaseMessaging.getInstance().subscribeToTopic("your_topic_name")
                                FirebaseMessaging.getInstance().setAutoInitEnabled(true)
                                FirebaseFirestore.getInstance().collection("RIDERS")
                                    .document(firebaseAuth.currentUser?.uid.toString())
                                    .update(
                                        "Last seen", FieldValue.serverTimestamp(),
                                        "token",token
                                    )
                                    .addOnCompleteListener { }
                                startActivity(Intent(this@SplashscreenActivity, PermissionActivity::class.java))
                                finish()
                            })
                    }
                }
            }
        }
        time.start()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("CHANNEL_ID", "My Foreground Service", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

//        var intent = Intent(this, MyServices::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            applicationContext.startForegroundService(intent)
//        }else{
//            this@SplashscreenActivity.startService(intent)
//        }
        //ServiceCaller(intent);
    }

    private fun ServiceCaller(intent: Intent) {
        stopService(intent)

//        Integer alarmHour = timePicker.getCurrentHour();
//        Integer alarmMinute = timePicker.getCurrentMinute();

//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//
//
//                        }
//                    }
//                });
        val time: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                } finally {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        this@SplashscreenActivity.startForegroundService(intent)
                    }else{
                        startService(intent)
                    }
                    //startService(intent)
                }
            }
        }
        time.start()
    }
}
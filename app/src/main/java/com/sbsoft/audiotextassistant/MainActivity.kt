package com.sbsoft.audiotextassistant

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.FOCUS_UP
import android.widget.Button
import android.widget.ScrollView
import android.widget.ViewFlipper
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sbsoft.audiotextassistant.Constants.TIMEOUT_DATE_STR
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         val activityResultCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    if (checkEnabled()) finish()
                }
            }

        val btnAbilita = findViewById<Button>(R.id.btnAbilita)
        btnAbilita.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activityResultCallback.launch(intent)
        }

        val btnClose = findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener {
            finish()
        }

        val btnExit = findViewById<Button>(R.id.btnExit)
        btnExit.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val vflipper = findViewById<ViewFlipper>(R.id.vflipper)

        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ITALIAN)
        val finalDate: Date? = sdf.parse(TIMEOUT_DATE_STR)

        val timestamp = System.currentTimeMillis()
        val currentDate = Date(timestamp)

        if (currentDate > finalDate) {
            vflipper.displayedChild = 2  //service is outdated
        } else {
            if (checkEnabled()) {
                vflipper.displayedChild = 1
            } else {
                vflipper.displayedChild = 0
            }
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollview)
        scrollView.fullScroll(FOCUS_UP)
    }

    private  fun checkEnabled():Boolean {
        val settingValue = Settings.Secure.getString(
            this.applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        settingValue?.let {
            if (settingValue.contains(MyAccessibilityService::class.java.simpleName)) {
                return true
            }
        }
        return false
    }

}
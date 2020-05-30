package com.pru.notes.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.pru.notes.R
import com.pru.notes.help.Constants
import com.pru.notes.help.Constants.getSharedPref
import com.pru.notes.help.Constants.hideKeyboard
import com.pru.notes.listeners.SecureDigitListener
import kotlinx.android.synthetic.main.pin_check_activity.*


class PinCheckActivity : AppCompatActivity() {


    private var password: String = ""
    private var isForRegister: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pin_check_activity)
        if (intent.hasExtra("Register") && intent.getBooleanExtra("Register", false)) {
            isForRegister = true
        }
        secure_digits.setListener(object : SecureDigitListener {
            override fun lastDigitChecked() {
                val handler = Handler()
                handler.postDelayed({
                    bn_submit.performClick()
                }, Constants.startActivityDelayTimeMilliS)
            }

        })
        if (isForRegister) {
            user_msg.setText("You can secure your notes by creating the \n4 Digit Pin")
            bn_submit.setText("Next")
        } else {
            user_msg.setText("Please enter your 4 Digit Pin")
            bn_submit.setText("Submit")
        }
        onClickListener()
    }

    private fun onClickListener() {
        pin_back.setOnClickListener {
            if (isForRegister) {
                if (bn_submit.text.toString().equals("Submit")) {
                    password=""
                    user_msg.setText("You can secure your notes by creating the \n" +
                            "4 Digit Pin")
                    bn_submit.setText("Next")
                    secure_digits.clearDigits()
                    val animate = TranslateAnimation(
                        secure_digits.width.toFloat(),
                        0f,
                        0f,
                        0f
                    )
                    animate.duration = 500
                    animate.fillAfter = true
                    secure_digits.startAnimation(animate)
                } else{
                    onBackPressed()
                }
            } else {
                onBackPressed()
            }
        }
        bn_submit.setOnClickListener {
            if (isForRegister) {
                if (bn_submit.text.toString().equals("Submit")) {

                    if (password.isNotEmpty() && password.equals(secure_digits.number)) {
                        val editor: SharedPreferences.Editor = getSharedPref().edit()
                        editor.putString(getString(R.string.pin_number), password)
                        editor.putBoolean(getString(R.string.is_pin_enabled), true)
                        editor.apply()
                        editor.commit()
                        setResult(Activity.RESULT_OK)
                        onBackPressed()
                    } else {
                        Constants.showSnack(
                            "Pins doesn't match, please check",
                            touch_root,
                            this@PinCheckActivity
                        )
                        secure_digits.clearDigits()
                    }
                } else {
                    if (secure_digits.number.length == 4) {
                        password = secure_digits.number
                        user_msg.setText("Confirm the 4 Digit Pin")
                        bn_submit.setText("Submit")
                        secure_digits.clearDigits()
                        val animate = TranslateAnimation(
                            secure_digits.width.toFloat(),
                            0f,
                            0f,
                            0f
                        )
                        animate.duration = 500
                        animate.fillAfter = true
                        secure_digits.startAnimation(animate)
                    } else {
                        Constants.showSnack(
                            "Please enter the pin",
                            touch_root,
                            this@PinCheckActivity
                        )
                    }
                }
            } else {
                if (secure_digits.number.equals(
                        getSharedPref().getString(
                            getString(R.string.pin_number),
                            "xxxx"
                        )
                    )
                ) {
                    isForRegister = true
                    onBackPressed()
                } else {
                    Constants.showSnack("Please check the pin", touch_root, this@PinCheckActivity)
                    secure_digits.clearDigits()
                }
            }
        }
        bn_clear.setOnClickListener {
            secure_digits.clearDigits()
        }
    }

    override fun onBackPressed() {
        if (isForRegister) {
            pin_back.hideKeyboard()
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
        }
        return true
    }
}

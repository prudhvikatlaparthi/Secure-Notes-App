package com.pru.notes.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.pru.notes.R
import com.pru.notes.help.Constants
import com.pru.notes.help.Constants.getSharedPref
import com.pru.notes.help.CustomDialog
import com.pru.notes.help.RequestCodes.Companion.PIN_CHECK_ACTIVITY_REQUEST_CODE
import com.pru.notes.listeners.ImagePickerListener
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.tool_bar.*


abstract class BaseActivity : AppCompatActivity() {
    private var customDialog: CustomDialog? = null
    private lateinit var snackbar: Snackbar
    lateinit var myInflater: LayoutInflater
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        myInflater = this.layoutInflater
        onClickListeners()
        initialize()
        setToolbar()
        checkToggle()
    }

    abstract fun initialize()

    private fun setToolbar() {
        if (this@BaseActivity is NotesListActivity) {
            tb_back.visibility = VISIBLE
            tb_save.visibility = GONE
            tb_toggle_anim.visibility = View.VISIBLE
            val layoutParams =
                RelativeLayout.LayoutParams(convertDpsToPixels(50), convertDpsToPixels(50))
            tb_back.setLayoutParams(layoutParams)
            tb_back.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notes))
        } else if (this@BaseActivity is AddEditNoteActivity) {
            tb_back.visibility = VISIBLE
            tb_save.visibility = VISIBLE
            tb_toggle_anim.visibility = GONE
        }
    }

    private fun onClickListeners() {
        tb_toggle_anim.setOnClickListener {
            if (tb_toggle_anim.tag != null && tb_toggle_anim.tag.equals("false")) {
                tb_toggle_anim.tag = "true"
                tb_toggle_anim.setMinAndMaxProgress(0.0f, 0.5f)
                tb_toggle_anim.playAnimation()
                startPinRegisterActivity()
            } else {
                tb_toggle_anim.tag = "false"
                tb_toggle_anim.setMinAndMaxProgress(0.5f, 1.0f)
                tb_toggle_anim.playAnimation()
                showAlert(
                    "Alert",
                    "Do you need to disable pin verification",
                    "Yes",
                    "NO",
                    "Pin_Disable",
                    true
                )
            }
        }

        tb_back.setOnClickListener {
            if (this@BaseActivity is AddEditNoteActivity) {
                onBackPressed()
            } else {

            }
        }

        tb_save.setOnClickListener {
            if (this@BaseActivity is AddEditNoteActivity) {
                this@BaseActivity.saveNote()
            }
        }
    }

    private fun startPinRegisterActivity() {
        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(baseContext, PinCheckActivity::class.java)
            intent.putExtra("Register", true)
            startActivityForResult(
                intent, PIN_CHECK_ACTIVITY_REQUEST_CODE
            )
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, Constants.startActivityDelayTimeMilliS)
    }

    fun showSnackMessage(msg: String) {

        Constants.showSnack(msg, llBody.getChildAt(0),this@BaseActivity)
        /*snackbar.setDuration(3000)
        snackbar.setAction("ok", object : View.OnClickListener {
            override fun onClick(view: View?) {
                snackbar.dismiss()
            }
        })
        snackbar.setActionTextColor(Color.YELLOW)

        val v: View = snackbar.getView()
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        val txt: TextView = v.findViewById(com.google.android.material.R.id.snackbar_text)
        txt.setTextColor(ContextCompat.getColor(this, R.color.white))

        snackbar.show()*/

    }

    override fun onResume() {
        super.onResume()
        bg_anim.playAnimation()
    }

    private fun checkToggle() {
        if (getSharedPref().getBoolean(getString(R.string.is_pin_enabled), false)) {
            tb_toggle_anim.tag = "true"
            tb_toggle_anim.setMinAndMaxProgress(0.0f, 0.5f)
            tb_toggle_anim.playAnimation()
        } else {
            tb_toggle_anim.tag = "false"
            tb_toggle_anim.setMinAndMaxProgress(0.5f, 1.0f)
            tb_toggle_anim.playAnimation()
        }
    }

    override fun onPause() {
        super.onPause()
        bg_anim.cancelAnimation()
    }

    open fun convertDpsToPixels(dps: Int): Int {
        val scale: Float = getResources().getDisplayMetrics().density
        return (dps * scale + 0.5f).toInt()
    }

    open fun showCaptureOptions(imagePickerListener: ImagePickerListener?) {
        val drawerPopUp = myInflater.inflate(R.layout.attachment_popup, null) as LinearLayout
        val cameraLayout =
            drawerPopUp.findViewById<View>(R.id.camera_layout) as LinearLayout
        val galleryLayout =
            drawerPopUp.findViewById<View>(R.id.gallery_layout) as LinearLayout
        val customDialog = CustomDialog(
            this@BaseActivity,
            drawerPopUp,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        customDialog.setCancelable(true)
        customDialog.show()
        cameraLayout.setOnClickListener {
            customDialog.dismiss()
            if (imagePickerListener != null) {
                imagePickerListener.isPicked(true)
            }
        }
        galleryLayout.setOnClickListener {
            customDialog.dismiss()
            if (imagePickerListener != null) {
                imagePickerListener.isPicked(false)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (!(this@BaseActivity is NotesListActivity)) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    open fun showAlert(
        title: String,
        message: String,
        firstBtnName: String,
        secondBtnName: String,
        from: String,
        isCancel: Boolean
    ) {
        val view = myInflater.inflate(R.layout.custom_pop, null) as LinearLayout

        customDialog = CustomDialog(
            this@BaseActivity,
            view, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            isCancel
        )
        customDialog!!.setCancelable(true)
        val tvTitle = view.findViewById<View>(R.id.tvTitlePopup)
        val ivDivider: View = view.findViewById(R.id.ivDividerPopUp)
        val view_middle: View = view.findViewById(R.id.view_middle)
        val tvMessage = view.findViewById<View>(R.id.tvMessagePopup) as TextView
        val btnYes = view.findViewById<View>(R.id.btnYesPopup) as Button
        val btnNo = view.findViewById<View>(R.id.btnNoPopup) as Button
        tvTitle.visibility = GONE
        ivDivider.visibility = GONE

        tvMessage.setText("" + message)
        btnYes.setText("" + firstBtnName)
        if (TextUtils.isEmpty(secondBtnName)) {
            btnNo.visibility = GONE
            view_middle.visibility = GONE
        }

        btnYes.setOnClickListener {
            customDialog!!.dismiss()
            onButtonYesClick(from)
        }

        btnNo.setOnClickListener {
            customDialog!!.dismiss()
            onButtonNoClick(from)
        }
        try {
            if (!customDialog!!.isShowing) customDialog!!.showCustomDialog()
        } catch (e: Exception) {
            throw RuntimeException("This can never happen", e)
        }
    }

    open fun onButtonNoClick(from: String) {
        checkToggle()
    }

    open fun onButtonYesClick(from: String) {
        if (from.equals("Pin_Disable")) {
            val editor: SharedPreferences.Editor = getSharedPref().edit()
            editor.putString(getString(R.string.pin_number), "")
            editor.putBoolean(getString(R.string.is_pin_enabled), false)
            editor.apply()
            editor.commit()
            checkToggle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_CHECK_ACTIVITY_REQUEST_CODE) {
            checkToggle()
        }
    }
}

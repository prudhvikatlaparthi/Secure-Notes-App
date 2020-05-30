package com.pru.notes.help

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.pru.notes.R


object Constants {
    val timerCount: Long = 30000 // 30 sec
    val startActivityDelayTimeMilliS: Long = 240

    fun Context.getSharedPref(): SharedPreferences {
        return this.getSharedPreferences(
            getString(R.string.preferences_file),
            Context.MODE_PRIVATE
        )
    }

    fun showSnack(msg: String, view: View, context: Context) {
        /*val snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE)
        snackbar.setDuration(2000)
        snackbar.setAction("ok", object : View.OnClickListener {
            override fun onClick(view: View?) {
                snackbar.dismiss()
            }
        })
        snackbar.setActionTextColor(Color.YELLOW)

        val v: View = snackbar.getView()
        v.setBackgroundColor(Color.parseColor("#222B4C"))
        val txt: TextView = v.findViewById(com.google.android.material.R.id.snackbar_text)
        txt.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.config(context)
        snackbar.show()*/
        val snack = Snackbar.make(
            view,
            msg,
            Snackbar.LENGTH_SHORT
        )
        val txt: TextView = snack.view.findViewById(com.google.android.material.R.id.snackbar_text)
        txt.setTextColor(context.resources.getColor(R.color.blue))
        txt.setTextSize(17f)
        val typeface = ResourcesCompat.getFont(context, R.font.bold_text)
        txt.setTypeface(typeface)
        snack.config(context)
        snack.show()
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun Snackbar.config(context: Context) {
        val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(50, 50, 50, 50)
        this.view.layoutParams = params

        this.view.background = context.getDrawable(R.drawable.snack_bg)

        ViewCompat.setElevation(this.view, 6f)
    }
}
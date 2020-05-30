package com.pru.notes.custom_views.security

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.pru.notes.R
import com.pru.notes.listeners.SecureDigitListener
import kotlinx.android.synthetic.main.activity_add_note.*

class DigitView : LinearLayout {
    private var mContext: Context
    lateinit var editView: EditText
    private var position = 0
    private var dataView: LinearLayout? = null
    private var isDeleted = false
    private var count = 0
    private var default :String ="*"
    private var secureDigitListener :SecureDigitListener? = null

    constructor(
        position: Int,
        context: Context,
        dataView: LinearLayout?,
        count: Int,
        default: String,
        secureDigitListener: SecureDigitListener
    ) : super(context) {
        mContext = context
        this.position = position
        this.dataView = dataView
        this.count = count
        this.default = default
        this.secureDigitListener = secureDigitListener
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        mContext = context
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        mContext = context
        init()
    }

    private fun init() {
        val mRootView =
            View.inflate(mContext, R.layout.digit_view, this) as LinearLayout
        editView = mRootView.findViewById(R.id.edit_view)
        editView.hint= default
        editView.addTextChangedListener(textWatcher)
        editView.setOnKeyListener(OnKeyListener { v, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@OnKeyListener true
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                isDeleted = true
                if (position != 0 && position - 1 < dataView!!.childCount) {
                    val DigitView =
                        dataView!!.getChildAt(position - 1) as DigitView
                    getFocus(true, DigitView)
                }
            } else {
                isDeleted = false
            }
            false
        })
    }

    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
        }

        override fun afterTextChanged(s: Editable) {
            if (position + 1 < dataView!!.childCount && !isDeleted) {
                val DigitView =
                    dataView!!.getChildAt(position + 1) as DigitView
                getFocus(false, DigitView)
            }
            if (position == count - 1 && !isDeleted) {
                hideKeyboard()
                secureDigitListener?.lastDigitChecked()
            }
        }
    }

    fun hideKeyboard() {
        val activity = mContext as Activity
        val imm =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getFocus(clear: Boolean, DigitView: DigitView) {
        val temp :EditText = DigitView.editView
        if (clear) {
            temp.requestFocus()
            temp.setSelection(temp.text.length)
        } else {
            temp.requestFocus()
        }
    }

    fun clearOTP() {
        editView.removeTextChangedListener(textWatcher)
        editView.setText("")
        editView.addTextChangedListener(textWatcher)
    }
}
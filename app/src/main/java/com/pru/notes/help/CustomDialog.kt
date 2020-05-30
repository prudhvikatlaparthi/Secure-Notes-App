package com.pru.notes.help

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.pru.notes.R
import com.pru.notes.activities.BaseActivity
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


class CustomDialog : Dialog {
    var isCancellable = true

    private var baseActivity: WeakReference<BaseActivity?>? = null

    constructor(context: Context?, view: View?) : super(
        context,
        R.style.Dialog
    ) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(view)
        if (context is BaseActivity) baseActivity =
            WeakReference<BaseActivity?>(context as BaseActivity?)
    }

    constructor(
        context: Context?,
        view: View?,
        lpW: Int,
        lpH: Int
    ) : this(context, view, lpW, lpH, true) {
        if (context is BaseActivity) baseActivity =
            WeakReference<BaseActivity?>(context as BaseActivity?)
    }

    constructor(
        context: Context?,
        view: View?,
        lpW: Int,
        lpH: Int,
        isCancellable: Boolean
    ) : super(context, R.style.Dialog) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val displaymetrics = DisplayMetrics()
        val activity = context as Activity
        activity.windowManager.defaultDisplay.getMetrics(displaymetrics)
        setContentView(view, ViewGroup.LayoutParams((displaymetrics.widthPixels * 0.75).roundToInt(), lpH))
        setlayoutAnimation(context, view)
        this.isCancellable = isCancellable
        if (context is BaseActivity) baseActivity =
            WeakReference<BaseActivity?>(context as BaseActivity?)
    }

    private fun setlayoutAnimation(
        context: Context?,
        view: View?
    ) {
        if (view is LinearLayout) {
            view.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.rc_layout_animation
            )
        } else if (view is RelativeLayout) {
            view.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.rc_layout_animation
            )
        } else if (view is FrameLayout) {
            view.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.rc_layout_animation
            )
        }
    }

    constructor(
        context: Context?,
        view: View?,
        isTrans: Boolean
    ) : super(context, R.style.Dialog) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(view)
        setlayoutAnimation(context, view)
        baseActivity = WeakReference<BaseActivity?>(context as BaseActivity?)
    }

    constructor(
        context: Context?,
        view: View?,
        lpW: Int,
        lpH: Int,
        isCancellable: Boolean,
        style: Int
    ) : super(context, R.style.Dialog) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(view, ViewGroup.LayoutParams(lpW, lpH))
        setlayoutAnimation(context, view)
        this.isCancellable = isCancellable
        if (context is BaseActivity) baseActivity =
            WeakReference<BaseActivity?>(context as BaseActivity?)
    }

    override fun onBackPressed() {
        if (isCancellable) super.onBackPressed()
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
    }

    fun showCustomDialog() {
        try {
            if (baseActivity != null && baseActivity!!.get() != null && !baseActivity!!.get()!!
                    .isFinishing()
            ) show()
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }
}
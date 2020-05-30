package com.pru.notes.help

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.pru.notes.R
import com.pru.notes.activities.PinCheckActivity
import com.pru.notes.help.Constants.getSharedPref


class ApplicationLifecycleHandler : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private val TAG = ApplicationLifecycleHandler::class.java.simpleName
    private var isInBackground = true
    private var activity: Activity? = null
//    private lateinit var a : WeekReference<Activity>

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        Log.i(TAG, "onActivityCreated")
    }

    override fun onActivityStarted(activity: Activity?) {
        this.activity = activity
        Log.i(TAG, "onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity?) {
        Log.i(TAG, "onActivityResumed")
        if (isInBackground) {
            if (activity != null) {

                if (activity.getSharedPref().getBoolean(
                        activity.getString(R.string.is_pin_enabled),
                        false
                    )
                ) {
                    val timeT: Long = activity.getSharedPref()
                        .getLong(activity.getString(R.string.time_millis), 0)
                    val timeS: Long = System.currentTimeMillis()
                    Log.i(TAG, "DIFF " + Math.abs(timeS - timeT) + " S " + timeS + " T " + timeT)
                    if (Math.abs(timeS - timeT) >= Constants.timerCount) {
                        val newIntent = Intent(activity, PinCheckActivity::class.java)
                        activity.startActivity(newIntent)
                        activity.overridePendingTransition(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                    }
                }
            }
            isInBackground = false
        }
    }

    override fun onActivityPaused(activity: Activity?) {
        Log.i(TAG, "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity?) {
        Log.i(TAG, "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) {
        Log.i(TAG, "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity?) {
        Log.i(TAG, "onActivityDestroyed")
        /*if (isInBackground) {
            if (activity != null && !activity.isDestroyed &&
                activity.getSharedPref()
                    .getBoolean(activity.getString(R.string.is_pin_enabled), false)
            ) {
                val editor: SharedPreferences.Editor = activity.getSharedPref().edit()
                val timeD: Long = System.currentTimeMillis()
                Log.i(TAG, "DIFF Add" + timeD + Constants.timerCount)
                editor.putLong(
                    activity.getString(R.string.time_millis),
                    timeD + Constants.timerCount
                )
                editor.apply()
                editor.commit()
            }
        }*/
    }

    override fun onConfigurationChanged(configuration: Configuration?) {
        Log.i(TAG, "onConfigurationChanged")
    }

    override fun onLowMemory() {
        Log.i(TAG, "onLowMemory")
    }

    override fun onTrimMemory(i: Int) {
        Log.i(TAG, "onTrimMemory")
        if (i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            if (activity != null && !activity!!.isDestroyed &&
                activity!!.getSharedPref()
                    .getBoolean(activity!!.getString(R.string.is_pin_enabled), false)
            ) {
                val editor: SharedPreferences.Editor = activity!!.getSharedPref().edit()
                editor.putLong(
                    activity!!.getString(R.string.time_millis),
                    System.currentTimeMillis()
                )
                editor.apply()
                editor.commit()
            }
            isInBackground = true
        }
    }
}
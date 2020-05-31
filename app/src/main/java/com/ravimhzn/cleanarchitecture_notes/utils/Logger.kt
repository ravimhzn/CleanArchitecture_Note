package com.ravimhzn.cleanarchitecture_notes.utils

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.ravimhzn.cleanarchitecture_notes.utils.Constants.DEBUG
import com.ravimhzn.cleanarchitecture_notes.utils.Constants.TAG

var isUnitTest = false

fun printLogD(className: String?, message: String) {
    if (DEBUG && !isUnitTest) {
        Log.d(TAG, "$className: $message")
    } else if (DEBUG && isUnitTest) {
        println("$className: $message")
    }
}

/*
    Priorities: Log.DEBUG, Log. etc....
 */
fun cLog(priority: Int, tag: String, msg: String?) {
    if (!DEBUG) {
        Crashlytics.log(priority, tag, msg)
    }
}

package com.ravimhzn.cleanarchitecture_notes.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
fun cLog(msg: String?) {
    if (!DEBUG) {
        msg?.let { FirebaseCrashlytics.getInstance().log(it) }
    }
}

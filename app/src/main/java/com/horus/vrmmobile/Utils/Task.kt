package com.horus.vrmmobile.Utils

import android.os.AsyncTask
import android.os.Build
import android.util.Log

/**
 * Created by mparraga on 24/8/2018.
 */

abstract class Task<T> {
    fun run(): AsyncTask<Void, Void, T> {
        val task = object : AsyncTask<Void, Void, T>() {
            override fun doInBackground(vararg params: Void): T? {
                var result: T? = null
                try {
                    result = task()
                } catch (e: Exception) {
                    Log.e("Error", e.message)
                }

                return result
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            return task.execute()
        }
    }

    abstract fun task(): T
}

package com.webapp.kotlin_webapp_video_editer.videoeditor

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.webapp.kotlin_webapp_video_editer.R


class Message {
    fun message(context: Context?, message: String?) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun messageSnackBar(context: Context?, message: String?, view: View) {
        try {
            // Create a Snackbar
            val snackbar = Snackbar.make(context!!,view, message!!, Snackbar.LENGTH_LONG)

            // Get the Snackbar's layout parameters
            //val params = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams

            // Set the gravity to top
            //params.gravity = Gravity.TOP

            // Set the layout parameters back to the Snackbar

            // Show the Snackbar
            val snackbarView = snackbar.view
            snackbarView.setBackgroundResource(R.drawable.red_back)
            snackbar.setAction("Action", null).show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
package com.icode.rentcar

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import junit.runner.BaseTestRunner.getPreference
import java.sql.ClientInfoStatus

fun Activity.toast(message: String, length: Int = Toast.LENGTH_SHORT): Toast =
    Toast.makeText(applicationContext, message, length).also { it.show() }

fun Fragment.toast(message: String, length: Int = Toast.LENGTH_SHORT): Toast {
  return activity?.toast(message, length) ?: throw Exception("Fragment not attached")
}

fun <T> AppCompatActivity.getSpinnerAdapter(items: List<T>) = ArrayAdapter<T>(this, android.R.layout.simple_spinner_item, items).apply {
  setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
}

fun <T> Fragment.getSpinnerAdapter(items: List<T>) = ArrayAdapter<T>(context, android.R.layout.simple_spinner_item, items).apply {
  setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
}

private fun updateField(collection: String, id: String, field: String, data: String, callback: () -> Unit = {}) {
  FirebaseFirestore.getInstance()
      .collection(collection)
      .document(id)
      .update(field, data)
      .addOnFailureListener { e ->
        Log.w("", "Error adding document", e)
        callback()
      }
}

fun updateVehicleField(id: String, field: String, data: String, callback: () -> Unit = {}) {
  updateField("vehicles", id, field, data, callback)
}

fun updateReservationField(id: String, field: String, data: String, callback: () -> Unit = {}) {
  updateField("reservations", id, field, data, callback)
}

fun showView(view: View, display: Boolean = true) {
  view.visibility = if (display) View.VISIBLE else View.INVISIBLE
}

fun Context.setPreferences(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
  PreferenceManager.getDefaultSharedPreferences(this)
      .edit()
      .block()
      .apply()
}

fun Context.getPreference(block: SharedPreferences.() -> Any) =
    PreferenceManager.getDefaultSharedPreferences(this).block()

fun Context.getUserType() = getPreference { getInt("user_type", USER_TYPE_CLIENT) } as Int
fun Context.getUserId() = getPreference { getString("user_id", "") } as String
fun isUserAdmin(type: Int) = when (type) {
  USER_TYPE_ADMIN -> true
  USER_TYPE_CLIENT -> false
  else -> false
}  // TODO: Update with rel dealer id

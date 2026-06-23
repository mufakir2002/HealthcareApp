package com.healthcare.portal.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Fragment.toast(msg: String) = requireContext().toast(msg)

fun View.snack(msg: String, duration: Int = Snackbar.LENGTH_LONG) =
    Snackbar.make(this, msg, duration).show()

fun View.snackError(msg: String) =
    Snackbar.make(this, msg, Snackbar.LENGTH_LONG).also {
        it.setBackgroundTint(0xFFE74C3C.toInt())
        it.setTextColor(0xFFFFFFFF.toInt())
    }.show()

// Status badge color mapping
fun statusColor(status: String): Int = when(status.lowercase()) {
    "approved"            -> 0xFF00B894.toInt()
    "rejected", "cancelled" -> 0xFFE74C3C.toInt()
    "completed"           -> 0xFF0A6EBD.toInt()
    "pendingverification" -> 0xFF3498DB.toInt()
    "pendingpayment"      -> 0xFFF39C12.toInt()
    else                  -> 0xFF636E72.toInt()
}

fun statusLabel(status: String): String = when(status) {
    "Submitted"           -> "Submitted"
    "PendingPayment"      -> "Pending Payment"
    "PendingVerification" -> "Pending Verification"
    "Approved"            -> "Approved"
    "Rejected"            -> "Rejected"
    "Cancelled"           -> "Cancelled"
    "Completed"           -> "Completed"
    "NoShow"              -> "No Show"
    else                  -> status
}

// Progress pct for status bar
fun statusProgress(status: String): Int = when(status) {
    "Submitted"           -> 20
    "PendingPayment"      -> 40
    "PendingVerification" -> 60
    "Approved"            -> 100
    "Completed"           -> 100
    "Rejected", "Cancelled" -> 100
    else                  -> 10
}

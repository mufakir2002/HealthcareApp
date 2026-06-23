package com.healthcare.portal.data.model

import com.google.gson.annotations.SerializedName

// ── API wrapper ───────────────────────────────────────────────
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val total: Int?
)

// ── Department ────────────────────────────────────────────────
data class Department(
    val id: Int,
    val name: String,
    val description: String?,
    val iconClass: String?,
    val imageUrl: String?,
    val displayOrder: Int,
    val isActive: Boolean
)

// ── Doctor ────────────────────────────────────────────────────
data class Doctor(
    val id: Int,
    val fullName: String,
    val specialty: String?,
    val qualification: String?,
    val photoUrl: String?,
    val isAvailable: Boolean,
    val fee: Double,
    val departmentId: Int
)

// ── Appointment ───────────────────────────────────────────────
data class Appointment(
    val id: Int,
    val appointmentNumber: String,
    val patientName: String?,
    val mobile: String?,
    val departmentName: String?,
    val doctorName: String?,
    val appointmentDate: String,
    val preferredTime: String?,
    val status: String,
    val statusLabel: String?,
    val paymentStatus: String?,
    val amountPaid: Double?,
    val internalNotes: String?,
    val createdAt: String
)

// ── Track Result ──────────────────────────────────────────────
data class TrackResult(
    val appointmentId: Int?,
    val appointmentNumber: String,
    val patientName: String,
    val mobile: String,
    val department: String,
    val doctor: String?,
    val appointmentDate: String,
    val appointmentTime: String?,
    val status: String,
    val statusLabel: String,
    val paymentStatus: String,
    val internalNotes: String?
)

// ── Payment Method ────────────────────────────────────────────
data class PaymentMethod(
    val id: Int,
    val name: String,
    val accountNumber: String?,
    val accountName: String?,
    val instructions: String?,
    val displayOrder: Int
)

// ── Book Appointment Request ──────────────────────────────────
data class BookAppointmentRequest(
    val fullName: String,
    val mobile: String,
    val departmentId: Int,
    val doctorId: Int?,
    val appointmentDate: String,        // yyyy-MM-dd
    val preferredTime: String?,
    val visitType: String = "New Visit",
    val reasonForVisit: String = "General Consultation",
    @SerializedName("isGuestBooking")
    val isGuestBooking: Boolean = true
)

// ── Submit Payment Request ────────────────────────────────────
data class SubmitPaymentRequest(
    val appointmentId: Int,
    val paymentMethodId: Int,
    val transactionReference: String,
    val senderNumber: String,
    val amountPaid: Double
)

// ── Hormuud EVC Request ───────────────────────────────────────
data class HormuudInitiateRequest(
    val appointmentId: Int,
    val appointmentNumber: String,
    val phone: String,
    val amount: Double,
    val paymentMethodId: Int
)

data class HormuudInitiateResponse(
    val success: Boolean,
    val isPending: Boolean,
    val isApproved: Boolean,
    val referenceId: String?,
    val transactionId: String?,
    val message: String?,
    val responseCode: String?
)

data class HormuudStatusResponse(
    val isApproved: Boolean,
    val isPending: Boolean,
    val isFailed: Boolean,
    val message: String?,
    val responseCode: String?
)

// ── Login ─────────────────────────────────────────────────────
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val userType: String?,
    val fullName: String?,
    val message: String?
)

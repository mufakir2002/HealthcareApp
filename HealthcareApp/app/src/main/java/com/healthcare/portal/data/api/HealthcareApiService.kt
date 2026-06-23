package com.healthcare.portal.data.api

import com.healthcare.portal.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface HealthcareApiService {

    // ── Departments ───────────────────────────────────────────
    @GET("api/departments")
    suspend fun getDepartments(): Response<ApiResponse<List<Department>>>

    // ── Doctors by department ─────────────────────────────────
    @GET("Appointment/GetDoctors")
    suspend fun getDoctors(@Query("departmentId") departmentId: Int): Response<List<Doctor>>

    // ── Payment Methods ───────────────────────────────────────
    @GET("api/payment-methods")
    suspend fun getPaymentMethods(): Response<ApiResponse<List<PaymentMethod>>>

    // ── Book appointment ──────────────────────────────────────
    @POST("api/appointments")
    suspend fun bookAppointment(@Body request: BookAppointmentRequest): Response<ApiResponse<Appointment>>

    // ── Submit manual payment ─────────────────────────────────
    @POST("api/payments/submit")
    suspend fun submitPayment(@Body request: SubmitPaymentRequest): Response<ApiResponse<Any>>

    // ── Track appointment ─────────────────────────────────────
    @POST("Home/TrackResult")
    suspend fun trackAppointment(@Body body: Map<String, String>): Response<ApiResponse<TrackResult>>

    // ── My appointments (authenticated) ───────────────────────
    @GET("api/appointments")
    suspend fun getMyAppointments(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Appointment>>>

    // ── Hormuud EVC payment ───────────────────────────────────
    @POST("api/payment/hormuud/initiate")
    suspend fun initiateEvcPayment(@Body request: HormuudInitiateRequest): Response<HormuudInitiateResponse>

    @GET("api/payment/hormuud/status/{referenceId}")
    suspend fun checkEvcStatus(
        @Path("referenceId") referenceId: String,
        @Query("appointmentId") appointmentId: Int
    ): Response<HormuudStatusResponse>

    // ── Auth ──────────────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

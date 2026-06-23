package com.healthcare.portal.ui.booking

import androidx.lifecycle.*
import com.healthcare.portal.data.api.RetrofitClient
import com.healthcare.portal.data.model.*
import kotlinx.coroutines.*

class BookingViewModel : ViewModel() {

    // ── Booking state ─────────────────────────────────────────
    var selectedDeptId   = 0
    var selectedDeptName = ""
    var selectedDoctor: Doctor? = null
    var fullName   = ""
    var mobile     = ""
    var apptDate   = ""
    var apptTime   = ""

    // ── Live data ─────────────────────────────────────────────
    private val _doctors  = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> = _doctors

    private val _loading  = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error    = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _booking  = MutableLiveData<Appointment?>()
    val booking: LiveData<Appointment?> = _booking

    private val _paymentMethods = MutableLiveData<List<PaymentMethod>>()
    val paymentMethods: LiveData<List<PaymentMethod>> = _paymentMethods

    // EVC polling
    private val _evcStatus = MutableLiveData<EvcStatus>()
    val evcStatus: LiveData<EvcStatus> = _evcStatus

    private var pollJob: Job? = null

    // ── Actions ───────────────────────────────────────────────
    fun setDepartment(id: Int, name: String) {
        selectedDeptId   = id
        selectedDeptName = name
    }

    fun setDoctor(doc: Doctor?) {
        selectedDoctor = doc
    }

    fun loadDoctors(departmentId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            try {
                val resp = RetrofitClient.api.getDoctors(departmentId)
                if (resp.isSuccessful) {
                    _doctors.value = resp.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load doctors"
                }
            } catch (e: Exception) {
                _error.value = "Connection error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadPaymentMethods() {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getPaymentMethods()
                if (resp.isSuccessful) _paymentMethods.value = resp.body()?.data ?: emptyList()
            } catch (_: Exception) { }
        }
    }

    fun submitBooking() {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            try {
                val request = BookAppointmentRequest(
                    fullName       = fullName,
                    mobile         = mobile,
                    departmentId   = selectedDeptId,
                    doctorId       = selectedDoctor?.id,
                    appointmentDate = apptDate,
                    preferredTime  = apptTime.ifBlank { null },
                    isGuestBooking = true
                )
                val resp = RetrofitClient.api.bookAppointment(request)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _booking.value = resp.body()?.data
                } else {
                    _error.value = resp.body()?.message ?: "Booking failed"
                }
            } catch (e: Exception) {
                _error.value = "Connection error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // ── Hormuud EVC Payment ───────────────────────────────────
    fun initiateEvcPayment(phone: String, amount: Double, appointmentId: Int, aptNumber: String, pmId: Int) {
        viewModelScope.launch {
            _evcStatus.value = EvcStatus.Sending
            try {
                val resp = RetrofitClient.api.initiateEvcPayment(
                    HormuudInitiateRequest(appointmentId, aptNumber, phone, amount, pmId)
                )
                val body = resp.body()
                if (resp.isSuccessful && body?.success == true) {
                    if (body.isApproved) {
                        _evcStatus.value = EvcStatus.Approved("Payment confirmed!")
                    } else {
                        _evcStatus.value = EvcStatus.WaitingForPin(
                            "Payment prompt sent to $phone. Please enter your EVC password on your phone."
                        )
                        body.referenceId?.let { startPolling(it, appointmentId) }
                    }
                } else {
                    _evcStatus.value = EvcStatus.Failed(body?.message ?: "Payment request failed")
                }
            } catch (e: Exception) {
                _evcStatus.value = EvcStatus.Failed("Connection error: ${e.message}")
            }
        }
    }

    private fun startPolling(referenceId: String, appointmentId: Int) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            repeat(24) { attempt ->
                delay(5000)
                try {
                    val resp = RetrofitClient.api.checkEvcStatus(referenceId, appointmentId)
                    val body = resp.body()
                    when {
                        body?.isApproved == true -> {
                            _evcStatus.value = EvcStatus.Approved("Payment confirmed! Your appointment is approved.")
                            return@launch
                        }
                        body?.isFailed == true -> {
                            _evcStatus.value = EvcStatus.Failed("Payment was declined.")
                            return@launch
                        }
                    }
                } catch (_: Exception) { }
            }
            _evcStatus.value = EvcStatus.Failed("Payment timeout. Please try again or use manual payment.")
        }
    }

    fun cancelPolling() { pollJob?.cancel() }

    override fun onCleared() { super.onCleared(); pollJob?.cancel() }
}

sealed class EvcStatus {
    object Sending : EvcStatus()
    data class WaitingForPin(val message: String) : EvcStatus()
    data class Approved(val message: String)      : EvcStatus()
    data class Failed(val message: String)        : EvcStatus()
}

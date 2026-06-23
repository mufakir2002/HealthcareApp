package com.healthcare.portal.ui.track

import androidx.lifecycle.*
import com.healthcare.portal.data.api.RetrofitClient
import com.healthcare.portal.data.model.TrackResult
import kotlinx.coroutines.launch

class TrackViewModel : ViewModel() {

    private val _result  = MutableLiveData<TrackResult?>()
    val result: LiveData<TrackResult?> = _result

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error   = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun track(appointmentNumber: String, mobile: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            _result.value  = null
            try {
                val body = buildMap<String, String> {
                    if (appointmentNumber.isNotBlank()) put("appointmentNumber", appointmentNumber)
                    if (mobile.isNotBlank())            put("mobile", mobile)
                }
                val resp = RetrofitClient.api.trackAppointment(body)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _result.value = resp.body()?.data
                } else {
                    _error.value = resp.body()?.message ?: "Appointment not found"
                }
            } catch (e: Exception) {
                _error.value = "Connection error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}

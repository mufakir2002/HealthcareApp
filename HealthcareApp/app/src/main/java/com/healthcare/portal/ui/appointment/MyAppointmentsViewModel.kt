package com.healthcare.portal.ui.appointment

import android.app.Application
import androidx.lifecycle.*
import com.healthcare.portal.data.api.RetrofitClient
import com.healthcare.portal.data.model.Appointment
import com.healthcare.portal.utils.SessionManager
import kotlinx.coroutines.launch

class MyAppointmentsViewModel(app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun load() {
        val token = session.bearerToken ?: run {
            _appointments.value = emptyList()
            return
        }
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp = RetrofitClient.api.getMyAppointments(token)
                if (resp.isSuccessful) {
                    _appointments.value = resp.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
                _appointments.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}

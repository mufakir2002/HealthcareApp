package com.healthcare.portal.ui.home

import androidx.lifecycle.*
import com.healthcare.portal.data.api.RetrofitClient
import com.healthcare.portal.data.model.Department
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _departments = MutableLiveData<List<Department>>()
    val departments: LiveData<List<Department>> = _departments

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDepartments() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val resp = RetrofitClient.api.getDepartments()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _departments.value = resp.body()?.data ?: emptyList()
                } else {
                    _error.value = resp.body()?.message ?: "Failed to load departments"
                }
            } catch (e: Exception) {
                _error.value = "Connection error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}

package com.pagzone.sonavi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pagzone.sonavi.data.repository.EmergencyContactRepository
import com.pagzone.sonavi.model.EmergencyContact
import com.pagzone.sonavi.model.EmergencyContactUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class EmergencyContactViewModel @Inject constructor(
    private val repository: EmergencyContactRepository
) : ViewModel() {

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _uiState = MutableStateFlow(EmergencyContactUiState())
    val uiState: StateFlow<EmergencyContactUiState> = _uiState.asStateFlow()

    val emergencyContacts = repository.getAllEmergencyContacts()
        .combine(_sortAscending) { contacts, asc ->
            if (asc) contacts.sortedBy { it.name }
            else contacts.sortedByDescending { it.name }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleSort() {
        _sortAscending.value = !_sortAscending.value
    }

    fun addEmergencyContact(name: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val contact = EmergencyContact(
                    name = name.trim(),
                    number = phoneNumber.trim()
                )
                repository.addContact(contact)
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Contact added successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                repository.updateEmergencyContact(contact)
                _uiState.value = _uiState.value.copy(message = "Contact updated successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                repository.deleteContact(contact)
                _uiState.value = _uiState.value.copy(message = "Contact deleted successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}
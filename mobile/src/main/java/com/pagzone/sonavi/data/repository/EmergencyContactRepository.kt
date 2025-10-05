package com.pagzone.sonavi.data.repository

import com.pagzone.sonavi.data.dao.EmergencyContactDao
import com.pagzone.sonavi.model.EmergencyContact
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactRepository @Inject constructor(
    private val dao: EmergencyContactDao
) {
    fun getAllEmergencyContacts(): Flow<List<EmergencyContact>> {
        return dao.getAll()
    }

    suspend fun getActiveEmergencyContacts(): List<EmergencyContact> {
        return dao.getActiveContacts()
    }

    suspend fun addContact(contact: EmergencyContact) {
        dao.insert(contact)
    }

    suspend fun updateEmergencyContact(contact: EmergencyContact) {
        dao.updateEmergencyContact(contact)
    }

    suspend fun deleteContact(contact: EmergencyContact) {
        dao.delete(contact)
    }
}
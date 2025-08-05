package com.pagzone.sonavi.viewmodel

import androidx.lifecycle.ViewModel
import com.pagzone.sonavi.data.repository.ClassificationResultRepository
import com.pagzone.sonavi.data.repository.ClassificationResultRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClassificationResultViewModel @Inject constructor(
    private val repository: ClassificationResultRepository = ClassificationResultRepositoryImpl
) : ViewModel() {
    val classificationResults = repository.results

    fun clear() {
        repository.clear()
    }
}
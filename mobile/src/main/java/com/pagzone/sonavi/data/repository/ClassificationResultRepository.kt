package com.pagzone.sonavi.data.repository

import com.pagzone.sonavi.model.ClassificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ClassificationResultRepository {
    val results: StateFlow<List<ClassificationResult>>

    fun addResult(result: ClassificationResult)
    fun clear()
}

object ClassificationResultRepositoryImpl : ClassificationResultRepository {
    private val _results = MutableStateFlow<List<ClassificationResult>>(emptyList())
    override val results: StateFlow<List<ClassificationResult>> = _results

    override fun addResult(result: ClassificationResult) {
        _results.value = _results.value + result
    }

    override fun clear() {
        _results.value = emptyList()
    }
}
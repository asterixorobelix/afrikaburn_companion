package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.MOOPReport
import io.asterixorobelix.afrikaburn.domain.usecase.SyncResult

interface MOOPRepository {
    suspend fun saveMOOPReport(report: MOOPReport)
    suspend fun syncMOOPReport(reportId: String): SyncResult
    suspend fun getMOOPReport(reportId: String): MOOPReport?
    suspend fun getAllMOOPReports(): List<MOOPReport>
    suspend fun getPendingSyncReports(): List<MOOPReport>
    suspend fun updateMOOPReport(report: MOOPReport)
    suspend fun deleteMOOPReport(reportId: String)
}